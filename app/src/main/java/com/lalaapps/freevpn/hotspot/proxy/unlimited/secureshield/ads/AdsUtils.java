package com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.ads;
import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AdsUtils {

    public static InterstitialAd mInterstitialAd;
    public static InterstitialAd getInterstitial() {
        return mInterstitialAd;
    }


    public static void loadInterstitial(Context context) {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(context, context.getResources().getString(R.string.app_inters), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {

                mInterstitialAd = null;
            }
        });
    }




    public static AdView showBannerSmall(Context context, LinearLayout linearLayout) {
        AdView adView = new AdView(context);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(context.getResources().getString(R.string.app_banner));
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        linearLayout.removeAllViews();
        linearLayout.addView(adView);
        return adView;
    }
    public static AdView showMediumRectangle(Context context, LinearLayout linearLayout) {
        AdView adView = new AdView(context);
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        adView.setAdUnitId(context.getResources().getString(R.string.app_banner));
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        linearLayout.removeAllViews();
        linearLayout.addView(adView);
        return adView;
    }

    public static AdView showInlineBanner(Context context, LinearLayout linearLayout) {

        //get width of device
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);

        AdSize adSize = AdSize.getInlineAdaptiveBannerAdSize(adWidth, 320);

        AdView bannerView = new AdView(context);
        bannerView.setAdUnitId(context.getResources().getString(R.string.app_banner));
        bannerView.setAdSize(adSize);
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerView.loadAd(adRequest);
        linearLayout.removeAllViews();
        linearLayout.addView(bannerView);
        return bannerView;

    }


}
