package com.lalaapps.vpnproxyapp.securevpn.fastconnection.stableconnection;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

import com.lalaapps.vpnproxyapp.securevpn.fastconnection.stableconnection.ads.AdsOpenUtils;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class App extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                new AdsOpenUtils(App.this);
            }
        });

    }
}
