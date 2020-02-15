package com.nextsol.digitalcompass.model;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Forecast implements Serializable {

    long timeStamp;
    double temp;
    String city;
    String status;
    double speedWind;
    double dirWind;
    double humidity;
    double pressure;
    double feelslike;
    double mintemp;
    double maxtemp;
    long visibility;
    int icon;
    int iconID;

    public int getIconID() {
        return iconID;
    }

    public void setIconID(int iconID) {
        this.iconID = iconID;
    }

    Map<String, Integer> map = new HashMap<>();

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public long getVisibility() {
        return visibility;
    }

    public void setVisibility(long visibility) {
        this.visibility = visibility;
    }

    public double getFeelslike() {
        return feelslike;
    }

    public void setFeelslike(double feelslike) {
        this.feelslike = feelslike;
    }

    public double getMintemp() {
        return mintemp;
    }

    public void setMintemp(double mintemp) {
        this.mintemp = mintemp;
    }

    public double getMaxtemp() {
        return maxtemp;
    }

    public void setMaxtemp(double maxtemp) {
        this.maxtemp = maxtemp;
    }

    public void setIcon(int icon) {
        this.icon = icon;

    }

    public int getIcon() {
        return icon;
    }

    public Forecast(long timeStamp, float temp, String status, float speedWind, float dirWind, float humidity, float pressure) {
        this.timeStamp = timeStamp;
        this.temp = temp;

        this.status = status;
        this.speedWind = speedWind;
        this.dirWind = dirWind;
        this.humidity = humidity;
        this.pressure = pressure;

    }

    public Forecast() {
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getSpeedWind() {
        return speedWind;
    }

    public void setSpeedWind(double speedWind) {
        this.speedWind = speedWind;
    }

    public double getDirWind() {
        return dirWind;
    }

    public void setDirWind(double dirWind) {
        this.dirWind = dirWind;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }


}
