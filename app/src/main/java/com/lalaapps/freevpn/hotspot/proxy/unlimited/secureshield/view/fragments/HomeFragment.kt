package com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.view.fragments

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.utils.AppSettings
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.utils.CheckInternetConnection
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.R
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.pref.SharedPreference
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.ads.AdsUtils
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.ads.AdsUtils.getInterstitial
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.ads.AdsUtils.loadInterstitial
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.databinding.FragmentHomeBinding
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.pojoClasses.ServerModel
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.utils.toast
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.view.activites.ChangeServerActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.OpenVPNThread
import de.blinkt.openvpn.core.VpnStatus
import java.io.IOException


class HomeFragment : Fragment() {

    private lateinit var mContext: Context

    private var binding: FragmentHomeBinding? = null
    private var connection: CheckInternetConnection? = null
    private var vpnStart = false

    private lateinit var globalServerModel: ServerModel
    private lateinit var vpnThread: OpenVPNThread
    private lateinit var vpnService: OpenVPNService
    private lateinit var sharedPreference: SharedPreference

    private var isServerSelected: Boolean = false

    private var admobInterstitialAd: com.google.android.gms.ads.interstitial.InterstitialAd? = null

    private val getServerResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedServerModel = result.data!!.getParcelableExtra<ServerModel>("serverextra")
                globalServerModel = selectedServerModel!!

                //update selected server
                binding!!.serverFlagName.text = selectedServerModel.getCountryLong()
                binding!!.serverFlagDes.text = selectedServerModel.getIpAddress()

