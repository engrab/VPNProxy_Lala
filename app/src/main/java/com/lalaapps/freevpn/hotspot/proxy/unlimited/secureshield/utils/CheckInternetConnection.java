package com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * This class is responsible for internet status checking
 */
public class CheckInternetConnection {

    public boolean isInternetConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();

        return nInfo != null && nInfo.isConnectedOrConnecting();
    }
}
