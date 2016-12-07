package com.thnki.gp.fashion.palace.firebase.fcm;

import android.os.AsyncTask;

import com.google.firebase.iid.FirebaseInstanceId;
import com.thnki.gp.fashion.palace.Brandfever;

import java.io.IOException;

import static com.thnki.gp.fashion.palace.firebase.fcm.NotificationInstanceIdService.NOTIFICATION_INSTANCE_ID;

public class UpdateDeleteTokenAsyncTask extends AsyncTask<Boolean, Void,Void>
{
    @Override
    protected Void doInBackground(Boolean... updateOrDelete)
    {
        if(updateOrDelete[0])
        {
            updateToken();
        }
        else
        {
            deleteToken();
        }
        return null;
    }

    private void deleteToken()
    {
        try
        {
            FirebaseInstanceId.getInstance().deleteInstanceId();
            Brandfever.getPreferences().edit().putString(NOTIFICATION_INSTANCE_ID, "").apply();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void updateToken()
    {
        String token = FirebaseInstanceId.getInstance().getToken();
        Brandfever.getPreferences().edit().putString(NOTIFICATION_INSTANCE_ID, token).apply();
    }
}
