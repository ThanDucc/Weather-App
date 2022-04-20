package com.weatherapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Weather> weatherArrayList;

    public WeatherAdapter(Context context, ArrayList<Weather> weatherArrayList) {
        this.context = context;
        this.weatherArrayList = weatherArrayList;
    }

    @NonNull
    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weather_rv_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.ViewHolder holder, int position) {
        Weather weather = weatherArrayList.get(position);
        holder.temperature_TV.setText(String.format("%s°C", weather.getTemperature()));
        Picasso.get().load("http:".concat(weather.getIcon())).into(holder.icon_IV);
        holder.windSpeed_TV.setText(String.format("%sKm/h", weather.getWindSpeed()));
        @SuppressLint("SimpleDateFormat") SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat output = new SimpleDateFormat("hh:mm aa");
        try {
            Date date = input.parse(weather.getTime());
            holder.time_TV.setText(output.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return weatherArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView time_TV, temperature_TV, windSpeed_TV;
        private ImageView icon_IV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            time_TV = itemView.findViewById(R.id.TV_time);
            temperature_TV = itemView.findViewById(R.id.TV_temperature);
            windSpeed_TV = itemView.findViewById(R.id.TV_windSpeed);
            icon_IV = itemView.findViewById(R.id.IM_icon);


        }
    }
}
