package com.aimbrain.bankingdemo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.aimbrain.sdk.faceCapture.FaceCaptureActivity;
import com.aimbrain.sdk.faceCapture.VideoFaceCaptureActivity;
import com.aimbrain.sdk.models.FaceAuthenticateModel;
import com.aimbrain.sdk.models.FaceEnrollModel;
import com.aimbrain.sdk.models.SessionModel;
import com.aimbrain.sdk.models.VoiceAuthenticateModel;
import com.aimbrain.sdk.models.VoiceEnrollModel;
import com.aimbrain.sdk.models.VoiceTokenModel;
import com.aimbrain.sdk.models.VoiceTokenType;
import com.aimbrain.sdk.server.FaceCapturesAuthenticateCallback;
import com.aimbrain.sdk.server.FaceCapturesEnrollCallback;
import com.aimbrain.sdk.server.VoiceCaptureEnrollCallback;
import com.aimbrain.sdk.server.VoiceCapturesAuthenticateCallback;
import com.aimbrain.sdk.server.VoiceTokenCallback;
import com.aimbrain.sdk.voiceCapture.VoiceCaptureActivity;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.util.HashSet;
import java.util.Set;


public class SignInActivity extends AppCompatActivity {

    private static final int enrollmentRequestcode = 1542;
    private static final int authenticationRequestcode = 1543;
    private static final int voiceEnrollmentRequestcode = 1544;
    private static final int voiceAuthenticationRequestcode = 1545;
    private static final String photoAuthUpperText = "To authenticate please face the camera directly, press 'camera' button and blink";
    private static final String photoLowerText = "Position your face fully within the outline.";
    private static final String faceAuthenticationHint = "Please BLINK now...";
    private static final String[] faceEnrollStepsTexts = {
            "To enroll please face the camera directly and press 'camera' button",
            "Face the camera slightly from the top and press 'camera' button",
            "Face the camera slightly from the bottom and press 'camera' button",
            "Face the camera slightly from the left and press 'camera' button",
            "Face the camera slightly from the right and press 'camera' button"
    };
    private static final String[] voiceEnrollStepsTexts = {
            "To enroll please press 'microphone' button read text below",
            "Please press 'microphone' button read the text below 2nd time",
            "Please press 'microphone' button read the text below 3rd time",
            "Please press 'microphone' button read the text below 4th time",
            "Please press 'microphone' button read the text below the last time"
    };
    private static final String voiceAuthenticationHint = "To authenticate please press 'microphone' button and read text below";


