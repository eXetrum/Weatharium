package com.exsoft.weatharium.model;

/**
 * Created by eXetrum on 02.12.2015.
 */

/*
Несколько ссылок для сверки данных

Санкт-Петербург
http://openweathermap.org/city/519690

Минск
http://openweathermap.org/city/625144

Париж
http://openweathermap.org/city/2988507


 */

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;

public class CityPreference {

    //SharedPreferences prefs;

    final String LOG_TAG = "CityPreference";
    HashMap<Integer, City> cityList;
    int defaultCityID;
    DBHelper dbHelper;

    public CityPreference(Activity activity){
        //prefs = activity.getPreferences(Activity.MODE_PRIVATE);
        cityList = new HashMap<>();
        dbHelper = new DBHelper(activity);
        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();//getReadableDatabase();
        Log.d(LOG_TAG, "CityPreference ctor, --- Rows in city_table: ---");
        // делаем запрос всех данных из таблицы city_table, получаем Cursor
        Cursor c = db.query("city_table", null, null, null, null, null, null);
        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {
            // определяем номера столбцов по имени в выборке
            int idColIndex = c.getColumnIndex("id");
            int nameColIndex = c.getColumnIndex("name");
            int countryColIndex = c.getColumnIndex("country");
            int lonColIndex = c.getColumnIndex("lon");
            int latColIndex = c.getColumnIndex("lat");

            do {
                // получаем значения по номерам столбцов и пишем все в лог
                Log.d(LOG_TAG,
                        "ID = " + c.getInt(idColIndex) +
                                ", name = " + c.getString(nameColIndex) +
                                ", country = " + c.getString(countryColIndex) +
                                ", longitude = " + c.getString(lonColIndex) +
                                ", latitude = " + c.getString(latColIndex));

                cityList.put(c.getInt(idColIndex), new City(
                        c.getInt(idColIndex),
                        c.getString(nameColIndex),
                        c.getString(countryColIndex),
                        c.getString(lonColIndex),
                        c.getString(latColIndex)
                ));

                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext());
        } else
            Log.d(LOG_TAG, "ERROR ! 0 rows readed !");
        c.close();

        Log.d(LOG_TAG, "--- Rows in app_storage_table: ---");
        // делаем запрос всех данных из таблицы city_table, получаем Cursor
        Cursor cc = db.query("app_storage_table", null, null, null, null, null, null);
        if (cc.moveToFirst()) {
            int defCol = cc.getColumnIndex("default_city_id");

            do {

                defaultCityID = cc.getInt(defCol);
                Log.d(LOG_TAG, "default_city_id = " + defaultCityID);

            } while (cc.moveToNext()) ;

        }


        cc.close();
        dbHelper.close();
    }

    // If the user has not chosen a city yet, return
    // Sydney as the default city
    public City getCity(){
        Log.d(LOG_TAG, "--- getCity ---");
        //return prefs.getString("city", "Sydney, AU");

        //return cityList.get(defaultCityID).toString();
        //City defCity = cityList.get(defaultCityID);
        //return defCity.Name();

        return cityList.get(defaultCityID);

        //return "Sankt-Peterburg";

    }

    public boolean setCity(int cityID){

        Log.d("setCity citypref", "cityID=" + cityID);
        if(!cityList.containsKey(cityID)) return false;

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("default_city_id", cityID); //These Fields should be your String values of actual column names

        //db.update("city_table", cv, "_id " + "=" + 1, null);

        int r = db.update("app_storage_table", cv, "default_city_id" + " = '" + defaultCityID + "'", null);
        Log.d(LOG_TAG, "--- SET CITY app_storage_table: ---" + r);
        //db.close();

        dbHelper.close();

        return true;

        //prefs.edit().putString("city", city).commit();
    }

    private class CustomComparator implements Comparator<City> {
        @Override
        public int compare(City o1, City o2) {
            // Сортировка по стране RU, BY, UA, UK, FR, BR, etc
            return o1.Country().compareTo(o2.Country());
        }
    }

    public ArrayList<City> getAllCity() {
        ArrayList<City> res = new ArrayList<>();
        for (City c : cityList.values()) {
            res.add(c);
        }
        Collections.sort(res, new CustomComparator());
        return res;
    }


    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            // конструктор суперкласса
            super(context, "app_db", null, 1);
            // !!!Для удаления данных раскомментировать!!!
            //context.deleteDatabase("app_db");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "--- onCreate database ---");
            // создаем таблицу с полями
            db.execSQL("create table city_table ("
                    //+ "id integer primary key autoincrement,"
                    + "id integer primary key,"
                    + "name text,"
                    + "country text,"
                    + "lon text,"
                    + "lat text"
                    + ");");
            //
            db.execSQL("create table app_storage_table ("
                    + "id integer primary key autoincrement,"
                    + "default_city_id integer"
                    + ");");
            // Добавляем данные городов
            ContentValues cv = new ContentValues();

