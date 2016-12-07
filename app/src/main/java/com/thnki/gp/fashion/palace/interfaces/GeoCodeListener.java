package com.thnki.gp.fashion.palace.interfaces;

public interface GeoCodeListener
{
    void onAddressObtained(String result);
    void onGeoCodingFailed();
    void onCancelled();
}
