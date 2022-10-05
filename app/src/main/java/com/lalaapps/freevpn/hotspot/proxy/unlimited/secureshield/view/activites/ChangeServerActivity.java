package com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.view.activites;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.AppSettings;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.BuildConfig;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.R;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.SharedPreference;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.adapter.ServerAdapter;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.ads.AdmobAdsUtils;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.databinding.ActivityChangeServerBinding;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.db.DbHelper;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.model.Server;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.utils.CsvParser;
import com.badoo.mobile.util.WeakHandler;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChangeServerActivity extends AppCompatActivity {

    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final List<Server> servers = new ArrayList<>();
    AdView adView;
    //TODO: About License
    private ActivityChangeServerBinding binding;
    private WeakHandler handler;
    private Request request;
    private Call mCall;
    private ServerAdapter adapter;
    private DbHelper dbHelper;
    private SharedPreference sharedPreference;

    //ads
    private com.google.android.gms.ads.interstitial.InterstitialAd admobInterstitialAd;
    private Server globalServer;
    private final ServerAdapter.ServerClickCallback serverClickCallback =
            server -> {
                Server selectedServer = new Server(
                        server.hostName,
                        server.ipAddress,
                        server.ping,
                        server.speed,
                        server.countryLong,
                        server.countryShort,
                        server.ovpnConfigData,
                        server.port,
                        server.protocol
                );


                sharedPreference.saveServer(selectedServer);
                globalServer = selectedServer;
                setIntentResult(selectedServer);
                showInterstitialAd();


            };
    private Dialog infoAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new WeakHandler();
        dbHelper = DbHelper.getInstance(getApplicationContext());
        sharedPreference = new SharedPreference(ChangeServerActivity.this);
        binding = ActivityChangeServerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AdmobAdsUtils.loadInterstitial(this);

        servers.addAll(dbHelper.getAll());
        setupSwipeRefreshLayout();
        setupRecyclerView();

        if (request == null) {
            request = new Request.Builder()
                    .url(BuildConfig.VPN_GATE_API)
                    .build();
        }

        if (servers.isEmpty()) {
            populateServerList();
        }

        binding.serverBackButton.setOnClickListener(view -> {
            finish();
        });
        binding.changeServerInfoBtn.setOnClickListener(v -> {
            infoDialog();
        });
        binding.changeServerRefreshBtn.setOnClickListener(vv -> {
            populateServerList();
        });

        loadBanner();

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInterstitialAd();
    }

    @Override
    protected void onDestroy() {


        if (mCall != null) {
            mCall.cancel();
            mCall = null;
        }
        if (infoAlertDialog != null) {
            if (infoAlertDialog.isShowing()) {
                infoAlertDialog.dismiss();
            }
        }
        binding.swipeRefresh.setOnRefreshListener(null);

        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    private void setupSwipeRefreshLayout() {
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimaryDark);
        binding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateServerList();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new ServerAdapter(servers, serverClickCallback);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(ChangeServerActivity.this, 0);
        binding.recyclerview.setHasFixedSize(true);
        binding.recyclerview.addItemDecoration(itemDecoration);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(binding.recyclerview.getContext()));
        binding.recyclerview.setAdapter(adapter);
    }

    private void loadServerList(List<Server> serverList) {
        adapter.setServerList(serverList);
        dbHelper.save(serverList);
    }

    /**
     * Displays the updated list of VPN servers
     */
    private void populateServerList() {
        binding.swipeRefresh.setRefreshing(true);
        binding.recyclerview.setVisibility(View.INVISIBLE);

        mCall = okHttpClient.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        binding.swipeRefresh.setRefreshing(false);
                        binding.recyclerview.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final List<Server> servers = CsvParser.parse(response);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadServerList(servers);
                            binding.swipeRefresh.setRefreshing(false);
                            binding.recyclerview.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
    }


    private void loadBanner() {
        if (!AppSettings.Companion.isUserPaid()) {
            binding.changeServerAdBlock.setVisibility(View.VISIBLE);
            binding.changeServerBannerAdmob.setVisibility(View.VISIBLE);

            if (AppSettings.Companion.getEnableAdmobAds()) {
                loadAdmobBanner();
            }
        } else {
            binding.changeServerAdBlock.setVisibility(View.GONE);

        }
    }

    private void loadAdmobBanner() {


        adView = AdmobAdsUtils.showBannerSmall(this, binding.changeServerBannerAdmob);

    }


    private void loadInterstitialAd() {
        if (!AppSettings.Companion.isUserPaid()) {
            if (AppSettings.Companion.getEnableAdmobAds()) {

                AdRequest adRequest = new AdRequest.Builder().build();
                com.google.android.gms.ads.interstitial.InterstitialAd.load(this, getResources().getString(R.string.app_inters), adRequest,
                        new InterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull com.google.android.gms.ads.interstitial.InterstitialAd interstitialAd) {
                                // The mInterstitialAd reference will be null until
                                // an ad is loaded.
                                admobInterstitialAd = interstitialAd;
                                admobInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                    @Override
                                    public void onAdClicked() {
                                        super.onAdClicked();
                                    }

                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        super.onAdDismissedFullScreenContent();
                                        setIntentResult(globalServer);
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                                        super.onAdFailedToShowFullScreenContent(adError);
                                    }

                                    @Override
                                    public void onAdImpression() {
                                        super.onAdImpression();
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        super.onAdShowedFullScreenContent();
                                        admobInterstitialAd = null;
                                    }
                                });
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                // Handle the error
                                admobInterstitialAd = null;
                            }
                        });
            }

        } else {

            admobInterstitialAd = null;
        }
    }

    private void showInterstitialAd() {
        if (admobInterstitialAd != null) {
            admobInterstitialAd.show(this);
        }
    }

    private void setIntentResult(Server server) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("serverextra", server);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void infoDialog() {

        infoAlertDialog = new Dialog(this);
        infoAlertDialog.setContentView(R.layout.info_dialog);
        infoAlertDialog.setCancelable(false);
        infoAlertDialog.setCanceledOnTouchOutside(false);

        Button okayButton = infoAlertDialog.findViewById(R.id.info_dialog_btn);
        TextView infoTextview = infoAlertDialog.findViewById(R.id.info_dialog_details);

        infoTextview.setMovementMethod(LinkMovementMethod.getInstance());

        okayButton.setOnClickListener(v -> {
            infoAlertDialog.dismiss();
        });

        infoAlertDialog.getWindow().setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        infoAlertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        infoAlertDialog.show();
    }
}