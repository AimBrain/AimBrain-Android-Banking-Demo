package com.aimbrain.bankingdemo.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aimbrain.sdk.Manager;
import com.aimbrain.sdk.models.ScoreModel;
import com.aimbrain.bankingdemo.R;
import com.aimbrain.bankingdemo.helpers.Constants;
import com.aimbrain.bankingdemo.score.ScoreListener;
import com.aimbrain.bankingdemo.score.ScoreManager;
import com.aimbrain.bankingdemo.MyApplication;
import com.aimbrain.sdk.models.SessionModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ToastFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ToastFragment extends Fragment implements ScoreListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static String lastSessionNotifiedEnrollingDone = null;
    public static String lastSessionNotifiedAuthenticatingDone = null;
    private static final int ENROLLING_SESSION_DONE = 2;
    private static final int AUTHENTICATING_SESSION_DONE = 3;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View view;
    private TextView statusTextView;
    private TextView scoreTextView;
    private TextView scoreLabelTextView;
    private TextView emailTextView;
    private TextView sessionTextView;
    private TextView lastUpdatedTextView;
    private long startTime;
    private TextView uuidTextView;

    private Handler timerHandler;
    private Runnable timerRunnable;
    ;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ToastFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment newInstance(String param1, String param2) {
        ToastFragment fragment = new ToastFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ToastFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_blank, container, false);
        emailTextView = (TextView)view.findViewById(R.id.userEmailTextView);
        emailTextView.setText(getUserEmail());

        sessionTextView = (TextView)view.findViewById(R.id.sessionIdTextView);
        sessionTextView.setText(getSession().getSessionId());

        statusTextView = (TextView)view.findViewById(R.id.statusTextView);
        scoreTextView = (TextView)view.findViewById(R.id.scoreTextView);
        scoreLabelTextView = (TextView)view.findViewById(R.id.scoreLabelTextView);

        uuidTextView = (TextView)view.findViewById(R.id.uuidTextView);
        uuidTextView.setText(getUserId());
        lastUpdatedTextView = (TextView)view.findViewById(R.id.lastUpdatedTextView);
        ScoreManager.getInstance().registerListener(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        ScoreManager.getInstance().unregisterListener(this);
    }

    @Override
    public void updateScore(ScoreModel scoreModel, long timestamp) {
        String session = scoreModel.getSession();
        switch (scoreModel.getStatus()) {
            case ENROLLING_SESSION_DONE:
                if(session != null && !session.equals(lastSessionNotifiedEnrollingDone)){
                    lastSessionNotifiedEnrollingDone = session;
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("End of Session");
                    builder.setMessage("Single session completed. Please log out and start a new session to continue enrolment.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            case ScoreModel.ENROLLING: {
                if(scoreModel.getStatus() == ENROLLING_SESSION_DONE){
                    statusTextView.setText("Enrolling  (end of session)");
                }else{
                    statusTextView.setText("Enrolling");
                }

                scoreLabelTextView.setText("Progress:");
                scoreTextView.setText(String.format("%.0f%%", (scoreModel.getScore() * 100)));
                view.setBackgroundColor(getResources().getColor(R.color.status_banner_not_decided));
                break;
            }
            case AUTHENTICATING_SESSION_DONE:
                if(session != null && !session.equals(lastSessionNotifiedAuthenticatingDone)){
                    lastSessionNotifiedAuthenticatingDone = session;
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("End of Session");
                    builder.setMessage("Single session completed. You can see the session score in the top banner.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            case ScoreModel.AUTHENTICATING: {

                scoreLabelTextView.setText("Score:");
                if(scoreModel.getStatus() == AUTHENTICATING_SESSION_DONE){
                    statusTextView.setText("Authenticating (end of session)");
                }else {
                    statusTextView.setText("Authenticating");
                }
                scoreTextView.setText(String.format("%.0f%%",(scoreModel.getScore() * 100)));
                if (scoreModel.getScore() >= 0.6 ) {
                    view.setBackgroundColor(getResources().getColor(R.color.status_banner_good_score));
                } else if (scoreModel.getScore() > 0.4 ) {
                    view.setBackgroundColor(getResources().getColor(R.color.status_banner_medium_score));
                }else{
                    view.setBackgroundColor(getResources().getColor(R.color.status_banner_bad_score));
                }
                break;
            }
            default: {
                break;
            }
        }
        resetLastUpdatedTextView(timestamp);
    }

    private void resetLastUpdatedTextView(long timestamp){
        startTime = (System.currentTimeMillis() - timestamp) / 1000;
        lastUpdatedTextView.setText(startTime + " seconds ago");
        if(timerHandler == null){
            timerHandler = new Handler();
            timerRunnable = new Runnable() {

                @Override
                public void run() {
                    startTime++;
                    lastUpdatedTextView.setText(startTime + " seconds ago");
                    timerHandler.postDelayed(this, 1000);
                }
            };
            timerHandler.postDelayed(timerRunnable, 1000);
        }
    }

    private String getUserId() {
        SharedPreferences prefs = this.getActivity().getSharedPreferences(Constants.SHARED_PREFS_TAG, Context.MODE_PRIVATE);
        return prefs.getString(Constants.USER_ID, "-");
    }
    private String getUserEmail() {
        SharedPreferences prefs = this.getActivity().getSharedPreferences(Constants.SHARED_PREFS_TAG, Context.MODE_PRIVATE);
        return prefs.getString(Constants.USER_EMAIL, "-");
    }
    private SessionModel getSession() {
       return Manager.getInstance().getSession();
    }
}
