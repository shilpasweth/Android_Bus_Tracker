package com.shilpasweth.android_bus_tracker;

import java.util.List;

/**
 * Created by Lenovo on 4/8/2017.
 */

public interface MapsPresenter {
    void setView(MapsPresenterImpl.View view);
    void updateMaps(List<BusInfo> buses);
    void onStart(List<BusInfo> buses);
}
