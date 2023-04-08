package com.lalaapps.vpnproxyapp.securevpn.fastconnection.stableconnection.view.activites

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lalaapps.vpnproxyapp.securevpn.fastconnection.stableconnection.databinding.ActivityHomeBinding
import com.lalaapps.vpnproxyapp.securevpn.fastconnection.stableconnection.databinding.ActivityPrivacyBinding

class PrivacyActivity : AppCompatActivity() {
    private val binding:ActivityPrivacyBinding by lazy { ActivityPrivacyBinding.inflate(layoutInflater)}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}