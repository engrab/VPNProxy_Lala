package com.lalaapps.vpnproxyapp.securevpn.fastconnection.stableconnection.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * This class is responsible for internet status checking
 */
public class NetworkConnections {

    public boolean isNetworkAvailable(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();

        return nInfo != null && nInfo.isConnectedOrConnecting();
    }
}
