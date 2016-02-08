package com.aimbrain.bankingdemo.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.aimbrain.bankingdemo.R;

import com.aimbrain.bankingdemo.models.Transaction;


public class TransactionsListViewAdapter extends ArrayAdapter<Transaction> {

    private int resource;

    public TransactionsListViewAdapter(Context context, int resource, Transaction[] objects) {
        super(context, resource, objects);
        this.resource = resource;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(this.resource, null);
        }

        Transaction transaction = getItem(position);

        if (transaction != null) {
            TextView transactionNameTextView = (TextView) v.findViewById(R.id.transactionNameTextView);
            TextView transactionValueTextView = (TextView) v.findViewById(R.id.transactionValueTextView);

            if (transactionNameTextView != null)
                transactionNameTextView.setText(transaction.getName());

            if (transactionValueTextView != null) {
                if (transaction.getValue() >= 0)
                    transactionValueTextView.setTextColor(Color.BLACK);
                else
                    transactionValueTextView.setTextColor(Color.RED);
                transactionValueTextView.setText("" + transaction.getValue());
            }
        }

        return v;
    }

}
