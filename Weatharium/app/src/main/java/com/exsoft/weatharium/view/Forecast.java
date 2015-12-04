package com.exsoft.weatharium.view;

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
public class Forecast extends Fragment {
    // Метка для логирования
    private static final String LOGTAG = "Forecast";
    // Используемый шрифт для иконок
    Typeface weatherFont;
    // Ссылка на объект хранилище
    CityPreference cityPreference;
    Handler handler;
    // Будем использовать для кеширования (пока не реализовано)
    JSONObject currentForecast;
    public JSONObject getCurrentForecast() {
        return currentForecast;
    }
    // Конструктор
    public Forecast(){
        handler = new Handler();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Логируем создание объекта
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);
        // Получаем шрифт загруженный самым первым фрагментом
        weatherFont = Weather.weatherFont; //Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");
        // Получаем ссылку на хранилище
        this.cityPreference = MainActivity.cityPref;
        // Вызываем обновление данных о прогнозе
        updateForecastData(cityPreference.getCity().ID());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Записываем отладочный лог
        Log.d(LOGTAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.forecast, container, false);
        return rootView;
    }
    // Метод обновления данных окна фрагмента (Запрос на сервер и в случае успешного ответа передача данных для рендера)
    private void updateForecastData(final Integer cityID) {
        // Обновление делаем в новом потоке
        new Thread(){
            public void run(){
                Log.d(LOGTAG, "udateForecastData RemoteFetch, cityID=" + cityID);
                // Делаем запрос на сервер для получения прогноза
                final JSONObject json = RemoteFetch.getJSON(getActivity(), RemoteFetch.DATA_TYPE.forecast, cityID);
                // Сервер не найдет или ошибка передачи данных
                if(json == null){
                    handler.post(new Runnable(){
                        public void run(){
                            Log.d(LOGTAG, "RemoteFetch result=place_not_found");
                            if(getActivity() == null)
                                Log.d(LOGTAG, "UpdateForecastData, RemoteFetch, getActivity()==null");
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.place_not_found),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    // Ответ получен успешно
                    handler.post(new Runnable(){
                        public void run(){
                            // Делаем отладочную запись
                            Log.d(LOGTAG, "UpdateForecastData, RemoteFetch result=OK");
                            // Запоминаем текущий прогноз[Используем для кеширования (пока не реализовано)]
                            currentForecast = json;
                            // Запускаем отображение полученной информации
                            renderWeather(json);
                        }
                    });
                }
            }
        }.start();
    }
    // Рендер полученных данных
    private void renderWeather(JSONObject json){
        // Начало вывода данных
        Log.d(LOGTAG, "renderWeather start");
        try {
            // Разбираем полученные данные
            // Получам массив записей прогноза
            JSONArray list = json.getJSONArray("list");
            // Выводим отладочную записть о количестве полученных объектов прогноза
            Log.d(LOGTAG, "list len=" + list.length());
            // Находим таблицу для вывода данных
            TableLayout table = (TableLayout)getActivity().findViewById(R.id.forecast_table);
            // Очищаем все строки
            table.removeAllViews();
            // Создаем настройки для строк
            TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(5, 5, 5, 5);
            rowParams.weight = 2.0f;
            // Настройки для поля иконки
            TableRow.LayoutParams paramsLeft = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            paramsLeft.setMargins(10, 5, 0, 5);
            paramsLeft.weight = 1.5f;
            paramsLeft.gravity = Gravity.CENTER;
            // Настройки для поля содержащего прогноз
            TableRow.LayoutParams paramsRight = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            paramsRight.setMargins(0, 5, 5, 5);
            paramsRight.weight = 0.5f;
            paramsRight.gravity = Gravity.CENTER;
            // Разбираем каждый объект прогноза и заносим в строку таблицы
            for(int i = 0; i < list.length(); ++i) {
                try
                {
                    // Сюда собираем строку - прогноз
                    StringBuilder forecastData = new StringBuilder();
                    // Получаем след. объект
                    JSONObject nextJSON = list.getJSONObject(i);
                    // Получаем объект описывающий детали погоды (см. формат на openweather)
                    JSONObject details = nextJSON.getJSONArray("weather").getJSONObject(0);
                    JSONObject main = nextJSON.getJSONObject("main");
                    //String ico = details.getString("icon");
                    // Идентификатор иконки
                    int icoID = details.getInt("id");
                    // Формируем строку описание прогноза
                    forecastData.append(nextJSON.get("dt_txt"));
                    forecastData.append("\n" + details.getString("description").toUpperCase(Locale.US));
                    forecastData.append("\n" + "Температура: от " + main.getString("temp_min") + " до " + main.getString("temp_max") + " ℃");
                    forecastData.append("\n" + "Влажность: " + main.getString("humidity") + "%");
                    forecastData.append("\n" + "Давление: " + main.getString("pressure") + " hPa");
                    // Поля "ветер" может и не быть, поэтому обернем в try/catch.
                    try {
                        forecastData.append("\n" + "Ветер: ");
                        forecastData.append(nextJSON.getJSONObject("wind").getString("speed") + " м/с");
                        forecastData.append(", " + nextJSON.getJSONObject("wind").getString("deg") + " °");
                    } catch (Exception wex) {}
                    //
                    String dateTime =  nextJSON.get("dt_txt").toString();
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = df.parse(dateTime);
                    Long dateVal = date.getTime() / 1000;
                    /*Log.d(LOGTAG, "date=" + dateTime);
                    Log.d(LOGTAG, "DATE VAL=" + val);
                    Date d = new Date(val * 1000);
                    Log.d(LOGTAG, d.toString());*/
                    // Создаем новую строку
                    TableRow tableRow = new TableRow(this.getContext());
                    // Задаем параметры которые создали ранее
                    tableRow.setLayoutParams(rowParams);
                    // Создаем поле вывода иконки и поле вывода прогноза
                    TextView icoTextView = new TextView(this.getContext());
                    TextView detailTextView = new TextView(this.getContext());
                    // Задаем используемый шрифт
                    icoTextView.setTypeface(weatherFont);
                    icoTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 60);
                    // Изменим шрифт отображения иконки
                    icoTextView.setTextColor(Color.parseColor("#0c1b2e"));
                    // Выводим иконку
                    icoTextView.setText(getWeatherIcon(icoID, dateVal));
                    // Выводим прогноз в созданное поле
                    detailTextView.setText(forecastData.toString());
                    // Добавляем поля вывода данных в строку
                    tableRow.addView(icoTextView, paramsLeft);
                    tableRow.addView(detailTextView, paramsRight);
                    // Каждую четную строку пометим другим цветом
                    if(i % 2 == 0)
                        tableRow.setBackgroundResource(R.color.lightRowColor);
                    // Добавляем строку в таблицу
                    table.addView(tableRow);

                }catch (Exception ex) {
                    Log.e(LOGTAG, "renderWeather, create next row, error=" + ex.getMessage());
                }
            }
        }catch(Exception e){
            Log.e(LOGTAG, "renderWeather, fill rows error=" + e.getMessage());
        }
        Log.d(LOGTAG, "renderWeather complete");
    }
    // Получить иконку по коду
    private String getWeatherIcon(int actualId, long currentTime){
        //Log.d(LOGTAG, "setWeatherIcon");
        int id = actualId / 100;
        ////////////////////////////
        long sunrise = 1449211097;
        long sunset = 1449233967;
        //
        // Нужно подправить, определение времени суток по переданному времени
        //////////////////////
        String icon = "";
        if(actualId == 800){
            //if(currentTime>=sunrise && currentTime<sunset) {
                icon = getActivity().getString(R.string.weather_sunny);
            /*} else {
                icon = getActivity().getString(R.string.weather_clear_night);
            }*/
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
        return icon;
    }
    // При смене города - вызываем обновление данных
    public void changeCity(Integer cityID){
        updateForecastData(cityID);
    }

}