            Log.d(LOG_TAG, "--- Insert in city_table: ---");

            //{"_id":536203,"name":"Sankt-Peterburg","country":"RU","coord":{"lon":30.25,"lat":59.916668}},
            cv.put("id", 536203);
            cv.put("name", "Санкт-Петербург"); cv.put("country", "RU"); cv.put("lon", 30.25); cv.put("lat", 59.916668);
            // вставляем запись и получаем ее ID
            long rowID = db.insert("city_table", null, cv);
            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
            cv.clear();

            //{"_id":524901,"name":"Moscow","country":"RU","coord":{"lon":37.615555,"lat":55.75222}},
            cv.put("id", 524901); cv.put("name", "Москва"); cv.put("country", "RU"); cv.put("lon", 37.615555);
            cv.put("lat", 55.75222);

            rowID = db.insert("city_table", null, cv);
            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
            cv.clear();

            //{"_id":1496153,"name":"Omsk","country":"RU","coord":{"lon":73.400002,"lat":55}},
            cv.put("id", 1496153);
            cv.put("name", "Омск"); cv.put("country", "RU"); cv.put("lon", 73.400002); cv.put("lat", 55);
            // вставляем запись и получаем ее ID
            rowID = db.insert("city_table", null, cv);
            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
            cv.clear();

            //{"_id":501175,"name":"Rostov-na-Donu","country":"RU","coord":{"lon":39.71389,"lat":47.236389}},
            cv.put("id", 501175);
            cv.put("name", "Ростов-на-Дону"); cv.put("country", "RU"); cv.put("lon", 39.71389); cv.put("lat", 47.236389);
            // вставляем запись и получаем ее ID
            rowID = db.insert("city_table", null, cv);
            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
            cv.clear();


            // подготовим данные для вставки в виде пар: наименование столбца - значение
            //{"_id":703448,"name":"Kiev","country":"UA","coord":{"lon":30.516666,"lat":50.433334}},
            cv.put("id", 703448); cv.put("name", "Киев"); cv.put("country", "UA"); cv.put("lon", 30.516666);
            cv.put("lat", 50.433334);

            rowID = db.insert("city_table", null, cv);
            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
            cv.clear();

            //{"_id":709930,"name":"Dnipropetrovsk","country":"UA","coord":{"lon":34.98333,"lat":48.450001}},
            cv.put("id", 709930);
            cv.put("name", "Днепропетровск"); cv.put("country", "UA"); cv.put("lon", 34.98333); cv.put("lat", 48.450001);
            // вставляем запись и получаем ее ID
            rowID = db.insert("city_table", null, cv);
            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
            cv.clear();

            //{"_id":698740,"name":"Odessa","country":"UA","coord":{"lon":30.732622,"lat":46.477474}},
            cv.put("id", 698740);
            cv.put("name", "Одесса"); cv.put("country", "UA"); cv.put("lon", 30.732622); cv.put("lat", 46.477474);
            // вставляем запись и получаем ее ID
            rowID = db.insert("city_table", null, cv);
            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
            cv.clear();

            //{"_id":625144,"name":"Minsk","country":"BY","coord":{"lon":27.566668,"lat":53.900002}},
            cv.put("id", 625144);
            cv.put("name", "Минск"); cv.put("country", "BY"); cv.put("lon", 27.566668); cv.put("lat", 53.900002);
            // вставляем запись и получаем ее ID
            rowID = db.insert("city_table", null, cv);
            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
            cv.clear();

            //{"_id":4119617,"name":"London","country":"US","coord":{"lon":-93.25296,"lat":35.328972}},
            cv.put("id", 4119617);
            cv.put("name", "Лондон"); cv.put("country", "US"); cv.put("lon", 93.25296); cv.put("lat", 35.328972);
            // вставляем запись и получаем ее ID
            rowID = db.insert("city_table", null, cv);
            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
            cv.clear();


            //{"_id":2968815,"name":"Paris","country":"FR","coord":{"lon":2.3486,"lat":48.853401}},
            cv.put("id", 2968815);
            cv.put("name", "Париж"); cv.put("country", "FR"); cv.put("lon", 2.3486); cv.put("lat", 48.853401);
            // вставляем запись и получаем ее ID
            rowID = db.insert("city_table", null, cv);
            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
            cv.clear();



            Log.d(LOG_TAG, "--- Insert in app_storage_table: ---");
            cv.put("default_city_id", 536203);
            rowID = db.insert("app_storage_table", null, cv);
            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    //


}
