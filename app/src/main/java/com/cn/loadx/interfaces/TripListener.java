package com.cn.loadx.interfaces;

/**
 * Created by Admin on 09-02-2018.
 */

public interface TripListener {
    void onTripClicked(boolean isExpand);
    void callSupervisorDialog();
    void onSourceLocationClicked();
    void onDestinationLocationClicked();
    void tryLoadingTripAgain();
}
