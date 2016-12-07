package com.thnki.gp.fashion.palace.interfaces;

public interface SettingsResultListener
{
    void onLocationSettingsTurnedOn();
    void onLocationSettingsCancelled();
    void onLocationSettingsPermanentlyDenied();
}
