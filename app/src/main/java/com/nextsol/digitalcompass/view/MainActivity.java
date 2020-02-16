package com.nextsol.digitalcompass.view;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
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
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.nextsol.digitalcompass.Utils.GlobalApplication;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nextsol.digitalcompass.R;
import com.nextsol.digitalcompass.Utils.FragmentUtils;
import com.nextsol.digitalcompass.fragment.FragmentCompass;
import com.nextsol.digitalcompass.fragment.FragmentFlashlight;
import com.nextsol.digitalcompass.fragment.FragmentForeCast;
import com.nextsol.digitalcompass.fragment.FragmentMaps;

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

    public GoogleApiClient googleApiClient;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    int backpress = 0;
    FusedLocationProviderClient mFusedLocationClient;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final String BROADCAST_ACTION = "android.location.PROVIDERS_CHANGED";
    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            showSettingDialog();
        }
    };
    Runnable runnableUpdateLocation;
    Handler handlerUpdateLocation;

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

    private void UpdateLocation() {
        runnableUpdateLocation = new Runnable() {
            @Override
            public void run() {
                //mylocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        mylocation = task.getResult();
                    }
                });

                if (mylocation != null) {
                    editor.putFloat("lat", (float) mylocation.getLatitude());
                    editor.putFloat("lon", (float) mylocation.getLongitude());
                    editor.commit();
                    handlerUpdateLocation.postDelayed(runnableUpdateLocation, 10000);
                }

            }
        };
        handlerUpdateLocation = new Handler();
        handlerUpdateLocation.postDelayed(runnableUpdateLocation, 10000);
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
            try {
                googleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            } catch (Exception e) {
            }

        }

    }

    private void showSettingDialog() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//Setting priotity of Location request to high
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);//5 sec Time interval for location update
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient to show dialog always when GPS is off

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        Log.d("123456", "onResult: \"GPS is Enabled in your device\"");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
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
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    mylocation = location;
                    editor.putFloat("lat", (float) mylocation.getLatitude());
                    editor.putFloat("lon", (float) mylocation.getLongitude());
                    editor.commit();

                }
            }
        });
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
        runnableUpdateLocation = new Runnable() {
            @Override
            public void run() {
                UpdateLocation();
                handlerUpdateLocation.postDelayed(runnableUpdateLocation, 5000);

            }
        };
        handlerUpdateLocation = new Handler();
        handlerUpdateLocation.postDelayed(runnableUpdateLocation, 5000);

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
        registerReceiver(gpsLocationReceiver, new IntentFilter(BROADCAST_ACTION));


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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK:
                        Log.e("Settings", "Result OK");
                        Log.d("123456", "onActivityResult:  GPS is Enabled in your device");
                        //startLocationUpdates();
                        break;
                    case RESULT_CANCELED:
                        Log.e("Settings", "Result Cancel");

                        Log.d("123456", "onActivityResult:  GPS is Disabled in your device");
                        break;
                }
                break;
        }
    }

    private BroadcastReceiver gpsLocationReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //If Action is Location
            if (intent.getAction().matches(BROADCAST_ACTION)) {
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                //Check if GPS is turned ON or OFF
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Log.e("About GPS", "GPS is Enabled in your device");

                } else {

                    new Handler().postDelayed(sendUpdatesToUI, 10);
                    // showSettingDialog();

                    Log.e("About GPS", "GPS is Disabled in your device");
                }
            }
        }
    };


}






