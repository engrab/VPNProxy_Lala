package com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.view.activites;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.badoo.mobile.util.BuildConfig;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.utils.AppSettings;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.R;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.pref.SharedPreference;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.ads.AdsUtils;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.databinding.ActivityChangeServerBinding;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.database.DbHelper;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.pojoClasses.ServerModel;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.utils.CsvParser;
import com.badoo.mobile.util.WeakHandler;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.utils.OvpnUtils;

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
    private final List<ServerModel> serverModels = new ArrayList<>();
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
    private ServerModel globalServerModel;
    private final ServerAdapter.ServerClickCallback serverClickCallback =
            server -> {
                ServerModel selectedServerModel = new ServerModel(
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


                sharedPreference.saveServer(selectedServerModel);
                globalServerModel = selectedServerModel;
                setIntentResult(selectedServerModel);
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

        AdsUtils.loadInterstitial(this);

        serverModels.addAll(dbHelper.getAll());
        setupSwipeRefreshLayout();
        setupRecyclerView();

        if (request == null) {
            request = new Request.Builder()
                    .url(getResources().getString(R.string.VPN_GATE_API))
                    .build();
        }

        if (serverModels.isEmpty()) {
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
        adapter = new ServerAdapter(serverModels, serverClickCallback);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(ChangeServerActivity.this, 0);
        binding.recyclerview.setHasFixedSize(true);
        binding.recyclerview.addItemDecoration(itemDecoration);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(binding.recyclerview.getContext()));
        binding.recyclerview.setAdapter(adapter);
    }

    private void loadServerList(List<ServerModel> serverModelList) {
        adapter.setServerList(serverModelList);
        dbHelper.save(serverModelList);
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
                    final List<ServerModel> serverModels = CsvParser.parse(response);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadServerList(serverModels);
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


        adView = AdsUtils.showBannerSmall(this, binding.changeServerBannerAdmob);

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
                                        setIntentResult(globalServerModel);
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

    private void setIntentResult(ServerModel serverModel) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("serverextra", serverModel);
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


    public static class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ViewHolder> {

        /*
        * Set the server the data
        * */
        private List<ServerModel> serverModels = new ArrayList<>();

        private ServerClickCallback callback;

        public ServerAdapter(List<ServerModel> serverModels, @NonNull ServerClickCallback callback) {
            this.serverModels.clear();
            this.serverModels.addAll(serverModels);
            this.callback = callback;
        }

        public void setServerList(@NonNull final List<ServerModel> serverModelList) {
            if (serverModels.isEmpty()) {
                serverModels.clear();
                serverModels.addAll(serverModelList);
                notifyItemRangeInserted(0, serverModelList.size());
            } else {
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return serverModels.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return serverModelList.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        ServerModel old = serverModels.get(oldItemPosition);
                        ServerModel serverModel = serverModelList.get(newItemPosition);
                        return old.hostName.equals(serverModel.hostName);
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        ServerModel old = serverModels.get(oldItemPosition);
                        ServerModel serverModel = serverModelList.get(newItemPosition);
                        return old.hostName.equals(serverModel.hostName)
                                && old.ipAddress.equals(serverModel.ipAddress)
                                && old.countryLong.equals(serverModel.countryLong);
                    }
                });
                serverModels.clear();
                serverModels.addAll(serverModelList);
                result.dispatchUpdatesTo(this);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.server_list_item, parent, false);
            return new ViewHolder(view, callback);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(serverModels.get(position));


        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return serverModels == null ? 0 : serverModels.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final View rootView;
            final TextView countryView;
            final TextView protocolView;
            final TextView ipAddressView;
            final TextView speedView;
            final TextView pingView;

            final ServerClickCallback callback;

            public ViewHolder(View view, ServerClickCallback callback) {
                super(view);
                rootView = view;
                countryView = view.findViewById(R.id.tv_country_name);
                protocolView = view.findViewById(R.id.tv_protocol);
                ipAddressView = view.findViewById(R.id.tv_ip_address);
                speedView = view.findViewById(R.id.tv_speed);
                pingView = view.findViewById(R.id.tv_ping);

                this.callback = callback;
            }

            public void bind(@NonNull final ServerModel serverModel) {
                final Context context = rootView.getContext();

                countryView.setText(serverModel.countryLong);
                protocolView.setText(serverModel.protocol.toUpperCase());
                ipAddressView.setText(context.getString(R.string.format_ip_address,
                        serverModel.ipAddress, serverModel.port));
                speedView.setText(context.getString(R.string.format_speed,
                        OvpnUtils.humanReadableCount(serverModel.speed, true)));
                pingView.setText(context.getString(R.string.format_ping, serverModel.ping));
                rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onItemClick(serverModel);
                    }
                });
            }
        }

        public interface ServerClickCallback {
            void onItemClick(@NonNull ServerModel serverModel);
        }
    }
}