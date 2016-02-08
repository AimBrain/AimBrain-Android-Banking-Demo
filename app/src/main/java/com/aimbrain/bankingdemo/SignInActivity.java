package com.aimbrain.bankingdemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.aimbrain.sdk.Manager;
import com.aimbrain.sdk.exceptions.InternalException;
import com.aimbrain.sdk.exceptions.SessionException;
import com.aimbrain.sdk.models.ScoreModel;
import com.aimbrain.sdk.privacy.PrivacyGuard;
import com.aimbrain.sdk.privacy.SensitiveViewGuard;
import com.aimbrain.sdk.server.AMBNResponseErrorListener;
import com.aimbrain.sdk.server.ScoreCallback;
import com.aimbrain.sdk.server.SessionCallback;
import com.aimbrain.bankingdemo.helpers.Constants;
import com.aimbrain.bankingdemo.score.ScoreManager;

import java.net.ConnectException;
import java.util.HashSet;
import java.util.Set;


public class SignInActivity extends AppCompatActivity {

    private Button signInButton;
    private EditText pinEditText;
    private EditText enteredEmailEditText;
    private TextView errorTextView;
    private ProgressDialog progressDialog;
    private TextView titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Set<View> ignoredViews = new HashSet<>();
        ignoredViews.add(pinEditText);
        PrivacyGuard privacyGuard = new PrivacyGuard(ignoredViews);
        Manager.getInstance().addPrivacyGuard(privacyGuard);

        String email = getEmail();
        this.enteredEmailEditText = (EditText) findViewById(R.id.enteredEmailEditText);
        this.enteredEmailEditText.setText(email);
        if (email != null && !email.isEmpty())
            this.enteredEmailEditText.setEnabled(false);

        titleTextView = (TextView)findViewById(R.id.titleTextView);
        titleTextView.setText(getString(R.string.sign_in_title) + " " + getPin());
        this.errorTextView = (TextView)findViewById(R.id.signInErrorTextView);
        this.pinEditText = (EditText) findViewById(R.id.pinEditText);
        pinEditText.setKeyListener(null);

        this.signInButton = (Button) findViewById(R.id.signInButton);
        this.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorTextView.setText("");
                String pin = pinEditText.getText().toString();
                if (pin.equals(getPin())) {
                    try {
                        createSession();
                    } catch (ConnectException | InternalException e) {
                        hideSpinner();
                        errorTextView.setText(e.getMessage());
                    }
                } else {
                    errorTextView.setText("Wrong PIN");
                }
                pinEditText.setText("");
            }
        });

        getSupportActionBar().setTitle("Sign in");
        SensitiveViewGuard.addView(getWindow().getDecorView());
    }

    @Override
    protected void onResume() {
        super.onResume();
        errorTextView.setText("");
    }

    @Override
    protected void onDestroy() {
        hideSpinner();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        sendCollectedData();
    }

    private void createSession() throws ConnectException, InternalException {
        progressDialog = Spinner.showSpinner(this);
        ScoreManager.getInstance().clearCache();
        Manager.getInstance().createSession(getUserId(), getApplicationContext(), new SessionCallback() {
                    @Override
                    public void onSessionCreated(String session) {
                        hideSpinner();
                        Log.i("SESSION", "session created: " + session);
                        Intent intent = new Intent(SignInActivity.this, DemoBankActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }, new AMBNResponseErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideSpinner();
                        error.printStackTrace();

                        String json = null;

                        NetworkResponse response = error.networkResponse;
                        if (response != null && response.data != null) {
                            json = new String(response.data);
                            json = trimMessage(json, "error");
                            if (json != null)
                                errorTextView.setText(json);
                            else
                                errorTextView.setText("Server problem. Unprocessable response");
                        } else if (error instanceof TimeoutError)
                            errorTextView.setText("Unable to connect to server. Request timed out.");
                        else
                            errorTextView.setText("Unable to connect to server. Please check network settings.");
                    }
                }
        );

    }

    private String getUserId() {
        SharedPreferences prefs = this.getSharedPreferences(Constants.SHARED_PREFS_TAG, Context.MODE_PRIVATE);
        return prefs.getString(Constants.USER_ID, null);
    }

    public String getEmail() {
        SharedPreferences prefs = this.getSharedPreferences(Constants.SHARED_PREFS_TAG, Context.MODE_PRIVATE);
        return prefs.getString(Constants.USER_EMAIL, null);
    }
    public String getPin() {
        SharedPreferences prefs = this.getSharedPreferences(Constants.SHARED_PREFS_TAG, Context.MODE_PRIVATE);
        return prefs.getString(Constants.PIN_TAG, null);
    }

    public void numberButtonClick(View view) {
        if (pinEditText == null)
            return;
        Button buttonClicked = (Button) view;
        pinEditText.append(buttonClicked.getText());
    }

    public void backspaceClick(View view) {
        if (pinEditText == null)
            return;
        Editable currentText = pinEditText.getText();
        if (currentText.length() > 0)
            currentText.delete(currentText.length() - 1, currentText.length());
    }

    public void hideSpinner(){
        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void sendCollectedData(){
        try {
            Manager.getInstance().submitCollectedData(new ScoreCallback() {
                @Override
                public void success(ScoreModel scoreModel) {
                    Log.i("DATA SENT", "and collected");
                    ScoreManager.getInstance().scoreChanged(scoreModel, System.currentTimeMillis());
                }
            });
        } catch (InternalException | ConnectException | SessionException e){
            e.printStackTrace();
        }
    }
}
