package com.vtb.parkingmap.support;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vtb.parkingmap.R;

import java.util.List;

/**
 * Created by vtb
 */
public class BookingHistoryListAdapter extends BaseAdapter {

    private Context mContext;
    private List<History> mProductList;
    private String reducedBookingId;


    //Constructor

    public BookingHistoryListAdapter(Context mContext, List<History> mProductList) {
        this.mContext = mContext;
        this.mProductList = mProductList;
    }

    public void addListItemToAdapter(List<History> list) {
        //Add list to current array list of data
        mProductList.addAll(list);
        //Notify UI
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mProductList.size();
    }

    @Override
    public Object getItem(int position) {
        return mProductList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = View.inflate(mContext, R.layout.item_history_list, null);
        TextView tvUsername = (TextView) v.findViewById(R.id.tv_name);
        TextView tvComment = (TextView) v.findViewById(R.id.tv_description);
        TextView tvLastupdated = (TextView) v.findViewById(R.id.tv_lastupdated);
        LinearLayout lnHistoryItem = (LinearLayout) v.findViewById(R.id.lnHistoryItem);

        //Set text for TextView
        tvUsername.setText(mProductList.get(position).getParkinglotName());
        tvComment.setText(mProductList.get(position).getLicensePlate());
        tvLastupdated.setText(mProductList.get(position).getCreateAt());

        Drawable myIcon;
        switch (mProductList.get(position).getStatus()) {
            case FINISHED:
                myIcon = mContext.getResources().getDrawable(R.drawable.finished_comment_item_background);
                lnHistoryItem.setBackground(myIcon);
                break;
            case REJECTED:
                myIcon = mContext.getResources().getDrawable(R.drawable.reject_comment_item_background);
                lnHistoryItem.setBackground(myIcon);
                break;
            case CANCELLED:
                myIcon = mContext.getResources().getDrawable(R.drawable.cancle_comment_item_background);
                lnHistoryItem.setBackground(myIcon);
                break;
            default:
                break;
        }
        v.setTag(mProductList.get(position).getIdBooking());
        return v;
    }
}
