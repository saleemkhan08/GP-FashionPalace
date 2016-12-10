package com.thnki.gp.fashion.palace.utils;

import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thnki.gp.fashion.palace.Brandfever;
import com.thnki.gp.fashion.palace.LoginActivity;
import com.thnki.gp.fashion.palace.models.Accounts;
import com.thnki.gp.fashion.palace.singletons.Otto;

import static com.thnki.gp.fashion.palace.StoreActivity.OWNER_PROFILE_UPDATED;
import static com.thnki.gp.fashion.palace.models.Accounts.USERS;
import static com.thnki.gp.fashion.palace.singletons.VolleyUtil.APP_ID;
import static com.thnki.gp.fashion.palace.singletons.VolleyUtil.DEFAULT_URL;
import static com.thnki.gp.fashion.palace.singletons.VolleyUtil.REQUEST_HANDLER_URL;

/**
 * 1. onLogin delete the old token
 * 2. save the account info to server
 * 3. onSave complete update the toke and save it back to server.
 */
public class UserUtil
{
    public static final String APP_DATA = "appData";
    public static final String USER_LIST = "users";
    public static final String OWNER_PHONE_NUMBER = "phoneNumber";
    private static final String TAG = "userUtil";
    private Accounts mAccount;
    private SharedPreferences mPreferences;

    public UserUtil()
    {
        mPreferences = Brandfever.getPreferences();
        mAccount = new Accounts();
    }

    private DatabaseReference getDbReference()
    {
        DatabaseReference databaseReference = null;
        if (mAccount.googleId != null && !mAccount.googleId.isEmpty())
        {
            databaseReference = FirebaseDatabase.getInstance().getReference().child(USERS)
                    .child(mAccount.googleId);
        }
        return databaseReference;
    }

    public void register(GoogleSignInAccount googleSignInAccount)
    {
        if (googleSignInAccount != null)
        {
            Uri photoUrl = googleSignInAccount.getPhotoUrl();
            mAccount.googleId = googleSignInAccount.getId();
            mAccount.email = googleSignInAccount.getEmail();
            mAccount.name = googleSignInAccount.getDisplayName();
            mAccount.photoUrl = photoUrl != null ? photoUrl.toString() : "";
            mPreferences.edit()
                    .putString(Accounts.NAME, mAccount.name)
                    .putBoolean(LoginActivity.LOGIN_STATUS, true)
                    .putString(Accounts.EMAIL, mAccount.email)
                    .putString(Accounts.PHOTO_URL, mAccount.photoUrl)
                    .putString(Accounts.GOOGLE_ID, mAccount.googleId)
                    .apply();

            updateRequestHandlerUrl();
            updateUserInfo();
            FavoritesUtil.getsInstance().updateFavoriteList();
            CartUtil.getsInstance().updateCartList();
        }
    }

    private void updateRequestHandlerUrl()
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(APP_DATA);
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                try
                {
                    String url = dataSnapshot.child(REQUEST_HANDLER_URL).getValue(String.class);
                    String serverKey = dataSnapshot.child(APP_ID).getValue(String.class);
                    if (url == null || url.isEmpty())
                    {
                        url = DEFAULT_URL;
                    }
                    Brandfever.getPreferences().edit()
                            .putString(APP_ID, serverKey)
                            .putString(REQUEST_HANDLER_URL, url).apply();
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
    }

    private void updateUserInfo()
    {
        final DatabaseReference usersDbRef = getDbReference();
        if (usersDbRef != null)
        {
            usersDbRef.setValue(mAccount);
        }
    }

    public static void updateIsOwner(final SharedPreferences preference)
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(Accounts.OWNERS);
        Log.d(TAG, "databaseReference : " + databaseReference);
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                try
                {
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    boolean isOwner = false;
                    for (DataSnapshot snapshot : children)
                    {
                        String googleId = "";
                        try
                        {
                            googleId = snapshot.getValue(String.class);
                        }
                        catch (Exception e)
                        {
                            return;
                        }
                        if (preference.getString(Accounts.GOOGLE_ID, "").equals(googleId))
                        {
                            isOwner = true;
                        }
                    }
                    preference.edit().putBoolean(Accounts.IS_OWNER, isOwner).apply();
                    Otto.post(OWNER_PROFILE_UPDATED);
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
    }
}