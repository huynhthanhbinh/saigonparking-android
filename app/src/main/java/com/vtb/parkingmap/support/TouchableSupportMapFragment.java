package com.vtb.parkingmap.support;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.SupportMapFragment;

public class TouchableSupportMapFragment extends SupportMapFragment {

    public View mContentView;
    public TouchableWrapper mTouchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        mContentView = super.onCreateView(inflater, parent, savedInstanceState);
        mTouchView = new TouchableWrapper(getActivity());
        mTouchView.addView(mContentView);
        return mTouchView;
    }

    @Override
    public View getView() {
        return mContentView;
    }
}