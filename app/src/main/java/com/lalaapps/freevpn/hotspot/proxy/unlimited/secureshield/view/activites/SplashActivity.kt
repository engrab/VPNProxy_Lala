package com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.view.activites

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.databinding.ActivitySplashBinding
//import com.onesignal.OneSignal


class SplashActivity : AppCompatActivity()
     {

    private var binding: ActivitySplashBinding ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        startMainActivity()
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }



    private fun startMainActivity() {
        Handler(Looper.myLooper()!!).postDelayed({
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }, 2000)
    }




}