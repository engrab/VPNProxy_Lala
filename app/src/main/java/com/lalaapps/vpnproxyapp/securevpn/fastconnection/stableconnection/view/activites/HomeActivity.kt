package com.lalaapps.vpnproxyapp.securevpn.fastconnection.stableconnection.view.activites

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.lalaapps.vpnproxyapp.securevpn.fastconnection.stableconnection.R
import com.lalaapps.vpnproxyapp.securevpn.fastconnection.stableconnection.databinding.ActivityHomeBinding
import com.lalaapps.vpnproxyapp.securevpn.fastconnection.stableconnection.view.fragments.HomeFragment

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var navigationView: NavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.app_name)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this@HomeActivity)

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, HomeFragment())
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        binding.drawerLayout.closeDrawers()
        when (item.itemId) {
            R.id.action_privacy -> {
                startActivity(Intent(this@HomeActivity, PrivacyActivity::class.java))
                return true
            }
            R.id.action_more -> {
                val url = "https://play.google.com/store/apps/developer?id=Lala+Apps+Studio"
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                }
                return true
            }
            R.id.action_rate -> {
                val url1 = "https://play.google.com/store/apps/details?id=$packageName"
                val uri1 = Uri.parse(url1)
                val intent1 = Intent(Intent.ACTION_VIEW, uri1)
                if (intent1.resolveActivity(packageManager) != null) {
                    startActivity(intent1)
                }
                return true
            }
            R.id.action_share -> {
                try {
                    val text = """Download VPN to connected other server
 https://play.google.com/store/apps/details?id=$packageName"""
                    val txtIntent = Intent(Intent.ACTION_SEND)
                    txtIntent.type = "text/plain"
                    txtIntent.putExtra(Intent.EXTRA_SUBJECT, "VPN")
                    txtIntent.putExtra(Intent.EXTRA_TEXT, text)
                    startActivity(Intent.createChooser(txtIntent, "Share VPN"))
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, "can not share text", Toast.LENGTH_SHORT)
                        .show()
                }
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.close()
        } else {
            super.onBackPressed()
        }
    }
}