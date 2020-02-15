package com.nextsol.digitalcompass.view;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.nextsol.digitalcompass.R;
import com.nextsol.digitalcompass.Utils.GlobalApplication;
import com.nextsol.digitalcompass.api.OpenWeatherAPI;
import com.nextsol.digitalcompass.model.Forecast;
import com.nextsol.digitalcompass.model.IconForeCast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Implementation of App Widget functionality.
 */
public class ForecastWidget extends AppWidgetProvider implements SharedPreferences.OnSharedPreferenceChangeListener {
    public SharedPreferences MysharedPreferences;

    public static Location Mylocation;
    Map<String, Integer> map;
    LatLng latLng;
    RemoteViews views;


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {


        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.forecast_widget);
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.linerWidgetForeCast, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
        //views.setTextViewText(R.id.appwidget_text, widgetText);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
        GlobalApplication globalApplication = (GlobalApplication) context.getApplicationContext();
        init(context);


    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void init(Context context) {
        map = new HashMap<>();
        setDataMap(context);


    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {

            MysharedPreferences = context.getSharedPreferences("location", Context.MODE_PRIVATE);
            if (MysharedPreferences != null) {
                float latold = MysharedPreferences.getFloat("lat", 21);
                float lonold = MysharedPreferences.getFloat("lon", 105);
                latLng = new LatLng(latold, lonold);


            } else {
                latLng = new LatLng(21, 105);

            }

            updateAppWidget(context, appWidgetManager, appWidgetId);
            getForeCastInfo(context, latLng.latitude, latLng.longitude, appWidgetManager, appWidgetIds);
            views = new RemoteViews(context.getPackageName(), R.layout.forecast_widget);
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("code", 113);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.linerWidgetForeCast, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);

        }


    }

    private void getForeCastInfo(Context context, double lat, double lon, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
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
                                if (response.getString("visibility").length() == 0 || response.getString("visibility") == null) {

                                    forecast.setVisibility(response.getLong("visibility"));

                                } else {
                                    forecast.setVisibility(-1);

                                }
                                Calendar calendar = Calendar.getInstance();
                                Locale locale = Locale.US;
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a", locale);

                                views.setTextViewText(R.id.textvieewTemp_Widget, (int) (forecast.getTemp()) + "°c");
                                views.setTextViewText(R.id.textviewStatus_Widget, forecast.getStatus());
                                views.setTextViewText(R.id.textviewCity_Widget, forecast.getCity());
                                views.setTextViewText(R.id.textviewClock_Widget, simpleDateFormat.format(calendar.getTime()));
                                views.setImageViewResource(R.id.imageIcon_Widget, forecast.getIcon());
                                //                                views.setImageViewBitmap(R.id.imageIcon_Widget,);
                                appWidgetManager.updateAppWidget(appWidgetIds, views);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("11111", "onErrorResponse: " + error.toString());

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("lat", Mylocation.getLatitude() + "");
                params.put("lon", Mylocation.getLongitude() + "");

                return params;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    public void setDataMap(Context context) {
        IconForeCast iconForeCast = new IconForeCast();
        map = iconForeCast.getMapicon();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        MysharedPreferences = sharedPreferences;
    }
}

