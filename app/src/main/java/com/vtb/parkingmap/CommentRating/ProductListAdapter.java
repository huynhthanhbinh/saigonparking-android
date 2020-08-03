package com.vtb.parkingmap.CommentRating;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.vtb.parkingmap.R;

import java.util.List;

/**
 * Created by vtb
 */
public class ProductListAdapter extends BaseAdapter {

    private Context mContext;
    private List<Product> mProductList;

    //Constructor

    public ProductListAdapter(Context mContext, List<Product> mProductList) {
        this.mContext = mContext;
        this.mProductList = mProductList;
    }

    public void addListItemToAdapter(List<Product> list) {
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
        View v = View.inflate(mContext, R.layout.item_product_list, null);
        TextView tvUsername = v.findViewById(R.id.tv_name);
        TextView tvRating = v.findViewById(R.id.tv_price);
        RatingBar ratingBar = v.findViewById(R.id.ratingBar1);
        TextView tvComment = v.findViewById(R.id.tv_description);
        TextView tvLastupdated = v.findViewById(R.id.tv_lastupdated);
        //Set text for TextView
        tvUsername.setText(mProductList.get(position).getUsername());
        tvRating.setText((mProductList.get(position).getRating()) + " stars");
        ratingBar.setRating(Float.valueOf(mProductList.get(position).getRating()));
        tvComment.setText(mProductList.get(position).getComment());
        tvLastupdated.setText(mProductList.get(position).getLastUpdated());
        //Save product id to tag
        v.setTag(mProductList.get(position).getId());

        return v;
    }
}
