package com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.ads.AdmobAppOpenAdsUtils;
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
                new AdmobAppOpenAdsUtils(App.this);
            }
        });

    }
}
