package com.exsoft.weatharium.view;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.exsoft.weatharium.R;
import com.exsoft.weatharium.model.CityPreference;
import com.exsoft.weatharium.utils.RemoteFetch;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by eXetrum on 02.12.2015.
 */


        import android.content.Context;
        import android.graphics.Color;
        import android.graphics.Typeface;
        import android.os.Bundle;
        import android.os.Handler;
        import android.support.v4.app.Fragment;
        import android.util.Log;
        import android.util.TypedValue;
        import android.view.Gravity;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.TableLayout;
        import android.widget.TableRow;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.exsoft.weatharium.R;
        import com.exsoft.weatharium.model.CityPreference;
        import com.exsoft.weatharium.utils.RemoteFetch;

        import org.json.JSONArray;
        import org.json.JSONObject;

        import java.text.DateFormat;
        import java.text.SimpleDateFormat;
        import java.util.Date;
        import java.util.Locale;

/**
 * Created by eXetrum on 02.12.2015.
 */

public class Day5Forecast extends Fragment {

    private static final String LOGTAG = "Day5Forecast";

    Typeface weatherFont;

    TextView cityField;
    TextView updatedField;
    TextView detailsField;
    TextView currentTemperatureField;
    TextView weatherIcon;

    CityPreference cityPreference;
    Handler handler;

    JSONObject currentWeather;

    public Day5Forecast(){
        handler = new Handler();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);
        weatherFont = Weather.weatherFont; //Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");

        this.cityPreference = MainActivity.cityPref;
        updateWeatherData(cityPreference.getCity().ID());
        //updateWeatherData(new CityPreference(getActivity()).getCity().ID());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOGTAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.day5_forecast, container, false);

        //weatherIcon = (TextView)rootView.findViewById(R.id.weather_icon);
        //weatherIcon.setTypeface(weatherFont);

