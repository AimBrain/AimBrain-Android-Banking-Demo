package com.aimbrain.bankingdemo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aimbrain.bankingdemo.R;

import java.util.ArrayList;
import java.util.TreeSet;

import com.aimbrain.bankingdemo.models.BankRow;


public class DemoBankListViewAdapter extends BaseAdapter {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_DETAIL = 1;
    private static final int TYPE_LOGOUT = 2;

    private ArrayList<BankRow> mData = new ArrayList<>();
    private TreeSet<Integer> sectionHeader = new TreeSet<Integer>();

    private LayoutInflater mInflater;

    public DemoBankListViewAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(final BankRow item) {
        mData.add(item);
        notifyDataSetChanged();
    }

    public void addSectionHeaderItem(final BankRow item) {
        mData.add(item);
        sectionHeader.add(mData.size() - 1);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if(position == mData.size()-1) return TYPE_LOGOUT;
        return sectionHeader.contains(position) ? TYPE_HEADER : TYPE_DETAIL;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public BankRow getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int rowType = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            switch (rowType) {
                case TYPE_HEADER:
                    convertView = mInflater.inflate(R.layout.header_row, null);
                    holder.firstTextView = (TextView) convertView.findViewById(R.id.headerTextView);
                    break;
                case TYPE_DETAIL:
                    convertView = mInflater.inflate(R.layout.details_row, null);
                    holder.firstTextView = (TextView) convertView.findViewById(R.id.leftTextView);
                    holder.secondTextView = (TextView) convertView.findViewById(R.id.rightTextView);
                    holder.secondTextView.setText(mData.get(position).getSecondValue());
                    break;
                case TYPE_LOGOUT:
                    convertView = mInflater.inflate(R.layout.logout_row, null);
                    holder.firstTextView = (TextView) convertView.findViewById(R.id.logoutTextView);
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        switch (rowType){
            case TYPE_DETAIL:
                holder.secondTextView.setText(mData.get(position).getSecondValue());;
            default:
                holder.firstTextView.setText(mData.get(position).getFirstValue());
                break;
        }

        return convertView;
    }

    public static class ViewHolder {
        public TextView firstTextView;
        public TextView secondTextView;
    }
}
