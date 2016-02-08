package com.aimbrain.bankingdemo;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.aimbrain.sdk.Manager;
import com.aimbrain.sdk.exceptions.InternalException;
import com.aimbrain.sdk.exceptions.SessionException;
import com.aimbrain.sdk.models.ScoreModel;
import com.aimbrain.sdk.server.ScoreCallback;
import com.aimbrain.bankingdemo.fragments.ToastFragment;
import com.aimbrain.bankingdemo.score.ScoreManager;

import java.net.ConnectException;


public class AccountDetailsActivity extends AppCompatActivity {

    private TextView balanceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);
        getSupportActionBar().setTitle("Account details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        balanceTextView = (TextView)findViewById(R.id.balanceValueTextView);
        balanceTextView.setText(intent.getStringExtra("balance"));

        addToastFragment();
    }

    @Override
    protected void onStop() {
        super.onStop();
        sendCollectedData();
    }

    public void makePaymentClick(View view) {
        Intent intent = new Intent(this, PaymentActivity.class);
        startActivity(intent);
    }

    public void viewTransactionsClick(View view) {
        Intent intent = new Intent(this, TransactionsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home ) {
            onBackPressed();
            return true;
        }
        // other menu select events may be present here

        return super.onOptionsItemSelected(item);
    }

    public void addToastFragment(){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ToastFragment fragment = new ToastFragment();
        fragmentTransaction.add(R.id.fragmentAccountDetailsContainer, fragment);
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
