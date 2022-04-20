package com.weatherapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout home_RL;
    private ProgressBar loading_PB;
    private TextView cityName_TV, temperature_TV, condition_TV;
    private RecyclerView weather_RV;
    private ImageView back_IV, icon_IV, search_IV;
    private EditText city_EdT;
    private ArrayList<Weather> weatherArrayList;
    private WeatherAdapter weatherAdapter;
    private LocationManager locationManager;
    private final int PERMISSION_CODE = 1;
    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

        mapping();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            }, PERMISSION_CODE);
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName = getCityName(location.getLongitude(), location.getLatitude());
        getWeatherInfo(cityName);
        search_IV.setOnClickListener(view -> {
            String city = city_EdT.getText().toString();
            if (city.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_LONG).show();
            } else {
                cityName_TV.setText(cityName);
                getWeatherInfo(city);
                city_EdT.setText("");
            }
        });

        weatherAdapter = new WeatherAdapter(this, weatherArrayList);
        weather_RV.setAdapter(weatherAdapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Permission granted...", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "Please provide permissions...", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String getCityName(double longitude, double latitude) {
        String cityName = "Not Found";
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 10);
            for (Address adr : addressList) {
                if (adr != null) {
                    String city = adr.getLocality();
                    if (city != null && !city.equals("")) {
                        cityName = city;
                    } else {
                        Toast.makeText(this, "City not found", Toast.LENGTH_LONG).show();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cityName;
    }

    public void mapping() {
        home_RL = findViewById(R.id.RL_Home);
        loading_PB = findViewById(R.id.PB_Loading);
        cityName_TV = findViewById(R.id.TV_CityName);
        temperature_TV = findViewById(R.id.TV_Temperature);
        condition_TV = findViewById(R.id.TV_Condition);
        weather_RV = findViewById(R.id.RV_Weather);
        back_IV = findViewById(R.id.IV_Back);
        icon_IV = findViewById(R.id.TV_Icon);
        search_IV = findViewById(R.id.IV_Search);
        city_EdT = findViewById(R.id.Edt_city);

        weatherArrayList = new ArrayList<>();
        weatherAdapter = new WeatherAdapter(this, weatherArrayList);
        weather_RV.setAdapter(weatherAdapter);
    }

    private void getWeatherInfo(String cityName) {
        String url = "http://api.weatherapi.com/v1/forecast.json?key=34b3d503802f4d108a6163608221604&q=" + cityName + "&days=1&aqi=yes&alerts=yes";
        cityName_TV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        @SuppressLint("NotifyDataSetChanged") JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            loading_PB.setVisibility(View.GONE);
            home_RL.setVisibility(View.VISIBLE);
            weatherArrayList.clear();

            try {
                String tem = response.getJSONObject("current").getString("temp_c");
                temperature_TV.setText(String.format("%s°C", tem));
                int is_day = response.getJSONObject("current").getInt("is_day");
                String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                Picasso.get().load("http:".concat(conditionIcon)).into(icon_IV);
                condition_TV.setText(condition);

                if (is_day == 1) {
                    Picasso.get().load("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSEjR9VGOsMyQ5jl9sFqaBDJBnuin29LDmTyDE4h9ObhfG1aVh8mK33WjEIcKumY7Iw8lw&usqp=CAU").into(back_IV);
                } else {
                    Picasso.get().load("https://discovery.sndimg.com/content/dam/images/discovery/fullset/2020/4/2/nightsky_getty.jpg.rend.hgtvcom.476.268.suffix/1585862315352.jpeg").into(back_IV);
                }

                JSONObject forecastObj = response.getJSONObject("forecast");
                JSONObject forcast = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                JSONArray hourArray = forcast.getJSONArray("hour");
                for (int i = 0; i < hourArray.length(); i++) {
                    JSONObject hourObj = hourArray.getJSONObject(i);
                    String time = hourObj.getString("time");
                    String temp = hourObj.getString("temp_c");
                    String img = hourObj.getJSONObject("condition").getString("icon");
                    String wind = hourObj.getString("wind_kph");
                    weatherArrayList.add(new Weather(time, temp, img, wind));
                }
                weatherAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Toast.makeText(MainActivity.this, "Please enter the valid city", Toast.LENGTH_LONG).show());
        requestQueue.add(objectRequest);
    }
}