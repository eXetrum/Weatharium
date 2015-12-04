package com.exsoft.weatharium.view;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.exsoft.weatharium.R;
import com.exsoft.weatharium.model.CityPreference;
import com.exsoft.weatharium.utils.RemoteFetch;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class Weather extends Fragment {

    private static final String LOGTAG = "Weather";

    public static Typeface weatherFont;

    TextView cityField;
    TextView updatedField;
    TextView detailsField;
    TextView currentTemperatureField;
    TextView weatherIcon;

    Handler handler;

    JSONObject currentWeather;

    CityPreference cityPreference;

    public Weather(){
        //this.cityPreference = cityPreference;
        this.cityPreference = MainActivity.cityPref;
        handler = new Handler();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");
        updateWeatherData(cityPreference.getCity().ID());
        //updateWeatherData(new CityPreference(getActivity()).getCity().ID());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOGTAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.weather, container, false);
        cityField = (TextView)getActivity().findViewById(R.id.city_field);
        updatedField = (TextView)getActivity().findViewById(R.id.updated_field);
        detailsField = (TextView)rootView.findViewById(R.id.details_field);
        currentTemperatureField = (TextView) rootView.findViewById(R.id.current_temperature_field);
        weatherIcon = (TextView)rootView.findViewById(R.id.weather_icon);
        weatherIcon.setTypeface(weatherFont);

        return rootView;
    }

    public JSONObject getCurrentWeather() {
        return currentWeather;
    }

    private void updateWeatherData(final Integer cityID) {//final String city){
        new Thread(){
            public void run(){
                Log.d(LOGTAG, "UpdateWeatherData RemoteFetch start, cityID=" + cityID);
                // Current weather
                final JSONObject json = RemoteFetch.getJSON(getActivity(), RemoteFetch.DATA_TYPE.weather, cityID);

                if(json == null){
                    handler.post(new Runnable(){
                        public void run(){
                            Log.d(LOGTAG, "UpdateWeatherData RemoteFetch result=place_not_found");
                            if(getActivity() == null)
                                Log.d(LOGTAG, "UpdateWeatherData RemoteFetch result, getActivity()==null");
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.place_not_found),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handler.post(new Runnable(){
                        public void run(){
                            Log.d(LOGTAG, "UpdateWeatherData RemoteFetch result=OK");
                            renderWeather(json);
                        }
                    });
                }
            }
        }.start();
    }

    private void renderWeather(JSONObject json){
        currentWeather = json;
        Log.d(LOGTAG, "renderWeather start");
        try {
            cityField.setText(json.getString("name").toUpperCase(Locale.US) +
                    ", " +
                    json.getJSONObject("sys").getString("country"));
            // Сюда собираем строку погоду
            StringBuilder weatherData = new StringBuilder();

            JSONObject main = json.getJSONObject("main");
            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            weatherData.append(details.getString("description").toUpperCase(Locale.US));
            weatherData.append("\n" + "Влажность: " + main.getString("humidity") + "%");
            weatherData.append("\n" + "Давление: " + main.getString("pressure") + " hPa");
            try {
                weatherData.append("\n" + "Ветер: ");
                weatherData.append(json.getJSONObject("wind").getString("speed") + " м/с");
                weatherData.append(", " + json.getJSONObject("wind").getString("deg") + " °");
                weatherData.append("\n" + "Облачность: " + json.getJSONObject("clouds").getString("all") + " %");
            } catch(Exception ex) {}
            detailsField.setText(weatherData.toString());

            currentTemperatureField.setText(
                    String.format("%.2f", main.getDouble("temp"))+ " ℃");

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt")*1000));
            updatedField.setText("Last update: " + updatedOn);

            setWeatherIcon(details.getInt("id"),
                    json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000);

        }catch(Exception e){
            Log.e(LOGTAG, "renderWeather exception: [One or more fields not found in the JSON data]");
        }
        Log.d(LOGTAG, "renderWeather complete");
    }

    private void setWeatherIcon(int actualId, long sunrise, long sunset){
        Log.d(LOGTAG, "setWeatherIcon");
        int id = actualId / 100;
        String icon = "";
        if(actualId == 800){
            long currentTime = new Date().getTime();
            if(currentTime>=sunrise && currentTime<sunset) {
                icon = getActivity().getString(R.string.weather_sunny);
            } else {
                icon = getActivity().getString(R.string.weather_clear_night);
            }
        } else {
            switch(id) {
                case 2 : icon = getActivity().getString(R.string.weather_thunder);
                    break;
                case 3 : icon = getActivity().getString(R.string.weather_drizzle);
                    break;
                case 7 : icon = getActivity().getString(R.string.weather_foggy);
                    break;
                case 8 : icon = getActivity().getString(R.string.weather_cloudy);
                    break;
                case 6 : icon = getActivity().getString(R.string.weather_snowy);
                    break;
                case 5 : icon = getActivity().getString(R.string.weather_rainy);
                    break;
            }
        }
        weatherIcon.setText(icon);
    }

    public void changeCity(Integer cityID){
        updateWeatherData(cityID);
    }

}