package com.exsoft.weatharium.view;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TabHost;
import android.widget.TextView;

import com.exsoft.weatharium.R;
import com.exsoft.weatharium.model.City;
import com.exsoft.weatharium.model.CityPreference;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOGTAG = "MainActivity";

    private TabHost mTabHost;
    private ViewPager mViewPager;
    private TabsAdapter mTabsAdapter;

    public static CityPreference cityPref;

    TextView cityField;
    TextView updatedField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        cityField = (TextView)findViewById(R.id.city_field);
        updatedField = (TextView)findViewById(R.id.updated_field);
        // Создаем экземпляр объекта хранящего список всех городов и последний выбранный город.
        cityPref = new CityPreference(this);
        //
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();
        //
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
        // Добавляем вкладки
        mTabsAdapter.addTab(mTabHost.newTabSpec("simple1").setIndicator(getString(R.string.weather_label)), Weather.class, null);
        mTabsAdapter.addTab(mTabHost.newTabSpec("simple2").setIndicator(getString(R.string.forecast_label)), Forecast.class, null);
        mTabsAdapter.addTab(mTabHost.newTabSpec("simple3").setIndicator(getString(R.string.day5_forecast_label)), Day5Forecast.class, null);

        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
            // Связываем фрагменты
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.weather_content, new Weather())
                    .commit();

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.forecast_content, new Forecast())
                    .commit();

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.day5_forecast_content, new Day5Forecast())
                    .commit();
        }

        cityField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создаем диалог
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                // Задаем заголовок
                dialog.setTitle("Выбор города");
                // Создаем адаптер для использования наших объектов City
                final ArrayAdapter<City> adapter = new ArrayAdapter<City>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
                // Получаем список всех объектов - городов, возможных для использования
                ArrayList<City> itemsList = cityPref.getAllCity();
                // Заносим в адаптер
                for(int i = 0; i < itemsList.size(); ++i)
                    adapter.add(itemsList.get(i));
                // Прикрепляем адаптер к диалогу
                dialog.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    // Обрабатываем нажатие
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(LOGTAG, "" + which);
                        // Получаем выбранный город (объекты городов прикреплены к каждому пункту меню, по сути каждый пункт это объект City)
                        City choice = adapter.getItem(which);
                        // Попытка смены города
                        if(cityPref.setCity(choice.ID())) {
                            changeCity(choice);
                            Log.d(LOGTAG, "Новый город по умолчанию установлен успешно");
                        } else {
                            // Пишем в лог и прерываем выполнение
                            Log.d(LOGTAG, " Ошибка смены города, город не в списке ?");
                        }
                    }
                });
                // Кнопка отмены
                dialog.setNegativeButton("Отмена", null);
                // Вызываем показ диалога
                dialog.show();
/*
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                //set the title for alert dialog
                builder.setTitle("Choose city: ");

                builder.setItems(names, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        // setting the button text to the selected itenm from the list
                        btn.setText(cities[item]);
                    }

                });
                builder.setCancelable(false).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //When clicked on CANCEL button the dalog will be dismissed
                        dialog.dismiss();
                    }
                });
                // Creating alert dialog

                AlertDialog alert = builder.create();
                alert.show();*/
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }
    // Метод смены города
    public void changeCity(City city){
        // Получаем идентификатор города
        int cityID = city.ID();
        // Получаем ссылки на фрагменты
        Weather weather = (Weather)getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.pager + ":0");

        Forecast forecast = (Forecast)getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.pager + ":1");

        Day5Forecast day5Forecast = (Day5Forecast)getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.pager + ":2");

        // Проверим полученные ссылки
        if(weather == null) {
            // Пишем в лог
            Log.d(LOGTAG, "changeCity, weather==null");
            // Прерываем выполнение
            return;
        }
        // Проверим что ссылка не нулевая
        if(forecast == null) {
            // Пишем в лог
            Log.d(LOGTAG, "changeCity, forecast==null");
            // Прерываем выполнение
            return;
        }
        // Проверим что ссылка не нулевая
        if(day5Forecast == null) {
            // Пишем в лог
            Log.d(LOGTAG, "changeCity, day5Forecast==null");
            // Прерываем выполнение
            return;
        }
        // Новый город установлен. Пора обновить информацию.
        // Обращаемся к каждому Fragment`у и вызываем смену города.
        // Внутри каждого из фрагментов произойдет вызов обновления информации
        weather.changeCity(cityID);
        forecast.changeCity(cityID);
        day5Forecast.changeCity(cityID);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
