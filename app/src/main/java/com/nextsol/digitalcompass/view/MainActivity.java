package com.nextsol.digitalcompass.view;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.material.snackbar.Snackbar;
import com.nextsol.digitalcompass.BuildConfig;
import com.nextsol.digitalcompass.Utils.GlobalApplication;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nextsol.digitalcompass.R;
import com.nextsol.digitalcompass.Utils.FragmentUtils;
import com.nextsol.digitalcompass.Utils.Utils;
import com.nextsol.digitalcompass.fragment.FragmentCompass;
import com.nextsol.digitalcompass.fragment.FragmentFlashlight;
import com.nextsol.digitalcompass.fragment.FragmentForeCast;
import com.nextsol.digitalcompass.fragment.FragmentMaps;
import com.nextsol.digitalcompass.service.LocationUpdatesService;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SharedPreferences.OnSharedPreferenceChangeListener {
    BottomNavigationView bottomNavigationView;
    FragmentCompass fragmentCompass;
    FragmentFlashlight fragmentFlashlight;
    FragmentForeCast fragmentForeCast;
    FragmentMaps fragmentMaps;
    int PERMISSION_ID = 44;
    GlobalApplication globalApplication;
    public static Location mylocation;

    GoogleApiClient googleApiClient;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    int backpress = 0;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkPlayService()) {
            buildGoogleApiClient();
        }
        init();
        bottomNavigationView.setSelectedItemId(R.id.menu_nav_compass);
        FragmentUtils.openFragment(fragmentCompass, getSupportFragmentManager(), R.id.framelayoutFragment);

        listener();

    }


    private boolean checkPlayService() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {

            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1000).show();

            } else {

                Toasty.error(this, "Device wasn't supported!").show();

            }
            return false;
        }
        return true;
    }

    private void buildGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        }
    }

    private void listener() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragmentselect = null;
                switch (menuItem.getItemId()) {
                    case R.id.menu_nav_compass:
                        fragmentselect = fragmentCompass;
                        break;
                    case R.id.menu_nav_map:
                        fragmentselect = fragmentMaps;
                        break;
                    case R.id.menu_nav_forecast:
                        fragmentselect = fragmentForeCast;
                        break;
                    case R.id.menu_nav_flashlight:
                        fragmentselect = fragmentFlashlight;
                        break;
                    default:
                        fragmentselect = fragmentMaps;

                        break;
                }

                FragmentUtils.openFragment(fragmentselect, getSupportFragmentManager(), R.id.framelayoutFragment);

                return true;
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void init() {
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.ctNavigationbotton);
        fragmentCompass = new FragmentCompass();
        fragmentFlashlight = new FragmentFlashlight();
        fragmentForeCast = new FragmentForeCast();

        fragmentMaps = new FragmentMaps();
        globalApplication = (GlobalApplication) getApplicationContext();
        sharedPreferences = getSharedPreferences("location", MODE_PRIVATE);
        editor = sharedPreferences.edit();


    }

    private void RequestPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);

        }
        mylocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (mylocation != null) {

            editor.putFloat("lat", (float) mylocation.getLatitude());
            editor.putFloat("lon", (float) mylocation.getLongitude());
            editor.commit();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                mylocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                if (mylocation != null) {

                    Toasty.success(this, "Get location success!").show();
                    editor.putFloat("lat", (float) mylocation.getLatitude());
                    editor.putFloat("lon", (float) mylocation.getLongitude());
                    editor.apply();
                }
                Toasty.success(this, "Request Location Permission Success!").show();

            } else {
                Toasty.error(this, "Request Location Permission Faild!").show();
                finish();

            }

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        RequestPermission();
    }


    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        googleApiClient.connect();

    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {

        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        googleApiClient.connect();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        backpress++;
        if (backpress >= 2) {
            finish();
        } else {
            Toasty.normal(this, "Press back to close the application!").show();

        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}


