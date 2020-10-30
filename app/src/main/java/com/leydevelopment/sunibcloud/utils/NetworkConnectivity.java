package com.leydevelopment.sunibcloud.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

public class NetworkConnectivity extends BroadcastReceiver {

    public static NetworkConnectivityListener connectivityListener;

    public NetworkConnectivity() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(connectivityListener!=null){
            connectivityListener.onNetworkConnectivityChanged(isConnected);
        }
    }

    public boolean isConnected(Context mContext){
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1){
            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean isConnected = false;
            Network[] allNetworks = cm.getAllNetworks(); // added in API 21 (Lollipop)

            for (Network network : allNetworks) {

                NetworkCapabilities networkCapabilities = cm.getNetworkCapabilities(network);

                if (networkCapabilities != null) {

                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

                            || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)

                            || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))

                        isConnected = true;

                }
            }
            return isConnected;
        } else{
            ConnectivityManager cm = (ConnectivityManager) MyApp.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
    }

    public interface NetworkConnectivityListener{
        void onNetworkConnectivityChanged(boolean isConnected);
    }
}
