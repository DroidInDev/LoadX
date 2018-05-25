package com.cn.loadx.interfaces;

/**
 * Created by Admin on 14-02-2018.
 */

public interface LoadListener {
    void onLoadCLicked(boolean isEpand);
    void onPODSelect();
    void onWSSelect();
    void onTripComplete();
    void onLoadDetailUpdated();
}
