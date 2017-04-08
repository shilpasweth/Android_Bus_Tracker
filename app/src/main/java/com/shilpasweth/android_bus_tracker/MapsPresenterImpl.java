package com.shilpasweth.android_bus_tracker;

import android.view.View;

import java.util.List;

/**
 * Created by Lenovo on 4/8/2017.
 */

public class MapsPresenterImpl implements MapsPresenter {

    private View mView;

    private List<BusInfo> mBuses;

    @Override
    public void onStart(List<BusInfo> buses) {
        mBuses = buses;
        updateMaps(mBuses);
    }

    @Override
    public void setView(View view) {
        mView = view;
    }

    @Override
    public void updateMaps(List<BusInfo> buses) {
        mView.updateBuses(buses);
    }

    public interface View {
        void updateBuses(List<BusInfo> events);
    }
}
