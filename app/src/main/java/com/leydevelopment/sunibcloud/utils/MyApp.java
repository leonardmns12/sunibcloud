package com.leydevelopment.sunibcloud.utils;

import android.app.Application;

public class MyApp extends Application {
    private static MyApp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    public static synchronized MyApp getInstance(){
        return mInstance;
    }

    public void setNetworkConnectionListener(NetworkConnectivity.NetworkConnectivityListener listner) {
         NetworkConnectivity.connectivityListener = listner;
    }
}
