package com.freeman.flyshare;


import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.freeman.flyshare.yweathergetter4a.WeatherInfo;
import com.freeman.flyshare.yweathergetter4a.YahooWeather;
import com.freeman.flyshare.yweathergetter4a.YahooWeatherExceptionListener;
import com.freeman.flyshare.yweathergetter4a.YahooWeatherInfoListener;


public class WeatherFragment extends Fragment implements YahooWeatherExceptionListener, YahooWeatherInfoListener {
    View mView;
    ImageView weatherImageView;
    TextView dateTextView, weatherTextView, temperatureTextView, windDirectionTextView, windSpeedTextView, visibilityTextView;
    private YahooWeather mYahooWeather = YahooWeather.getInstance(5000, 5000, true);
    Activity fragmentActivity;
    Button refreshButton;
    public WeatherFragment() {
        // Required empty public constructor
    }


    public static WeatherFragment newInstance() {
        WeatherFragment fragment = new WeatherFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void searchByGPS() {
        mYahooWeather.setNeedDownloadIcons(true);
        mYahooWeather.setUnit(YahooWeather.UNIT.CELSIUS);
        mYahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.GPS);
        mYahooWeather.queryYahooWeatherByGPS(fragmentActivity, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_wether, container, false);
        mYahooWeather.setExceptionListener(this);
        fragmentActivity = getActivity();
        searchByGPS();
        weatherImageView = (ImageView) mView.findViewById(R.id.weather_imageView);
        dateTextView = (TextView) mView.findViewById(R.id.date_textView);
        weatherTextView = (TextView) mView.findViewById(R.id.weather_textView);
        temperatureTextView = (TextView) mView.findViewById(R.id.temperature_textView);
        windDirectionTextView = (TextView) mView.findViewById(R.id.wind_direction_textView);
        windSpeedTextView = (TextView) mView.findViewById(R.id.wind_speed_textView);
        visibilityTextView = (TextView) mView.findViewById(R.id.visibility_textView);
        refreshButton = (Button) mView.findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchByGPS();
            }
        });
        return mView;
    }

    private void failToGetWeather() {
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dateTextView.setText("N/A");
                weatherTextView.setText("N/A");
                temperatureTextView.setText("N/A");
                windDirectionTextView.setText("N/A");
                windSpeedTextView.setText("N/A");
                visibilityTextView.setText("N/A");
            }
        });

    }

    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onFailConnection(Exception e) {
        failToGetWeather();
        Log.e(this.getClass().getName(), "onFailConnection: " + e.getMessage());
        e.printStackTrace();
    }

    @Override
    public void onFailParsing(Exception e) {
        failToGetWeather();
        Log.e(this.getClass().getName(), "onFailParsing: " + e.getMessage());
        e.printStackTrace();
    }

    @Override
    public void onFailFindLocation(Exception e) {
        failToGetWeather();
        Log.e(this.getClass().getName(), "onFailParsing: " + e.getMessage());
        e.printStackTrace();
    }

    @Override
    public void gotWeatherInfo(final WeatherInfo weatherInfo) {
        if (weatherInfo != null) {
            fragmentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dateTextView.setText(weatherInfo.getCurrentConditionDate());
                    weatherTextView.setText(weatherInfo.getCurrentText());
                    float celTmp = (float) (((float) weatherInfo.getCurrentTemp() - 32f) * (5.0 / 9.0));
                    Log.e("Temprature", " ========================>>> Current:" + Integer.toString(weatherInfo.getCurrentTemp()));
                    temperatureTextView.setText(Float.toString(celTmp) + " °C");
                    windSpeedTextView.setText(weatherInfo.getWindSpeed() + " KPM");
                    windDirectionTextView.setText(weatherInfo.getWindDirection() + " degrees");
                    visibilityTextView.setText(weatherInfo.getAtmosphereVisibility());
                    if (weatherInfo.getCurrentConditionIcon() != null) {
                        weatherImageView.setImageBitmap(scaleBitmap(weatherInfo.getCurrentConditionIcon(), 160, 160));
                    }
                }
            });

        }
    }

    public static Bitmap scaleBitmap(Bitmap bitmapToScale, float newWidth, float newHeight) {
        if (bitmapToScale == null)
            return null;
        int width = bitmapToScale.getWidth();
        int height = bitmapToScale.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(newWidth / width, newHeight / height);
        return Bitmap.createBitmap(bitmapToScale, 0, 0, bitmapToScale.getWidth(), bitmapToScale.getHeight(), matrix, true);
    }

}
