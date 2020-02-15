package com.nextsol.digitalcompass.model;

import com.nextsol.digitalcompass.R;

import java.util.HashMap;
import java.util.Map;

public class IconForeCast {
    public Map<String, Integer> mapicon;

    public IconForeCast() {
        this.mapicon = mapicon;
        mapicon =new HashMap<>();
        mapicon.put("01d", R.drawable.i01d);
        mapicon.put("01n", R.drawable.i01n);
        mapicon.put("02d", R.drawable.i02d);
        mapicon.put("02n", R.drawable.i02n);
        mapicon.put("03d", R.drawable.i03d);
        mapicon.put("03n", R.drawable.i03n);
        mapicon.put("04d", R.drawable.i04d);
        mapicon.put("04n", R.drawable.i04n);
        mapicon.put("09d", R.drawable.i09d);
        mapicon.put("09n", R.drawable.i09n);
        mapicon.put("10n", R.drawable.i10n);
        mapicon.put("10d", R.drawable.i10d);
        mapicon.put("11d", R.drawable.i11d);
        mapicon.put("11n", R.drawable.i11n);
        mapicon.put("13d", R.drawable.i13d);
        mapicon.put("13n", R.drawable.i13n);
        mapicon.put("50d", R.drawable.i50d);
        mapicon.put("50n", R.drawable.i50n);

    }

    public Map<String, Integer> getMapicon() {
        return mapicon;
    }

    public void setMapicon(Map<String, Integer> mapicon) {
        this.mapicon = mapicon;
    }
}
