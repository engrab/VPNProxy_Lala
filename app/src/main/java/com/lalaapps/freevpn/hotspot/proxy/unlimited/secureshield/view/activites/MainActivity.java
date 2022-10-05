package com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.view.activites;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.R;
import com.lalaapps.freevpn.hotspot.proxy.unlimited.secureshield.view.fragments.HomeFragment;
import com.google.android.material.navigation.NavigationView;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawer = findViewById(R.id.drawer_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(MainActivity.this);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new HomeFragment())
                .commit();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_premium, menu);
//
//        // return true so that the menu pop up is opened
//        return true;
//    }
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch(item.getItemId())
//        {
//            case R.id.navPremium:
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setTitle("Premium");
//                builder.setMessage("This features is available soon...");
//                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//                AlertDialog alertDialog = builder.create();
//                alertDialog.show();
//                break;
//        }
//        return true;
//    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.close();
        } else {
            startActivity(new Intent(MainActivity.this, ExitActivity.class));
            finish();

        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawers();
        switch (item.getItemId()) {

            case R.id.action_privacy:

                startActivity(new Intent(MainActivity.this, PrivacyPolicyActivity.class));
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
                    String text = "Download VPN to set as home and lock screen\n https://play.google.com/store/apps/details?id=" + getPackageName();
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