                binding!!.connectionIp.text = selectedServerModel.getIpAddress()
                isServerSelected = true
            }
        }

    private val vpnResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { vpnResult ->
            if (vpnResult.resultCode == Activity.RESULT_OK) {
                //Permission granted, start the VPN
                startVpn()
            } else {
                mContext.toast("For a successful VPN connection, permission must be granted.")
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vpnThread = OpenVPNThread()
        vpnService = OpenVPNService()
        connection =
            CheckInternetConnection()
        sharedPreference =
            SharedPreference(
                mContext
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        loadInterstitial(mContext)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Checking is vpn already running or not
        isServiceRunning()
        VpnStatus.initLogCache(mContext.cacheDir)


        AdsUtils.showBannerSmall(mContext, binding!!.llAds)

        binding!!.serverSelectionBlock.setOnClickListener {
            if (!vpnStart) {
                getServerResult.launch(
                    Intent(mContext, ChangeServerActivity::class.java)
                )
            } else {
                mContext.toast(resources.getString(R.string.disconnect_first))
            }
        }

        binding!!.ivStart.setOnClickListener {



            if (!vpnStart && isServerSelected) {
                prepareVpn()
            } else if (!isServerSelected && !vpnStart) {
                getServerResult.launch(
                    Intent(mContext, ChangeServerActivity::class.java)
                )
            } else if (vpnStart && !isServerSelected) {
                mContext.toast(resources.getString(R.string.disconnect_first))
            } else {
                mContext.toast("Unable to connect the VPN")
            }
        }

        binding!!.ivStop.setOnClickListener {
            if (vpnStart) {

                confirmDisconnect()
            }
        }

            loadInterstitialAd()


    }

    override fun onDestroyView() {
        binding = null

        super.onDestroyView()
    }

    private fun isServiceRunning() {
        setStatus(OpenVPNService.getStatus())
    }

    private fun getInternetStatus(): Boolean {
        return connection!!.isInternetConnected(mContext)
    }

    fun setStatus(connectionState: String?) {
        if (connectionState != null) when (connectionState) {
            "DISCONNECTED" -> {
                status("Connect")
                vpnStart = false
                OpenVPNService.setDefaultStatus()
                binding!!.connectionTextStatus.text = "Disconnected"
            }
            "CONNECTED" -> {
                vpnStart = true // it will use after restart this activity
                status("Connected")
                binding!!.connectionTextStatus.text = "Connected"
            }
            "WAIT" -> binding!!.connectionTextStatus.text = "Waiting for server connection"
            "AUTH" -> binding!!.connectionTextStatus.text = "Authenticating server"
            "RECONNECTING" -> {
                status("Connecting")
                binding!!.connectionTextStatus.text = "Reconnecting..."
            }
            "NONETWORK" -> binding!!.connectionTextStatus.text = "No network connection"
        }
    }

    private fun status(status: String) {
        //update UI here
        when (status) {
            "Connect" -> {
                onDisconnectDone()
            }
            "Connecting" -> {
            }
            "Connected" -> {
                onConnectionDone()
            }
            "tryDifferentServer" -> {
            }
            "loading" -> {
            }
            "invalidDevice" -> {
            }
            "authenticationCheck" -> {
            }
        }
    }

    private fun prepareVpn() {
        if (!vpnStart) {
            if (getInternetStatus()) {
                val intent = VpnService.prepare(context)
                if (intent != null) {
                    vpnResult.launch(intent)
                } else {
                    startVpn()
                }
                status("Connecting")

                showInterstitialAd()

            } else {
                mContext.toast("No Internet Connection")
            }
        } else if (stopVpn()) {
            mContext.toast("Disconnect Successfully")
        }
    }

    private fun confirmDisconnect() {
        val builder = AlertDialog.Builder(
            mContext
        )
        builder.setMessage(mContext.getString(R.string.connection_close_confirm))
        builder.setPositiveButton(
            mContext.getString(R.string.yes)
        ) { dialog, id -> stopVpn() }
        builder.setNegativeButton(
            mContext.getString(R.string.no)
        ) { dialog, id ->
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun stopVpn(): Boolean {
        try {
            OpenVPNThread.stop()
            status("Connect")
            showInterstitialAd()
            vpnStart = false
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    private fun startVpn() {
        try {
            val conf = globalServerModel.getOvpnConfigData()
            OpenVpnApi.startVpn(context, conf, globalServerModel.getCountryShort(), "vpn", "vpn")
            binding!!.connectionTextStatus.text = "Connecting..."
            vpnStart = true
        } catch (exception: IOException) {
            exception.printStackTrace()
        } catch (exception: RemoteException) {
            exception.printStackTrace()
        }
        showInterstitialAd()
    }

    /**
     * Broadcast receivers ***************************
     */

    var broadcastReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                setStatus(intent.getStringExtra("state"))
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            try {
                var duration = intent.getStringExtra("duration")
                var lastPacketReceive = intent.getStringExtra("lastPacketReceive")
                var byteIn = intent.getStringExtra("byteIn")
                var byteOut = intent.getStringExtra("byteOut")
                if (duration == null) duration = "00:00:00"
                if (lastPacketReceive == null) lastPacketReceive = "0"
                if (byteIn == null) byteIn = "0.0"
                if (byteOut == null) byteOut = "0.0"
                updateConnectionStatus(duration, lastPacketReceive, byteIn, byteOut)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Update status UI
     * @param duration: running time
     * @param lastPacketReceive: last packet receive time
     * @param byteIn: incoming data
     * @param byteOut: outgoing data
     */
    fun updateConnectionStatus(
        duration: String,
        lastPacketReceive: String,
        byteIn: String,
        byteOut: String
    ) {
        binding!!.vpnConnectionTime.text = "$duration"
        binding!!.downloadSpeed.text = "$byteIn"
        binding!!.uploadSpeed.text = "$byteOut"
    }

    override fun onResume() {
        LocalBroadcastManager.getInstance(mContext).registerReceiver(
            broadcastReceiver!!, IntentFilter("connectionState")
        )
        if (!this::globalServerModel.isInitialized) {
            if (sharedPreference.isPrefsHasServer) {
                globalServerModel = sharedPreference.server
                //update selected server
                binding!!.serverFlagName.text = globalServerModel.getCountryLong()
                binding!!.serverFlagDes.text = globalServerModel.getIpAddress()

                binding!!.connectionIp.text = globalServerModel.getIpAddress()
                isServerSelected = true

            } else {
                binding!!.serverFlagName.text = resources.getString(R.string.country_name)
                binding!!.serverFlagDes.text = resources.getString(R.string.IP_address)

                binding!!.connectionIp.text = resources.getString(R.string.IP_address)
            }
        }
        super.onResume()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(
            broadcastReceiver!!
        )
        super.onPause()
    }

    /**
     * Save current selected server on local shared preference
     */
    override fun onStop() {
        if (this::globalServerModel.isInitialized) {
            sharedPreference.saveServer(globalServerModel)
        }
        super.onStop()
    }

    private fun onConnectionDone() {
        binding!!.connectionTextBlock.visibility = View.VISIBLE
        binding!!.serverSelectionBlock.visibility = View.INVISIBLE

        binding!!.afterConnectionDetailBlock.visibility = View.VISIBLE
        binding!!.ivStop.visibility = View.VISIBLE
    }

    private fun onDisconnectDone() {
        binding!!.connectionTextBlock.visibility = View.VISIBLE
        binding!!.serverSelectionBlock.visibility = View.VISIBLE
        binding!!.afterConnectionDetailBlock.visibility = View.INVISIBLE
        binding!!.ivStop.visibility = View.GONE
    }

    private fun loadInterstitialAd() {
        if (!AppSettings.isUserPaid) {
            if (AppSettings.enableAdmobAds) {
                var adRequest = AdRequest.Builder().build()
                com.google.android.gms.ads.interstitial.InterstitialAd.load(
                    mContext,
                    resources.getString(R.string.app_inters),
                    adRequest,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            super.onAdFailedToLoad(p0)
                            admobInterstitialAd = null

                            Log.w("Asdasd", "onError: ${p0!!.message}")
                        }

                        override fun onAdLoaded(p0: com.google.android.gms.ads.interstitial.InterstitialAd) {
                            super.onAdLoaded(p0)
                            admobInterstitialAd = p0
                            admobInterstitialAd!!.fullScreenContentCallback = object :
                                FullScreenContentCallback() {
                                override fun onAdClicked() {
                                    super.onAdClicked()
                                }

                                override fun onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent()
                                    admobInterstitialAd = null
                                }

                                override fun onAdFailedToShowFullScreenContent(p0: com.google.android.gms.ads.AdError) {
                                    super.onAdFailedToShowFullScreenContent(p0)
                                }

                                override fun onAdImpression() {
                                    super.onAdImpression()
                                }

                                override fun onAdShowedFullScreenContent() {
                                    super.onAdShowedFullScreenContent()
                                    admobInterstitialAd = null
                                }
                            }
                        }
                    }
                );
            }

        } else {
            admobInterstitialAd = null

        }
    }

    private fun showInterstitialAd() {
        if (admobInterstitialAd != null) {
            admobInterstitialAd!!.show(requireActivity())

            loadInterstitialAd()
        }
    }
}