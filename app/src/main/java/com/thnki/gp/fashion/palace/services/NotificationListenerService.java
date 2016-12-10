package com.thnki.gp.fashion.palace.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thnki.gp.fashion.palace.Brandfever;
import com.thnki.gp.fashion.palace.models.Accounts;
import com.thnki.gp.fashion.palace.models.NotificationModel;
import com.thnki.gp.fashion.palace.utils.NotificationsUtil;

public class NotificationListenerService extends Service implements ChildEventListener
{
    public static final String TAG = "NotificationLService";

    public NotificationListenerService()
    {
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(Brandfever.getPreferences().getString(Accounts.GOOGLE_ID, ""))
                .child(NotificationsUtil.TAG);
        reference.addChildEventListener(this);
        Log.d(TAG, "onCreate");
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s)
    {
        Log.d(TAG, "onChildAdded : " + dataSnapshot.getValue());
        try
        {
            NotificationModel model = dataSnapshot.getValue(NotificationModel.class);
            if (!model.isRead)
            {
                NotificationsUtil.getInstance().showNotification(model);
            }
        }
        catch (Exception e)
        {
            Log.d(TAG, "Exception :" + e.getMessage());
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s)
    {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot)
    {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s)
    {

    }

    @Override
    public void onCancelled(DatabaseError databaseError)
    {

    }
}
