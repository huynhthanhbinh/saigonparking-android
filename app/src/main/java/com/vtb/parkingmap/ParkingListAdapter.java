package com.vtb.parkingmap;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bht.parkingmap.api.proto.parkinglot.ParkingLotResult;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Created by User on 3/14/2017.
 */

public class ParkingListAdapter extends ArrayAdapter<ParkingLotResult> {

    private static final String TAG = "ParkingListAdapter";

    private Context mContext;
    private int mResource;
    private int lastPosition = -1;

    /**
     * Holds variables in a View
     */
    private static class ViewHolder {
        TextView name;
        TextView distance;

    }

    /**
     * Default constructor for the ParkingListAdapter
     *
     * @param context
     * @param resource
     * @param objects
     */
    public ParkingListAdapter(Context context, int resource, List<ParkingLotResult> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //get the parking information
        ParkingLotResult parkingLotResult = getItem(position);

        //create the view result for showing the animation
        View result;

        //ViewHolder object
        ViewHolder holder;


        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.textView1);
            holder.distance = (TextView) convertView.findViewById(R.id.textView2);


            result = convertView;

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext,
                (position > lastPosition) ? R.anim.load_down_anim : R.anim.load_up_anim);
        result.startAnimation(animation);
        lastPosition = position;

        holder.name.setText(Objects.requireNonNull(parkingLotResult).getName());
        holder.distance.setText(String.format(Locale.FRENCH, "%.2f Km", parkingLotResult.getDistance()));


        return convertView;
    }
}

