package com.freeman.flyshare.yweathergetter4a;

public interface YahooWeatherExceptionListener {
    public void onFailConnection(final Exception e);
    public void onFailParsing(final Exception e);
    public void onFailFindLocation(final Exception e);
}
