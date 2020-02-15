package com.nextsol.digitalcompass.Utils;

import android.util.Log;

public class NumderUltils {
    public static String convertNumbertoDegree(double number){
        int hour=0;
        int min=0;
        int second=0;
        hour=(int) number;
        min=(int) ((number-hour)*60);
        second=(int)((number-hour-(min/60))*3600);
        Log.d("11111", "convertNumbertoDegree: "+(number-hour-(min/60)));
        return hour+"°"+min+"'"+second;

//        105.808367
//
//        105.808367-105-0.8=0.008367




    }
    public static String getFormattedLattitudeInDegree(double latitude) {
        try {
            int latSeconds = (int) Math.round(latitude * 3600);
            int latDegrees = latSeconds / 3600;
            latSeconds = Math.abs(latSeconds % 3600);
            int latMinutes = latSeconds / 60;
            latSeconds %= 60;


            String latDegree = latDegrees >= 0 ? "N" : "S";

            return  Math.abs(latDegrees) + "°" + latMinutes
                    + "'" + latSeconds + "\"" + latDegree;

        } catch (Exception e) {
            return ""+ String.format("%8.5f", latitude) ;
        }
    }
    public static String getFormattedLongtitudeInDegree(double longitude) {
        try {


            int longSeconds = (int) Math.round(longitude * 3600);
            int longDegrees = longSeconds / 3600;
            longSeconds = Math.abs(longSeconds % 3600);
            int longMinutes = longSeconds / 60;
            longSeconds %= 60;

            String lonDegrees = longDegrees >= 0 ? "E" : "W";

            return  Math.abs(longDegrees) + "°" + longMinutes
                    + "'" + longSeconds + "\"" + lonDegrees;
        } catch (Exception e) {
            return "" + String.format("%8.5f", longitude);
        }
    }
    public static String getsymbolDirection(double degree){
        if (degree >= 0 && degree < 22.5 || degree >= 337.5) {

            return "N";
        } else if (degree >= 22.5 && degree < 67.5) {

            return  "NW";
        } else if (degree >= 67.5 && degree < 112.5) {

            return "W";
        } else if (degree >= 122.5 && degree < 157.5) {

            return "SW";
        } else if (degree >= 157.5 && degree < 202.5) {

            return "S";
        } else if (degree >= 202.5 && degree < 247.5) {

            return "SE";
        } else if (degree >= 247.5 && degree < 292.5) {

            return "E";
        } else if (degree >= 292.5 && degree < 337.5) {

            return "NE";
        }
        else {
            return "N";

        }

    }
}
