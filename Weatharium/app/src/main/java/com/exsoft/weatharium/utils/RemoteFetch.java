package com.exsoft.weatharium.utils;


import com.exsoft.weatharium.R;

/**
 * Created by eXetrum on 02.12.2015.
 */


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class RemoteFetch {

    public enum DATA_TYPE {
        weather,
        forecast,
        forecast_daily
    }

    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5";//OPEN_WEATHER_MAP_API = "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric";

    public static JSONObject getJSON(Context context, DATA_TYPE data_type, Integer cityID){
        try {
            String pUrl = String.format("%s/%s?id=%s&APPID=%s&lang=ru&units=metric&cnt=10", BASE_URL, data_type, cityID, context.getString(R.string.open_weather_maps_app_id));
            pUrl = pUrl.replace('_', '/');
            URL url = new URL(pUrl);
            Log.d("GET JSON", url.toString());
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            // This value will be 404 if the request was not
            // successful
            if(data.getInt("cod") != 200){
                return null;
            }

            return data;
        }catch(Exception e){
            return null;
        }
    }
}

/*public class RemoteFetch {

    public String getJson() {
        new AsyncTask<Void, Void, API.ApiResponse>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected API.ApiResponse doInBackground(Void... x) {
                ArrayList<String> params = new ArrayList<String>();
                params.add("id");params.add("5601538");
                params.add("APPID");params.add("58c3cdec0969373fd82d01a13c7de5bc");
                params.add("lang");params.add("ru");
                params.add("units");params.add("metric");

                return API.execute(API.ApiMethod.GET_WEATHER.format(), API.HttpMethod.GET, params.toArray(new String[params.size()]));
            }

            @Override
            protected void onPostExecute(API.ApiResponse apiResponse) {
                super.onPostExecute(apiResponse);
                try {
                    if (apiResponse.isSuccess()) {
                        android.util.Log.d("Weather",apiResponse.getJson().toString());
                    }

                } catch (Exception e) {
                    android.util.Log.e("Weather", "ALERT! ALERT! Exception!", e);
                } finally {

                }
            }
        }.execute();

        return "";
    }
}*/
