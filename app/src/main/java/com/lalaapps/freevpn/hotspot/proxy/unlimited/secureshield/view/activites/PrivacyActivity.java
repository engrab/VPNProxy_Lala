package com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.view.activites;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.R;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.ads.AdsUtils;
import com.google.android.gms.ads.AdView;
public class PrivacyActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private WebView privacy_policy;
    private ProgressBar progressBar_policy;
    AdView adView;
    public static String privacyPolicy = "https://sites.google.com/view/trendingfreeappsstudio/home";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        toolbar = this.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        progressBar_policy = findViewById(R.id.progressBar_policy);

        privacy_policy = findViewById(R.id.privacy_policy);
        privacy_policy.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                //Make the bar disappear after URL is loaded, and changes string to Loading...
                toolbar.setTitle("Loading...");
                setProgress(progress * 100); //Make the bar disappear after URL is loaded

                // Return the app name after finish loading
                if(progress == 100){
                    toolbar.setTitle(R.string.app_name);
                    progressBar_policy.setVisibility(View.GONE);
                }
            }
        });
        privacy_policy.getSettings().setJavaScriptEnabled(true);
        privacy_policy.loadUrl(privacyPolicy);


        adView = AdsUtils.showBannerSmall(this, findViewById(R.id.llAdView));
    }

    @Override
    protected void onDestroy() {
        if (adView!=null){
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}