        return rootView;
    }

    public JSONObject getCurrentWeather() {
        return currentWeather;
    }

    private void updateWeatherData(final Integer cityID) {//final String city){
        new Thread(){
            public void run(){
                Log.d(LOGTAG, "udateWeatherData RemoteFetch, cityID=" + cityID);
                // Forecast
                final JSONObject json = RemoteFetch.getJSON(getActivity(), RemoteFetch.DATA_TYPE.forecast_daily, cityID);

                if(json == null){
                    handler.post(new Runnable(){
                        public void run(){
                            Log.d(LOGTAG, "RemoteFetch result=place_not_found");
                            if(getActivity() == null)
                                Log.d("UpdateWeatherData", "getActivity()==null");
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.place_not_found),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handler.post(new Runnable(){
                        public void run(){
                            Log.d(LOGTAG, "RemoteFetch result=OK");
                            renderWeather(json);
                        }
                    });
                }
            }
        }.start();
    }

    private void renderWeather(JSONObject json){
        // Используем позже для кеширования (пока не реализовано)
        currentWeather = json;
        // Пишем сообщение в лог
        Log.d(LOGTAG, "renderWeather start");
        try {
            // Получаем список объектов которые вернул сервер
            JSONArray list = json.getJSONArray("list");
            // Пишем в лог количество полученных прогнозов
            Log.d(LOGTAG, "forecast list len=" + list.length());
            // Находим идентификатор таблицы в которую будем выводить прогноз
            TableLayout table = (TableLayout)getActivity().findViewById(R.id.day5_table);
            // Очищаем содержимое (Если рендеринг вызывается повторно, вероятно таблица уже заполнена данными, поэтому и очищаем)
            table.removeAllViews();
            // Создаем настройки строки которые будем применять для всех дальнейших вставляемых строк
            TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(5, 5, 5, 5);
            rowParams.weight = 2.0f;
            // Создаем настройки для поля вывода иконки
            TableRow.LayoutParams paramsLeft = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            paramsLeft.setMargins(10, 5, 0, 5);
            paramsLeft.weight = 1.5f;
            paramsLeft.gravity = Gravity.CENTER;
            // Создаем настройки для поля вывода данных о погоде
            TableRow.LayoutParams paramsRight = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            paramsRight.setMargins(0, 5, 5, 5);
            paramsRight.weight = 0.5f;
            paramsRight.gravity = Gravity.CENTER;
            // Разбираем все полученные от сервера объекты и заносим разобрынне данные построчно в таблицу
            for(int i = 0; i < list.length(); ++i) {
                // Отлавливаем ошибки разбора JSON объектов
                try
                {
                    // Начинаем разбор
                    // Сюда собираем строку - прогноз
                    StringBuilder day5forecastData = new StringBuilder();
                    // Получаем след. объект
                    JSONObject nextJSON = list.getJSONObject(i);
                    // Получаем временную метку прогноза (день на который получен прогноз)
                    Long dateLong = nextJSON.getLong("dt");
                    // Получаем объек описывающий температуру
                    JSONObject temp = nextJSON.getJSONObject("temp");
                    // Описание деталей погоды
                    JSONObject details = nextJSON.getJSONArray("weather").getJSONObject(0);
                    //String ico = details.getString("icon");
                    // Идентификатор иконки
                    int icoID = details.getInt("id");
                    // Преобразуем числовую метку в читабельный вид
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date(dateLong * 1000);
                    // Добавляем данные о числе на которое получен прогноз
                    day5forecastData.append(df.format(date).toString());
                    day5forecastData.append("\n" + details.getString("description").toUpperCase(Locale.US));
                    day5forecastData.append("\n" + "Температура: от " + temp.getString("min") + " до " + temp.getString("max") + " ℃");
                    day5forecastData.append("\n" + "Влажность: " + nextJSON.getString("humidity") + "%");
                    day5forecastData.append("\n" + "Давление: " + nextJSON.getString("pressure") + " hPa");
                    // Ветер
                    try {
                        day5forecastData.append("\n" + "Ветер: ");
                        day5forecastData.append(nextJSON.getString("speed") + " м/с");
                        day5forecastData.append(", " + nextJSON.getString("deg") + " °");
                    } catch (Exception wex) {}
                    // Создаем строку
                    TableRow tableRow = new TableRow(this.getContext());
                    tableRow.setLayoutParams(rowParams);
                    // Поля вывода
                    TextView icoTextView = new TextView(this.getContext());
                    TextView detailTextView = new TextView(this.getContext());
                    // Выводим иконку
                    icoTextView.setTypeface(weatherFont);
                    icoTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 60);
                    icoTextView.setTextColor(Color.parseColor("#0c1b2e"));
                    icoTextView.setText(getWeatherIcon(icoID));
                    // Выводим данные прогноза
                    detailTextView.setText(day5forecastData.toString());
                    // Добавляем поля вывода данных в строку
                    tableRow.addView(icoTextView, paramsLeft);
                    tableRow.addView(detailTextView, paramsRight);
                    // Каждую четную строку пометим другим цветом
                    if(i % 2 == 0)
                        tableRow.setBackgroundResource(R.color.lightRowColor);
                    // Добавляем строку в таблицу
                    table.addView(tableRow);

                }catch (Exception ex) {
                    Log.e(LOGTAG, ex.getMessage());
                }
            }

            //Log.d(LOGTAG, json.toString());
            /*cityField.setText(json.getString("name").toUpperCase(Locale.US) +
                    ", " +
                    json.getJSONObject("sys").getString("country"));

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            detailsField.setText(
                    details.getString("description").toUpperCase(Locale.US) +
                            "\n" + "Humidity: " + main.getString("humidity") + "%" +
                            "\n" + "Pressure: " + main.getString("pressure") + " hPa" +
                            "\n" + String.format("%.2f", main.getDouble("temp"))+ " ℃");

            setWeatherIcon(details.getInt("id"),
                    json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000);
*/
        }catch(Exception e){
            Log.e(LOGTAG, "One or more fields not found in the JSON data");
        }
        Log.d(LOGTAG, "renderWeather complete");
    }

    private String getWeatherIcon(int actualId){
        //Log.d(LOGTAG, "setWeatherIcon");
        int id = actualId / 100;
        String icon = "";
        if(actualId == 800){
            icon = getActivity().getString(R.string.weather_sunny);
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
        //weatherIcon.setText(icon);
        return icon;
    }


    public void changeCity(Integer cityID){
        updateWeatherData(cityID);
    }

}
