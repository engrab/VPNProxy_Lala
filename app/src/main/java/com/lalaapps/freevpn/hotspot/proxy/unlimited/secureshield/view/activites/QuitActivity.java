package com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.view.activites;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;


import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.R;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.ads.AdsUtils;
import com.google.android.gms.ads.AdView;

import androidx.appcompat.app.AppCompatActivity;


public class QuitActivity extends AppCompatActivity {


    AdView adView;
    @Override
    protected void onDestroy() {
        if (adView != null){
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quit);

        adView = AdsUtils.showInlineBanner(this, findViewById(R.id.llAdds));


        findViewById(R.id.chip_give_us_rating).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://play.google.com/store/apps/details?id=" + getPackageName();
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(QuitActivity.this, HomeActivity.class));

                finish();
            }
        });

        findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(QuitActivity.this, HomeActivity.class));
        finish();
    }


}