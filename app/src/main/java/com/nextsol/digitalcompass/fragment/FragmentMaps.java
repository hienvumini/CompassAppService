package com.nextsol.digitalcompass.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.nextsol.digitalcompass.R;
import com.nextsol.digitalcompass.Utils.MapUtils;
import com.nextsol.digitalcompass.Utils.NetWorkUltils;
import com.nextsol.digitalcompass.Utils.NumderUltils;
import com.github.shchurov.horizontalwheelview.HorizontalWheelView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.nextsol.digitalcompass.Utils.SOTWFormatter;
import com.nextsol.digitalcompass.model.Compass;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class FragmentMaps extends Fragment implements OnMapReadyCallback,
        View.OnClickListener {
    FrameLayout frameLayoutMaps;
    GoogleMap map;
    LatLng center;
    TextView textViewLatCenter, textViewLonCenter, textViewLatCenterDegree,
            textViewLonCenterDegree, textViewTimeLocal, textViewTimeGmt,
            textViewDate, textViewLat, textViewLon, textViewAddress, textViewDirection;
    ImageView imageViewZomin, imageViewZomout, imageViewMode, imageViewGrid,
            imageViewLocation, imageViewShare, imageViewGridImage, imageViewTarget;
    String string_lat = "";
    String string_lon = "";
    double lat = 0;
    double lon = 0;
    ArrayList<Integer> listmode;
    int mode = 3;
    boolean enableGrid = false;
    Location location;
    HorizontalWheelView horizontalWheelView;
    public Compass compass;

    public float currentAzimuth;
    public SOTWFormatter sotwFormatter;
    SharedPreferences sharedPreferences;
    View view;
    Handler handler;
    public static final int CODE_GET_ADDRESS_MESAGE = 1001;
    public static final int CODE_GET_MY_LOCATION = 1002;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_maps, container, false);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_Map);
        supportMapFragment.getMapAsync(this);
        init(view);
        return view;
    }


    private void init(View view) {
        frameLayoutMaps = (FrameLayout) view.findViewById(R.id.map_Map);
        textViewDate = (TextView) view.findViewById(R.id.textviewdate_Map);
        textViewLatCenter = (TextView) view.findViewById(R.id.textviewCenterLat_Map);
        textViewLonCenter = (TextView) view.findViewById(R.id.textviewCenterLon_Map);
        textViewLatCenterDegree = (TextView) view.findViewById(R.id.textviewCenterLatDegree_Map);
        textViewLonCenterDegree = (TextView) view.findViewById(R.id.textviewCenterLonDegree_Map);
        textViewTimeLocal = (TextView) view.findViewById(R.id.textviewTimeLocal_Map);
        textViewTimeGmt = (TextView) view.findViewById(R.id.textviewTimeGmt_Map);
        textViewLat = (TextView) view.findViewById(R.id.textviewLat_Map);
        textViewLon = (TextView) view.findViewById(R.id.textviewLon_Map);
        textViewAddress = (TextView) view.findViewById(R.id.textviewlocation_Maps);
        textViewAddress.setSelected(true);
        textViewDirection = (TextView) view.findViewById(R.id.textviewDirection_Map);
        imageViewZomin = (ImageView) view.findViewById(R.id.imageviewZomin_Map);
        imageViewZomout = (ImageView) view.findViewById(R.id.imageviewZomout_Map);
        imageViewMode = (ImageView) view.findViewById(R.id.imageviewMode_Map);
        imageViewGrid = (ImageView) view.findViewById(R.id.imageviewGrid_Map);
        imageViewLocation = (ImageView) view.findViewById(R.id.imageviewMyLocation_Map);
        imageViewShare = (ImageView) view.findViewById(R.id.imageviewShareMap_Map);
        imageViewGridImage = (ImageView) view.findViewById(R.id.imageviewGridImage_Map);
        imageViewZomin.setOnClickListener(this);
        imageViewZomout.setOnClickListener(this);
        imageViewMode.setOnClickListener(this);
        imageViewGrid.setOnClickListener(this);
        imageViewLocation.setOnClickListener(this);
        imageViewShare.setOnClickListener(this);
        listmode = new ArrayList<>();
        listmode.add(GoogleMap.MAP_TYPE_SATELLITE);
        listmode.add(GoogleMap.MAP_TYPE_HYBRID);
        listmode.add(GoogleMap.MAP_TYPE_NORMAL);
        listmode.add(GoogleMap.MAP_TYPE_TERRAIN);
        horizontalWheelView = (HorizontalWheelView) view.findViewById(R.id.hw);
        horizontalWheelView.setMarksCount(50);
        horizontalWheelView.setShowActiveRange(true);
        horizontalWheelView.setMarksCount(36);
        compass = new Compass(getActivity());
        Compass.CompassListener cl = getCompassListener();
        compass.setListener(cl);
        sotwFormatter = new SOTWFormatter(getActivity());
        sharedPreferences = getActivity().getSharedPreferences("location", Context.MODE_PRIVATE);
        location = new Location("");
        location.setLatitude(sharedPreferences.getFloat("lat", 21));
        location.setLongitude(sharedPreferences.getFloat("lon", 105));
        handler = new Handler(new Handler.Callback() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean handleMessage(@NonNull Message msg) {

                switch (msg.what) {
                    case CODE_GET_ADDRESS_MESAGE:

                        Bundle bundle = msg.getData();
                        LatLng latLng = bundle.getParcelable("latlng");
                        if (latLng != null) {
                            getAddressCenterPoint(latLng.latitude, latLng.longitude);
                        }
                        break;
                    case CODE_GET_MY_LOCATION:
                        Bundle bundle2 = msg.getData();
                        LatLng latLng2 = bundle2.getParcelable("latlng");
                        if (latLng2 != null) {
                            Location location2=new Location("");
                            location.setLatitude(latLng2.latitude);
                            location.setLongitude(latLng2.longitude);
                           ZoomtoMyLocation(location2);
                        }
                        break;


                }

                return false;
            }
        });

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;


        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setMinZoomPreference(1);

        if (location!=null){

            ZoomtoMyLocation(location);
        }

        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                center = map.getCameraPosition().target;
                lat = center.latitude;
                lon = center.longitude;
                LatLng latLng = new LatLng(lat, lon);
                Bundle bundle = new Bundle();
                bundle.putParcelable("latlng", latLng);
                Message message = new Message();
                message.what = CODE_GET_ADDRESS_MESAGE;
                message.setData(bundle);
                handler.sendMessage(message);


            }
        });


    }

    private void getAddressCenterPoint(double lat, double lon) {
        if (NetWorkUltils.isNetworkConnected(getActivity())) {
            textViewAddress.setTextColor(getActivity().getResources().getColor(R.color.mgreen));
            string_lat = NumderUltils.getFormattedLattitudeInDegree(lat);
            string_lon = NumderUltils.getFormattedLongtitudeInDegree(lon);
            textViewLatCenter.setText((int) (lat) + "°" + Math.round((lat - (int) (lat)) * 60));
            textViewLonCenter.setText((int) (lon) + "°" + Math.round((lon - (int) (lon)) * 60));
            textViewLatCenterDegree.setText(string_lat);
            textViewLonCenterDegree.setText(string_lon);
            textViewLat.setText(string_lat);
            textViewLon.setText(string_lon);
            textViewAddress.setText(MapUtils.getCompleteAddressString(getActivity(), lat, lon));
            textViewAddress.setVisibility(View.VISIBLE);
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("E dd.LL.yyyy", Locale.US);
            textViewTimeLocal.setText(simpleDateFormat.format(calendar.getTime()));
            textViewDate.setText(simpleDateFormat1.format(calendar.getTime()));
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date currentTime = new Date();
            textViewTimeGmt.setText(simpleDateFormat.format(currentTime.getTime()));
        } else {
            textViewAddress.setText("Internet was Interup, Please check connection!");
            textViewAddress.setTextColor(getActivity().getResources().getColor(R.color.mred));
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageviewZomin_Map:
                map.animateCamera(CameraUpdateFactory.zoomIn());
                break;
            case R.id.imageviewZomout_Map:
                map.animateCamera(CameraUpdateFactory.zoomOut());

                break;
            case R.id.imageviewMode_Map:
                if (mode + 1 >= listmode.size()) {
                    mode = 0;
                } else {
                    mode += 1;
                }
                map.setMapType(listmode.get(mode));

                break;
            case R.id.imageviewGrid_Map:
                enableGrid = !enableGrid;
                if (enableGrid) {
                    imageViewGridImage.setVisibility(View.VISIBLE);
                } else {
                    imageViewGridImage.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.imageviewMyLocation_Map:
                if (location != null) {
                    ZoomtoMyLocation(location);
                    getAddressCenterPoint(location.getLatitude(), location.getLongitude());
                }
                break;
            case R.id.imageviewShareMap_Map:
                if (location != null) {
                    Location mlocation = location;
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Share Your Location");
                    String url = "https://www.google.com/maps/@" + mlocation.getLatitude() + ","
                            + mlocation.getLongitude() + "," + map.getCameraPosition().zoom + "z?hl=en_US";

                    intent.putExtra(Intent.EXTRA_TEXT, url);
                    startActivity(Intent.createChooser(intent, "Share Using"));

                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void ZoomtoMyLocation(Location location) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the mapicon to location user
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(0)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    private Compass.CompassListener getCompassListener() {
        return new Compass.CompassListener() {
            @Override
            public void onNewAzimuth(final float azimuth) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adjustArrow(azimuth);

                    }
                });
            }

            @Override
            public void onNewMagnetic(float magnetic) {

            }
        };
    }

    private void adjustArrow(float azimuth) {
        Animation an = new RotateAnimation(-currentAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        currentAzimuth = azimuth;

        an.setDuration(1);
        an.setRepeatCount(0);
        an.setFillAfter(true);
        horizontalWheelView.setDegreesAngle(azimuth);
        textViewDirection.setText(sotwFormatter.format(azimuth));

    }

    @Override
    public void onStart() {
        super.onStart();
        compass.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        compass.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        compass.stop();
    }

    @Override
    public void onStop() {
        super.onStop();
        compass.stop();

    }
}


