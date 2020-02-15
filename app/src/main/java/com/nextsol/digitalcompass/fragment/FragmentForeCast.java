package com.nextsol.digitalcompass.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
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
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class FragmentForeCast extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_fore_cast, container, false);

        init(view);
        listener(view);
        if (NetWorkUltils.isNetworkConnected(getActivity())) {
            init(view);
            listener(view);
            getAllInfoForeCast();

        } else {

            Toasty.error(getActivity(), "Internet was interup,\n Please check Wifi/Mobile Network").show();
        }
        return view;
    }

    private void getAllInfoForeCast() {
        if (NetWorkUltils.isNetworkConnected(getActivity())) {
            getForecast(location.getLatitude(), location.getLongitude());
            getGeoCast5Days(location.getLatitude(), location.getLongitude());


        } else {
            Toasty.normal(getActivity(), "Internet was interup, please check the network!").show();

        }
    }


    private void getForecast(final double lattitude, final double longtitude) {
        textViewUpdateTime.setVisibility(View.INVISIBLE);

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                OpenWeatherAPI.getPathAsGeo(lattitude, longtitude, 1), null,
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
                                if (response.getString("visibility").length() == 0 || response.getString("visibility") == null) {

                                    forecast.setVisibility(response.getLong("visibility"));

                                } else {
                                    forecast.setVisibility(-1);

                                }

                                calendar = Calendar.getInstance();
                                textViewUpdateTime.setVisibility(View.VISIBLE);
                                textViewUpdateTime.setText(FormatDate.formatDate(FormatDate.simpleformat1, calendar) + " Local Time");
                                //gui di forecat
                                Message messagetoday = new Message();
                                messagetoday.what=CODE_FORECAST_TODAY;
                                Bundle bundletoday = new Bundle();
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
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("lat", lattitude + "");
                params.put("lon", longtitude + "");

                return params;
            }
        };
        requestQueue.add(jsonObjectRequest);

    }

    public void getGeoCast5Days(final double lattitude, final double longtitude) {
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, OpenWeatherAPI.getPathAsGeo5Days(lattitude, longtitude, 1), null, new Response.Listener<JSONObject>() {
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
                            Message message=new Message();
                            message.what=CODE_FORECAST_5DAY;
                            Bundle bundle5day=new Bundle();
                            bundle5day.putSerializable("forecast5days",listForeCastDays);
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
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("lat", lattitude + "");
                map.put("lon", longtitude + "");
                return map;
            }
        };
        requestQueue.add(jsonObjectRequest);

    }

    private void listener(View view) {
        imageViewReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getForecast(location.getLatitude(), location.getLongitude());
                getGeoCast5Days(location.getLatitude(), location.getLongitude());

            }
        });
    }

    private void init(View view) {
        scrollView=(ScrollView)  view.findViewById(R.id.scollview_ForeCast);
        relativeLayout = (RelativeLayout) view.findViewById(R.id.Relcative_Forecast);
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
                switch (msg.what){
                    case CODE_FORECAST_TODAY:
                        Bundle bundletoday=msg.getData();
                        Forecast forecasttoday= (Forecast) bundletoday.getSerializable("forecastToday");
                        if (forecasttoday!=null){
                            setInfoForecastToday(forecasttoday);
                        }
                        scrollView.setVisibility(View.VISIBLE);

                        break;
                    case CODE_FORECAST_5DAY:
                        Bundle bundle5days=msg.getData();
                        listForeCastDays=new ArrayList<>();
                        listForeCastDays= (ArrayList<Forecast>) bundle5days.getSerializable("forecast5days");
                        if (listForeCastDays.size()>0){
                            foreCastAdapter = new ForeCastAdapter(getActivity(), R.id.recycleviewDays_ForeCast, listForeCastDays);
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
        getForecast(location.getLatitude(), location.getLongitude());
        getGeoCast5Days(location.getLatitude(), location.getLongitude());

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
