package com.aimbrain.bankingdemo;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import java.net.ConnectException;
import java.util.ArrayList;

import com.aimbrain.sdk.Manager;
import com.aimbrain.sdk.exceptions.InternalException;
import com.aimbrain.sdk.exceptions.SessionException;
import com.aimbrain.sdk.models.ScoreModel;
import com.aimbrain.sdk.server.ScoreCallback;
import com.aimbrain.bankingdemo.adapters.TransactionsListViewAdapter;
import com.aimbrain.bankingdemo.fragments.ToastFragment;
import com.aimbrain.bankingdemo.models.Transaction;
import com.aimbrain.bankingdemo.score.ScoreManager;


public class TransactionsActivity extends AppCompatActivity {

    private ListView listView;
    private TransactionsListViewAdapter transactionsListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);
        listView = (ListView)findViewById(R.id.transactionsListView);
        ArrayList<Transaction> generatedTransactions = Transaction.generateTransactions();
        transactionsListViewAdapter = new TransactionsListViewAdapter(this, R.layout.transactions_list_view_row, generatedTransactions.toArray(new Transaction[generatedTransactions.size()]));
        listView.setAdapter(transactionsListViewAdapter);

        getSupportActionBar().setTitle("Transactions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addToastFragment();
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

    public void addToastFragment(){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ToastFragment fragment = new ToastFragment();
        fragmentTransaction.add(R.id.fragmentTransactionsContainer, fragment);
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
