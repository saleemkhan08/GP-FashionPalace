package com.thnki.gp.fashion.palace.fragments;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thnki.gp.fashion.palace.Brandfever;
import com.thnki.gp.fashion.palace.R;
import com.thnki.gp.fashion.palace.models.Accounts;
import com.thnki.gp.fashion.palace.models.NotificationModel;
import com.thnki.gp.fashion.palace.models.Order;
import com.thnki.gp.fashion.palace.singletons.Otto;
import com.thnki.gp.fashion.palace.utils.CartUtil;
import com.thnki.gp.fashion.palace.utils.ConnectivityUtil;
import com.thnki.gp.fashion.palace.utils.NotificationsUtil;
import com.thnki.gp.fashion.palace.utils.OrdersUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.thnki.gp.fashion.palace.Brandfever.toast;

public class NotificationDialogFragment extends DialogFragment
{
    public static final String TAG = "EditAddressDialogFragment";

    @Bind(R.id.notificationMsgEditText)
    EditText mNotificationMsgEditText;

    @Bind(R.id.textInputHint)
    TextInputLayout mTextInputHint;

    @Bind(R.id.dialogTitle)
    TextView mDialogTitleTextView;

    private DatabaseReference mOrderDbRef;
    private SharedPreferences mPreference;
    private String mGoogleId;
    private int mNotificationMessageResId;
    private Order mOrder;

    public static NotificationDialogFragment getInstance(Order order, String googleId, int msgResourceId)
    {
        NotificationDialogFragment fragment = new NotificationDialogFragment();
        fragment.mPreference = Brandfever.getPreferences();
        fragment.mOrder = order;
        Log.d("OrderStatus", "getInstance : "+order.getOrderStatus());
        fragment.mOrderDbRef = FirebaseDatabase.getInstance().getReference()
                .child(googleId)
                .child(OrdersUtil.ORDERS).child(CartUtil.getsInstance().getKey(order));
        fragment.mGoogleId = googleId;
        fragment.mNotificationMessageResId = msgResourceId;
        return fragment;
    }

    public NotificationDialogFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Window window = getDialog().getWindow();
        if (window != null)
        {
            window.requestFeature(Window.FEATURE_NO_TITLE);
        }
        View parentView = inflater.inflate(R.layout.fragment_notification_dialog, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);
        updateNotificationEditText();

        return parentView;
    }

    private void updateNotificationEditText()
    {
        if (mNotificationMessageResId == 0)
        {
            mDialogTitleTextView.setText(R.string.orderCancellation);
            mTextInputHint.setHint(getString(R.string.cancellationReason));
            mNotificationMsgEditText.setText("");
        }
        else if (mNotificationMessageResId == -1)
        {
            mDialogTitleTextView.setText(R.string.returnOrder);
            mTextInputHint.setHint(getString(R.string.returnReason));
            mNotificationMsgEditText.setText("");
        }
        else
        {
            mDialogTitleTextView.setText(R.string.notification);
            mTextInputHint.setHint(getString(R.string.message));
            mNotificationMsgEditText.setText(mNotificationMessageResId);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        Otto.unregister(this);
    }

    @OnClick(R.id.closeDialog)
    public void close()
    {
        dismiss();
    }

    @OnClick(R.id.sendNotificationButton)
    public void sendNotification()
    {
        Log.d("NotificationFlow", "NotificationDialog sendNotification");
        if (ConnectivityUtil.isConnected())
        {
            NotificationModel model = new NotificationModel();
            model.notification = mNotificationMsgEditText.getText().toString();
            if (model.notification.trim().isEmpty())
            {
                toast(R.string.pleaseEnterMessage);
            }
            else
            {
                mOrderDbRef.child(Order.ORDER_STATUS).setValue(mOrder.getOrderStatus());
                mOrderDbRef.child(Order.TIME_STAMP).setValue(-System.currentTimeMillis());

                model.action = mOrder.getOrderStatus();
                Log.d("NotificationFlow", "NotificationDialog action : "+model.action);
                model.googleId = mPreference.getString(Accounts.GOOGLE_ID, "");
                model.timeStamp = -System.currentTimeMillis();
                NotificationsUtil notificationsUtil = NotificationsUtil.getInstance();
                if (mPreference.getBoolean(Accounts.IS_OWNER, false))
                {
                    model.username = getString(R.string.app_name);
                    model.photoUrl = mOrder.getPhotoUrl();
                }
                else
                {
                    model.username = mPreference.getString(Accounts.NAME, "");
                    model.photoUrl = mPreference.getString(Accounts.PHOTO_URL, "");
                    mGoogleId = null;
                }
                notificationsUtil.sendNotificationToAll(model, mGoogleId);
                toast(R.string.sent);
                dismiss();
            }
        }
        else
        {
            toast(R.string.noInternet);
        }
    }
}