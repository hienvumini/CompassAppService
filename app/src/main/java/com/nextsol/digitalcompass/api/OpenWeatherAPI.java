package com.nextsol.digitalcompass.api;

public class OpenWeatherAPI {
    public final static String APIKey = "f7d8bbdc159f450f44d7f242146e9683";

    public static String getPathAsGeo(double lattitude, double longtitute, int unitType) {
        String unit = "";
        if (unitType == 1) {
            unit = "metric";

        } else {
            if (unitType==2){

                unit="imperial";
            }

        }
        return "http://api.openweathermap.org/data/2.5/weather?lat=" + lattitude + "&lon=" + longtitute + "&units="+unit+"&appid="+APIKey;


    }
    public static String getPathAsGeo5Days(double lattitude,double longtitude, int unitType){
        String unit = "";
        if (unitType == 1) {
            unit = "metric";

        } else {
            if (unitType==2){

                unit="imperial";
            }

        }
        return "http://api.openweathermap.org/data/2.5/forecast?lat="+lattitude+"&lon="+longtitude+"&units="+unit+"&appid="+APIKey;
    }


}
