package com.thnki.gp.fashion.palace.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.thnki.gp.fashion.palace.Brandfever;
import com.thnki.gp.fashion.palace.firebase.database.models.Accounts;
import com.thnki.gp.fashion.palace.firebase.database.models.NotificationModel;
import com.thnki.gp.fashion.palace.utils.NotificationsUtil;

public class ShowNotificationService extends Service
{
    public ShowNotificationService()
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        final DatabaseReference notificationDbRef = FirebaseDatabase.getInstance().getReference()
                .child(Brandfever.getPreferences().getString(Accounts.GOOGLE_ID, ""))
                .child(NotificationsUtil.TAG);
        notificationDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                try
                {
                    Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                    for (DataSnapshot snapshot : snapshots)
                    {
                        NotificationModel model = snapshot.getValue(NotificationModel.class);
                        model.isNotified = true;
                        notificationDbRef.child(snapshot.getKey()).setValue(model);
                        //TODO Show notification
                    }
                }
                catch (Exception e)
                {
                    Log.d("Exception", e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
        return START_NOT_STICKY;
    }
}
