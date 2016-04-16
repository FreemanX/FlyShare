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
    private ProgressDialog mProgressDialog;
    Activity fragmentActivity;

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

    private void searchByGPS() {
        mYahooWeather.setNeedDownloadIcons(true);
        mYahooWeather.setUnit(YahooWeather.UNIT.CELSIUS);
        mYahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.GPS);
        mYahooWeather.queryYahooWeatherByGPS(getContext(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_wether, container, false);
        mYahooWeather.setExceptionListener(this);
        searchByGPS();

        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        fragmentActivity = getActivity();

        weatherImageView = (ImageView) mView.findViewById(R.id.weather_imageView);
        dateTextView = (TextView) mView.findViewById(R.id.date_textView);
        weatherTextView = (TextView) mView.findViewById(R.id.weather_textView);
        temperatureTextView = (TextView) mView.findViewById(R.id.temperature_textView);
        windDirectionTextView = (TextView) mView.findViewById(R.id.wind_direction_textView);
        windSpeedTextView = (TextView) mView.findViewById(R.id.wind_speed_textView);
        visibilityTextView = (TextView) mView.findViewById(R.id.visibility_textView);
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

    @Override
    public void onDestroy() {
        hideProgressDialog();
        mProgressDialog = null;
        super.onDestroy();
    }

    @Override
    public void onResume() {
        showProgressDialog();
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
        hideProgressDialog();
        if (weatherInfo != null) {
            fragmentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dateTextView.setText(weatherInfo.getCurrentConditionDate());
                    weatherTextView.setText(weatherInfo.getCurrentText());
                    int celTmp = (weatherInfo.getCurrentTemp() - 32) * (5 / 9);
                    temperatureTextView.setText(Integer.toString(celTmp) + "°C");
                    windSpeedTextView.setText(weatherInfo.getWindSpeed() + " KPM");
                    windDirectionTextView.setText(weatherInfo.getWindDirection() + " degrees");
                    visibilityTextView.setText(weatherInfo.getAtmosphereVisibility());
                    if (weatherInfo.getCurrentConditionIcon() != null) {
                        weatherImageView.setImageBitmap(scaleBitmap(weatherInfo.getCurrentConditionIcon(), 200, 200));
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

    private void showProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
    }
}
