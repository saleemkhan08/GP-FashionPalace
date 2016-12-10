package com.thnki.gp.fashion.palace.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.thnki.gp.fashion.palace.services.NotificationListenerService;

public class ChargerConnectionReceiver extends BroadcastReceiver
{
    private static final int NOTIFICATION_JOB_NUM = 109;

    public ChargerConnectionReceiver()
    {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(NotificationListenerService.TAG, "onBroadcastReceived");
        context.startService(new Intent(context, NotificationListenerService.class));
    }
}
