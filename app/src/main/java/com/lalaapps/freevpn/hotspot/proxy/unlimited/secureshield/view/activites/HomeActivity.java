package com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.view.activites;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.R;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.view.fragments.HomeFragment;
import com.google.android.material.navigation.NavigationView;


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        drawer = findViewById(R.id.drawer_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(HomeActivity.this);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new HomeFragment())
                .commit();
    }


    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.close();
        } else {
            startActivity(new Intent(HomeActivity.this, QuitActivity.class));
            finish();

        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawers();
        switch (item.getItemId()) {

            case R.id.action_privacy:

                startActivity(new Intent(HomeActivity.this, PrivacyActivity.class));
                return true;

            case R.id.action_more:

                String url = "https://play.google.com/store/apps/developer?id=Lala+Apps+Studio";
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                return true;

            case R.id.action_rate:

                String url1 = "https://play.google.com/store/apps/details?id=" + getPackageName();
                Uri uri1 = Uri.parse(url1);
                Intent intent1 = new Intent(Intent.ACTION_VIEW, uri1);
                if (intent1.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent1);
                }
                return true;
            case R.id.action_share:

                try {
                    String text = "Download VPN to connected other server\n https://play.google.com/store/apps/details?id=" + getPackageName();
                    Intent txtIntent = new Intent(android.content.Intent.ACTION_SEND);
                    txtIntent.setType("text/plain");
                    txtIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "VPN");
                    txtIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
                    startActivity(Intent.createChooser(txtIntent, "Share VPN"));
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "can not share text", Toast.LENGTH_SHORT).show();
                }

                return true;


        }
        return false;
    }
}