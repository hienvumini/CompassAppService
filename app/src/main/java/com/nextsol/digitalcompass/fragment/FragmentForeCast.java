package com.nextsol.digitalcompass.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.nextsol.digitalcompass.R;
import com.nextsol.digitalcompass.Utils.FormatDate;
import com.nextsol.digitalcompass.Utils.NetWorkUltils;
import com.nextsol.digitalcompass.Utils.NumderUltils;
import com.nextsol.digitalcompass.adapter.ForeCastAdapter;
import com.nextsol.digitalcompass.api.OpenWeatherAPI;
import com.nextsol.digitalcompass.model.Forecast;
import com.nextsol.digitalcompass.model.IconForeCast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public final class FragmentForeCast extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    ImageView imageViewReload, imageViewIcon;
    TextView textViewCity, textViewUpdateTime, textViewDegree, textViewWindSpeed,
            textViewWindDir, textViewHumidity, textViewPressure,
            textViewStatus, textViewVisibility, textViewFeelslike, textViewminmaxTemp;
    Calendar calendar;
    Location location;
    RecyclerView recyclerViewDays;
    ArrayList<Forecast> listForecast3Hour;
    ArrayList<Forecast> listForeCastDays;
    ForeCastAdapter foreCastAdapter;
    Map<String, Integer> map;
    View view;
    SharedPreferences sharedPreferences;
    RelativeLayout relativeLayout;
    Handler handler;
    public static final int CODE_FORECAST_TODAY = 901;
    public static final int CODE_FORECAST_5DAY = 902;
    ScrollView scrollView;
    Runnable runnableUpdateForeCast;
    Handler handlerupdateForeCast;
    LatLng latLng;
    Bundle bundletoday;
    Bundle bundletodayreceive;
    Bundle bundle5daysreceive;
    Message messagetoday;
    Forecast forecasttoday;
    LinearLayout linearLayoutLoadingForeCast;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_fore_cast, container, false);

        init(view);
        listener();
        getAllInfoForeCast();
        runnableUpdateForeCast = new Runnable() {
            @Override
            public void run() {

                getAllInfoForeCast();

                handler.postDelayed(runnableUpdateForeCast, 600000);
            }
        };
        handlerupdateForeCast = new Handler();
        handlerupdateForeCast.postDelayed(runnableUpdateForeCast, 60000);
        return view;
    }

    private void getAllInfoForeCast() {
        if (NetWorkUltils.isNetworkConnected(getActivity())) {
            getForecast();
            getGeoCast5Days();
            relativeLayout.setTop(0);
            scrollView.smoothScrollTo(0, 0);


        } else {
            Toasty.normal(getActivity(), "Internet was interup, please check the network!").show();

        }
    }


    private void getForecast() {

        if (sharedPreferences != null) {
            float latold = sharedPreferences.getFloat("lat", 21);
            float lonold = sharedPreferences.getFloat("lon", 105);
            latLng = new LatLng(latold, lonold);


        } else {
            latLng = new LatLng(21, 105);

        }
        double lat = latLng.latitude;
        double lon = latLng.longitude;

        textViewUpdateTime.setVisibility(View.INVISIBLE);

        linearLayoutLoadingForeCast.setVisibility(View.VISIBLE);

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                OpenWeatherAPI.getPathAsGeo(lat, lon, 1), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response != null) {
                            Forecast forecast = new Forecast();
                            try {
                                JSONArray jsonArray = response.getJSONArray("weather");

                                forecast.setIcon(map.get(jsonArray.getJSONObject(0).getString("icon")));
                                forecast.setStatus(jsonArray.getJSONObject(0).getString("description"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                JSONObject jsonObject = response.getJSONObject("main");
                                forecast.setTemp(jsonObject.getDouble("temp"));
                                forecast.setMintemp(jsonObject.getDouble("temp_min"));
                                forecast.setMaxtemp(jsonObject.getDouble("temp_max"));
                                forecast.setFeelslike(jsonObject.getDouble("feels_like"));
                                forecast.setHumidity(jsonObject.getDouble("humidity"));
                                forecast.setPressure(jsonObject.getDouble("pressure"));
                                jsonObject = response.getJSONObject("wind");
                                forecast.setSpeedWind(jsonObject.getDouble("speed"));
                                try {
                                    forecast.setDirWind(jsonObject.getDouble("deg"));
                                } catch (Exception e) {
                                }
                                forecast.setTimeStamp(response.getLong("dt"));
                                forecast.setCity(response.getString("name"));


                                calendar = Calendar.getInstance();
                                textViewUpdateTime.setVisibility(View.VISIBLE);
                                textViewUpdateTime.setText(FormatDate.formatDate(FormatDate.simpleformat1, calendar) + " Local Time");
                                //gui di forecat

                                messagetoday = new Message();


                                messagetoday.what = CODE_FORECAST_TODAY;
                                if (bundletoday == null) {
                                    bundletoday = new Bundle();
                                }
                                bundletoday.putSerializable("forecastToday", (Serializable) forecast);
                                messagetoday.setData(bundletoday);
                                handler.sendMessage(messagetoday);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


            }
        });
        requestQueue.add(jsonObjectRequest);

    }

    public void getGeoCast5Days() {
        if (sharedPreferences != null) {
            float latold = sharedPreferences.getFloat("lat", 21);
            float lonold = sharedPreferences.getFloat("lon", 105);
            latLng = new LatLng(latold, lonold);


        } else {
            latLng = new LatLng(21, 105);

        }
        final double lat = latLng.latitude;
        final double lon = latLng.longitude;

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, OpenWeatherAPI.getPathAsGeo5Days(lat, lon, 1), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response != null) {
                    if (response.length() > 0) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("list");

                            String timeStamp;
                            JSONObject jsonObject = new JSONObject();
                            JSONArray jsonArraychild = new JSONArray();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Forecast forecast = new Forecast();
                                JSONObject jsonObjectchild = new JSONObject();
                                jsonObject = (JSONObject) jsonArray.get(i);
                                forecast.setTimeStamp(jsonObject.getLong("dt"));
                                forecast.setTemp(jsonObject.getJSONObject("main").getDouble("temp"));
                                forecast.setHumidity(jsonObject.getJSONObject("main").getDouble("humidity"));
                                forecast.setPressure(jsonObject.getJSONObject("main").getDouble("pressure"));
                                forecast.setStatus(jsonObject.getJSONArray("weather").getJSONObject(0).getString("main"));
                                forecast.setIcon(map.get(jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon")));
                                forecast.setSpeedWind(jsonObject.getJSONObject("wind").getDouble("speed"));
                                forecast.setDirWind(jsonObject.getJSONObject("wind").getDouble("deg"));
                                listForecast3Hour.add(forecast);
                                if (i % 8 == 0) {
                                    listForeCastDays.add(forecast);
                                }
                            }
                            Message message = new Message();
                            message.what = CODE_FORECAST_5DAY;
                            Bundle bundle5day = new Bundle();
                            bundle5day.putSerializable("forecast5days", listForeCastDays);
                            message.setData(bundle5day);
                            handler.sendMessage(message);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonObjectRequest);

    }

    private void listener() {
        imageViewReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                scrollView.setVisibility(View.INVISIBLE);
                getAllInfoForeCast();




            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void init(View view) {

        scrollView = (ScrollView) view.findViewById(R.id.scollview_ForeCast);
        relativeLayout = (RelativeLayout) view.findViewById(R.id.Relcative_Forecast);
        linearLayoutLoadingForeCast = (LinearLayout) view.findViewById(R.id.linerLayoutLoading_ForeCast);
        if (isNight()) {
            relativeLayout.setBackgroundResource(R.drawable.night_sky);

        } else {
            relativeLayout.setBackgroundResource(R.drawable.sky_blue);
        }

        sharedPreferences = getActivity().getSharedPreferences("location", Context.MODE_PRIVATE);
        location = new Location("");
        location.setLatitude(sharedPreferences.getFloat("lat", 21));
        location.setLongitude(sharedPreferences.getFloat("lon", 105));
        imageViewIcon = (ImageView) view.findViewById(R.id.imageviewIcon_ForeCast);
        imageViewReload = (ImageView) view.findViewById(R.id.imgReload_Maps);
        imageViewReload.setColorFilter(view.getResources().getColor(R.color.mwhite));
        textViewCity = (TextView) view.findViewById(R.id.textviewCity_Forecast);
        textViewUpdateTime = (TextView) view.findViewById(R.id.textviewTimeupdate_Forecast);
        textViewDegree = (TextView) view.findViewById(R.id.textviewDegree_Forecast);
        textViewWindSpeed = (TextView) view.findViewById(R.id.textviewWindspeed_Forecast);
        textViewWindDir = (TextView) view.findViewById(R.id.textviewWindDir_Forecast);
        textViewHumidity = (TextView) view.findViewById(R.id.textviewHumidity_Forecast);
        textViewStatus = (TextView) view.findViewById(R.id.textviewStatus_Forecast);
        textViewVisibility = (TextView) view.findViewById(R.id.textviewVisibility_Forecast);
        textViewPressure = (TextView) view.findViewById(R.id.textviewPressure_Forecast);
        textViewFeelslike = (TextView) view.findViewById(R.id.textviewFeelslike_Forecast);
        textViewminmaxTemp = (TextView) view.findViewById(R.id.textviewMinmaxTemp_ForeCast);
        calendar = Calendar.getInstance();
        recyclerViewDays = (RecyclerView) view.findViewById(R.id.recycleviewDays_ForeCast);
        listForecast3Hour = new ArrayList<>();
        listForeCastDays = new ArrayList<>();
        foreCastAdapter = new ForeCastAdapter(getActivity(), R.id.recycleviewDays_ForeCast, listForeCastDays);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerViewDays.setHasFixedSize(true);
        recyclerViewDays.setNestedScrollingEnabled(false);
        recyclerViewDays.setLayoutManager(linearLayoutManager);
        recyclerViewDays.setAdapter(foreCastAdapter);
        setDataMap();
        setInfoForeCast();


    }

    private void setInfoForeCast() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case CODE_FORECAST_TODAY:
                        bundletodayreceive = msg.getData();
                        if (forecasttoday == null) {
                            forecasttoday = (Forecast) bundletodayreceive.getSerializable("forecastToday");
                        }
                        if (forecasttoday != null) {
                            setInfoForecastToday(forecasttoday);
                        }
                        scrollView.scrollTo(view.getTop(), view.getRight());
                        scrollView.setVisibility(View.VISIBLE);
                        linearLayoutLoadingForeCast.setVisibility(View.INVISIBLE);


                        break;
                    case CODE_FORECAST_5DAY:
                        bundle5daysreceive = msg.getData();
                        listForeCastDays = new ArrayList<>();
                        listForeCastDays = (ArrayList<Forecast>) bundle5daysreceive.getSerializable("forecast5days");
                        if (listForeCastDays.size() > 0) {
                            if (foreCastAdapter == null) {
                                foreCastAdapter = new ForeCastAdapter(getActivity(), R.id.recycleviewDays_ForeCast, listForeCastDays);

                            }
                            recyclerViewDays.setAdapter(foreCastAdapter);
                        }

                        break;

                }
                return false;
            }
        });

    }

    public void setDataMap() {

        IconForeCast iconForeCast = new IconForeCast();
        map = iconForeCast.getMapicon();

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        getForecast();
        getGeoCast5Days();

    }

    public boolean isNight() {

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour < 6 || hour > 18) {
            return true;
        } else {
            return false;
        }

    }

    public void setInfoForecastToday(Forecast forecast1) {
        textViewminmaxTemp.setText("Day ↑" + (int) (forecast1.getMaxtemp()) + "°C ⋅ Night ↓"
                + (int) (forecast1.getMintemp()) + "°C");
        textViewDegree.setText((int) (forecast1.getTemp()) + "°C");
        textViewFeelslike.setText("Feels like " + (int) (forecast1.getFeelslike()) + "°C");
        imageViewIcon.setImageResource(forecast1.getIcon());
        textViewStatus.setText(forecast1.getStatus());
        textViewWindSpeed.setText(forecast1.getSpeedWind() + "km/h");
        textViewWindDir.setText(forecast1.getDirWind() + "°" + NumderUltils.getsymbolDirection(forecast1.getDirWind()));
        textViewHumidity.setText(forecast1.getHumidity() + "%");
        textViewPressure.setText((int) (forecast1.getPressure()) + "mb");
        if (forecast1.getVisibility() != -1) {
            textViewVisibility.setText("Visibility: " + forecast1.getVisibility() + " (" + forecast1.getVisibility() / 1000 + " km)");

        } else {
            textViewVisibility.setVisibility(View.GONE);

        }
        textViewCity.setText(forecast1.getCity());
        textViewCity.setVisibility(View.VISIBLE);

    }
}
