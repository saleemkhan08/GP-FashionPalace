package com.thnki.gp.fashion.palace.firebase.fcm;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import com.thnki.gp.fashion.palace.utils.UserUtil;

public class NotificationInstanceIdService extends FirebaseInstanceIdService
{
    public static final String NOTIFICATION_INSTANCE_ID = "NotificationInstanceId";

    @Override
    public void onTokenRefresh()
    {
        String notificationInstanceId = FirebaseInstanceId.getInstance().getToken();
        new UserUtil().updateNotificationInstanceId(notificationInstanceId);
    }
}
