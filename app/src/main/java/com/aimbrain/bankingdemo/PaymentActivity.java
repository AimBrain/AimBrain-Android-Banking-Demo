package com.aimbrain.bankingdemo;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.aimbrain.sdk.Manager;
import com.aimbrain.sdk.exceptions.InternalException;
import com.aimbrain.sdk.exceptions.SessionException;
import com.aimbrain.sdk.models.ScoreModel;
import com.aimbrain.sdk.server.ScoreCallback;
import com.aimbrain.bankingdemo.fragments.ToastFragment;
import com.aimbrain.bankingdemo.score.ScoreManager;

import java.net.ConnectException;


public class PaymentActivity extends AppCompatActivity {

    private EditText referenceEditText;
    private EditText amountEditText;
    private EditText toEditText;
    private EditText fromEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        getSupportActionBar().setTitle("Payment");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addToastFragment();
        referenceEditText = (EditText)findViewById(R.id.referenceEditText);
        amountEditText = (EditText)findViewById(R.id.amountEditText);
        toEditText = (EditText)findViewById(R.id.toEditText);
        fromEditText = (EditText)findViewById(R.id.fromEditText);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sendCollectedData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // API 5+ solution
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void makePaymentClick(View view) {
        if(fromEditText.getText().toString().equals("122927804349")
                && toEditText.getText().toString().equals("922127809434")
                && amountEditText.getText().toString().matches("143[.,]21")
                && referenceEditText.getText().toString().trim().equalsIgnoreCase("Apartment rental"))
            finish();
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Invalid data has been entered");
            builder.setMessage("Please enter exact values from hints above text fields")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

    }

    public void addToastFragment(){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ToastFragment fragment = new ToastFragment();
        fragmentTransaction.add(R.id.fragmentPaymentContainer, fragment);
        fragmentTransaction.commit();
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
