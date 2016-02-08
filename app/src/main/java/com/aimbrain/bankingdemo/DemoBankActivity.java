package com.aimbrain.bankingdemo;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.aimbrain.sdk.Manager;
import com.aimbrain.sdk.exceptions.InternalException;
import com.aimbrain.sdk.exceptions.SessionException;
import com.aimbrain.sdk.models.ScoreModel;
import com.aimbrain.sdk.server.ScoreCallback;
import com.aimbrain.bankingdemo.adapters.DemoBankListViewAdapter;
import com.aimbrain.bankingdemo.fragments.ToastFragment;

import com.aimbrain.bankingdemo.helpers.Constants;
import com.aimbrain.bankingdemo.models.BankRow;
import com.aimbrain.bankingdemo.score.ScoreManager;

import java.net.ConnectException;


public class DemoBankActivity extends AppCompatActivity{

    private ListView demoBankListView;
    private DemoBankListViewAdapter demoBankListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_bank);
        getSupportActionBar().setTitle("AimBrain Authentication");
        setUpListView();
        addToastFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_demo_bank, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_change_account){
            changeAccount();
        }
        return true;
    }
    @Override
    protected void onStop() {
        super.onStop();
        sendCollectedData();
    }

    public void changeAccount() {
        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS_TAG, Context.MODE_PRIVATE);
        prefs.edit().remove(Constants.USER_EMAIL).apply();
        prefs.edit().remove(Constants.USER_ID).apply();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    public void logOut() {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.startActivity(intent);
    }

    private void addToastFragment(){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ToastFragment fragment = new ToastFragment();
        fragmentTransaction.add(R.id.fragmentBankDemoContainer, fragment);
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

    private void setUpListView(){
        demoBankListView = (ListView)findViewById(R.id.demoBankListView);
        demoBankListAdapter = new DemoBankListViewAdapter(this);
        demoBankListAdapter.addSectionHeaderItem(new BankRow("ACCOUNTS", null));
        demoBankListAdapter.addItem(new BankRow("40486562136016", "£2034.42"));
        demoBankListAdapter.addItem(new BankRow("53112599900015", "£14809.00"));
        demoBankListAdapter.addSectionHeaderItem(new BankRow("SERVICES", null));
        demoBankListAdapter.addItem(new BankRow("Direct debit", null));
        demoBankListAdapter.addItem(new BankRow("Standing orders", null));
        demoBankListAdapter.addSectionHeaderItem(new BankRow("SUPPORT", null));
        demoBankListAdapter.addItem(new BankRow("E-mail", "team@aimbrain.com"));
        demoBankListAdapter.addItem(new BankRow("LOG OUT", null));



        demoBankListView.setAdapter(demoBankListAdapter);

        demoBankListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1 || position == 2) {
                    Intent intent = new Intent(DemoBankActivity.this, AccountDetailsActivity.class);
                    intent.putExtra("balance", demoBankListAdapter.getItem(position).getSecondValue());
                    startActivity(intent);
                }
                if ( position == 4 || position == 5){
                    Intent intent = new Intent(DemoBankActivity.this, AccountDetailsActivity.class);
                    intent.putExtra("balance", demoBankListAdapter.getItem(1).getSecondValue());
                    startActivity(intent);
                }
                if ( position == 7 ){
                    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                    emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    emailIntent.setType("vnd.android.cursor.item/email");
                    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {"team@aimbrain.com"});
                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Banking Authentication App Android Feedback");
                    startActivity(Intent.createChooser(emailIntent, "Send e-mail using..."));
                }
                if (position == demoBankListAdapter.getCount() - 1) {
                    logOut();
                }

            }
        });
    }
}
