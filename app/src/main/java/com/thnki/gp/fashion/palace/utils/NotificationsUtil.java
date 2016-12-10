package com.thnki.gp.fashion.palace.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thnki.gp.fashion.palace.Brandfever;
import com.thnki.gp.fashion.palace.R;
import com.thnki.gp.fashion.palace.StoreActivity;
import com.thnki.gp.fashion.palace.models.Accounts;
import com.thnki.gp.fashion.palace.models.NotificationModel;
import com.thnki.gp.fashion.palace.services.NotificationListenerService;

import java.net.URL;
import java.util.HashSet;

public class NotificationsUtil
{
    public static final String TAG = "Notifications";
    private NotificationManager mNotificationManager;
    private static NotificationsUtil sInstance;
    private SharedPreferences mPreferences;
    private DatabaseReference mRootDbRef;

    private NotificationsUtil()
    {
        mNotificationManager = (NotificationManager) Brandfever.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mPreferences = Brandfever.getPreferences();
        mRootDbRef = FirebaseDatabase.getInstance().getReference();
    }

    public static NotificationsUtil getInstance()
    {
        if (sInstance == null)
        {
            sInstance = new NotificationsUtil();
        }
        return sInstance;
    }

    public void showNotification(NotificationModel data)
    {
        Log.d(NotificationListenerService.TAG, "showNotification : " + data);
        showNotification(data, null);
        /*if (data.photoUrl == null)
        {
            showNotification(data, null);
        }
        else
        {
            Log.d(NotificationListenerService.TAG, "showNotification : Executing ShowNormalNotification" );
            new ShowNormalNotification().execute(data);
        }*/
    }

    private void showNotification(NotificationModel model, Bitmap mLargeIcon)
    {
        Log.d(NotificationListenerService.TAG, "showNotification : " + model + ", " + mLargeIcon);
        Context mAppContext = Brandfever.getAppContext();
        Intent contentIntent = new Intent(Brandfever.getAppContext(), StoreActivity.class);

        contentIntent.putExtra(StoreActivity.NOTIFICATION_ACTION, model.action);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(Brandfever.getAppContext(),
                (int) System.currentTimeMillis(), contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mAppContext);
        mBuilder.setContentTitle(model.username)
                .setSmallIcon(R.mipmap.shopping_cart_white)
                .setAutoCancel(true)
                .setContentText(model.notification)
                .setContentIntent(contentPendingIntent);

        if (mLargeIcon != null)
        {
            mBuilder.setLargeIcon(mLargeIcon);
        }

        Notification notificationDefault = new Notification();
        notificationDefault.defaults |= Notification.DEFAULT_LIGHTS; // LED
        notificationDefault.defaults |= Notification.DEFAULT_VIBRATE; //Vibration
        notificationDefault.defaults |= Notification.DEFAULT_SOUND; // Sound
        mBuilder.setDefaults(notificationDefault.defaults);
        mNotificationManager.notify((int) System.currentTimeMillis(), mBuilder.build());
    }

    private Bitmap getCircleBitmapFromUrl(String photoUrl)
    {
        Log.d(NotificationListenerService.TAG, "showNotification : getCircleBitmapFromUrl");
        try
        {
            URL url = new URL(photoUrl);
            return getSquareBitmap(BitmapFactory.decodeStream(url.openConnection().getInputStream()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d(NotificationListenerService.TAG, " Error : " + e.getMessage());
        }
        return BitmapFactory.decodeResource(Brandfever.getAppContext().getResources(), R.mipmap.app_icon);
    }

    private class ShowNormalNotification extends AsyncTask<NotificationModel, Void, Void>
    {
        NotificationModel mModel;
        Bitmap mLargeIcon;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            Log.d(NotificationListenerService.TAG, "showNotification : onPreExecute");
        }

        @Override
        protected Void doInBackground(NotificationModel... params)
        {
            Log.d(NotificationListenerService.TAG, "showNotification : doInBackground");
            mModel = params[0];
            mLargeIcon = getCircleBitmapFromUrl(mModel.photoUrl);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            Log.d(NotificationListenerService.TAG, "showNotification : onPostExecute");
            showNotification(mModel, mLargeIcon);
        }
    }

    private Bitmap getSquareBitmap(Bitmap srcBmp)
    {
        Log.d(NotificationListenerService.TAG, "showNotification : getSquareBitmap");
        Bitmap dstBmp = Bitmap.createBitmap(
                srcBmp,
                0,
                srcBmp.getHeight() / 2 - srcBmp.getWidth() / 2,
                srcBmp.getWidth(),
                srcBmp.getWidth()
                                           );
        if (srcBmp.getWidth() >= srcBmp.getHeight())
        {

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth() / 2 - srcBmp.getHeight() / 2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
                                        );

        }
        return Bitmap.createScaledBitmap(dstBmp, 120, 120, true);
    }

    private void saveNotification(NotificationModel notificationModel, String googleId)
    {
        if (googleId != null && !googleId.equals(mPreferences.getString(Accounts.GOOGLE_ID, "")))
        {
            Log.d("NotificationFlow", "saveNotification Owner googleId : " + googleId);
            DatabaseReference ownersNotificationDbRef = mRootDbRef.child(googleId).child(NotificationsUtil.TAG);
            ownersNotificationDbRef.push().setValue(notificationModel);
        }
    }

    public void sendNotificationToAll(NotificationModel notificationModel, String userGoogleId)
    {
        /**
         *  //TODO check if notificationModel already exits and then save it.
         */
        Log.d("NotificationFlow", "saveNotificationInAllOwnersProfile : " + notificationModel.toString());
        saveNotification(notificationModel, userGoogleId);
        for (String googleId : Brandfever.getPreferences().getStringSet(Accounts.OWNERS_GOOGLE_IDS, new HashSet<String>()))
        {
            saveNotification(notificationModel, googleId);
        }
    }
}