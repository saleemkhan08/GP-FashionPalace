package com.thnki.gp.fashion.palace.utils;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.thnki.gp.fashion.palace.Brandfever;
import com.thnki.gp.fashion.palace.R;

import static com.thnki.gp.fashion.palace.LoginActivity.LOGIN_STATUS;
import static com.thnki.gp.fashion.palace.firebase.fcm.NotificationInstanceIdService.NOTIFICATION_INSTANCE_ID;

public class AuthenticationUtil
{
    private static final String TAG = "AuthenticationUtil";
    public final FirebaseAuth mAuth;
    public GoogleApiClient mGoogleApiClient;
    private AppCompatActivity mAppCompatActivity;
    private SharedPreferences mPreference;
    private GoogleApiClient.OnConnectionFailedListener mListener;

    public AuthenticationUtil(AppCompatActivity appCompatActivity)
    {
        mAuth = FirebaseAuth.getInstance();
        mAppCompatActivity = appCompatActivity;
        mPreference = Brandfever.getPreferences();
        if(mAppCompatActivity instanceof GoogleApiClient.OnConnectionFailedListener)
        {
            mListener = (GoogleApiClient.OnConnectionFailedListener) mAppCompatActivity;
        }
    }

    public GoogleSignInOptions setUpLogin()
    {
        Log.d(TAG, "setUpLogin");
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Brandfever.getResString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build GoogleAPIClient with the Google Sign-In API and the above options.
        mGoogleApiClient = new GoogleApiClient.Builder(mAppCompatActivity)
                .enableAutoManage(mAppCompatActivity, mListener)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks()
        {
            @Override
            public void onConnected(@Nullable Bundle bundle)
            {
                if (!mPreference.getBoolean(LOGIN_STATUS, false))
                {
                    String token = mPreference.getString(NOTIFICATION_INSTANCE_ID, "");
                    mPreference.edit().clear()
                            .putString(NOTIFICATION_INSTANCE_ID, token)
                            .apply();

                    FavoritesUtil.clearInstance();
                    CartUtil.clearInstance();
                    revokeAccess();
                    signOut();
                }
            }

            @Override
            public void onConnectionSuspended(int i)
            {

            }
        });
        return gso;
    }

    public void signOut()
    {
        if (mGoogleApiClient.isConnected())
        {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>()
                    {
                        @Override
                        public void onResult(@NonNull Status status)
                        {
                            Log.d(TAG, "signOut:onResult:" + status);
                        }
                    });
        }
    }

    public void revokeAccess()
    {
        if (mGoogleApiClient.isConnected())
        {
            Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>()
                    {
                        @Override
                        public void onResult(@NonNull Status status)
                        {
                            Log.d(TAG, "revokeAccess:onResult:" + status);
                        }
                    });
        }
    }
}