    private Button signInButton;
    private EditText pinEditText;
    private EditText enteredEmailEditText;
    private TextView errorTextView;
    private ProgressDialog progressDialog;
    private TextView titleTextView;
    private int triesAmount;
    private int voiceTriesAmount;
    private boolean isActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.triesAmount = savedInstanceState.getInt("triesAmount");
            this.voiceTriesAmount = savedInstanceState.getInt("voiceTriesAmount");
        }
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

        titleTextView = (TextView) findViewById(R.id.titleTextView);
        titleTextView.setText(getString(R.string.sign_in_title) + " " + getPin());
        this.errorTextView = (TextView) findViewById(R.id.signInErrorTextView);
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
                        createSessionUsingPin();
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


    public void faceEnrollButtonClick(View view) {
        final CharSequence[] items = {getString(R.string.auth_type_face), getString(R.string.auth_type_voice) };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_select_auth_type));
        builder.setPositiveButton("Cancel", null);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    //face selected
                    faceAuthType();
                } else {
                    //voice selected
                    voiceAuthType();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void faceAuthType() {
        try {
            createSessionUsingPhoto();
        } catch (ConnectException | InternalException e) {
            hideSpinner();
            errorTextView.setText(e.getMessage());
        }
    }

    private void voiceAuthType() {
        try {
            createSessionUsingVoice();
        } catch (ConnectException | InternalException e) {
            hideSpinner();
            errorTextView.setText(e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        errorTextView.setText("");
        isActive = true;
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("triesAmount", triesAmount);
        outState.putInt("voiceTriesAmount", voiceTriesAmount);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
    }

    private void createSessionUsingPin() throws ConnectException, InternalException {
        progressDialog = Spinner.showSpinner(this);
        ScoreManager.getInstance().clearCache();
        Manager.getInstance().createSession(getUserId(), getApplicationContext(), new SessionCallback() {
                    @Override
                    public void onSessionCreated(SessionModel session) {
                        hideSpinner();
                        if (!isActive) {
                            return;
                        }
                        Log.i("SESSION", "session id: " + session.getSessionId());
                        Log.i("SESSION", "session face status: " + session.getFaceStatus());
                        Log.i("SESSION", "session behaviour status: " + session.getBehaviourStatus());
                        startDemoBankActivity();
                    }
                }, new AMBNResponseErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideSpinner();
                        errorTextView.setText(getErrorMessage(error));
                    }
                }
        );

    }

    private String getErrorMessage(VolleyError error) {
        String json = null;
        String errorMessage = null;
        NetworkResponse response = error.networkResponse;
        if (response != null && response.data != null) {
            json = new String(response.data);
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(json);
                errorMessage = jsonObject.getString("error");
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            if (errorMessage != null)
                return errorMessage;
            else
                return "Server problem. Unprocessable response";
        } else if (error instanceof TimeoutError)
            return "Unable to connect to server. Request timed out.";
        else
            return "Unable to connect to server. Please check network settings.";
    }

    private void createSessionUsingPhoto() throws ConnectException, InternalException {
        progressDialog = Spinner.showSpinner(this);
        ScoreManager.getInstance().clearCache();
        Manager.getInstance().createSession(getUserId(), getApplicationContext(), new SessionCallback() {
                    @Override
                    public void onSessionCreated(SessionModel session) {
                        hideSpinner();
                        if (!isActive) {
                            return;
                        }
                        Log.i("SESSION", "session id: " + session.getSessionId());
                        Log.i("SESSION", "session face status: " + session.getFaceStatus());
                        Log.i("SESSION", "session behaviour status: " + session.getBehaviourStatus());
                        Log.i("SESSION", "session voice status: " + session.getVoiceStatus());
                        if (session.getFaceStatus() == SessionModel.BUILDING) {
                            new AlertDialog.Builder(SignInActivity.this)
                                    .setTitle("Face detection unavailable")
                                    .setMessage("Generating template. Please try again in a few seconds")
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        } else if (session.getFaceStatus() == SessionModel.ENROLLED) {
                            authenticateWithPhotos();
                        } else if (session.getFaceStatus() == SessionModel.NOT_ENROLLED) {
                            showEnrollmentDialog();
                        }
                    }
                }, new AMBNResponseErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideSpinner();
                        errorTextView.setText(getErrorMessage(error));
                    }
                }
        );

    }

    private void showEnrollmentDialog() {
        new AlertDialog.Builder(SignInActivity.this)
                .setMessage("Please enroll  before using Facial authentication")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        triesAmount = 0;
                        enrollWithPhotos();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).setCancelable(false).show();
    }

    private void enrollWithPhotos() {
        if (triesAmount < 5) {
            openFaceImageCaptureActivity(enrollmentRequestcode, faceEnrollStepsTexts[triesAmount], photoLowerText);
        } else {
            hideSpinner();
            new AlertDialog.Builder(SignInActivity.this)
                    .setMessage("Enrollment finished successfully")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
        }
    }

    private void authenticateWithPhotos() {
        openFaceImageCaptureActivity(authenticationRequestcode, photoAuthUpperText, photoLowerText, faceAuthenticationHint);
    }

    private void createSessionUsingVoice() throws ConnectException, InternalException {
        progressDialog = Spinner.showSpinner(this);
        Manager.getInstance().createSession(getUserId(), getApplicationContext(), new SessionCallback() {
                    @Override
                    public void onSessionCreated(SessionModel session) {
                        hideSpinner();
                        if (!isActive) {
                            return;
                        }
                        Log.i("SESSION", "session id: " + session.getSessionId());
                        Log.i("SESSION", "session face status: " + session.getFaceStatus());
                        Log.i("SESSION", "session behaviour status: " + session.getBehaviourStatus());
                        Log.i("SESSION", "session voice status: " + session.getVoiceStatus());
                        if (session.getVoiceStatus() == SessionModel.BUILDING) {
                            new AlertDialog.Builder(SignInActivity.this)
                                    .setTitle("Voice detection unavailable")
                                    .setMessage("Generating template. Please try again in a few seconds")
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        } else if (session.getVoiceStatus() == SessionModel.ENROLLED) {
                            authenticateWithVoice();
                        } else if (session.getVoiceStatus() == SessionModel.NOT_ENROLLED) {
                            showVoiceEnrollmentDialog();
                        }
                    }
                }, new AMBNResponseErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideSpinner();
                        errorTextView.setText(getErrorMessage(error));
                    }
                }
        );
    }

    private void showVoiceEnrollmentDialog() {
        new AlertDialog.Builder(SignInActivity.this)
                .setMessage("Please enroll  before using Voice authentication")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        voiceTriesAmount = 0;
                        enrollWithVoice();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hideSpinner();
            }
        }).setCancelable(false).show();
    }

    private void enrollWithVoice() {
        if (voiceTriesAmount < 5) {
            VoiceTokenType tokenType = VoiceTokenType.ENROLL1;
            switch (voiceTriesAmount) {
                case 0: tokenType = VoiceTokenType.ENROLL1;
                    break;
                case 1: tokenType = VoiceTokenType.ENROLL2;
                    break;
                case 2: tokenType = VoiceTokenType.ENROLL3;
                    break;
                case 3: tokenType = VoiceTokenType.ENROLL4;
                    break;
                case 4: tokenType = VoiceTokenType.ENROLL5;
                    break;
            }

            Log.d("SignInActivity", "Voice token type " + tokenType.toString());
            try {
                progressDialog = Spinner.showSpinner(this);
                Manager.getInstance().getVoiceToken(tokenType, new VoiceTokenCallback() {
                    @Override
                    public void success(VoiceTokenModel tokenModel) {
                        hideSpinner();
                        if (!isActive) {
                            return;
                        }
                        openVoiceCaptureActivity(voiceEnrollmentRequestcode, voiceEnrollStepsTexts[voiceTriesAmount],
                                tokenModel.getToken());
                    }
                });
            } catch (InternalException | SessionException | ConnectException e) {
                Log.e("signingActivity", "voiceEnroll", e);
            }
        }
        else {
            hideSpinner();
            new AlertDialog.Builder(SignInActivity.this)
                    .setMessage("Enrollment finished successfully")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
        }
    }

    private void authenticateWithVoice() {
        try {
            progressDialog = Spinner.showSpinner(this);
            Manager.getInstance().getVoiceToken(VoiceTokenType.AUTH, new VoiceTokenCallback() {
                @Override
                public void success(VoiceTokenModel tokenModel) {
                    hideSpinner();
                    if (!isActive) {
                        return;
                    }
                    openVoiceCaptureActivity(voiceAuthenticationRequestcode, voiceAuthenticationHint, tokenModel.getToken());
                }
            });
        } catch (InternalException | SessionException | ConnectException e) {
            Log.e("signingActivity", "voiceEnroll", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case enrollmentRequestcode:
                if (resultCode != RESULT_OK) {
                    return;
                }

                try {
                    progressDialog = Spinner.showSpinner(this);
                    Manager.getInstance().sendProvidedFaceCapturesToEnroll(VideoFaceCaptureActivity.video, new FaceCapturesEnrollCallback() {
                        @Override
                        public void success(FaceEnrollModel faceEnrollModel) {
                            hideSpinner();
                            if (!isActive) {
                                return;
                            }
                            triesAmount++;
                            enrollWithPhotos();
                        }

                        @Override
                        public void failure(VolleyError volleyError) {
                            hideSpinner();
                            if (!isActive) {
                                return;
                            }
                            showRetryPhotosEnrollmentDialog("Please re-take the picture, reason: " + getErrorMessage(volleyError));
                        }
                    });
                } catch (ConnectException | InternalException e) {
                    hideSpinner();
                    showRetryPhotosEnrollmentDialog("Please re-take the picture, reason: " + e.getMessage());
                } catch (SessionException e) {
                    hideSpinner();
                    Log.e("signingActivity", "sendProvidedFaceCapturesToEnroll", e);
                }
                break;
            case authenticationRequestcode:
                if (resultCode != RESULT_OK) {
                    return;
                }
                try {
                    progressDialog = Spinner.showSpinner(this);
                    Manager.getInstance().sendProvidedFaceCapturesToAuthenticate(VideoFaceCaptureActivity.video, new FaceCapturesAuthenticateCallback() {
                        @Override
                        public void success(FaceAuthenticateModel faceAuthenticateModel) {
                            hideSpinner();
                            if (!isActive) {
                                return;
                            }
                            if (faceAuthenticateModel.getScore() >= 0.5) {
                                if (faceAuthenticateModel.getLiveliness() >= 0.5) {
                                    startDemoBankActivity();
                                } else {
                                    showRetryPhotosAuthDialog(String.format("Your face matched to %.0f%%, but it failed the liveliness test", faceAuthenticateModel.getScore() * 100));
                                }
                            } else {
                                showRetryPhotosAuthDialog("Access denied");
                            }
                        }

                        @Override
                        public void failure(VolleyError volleyError) {
                            hideSpinner();
                            if (!isActive) {
                                return;
                            }
                            showRetryPhotosAuthDialog("Please re-take the picutre, reason: " + getErrorMessage(volleyError));
                        }
                    });
                } catch (ConnectException | InternalException e) {
                    hideSpinner();
                    showRetryPhotosAuthDialog(e.getMessage());
                } catch (SessionException e) {
                    hideSpinner();
                    Log.e("signingActivity", "sendProvidedFaceCapturesToAuthenticate", e);
                }
                break;
            case voiceEnrollmentRequestcode:
                if (resultCode != RESULT_OK) {
                    return;
                }
                try {
                    progressDialog = Spinner.showSpinner(this);
                    Manager.getInstance().sendProvidedVoiceCapturesToEnroll(VoiceCaptureActivity.audio, new VoiceCaptureEnrollCallback() {
                        @Override
                        public void success(VoiceEnrollModel faceEnrollModel) {
                            hideSpinner();
                            if (!isActive) {
                                return;
                            }
                            Log.d("signingActivity", "success " + faceEnrollModel.getVoiceSamples());
                            voiceTriesAmount++;
                            enrollWithVoice();
                        }

                        @Override
                        public void failure(VolleyError volleyError) {
                            hideSpinner();
                            if (!isActive) {
                                return;
                            }
                            showRetryVoiceEnrollmentDialog("Please retry, reason: " + getErrorMessage(volleyError));
                        }
                    });
                } catch (ConnectException | InternalException e) {
                    hideSpinner();
                    showRetryVoiceEnrollmentDialog("Please retry, reason: " + e.getMessage());
                } catch (SessionException e) {
                    hideSpinner();
                    Log.e("signingActivity", "sendProvidedVoiceCapturesToEnroll", e);
                }
                break;
            case voiceAuthenticationRequestcode:
                if (resultCode != RESULT_OK) {
                    return;
                }
                try {
                    progressDialog = Spinner.showSpinner(this);
                    Manager.getInstance().sendProvidedVoiceCapturesToAuthenticate(VoiceCaptureActivity.audio, new VoiceCapturesAuthenticateCallback() {
                        @Override
                        public void success(VoiceAuthenticateModel voiceAuthenticateModel) {
                            hideSpinner();
                            if (!isActive) {
                                return;
                            }
                            if (voiceAuthenticateModel.getScore() >= 0.5) {
                                if (voiceAuthenticateModel.getLiveliness() >= 0.5) {
                                    startDemoBankActivity();
                                } else {
                                    showRetryVoiceAuthDialog(String.format("Your voice matched to %.0f%%, but it failed the liveliness test", voiceAuthenticateModel.getScore() * 100));
                                }
                            } else {
                                showRetryVoiceAuthDialog("Access denied");
                            }
                        }

                        @Override
                        public void failure(VolleyError volleyError) {
                            hideSpinner();
                            if (!isActive) {
                                return;
                            }
                            showRetryVoiceAuthDialog("Please retry again, reason: " + getErrorMessage(volleyError));
                        }
                    });
                } catch (ConnectException | InternalException e) {
                    hideSpinner();
                    showRetryVoiceAuthDialog(e.getMessage());
                } catch (SessionException e) {
                    hideSpinner();
                    Log.e("signingActivity", "sendProvidedVoiceCapturesToAuthenticate", e);
                }
                break;
        }
    }

    private void openFaceImageCaptureActivity(int requestCode, String upperText, String lowerText) {
        openFaceImageCaptureActivity(requestCode, upperText, lowerText, null);
    }

    private void openFaceImageCaptureActivity(int requestCode, String upperText, String lowerText, String recordingHint) {
        Intent intent = new Intent(this, VideoFaceCaptureActivity.class);
        intent.putExtra(FaceCaptureActivity.EXTRA_UPPER_TEXT, upperText);
        intent.putExtra(FaceCaptureActivity.EXTRA_LOWER_TEXT, lowerText);
        if (recordingHint != null) {
            intent.putExtra(FaceCaptureActivity.RECORDING_HINT, recordingHint);
        }
        startActivityForResult(intent, requestCode);
    }

    private void showRetryPhotosAuthDialog(String message) {
        new AlertDialog.Builder(SignInActivity.this)
                .setMessage(message)
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            createSessionUsingPhoto();
                        } catch (ConnectException | InternalException e) {
                            hideSpinner();
                            errorTextView.setText(e.getMessage());
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setCancelable(false).show();
    }

    private void showRetryPhotosEnrollmentDialog(String message) {
        new AlertDialog.Builder(SignInActivity.this)
                .setTitle("Error")
                .setMessage(message + (message.endsWith(".") ? " " : ". ") + " Do you want to try again?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        enrollWithPhotos();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setCancelable(false).show();
    }

    private void openVoiceCaptureActivity(int requestCode, String upperText, String recordingToken) {
        Intent intent = new Intent(this, VoiceCaptureActivity.class);
        intent.putExtra(VoiceCaptureActivity.EXTRA_UPPER_TEXT, upperText);
        intent.putExtra(VoiceCaptureActivity.EXTRA_RECORDING_HINT, recordingToken);
        startActivityForResult(intent, requestCode);
    }

    private void showRetryVoiceAuthDialog(String message) {
        new AlertDialog.Builder(SignInActivity.this)
                .setMessage(message)
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            createSessionUsingVoice();
                        } catch (ConnectException | InternalException e) {
                            hideSpinner();
                            errorTextView.setText(e.getMessage());
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).setCancelable(false).show();
    }

    private void showRetryVoiceEnrollmentDialog(String message) {
        new AlertDialog.Builder(SignInActivity.this)
                .setTitle("Error")
                .setMessage(message + (message.endsWith(".") ? " " : ". ") + " Do you want to try again?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        enrollWithVoice();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setCancelable(false).show();
    }

    private void startDemoBankActivity() {
        Intent intent = new Intent(SignInActivity.this, DemoBankActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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

    public void hideSpinner() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void sendCollectedData() {
        try {
            Manager.getInstance().submitCollectedData(new ScoreCallback() {
                @Override
                public void success(ScoreModel scoreModel) {
                    Log.i("DATA SENT", "and collected");
                    ScoreManager.getInstance().scoreChanged(scoreModel, System.currentTimeMillis());
                }
            });
        } catch (InternalException | ConnectException | SessionException e) {
            Log.e("signingActivity", "submitCollectedData", e);
        }
    }
}
