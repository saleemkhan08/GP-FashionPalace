package com.thnki.gp.fashion.palace.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;

import com.thnki.gp.fashion.palace.Brandfever;
import com.thnki.gp.fashion.palace.R;
import com.thnki.gp.fashion.palace.interfaces.ConnectivityListener;

public class ConnectivityUtil
{
    public static boolean isConnected()
    {
        ConnectivityManager connectivity = (ConnectivityManager) Brandfever.getAppContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected())
            {
                return true;
            }
        }
        return false;
    }

    public static void isConnected(final ConnectivityListener listener, final Context context)
    {
        if (!isConnected())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.no_network_access);
            builder.setPositiveButton("Try Again",
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            if (isConnected())
                            {
                                if (listener != null)
                                {
                                    listener.onInternetConnected();
                                }
                                dialog.dismiss();
                            }
                            else
                            {
                                isConnected(listener, context);
                            }
                        }
                    });
            builder.setCancelable(false);
            builder.setTitle(R.string.internet_failure);
            builder.setOnKeyListener(new DialogInterface.OnKeyListener()
            {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                                     KeyEvent event)
                {
                    if (keyCode == KeyEvent.KEYCODE_BACK
                            && event.getAction() == KeyEvent.ACTION_UP
                            && !event.isCanceled())
                    {
                        dialog.dismiss();
                        listener.onCancelled();
                    }
                    return false;
                }
            });
            builder.show();
        }
        else
        {
            if (listener != null)
            {
                listener.onInternetConnected();
            }
        }
    }
}

