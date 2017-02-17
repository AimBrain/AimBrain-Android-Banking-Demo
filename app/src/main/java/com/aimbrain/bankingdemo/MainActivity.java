package com.aimbrain.bankingdemo;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.aimbrain.sdk.exceptions.InternalException;
import com.aimbrain.bankingdemo.helpers.Constants;
import com.aimbrain.bankingdemo.server.DemoServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private final DemoServer demoServer = new DemoServer();
    private EditText emailEditText;
    private TextView errorTextView;
    private ProgressDialog progressDialog;
    private Handler retryHandler;
    private Runnable loginRetry;
    private String email;
    private String loginURL;
    private boolean destroyed = false;

    @TargetApi(23)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        destroyed = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.emailEditText = (EditText)findViewById(R.id.enterEmailEditText);
        this.errorTextView = (TextView)findViewById(R.id.mainErrorTextView);
        retryHandler = new Handler(getMainLooper());
        getSupportActionBar().setTitle("Registration");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS}, 1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        errorTextView.setText("");
    }

    @Override
    protected void onDestroy() {
        hideSpinner();
        destroyed = true;
        super.onDestroy();
    }

    public void registerClick(View view) {
        showProgressDialog();
        errorTextView.setText("");
        email = this.emailEditText.getText().toString();
        SharedPreferences prefs = this.getSharedPreferences(Constants.SHARED_PREFS_TAG, Context.MODE_PRIVATE);
        String interfaceUUID = prefs.getString(Constants.INTERFACE_UUID, null);
            try {
                final String registeredEmail = email;
                demoServer.register(registeredEmail, interfaceUUID, getEmails(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (!registeredEmail.equals(email)) {
                        return;
                    }
                    try {
                        loginURL = response.getString("loginurl");
                        loginRetry = new Runnable() {
                            @Override
                            public void run() {
                                if (!registeredEmail.equals(email)) {
                                    return;
                                }
                                verifyEmail(registeredEmail);
                            }
                        };
                        loginRetry.run();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    hideSpinner();
                    if (!registeredEmail.equals(email)) {
                        return;
                    }
                    NetworkResponse response = error.networkResponse;
                    if(response != null && response.data != null){
                        switch(response.statusCode){
                            default:
                                String errorMessage = null;
                                try {
                                    JSONObject json = new JSONObject(new String(response.data));
                                    errorMessage = json.getString("error");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if(errorMessage != null) {
                                    errorTextView.setText(errorMessage);
                                }else{
                                    errorTextView.setText("Server problem, unprocessable response");
                                }
                                break;
                        }
                    }
                    else if (error instanceof TimeoutError)
                        errorTextView.setText("Unable to connect to server. Request timed out.");
                    else
                        errorTextView.setText("Unable to connect to server. Please check network settings.");
                }
            });
        } catch (InternalException e) {
            hideSpinner();
            e.printStackTrace();
        }
    }

    private void verifyEmail(final String verifiedEmail){
        demoServer.login(loginURL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hideSpinner();
                if (!verifiedEmail.equals(email)) {
                    return;
                }
                try {
                    retryHandler.removeCallbacksAndMessages(null);
                    loginRetry = null;
                    String pin = response.getString("pin");
                    String userId = response.getString("userId");
                    saveUser(userId, email, pin);

                    Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!verifiedEmail.equals(email)) {
                    return;
                }
                String errorMessage = null;
                if(error.networkResponse != null) {
                    try {
                        JSONObject json = new JSONObject(new String(error.networkResponse.data));
                        errorMessage = json.getString("error");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    errorTextView.setText("Unable to connect to server. Please check network settings.");
                }
                if (progressDialog != null) {
                    if (errorMessage != null) {
                        progressDialog.setMessage(errorMessage);
                    } else {
                        progressDialog.setMessage("Server problem, unprocessable response");
                    }
                }
                if(loginRetry != null && !destroyed) {
                    retryHandler.postDelayed(loginRetry, 1500);
                }
            }
        });
    }

    public void saveUser(String userId, String userEmail, String pin) {
        SharedPreferences prefs = this.getSharedPreferences(Constants.SHARED_PREFS_TAG, Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.USER_ID, userId).apply();
        prefs.edit().putString(Constants.USER_EMAIL, userEmail).apply();
        prefs.edit().putString(Constants.PIN_TAG, pin).apply();
    }

    public void hideSpinner(){
        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
    public String[] getEmails(){
        Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");
        List<String> emails = new ArrayList<>();
        for (Account account : accounts) {
            emails.add(account.name);
            Log.i("ACCOUNT", account.name);
        }
        return emails.toArray(new String[emails.size()]);
    }

    private void showProgressDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Verifying your email, please wait.");
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Use different email", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                retryHandler.removeCallbacksAndMessages(null);
                loginRetry = null;
                email = null;
                hideSpinner();
            }
        });
        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    retryHandler.removeCallbacksAndMessages(null);
                    loginRetry = null;
                    email = null;
                    hideSpinner();
                }
                return true;
            }
        });
        progressDialog.show();
    }
}
