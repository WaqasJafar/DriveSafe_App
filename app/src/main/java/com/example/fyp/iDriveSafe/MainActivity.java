package com.example.fyp.iDriveSafe;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.multidex.MultiDex;

import com.google.android.material.navigation.NavigationView;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends FragmentActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    FrameLayout frame;
    Button agree,disagree;
    private int Home;
    private String key_2 = "waqas project";
    private String key_4 = "senstivity";
    int s = 2;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        frame = (FrameLayout)findViewById(R.id.frame);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame,new com.example.fyp.iDriveSafe.monitor_menu()).commit();
        Toast.makeText(getApplicationContext(),"Swipe left for menu",Toast.LENGTH_SHORT).show();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        boolean isFirstTime = com.example.fyp.iDriveSafe.MyPreferences.isFirst(MainActivity.this);
        if(isFirstTime == true)
        {
            Intent help = new Intent(MainActivity.this, com.example.fyp.iDriveSafe.help.class);
            startActivity(help);

        }
//        Handler handlerData = new Handler();
//        handlerData.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                handlerData.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                    }
//                }, 1000);
//
//            }
//        }, 2000);
        if (getIntent().getStringExtra("flipcamera") != null) {
            Intent intent = new Intent(MainActivity.this, FaceTrackerActivity.class);
            intent.putExtra(key_4,""+s);
            intent.putExtra(key_2, DateFormat.getDateTimeInstance().format(new Date()));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        if(getIntent().getStringExtra("flipcamera2") != null ) {
            Intent intent = new Intent(MainActivity.this, MainActvity2.class);
            intent.putExtra(key_4,""+s);
            intent.putExtra(key_2, DateFormat.getDateTimeInstance().format(new Date()));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }



    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Closing Application")
                    .setMessage("Are you sure you want to close this application?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
        }
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame,new com.example.fyp.iDriveSafe.monitor_menu()).commit();
        }

        else if(id == R.id.fragment_home)
        {

            getSupportFragmentManager().beginTransaction().replace(R.id.frame,new com.example.fyp.iDriveSafe.Home()).commit();

        }


        else if(id == R.id.fragment_about_us)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame,new com.example.fyp.iDriveSafe.About_Us()).commit();
        }

        else if(id == R.id.fragment_technology)
        {

            getSupportFragmentManager().beginTransaction().replace(R.id.frame,new com.example.fyp.iDriveSafe.Technology()).commit();
        }


        else if(id == R.id.fragment_setting)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame,new com.example.fyp.iDriveSafe.Setting()).commit();

        }




        else if(id == R.id.help_page)
        {
            Intent hp = new Intent(MainActivity.this, com.example.fyp.iDriveSafe.help.class);
            startActivity(hp);
        }
      else if (id == R.id.nav_send) {

            getSupportFragmentManager().beginTransaction().replace(R.id.frame,new com.example.fyp.iDriveSafe.contactus()).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
