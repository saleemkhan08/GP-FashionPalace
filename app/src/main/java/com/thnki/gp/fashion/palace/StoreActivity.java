package com.thnki.gp.fashion.palace;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Subscribe;
import com.thnki.gp.fashion.palace.fragments.CartFragment;
import com.thnki.gp.fashion.palace.fragments.EditAddressDialogFragment;
import com.thnki.gp.fashion.palace.fragments.FavoritesFragment;
import com.thnki.gp.fashion.palace.fragments.MainPageFragment;
import com.thnki.gp.fashion.palace.fragments.NotificationListFragment;
import com.thnki.gp.fashion.palace.fragments.ProductsFragment;
import com.thnki.gp.fashion.palace.fragments.UserListFragment;
import com.thnki.gp.fashion.palace.fragments.UsersOrdersFragment;
import com.thnki.gp.fashion.palace.models.Accounts;
import com.thnki.gp.fashion.palace.singletons.Otto;
import com.thnki.gp.fashion.palace.utils.CartUtil;
import com.thnki.gp.fashion.palace.utils.ConnectivityUtil;
import com.thnki.gp.fashion.palace.utils.FavoritesUtil;
import com.thnki.gp.fashion.palace.utils.NavigationDrawerUtil;
import com.thnki.gp.fashion.palace.utils.UserUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.Gravity.CENTER;
import static android.view.Gravity.CENTER_HORIZONTAL;
import static com.thnki.gp.fashion.palace.LoginActivity.LOGIN_STATUS;
import static com.thnki.gp.fashion.palace.fragments.ProductsFragment.REQUEST_CODE_SDCARD_PERMISSION;
import static com.thnki.gp.fashion.palace.interfaces.Const.AVAILABLE_FIRST_LEVEL_CATEGORIES;
import static com.thnki.gp.fashion.palace.interfaces.DrawerItemClickListener.ENTER;
import static com.thnki.gp.fashion.palace.utils.CartUtil.CART_LIST;
import static com.thnki.gp.fashion.palace.utils.FavoritesUtil.FAV_LIST;

public class StoreActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener
{
    private static final String TAG = "storeActivity";
    public static final String NOTIFICATION_ACTION = "";
    public static final String OWNER_PROFILE_UPDATED = "ownerProfileUpdated";
    public static final String RESTART_ACTIVITY = "restartActivity";
    public static final String ON_REQUEST_PERMISSION_RESULT = "onRequestPermissionResult";
    public static final String LOG_OUT = "logOut";

    @Bind(R.id.content_main)
    RelativeLayout mContainer;
    private FragmentManager mFragmentManager;
    private NavigationDrawerUtil mNavigationDrawerUtil;
    private SharedPreferences mPreferences;

    @Bind(R.id.title)
    TextView mTitle;

    @Bind(R.id.cartCount)
    TextView mCartCount;

    @Bind(R.id.favCount)
    TextView mFavCount;

    @Bind(R.id.cart)
    View mCartIcon;

    @Bind(R.id.favorite)
    View mFavIcon;

    private String mCurrentFragment = "";
    private String mGoogleId;

    @Bind(R.id.home)
    View mHome;

    @Bind(R.id.drawer)
    View mDrawer;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setupWindowProperties();
        mPreferences = Brandfever.getPreferences();
        UserUtil.updateIsOwner(mPreferences);
        setContentView(R.layout.activity_store);
        ButterKnife.bind(this);
        Otto.register(this);
        mFragmentManager = getSupportFragmentManager();
        mNavigationDrawerUtil = new NavigationDrawerUtil(mFragmentManager, this);
        mNavigationDrawerUtil.onFirstLevelItemClick(AVAILABLE_FIRST_LEVEL_CATEGORIES, ENTER);

        mFragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener()
        {
            @Override
            public void onBackStackChanged()
            {
                int currentEntryCount = (mFragmentManager.getBackStackEntryCount());
                if (currentEntryCount > 0)
                {
                    mCurrentFragment = mFragmentManager.getBackStackEntryAt(currentEntryCount - 1).getName();
                }
                else
                {
                    mCurrentFragment = "";
                }
                Log.d("CurrentFrag", "Check - mCurrentFragment : " + mCurrentFragment);
            }
        });

        mGoogleId = mPreferences.getString(Accounts.GOOGLE_ID, "dummyId");
        registerCountUpdateListener();
        Intent intent = getIntent();
        Log.d("StoreActivity", "Launched");
        addMainPageFragment();
        if (intent.hasExtra(NOTIFICATION_ACTION))
        {
            switch (intent.getStringExtra(NOTIFICATION_ACTION))
            {
                default:
                    showNotification();
                    break;
            }
        }
        /*
        Log.d("DataFilesList", "Src : " + srcFile.getAbsolutePath());
        File dstFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DataFolder");
        Log.d("DataFilesList", "Dst : " + dstFile.getAbsolutePath());
        try
        {
            copyDirectories(srcFile, dstFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Log.d("DataFilesList", "Exception : "+e.getMessage());
        }*/

    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        if (intent.hasExtra(NOTIFICATION_ACTION))
        {
            switch (intent.getStringExtra(NOTIFICATION_ACTION))
            {
                default:
                    showNotification();
                    break;
            }
        }
    }

    private void setupWindowProperties()
    {
        if (Build.VERSION.SDK_INT >= 21)
        {
            getWindow().setSharedElementExitTransition(TransitionInflater.from(this)
                    .inflateTransition(R.transition.shared_product_transition));
            getWindow().setSharedElementEnterTransition(TransitionInflater.from(this)
                    .inflateTransition(R.transition.shared_product_transition));
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void registerCountUpdateListener()
    {
        Log.d(CART_LIST, "googleId : " + mGoogleId);
        DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference cartDbRef = rootDbRef.child(mGoogleId).child(CART_LIST);

        DatabaseReference favDbRef = rootDbRef.child(mGoogleId).child(FAV_LIST);

        favDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                try
                {
                    updateFavNo(dataSnapshot.getChildrenCount());
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

        cartDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                try
                {
                    updateCartNo(dataSnapshot.getChildrenCount());
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

    private void updateFavNo(long size)
    {
        if (size > 0)
        {
            mFavCount.setText(String.valueOf(size));
        }
        else
        {
            mFavCount.setText("");
        }
    }

    @OnClick(R.id.home)
    public void addMainPageFragment()
    {
        addFragment(MainPageFragment.TAG);
    }

    @Override
    public void onBackPressed()
    {
        if (mNavigationDrawerUtil.onBackPressed())
        {
            if (mHome.isShown())
            {
                super.onBackPressed();
            }
            else
            {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Otto.unregister(this);
        System.exit(0);
    }

    @OnClick(R.id.drawer)
    public void openDrawer()
    {
        mNavigationDrawerUtil.openDrawer();
    }

    public void logout(View view)
    {
        mNavigationDrawerUtil.closeDrawer();
        if (ConnectivityUtil.isConnected())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.areYouSureYouWantToLogout);
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {

                }
            }).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    logout();
                }
            }).show();
        }
        else
        {
            snack(R.string.noInternet);
        }
    }

    public void logout()
    {

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.loggingOut));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                // Build GoogleAPIClient with the Google Sign-In API and the above options.
                mGoogleApiClient = new GoogleApiClient.Builder(StoreActivity.this)
                        .enableAutoManage(StoreActivity.this, StoreActivity.this)
                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .build();

                mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks()
                {
                    @Override
                    public void onConnected(@Nullable Bundle bundle)
                    {
                        FavoritesUtil.clearInstance();
                        CartUtil.clearInstance();
                        revokeAccess();
                        signOut();
                        launchLoginActivity();
                    }

                    @Override
                    public void onConnectionSuspended(int i)
                    {
                        launchLoginActivity();
                    }
                });
            }
        }, 1000);
    }

    public void forceCloseApp()
    {
        mProgressDialog.setMessage(getString(R.string.closingApplication));
        mProgressDialog.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mProgressDialog.dismiss();
                finishAffinity();
            }
        }, 1000);
    }

    private void launchLoginActivity()
    {
        mPreferences.edit().putBoolean(LOGIN_STATUS, false).apply();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(StoreActivity.this, LoginActivity.class);
        intent.putExtra(LOG_OUT, true);
        startActivity(intent);
        forceCloseApp();
    }

    public void setToolBarTitle(String title)
    {
        if (title.equals(getString(R.string.app_name)))
        {
            mHome.setVisibility(View.GONE);
            mDrawer.setVisibility(View.VISIBLE);
        }
        else
        {
            mHome.setVisibility(View.VISIBLE);
            mDrawer.setVisibility(View.GONE);
        }

        if (mTitle != null)
        {
            mTitle.setTypeface(Brandfever.getTypeFace());
            mTitle.setText(title);
        }
    }

    @OnClick(R.id.favorite)
    public void showFavoriteList()
    {
        addFragment(FavoritesFragment.TAG);
    }

    @OnClick(R.id.cart)
    public void showCartList()
    {
        addFragment(CartFragment.TAG);
    }

    public void addFragment(String tag)
    {
        Fragment fragment;
        if (isNewObjectRequired(tag))
        {
            switch (tag)
            {
                case CartFragment.TAG:
                    fragment = mFragmentManager.findFragmentByTag(CartFragment.TAG);
                    if (fragment == null)
                    {
                        fragment = new CartFragment();
                    }
                    break;
                case FavoritesFragment.TAG:
                    fragment = mFragmentManager.findFragmentByTag(FavoritesFragment.TAG);
                    if (fragment == null)
                    {
                        fragment = new FavoritesFragment();
                    }
                    break;
                case MainPageFragment.TAG:
                    fragment = mFragmentManager.findFragmentByTag(MainPageFragment.TAG);
                    if (fragment == null)
                    {
                        fragment = new MainPageFragment();
                    }
                    break;
                default:
                    fragment = new ProductsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(ProductsFragment.FRAGMENT_TAG, tag);
                    fragment.setArguments(bundle);
                    break;
            }
            animateFragment(fragment, tag);
        }
    }

    private void animateFragment(Fragment fragment, String tag)
    {
        int enterTransition = R.anim.enter_directly;
        if (tag.equals(MainPageFragment.TAG))
        {
            mHome.setVisibility(View.GONE);
            mDrawer.setVisibility(View.VISIBLE);
        }
        else
        {
            enterTransition = R.anim.enter_from_bottom;
        }
        mFragmentManager.beginTransaction()
                .setCustomAnimations(enterTransition, R.anim.exit_to_top,
                        R.anim.enter_from_top, R.anim.exit_to_bottom)
                .replace(R.id.content_main, fragment)
                .addToBackStack(tag)
                .commit();
    }

    public void updateCartNo(long size)
    {
        if (size > 0)
        {
            mCartCount.setText(String.valueOf(size));
        }
        else
        {
            mCartCount.setText("");
        }
    }

    public void showNotification(View view)
    {
        showNotification();
        mNavigationDrawerUtil.closeDrawer();
    }

    private void showNotification()
    {
        if (isNewObjectRequired(NotificationListFragment.TAG))
        {
            Fragment fragment = mFragmentManager.findFragmentByTag(NotificationListFragment.TAG);
            if (fragment == null)
            {
                fragment = new NotificationListFragment();
            }
            animateFragment(fragment, NotificationListFragment.TAG);
            mNavigationDrawerUtil.closeDrawer();
        }
    }

    public void myOrders(View view)
    {
        showOrdersFragment();
        mNavigationDrawerUtil.closeDrawer();
    }

    private void showOrdersFragment()
    {
        showUserOrdersFragment(mPreferences.getString(Accounts.GOOGLE_ID, ""));
        mNavigationDrawerUtil.closeDrawer();
    }

    public void showAddressEditorFragment()
    {
        if (ConnectivityUtil.isConnected())
        {
            EditAddressDialogFragment fragment = (EditAddressDialogFragment) mFragmentManager
                    .findFragmentByTag(EditAddressDialogFragment.TAG);

            if (fragment == null)
            {
                fragment = new EditAddressDialogFragment();
            }
            fragment.show(mFragmentManager, EditAddressDialogFragment.TAG);
            mNavigationDrawerUtil.closeDrawer();
        }
        else
        {
            snack(R.string.noInternet);
        }
    }

    public void userList(View view)
    {
        if (isNewObjectRequired(UserListFragment.TAG))
        {
            Fragment fragment = mFragmentManager.findFragmentByTag(UserListFragment.TAG);
            if (fragment == null)
            {
                fragment = new UserListFragment();
            }
            animateFragment(fragment, UserListFragment.TAG);
        }
        mNavigationDrawerUtil.closeDrawer();
    }

    public void showUserOrdersFragment(String googleId)
    {
        if (isNewObjectRequired(UsersOrdersFragment.TAG))
        {
            Fragment fragment = mFragmentManager.findFragmentByTag(UsersOrdersFragment.TAG);
            if (fragment == null)
            {
                fragment = new UsersOrdersFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Accounts.GOOGLE_ID, googleId);
                fragment.setArguments(bundle);
            }
            animateFragment(fragment, UsersOrdersFragment.TAG);
            mNavigationDrawerUtil.closeDrawer();
        }
    }

    private boolean isNewObjectRequired(String tag)
    {
        if (ConnectivityUtil.isConnected() || tag.equals(MainPageFragment.TAG))
        {
            if (!mCurrentFragment.equals(tag))
            {
                boolean popped = mFragmentManager.popBackStackImmediate(tag, 0);
                if (!popped)
                {
                    return true;
                }
            }
        }
        else
        {
            snack(R.string.noInternet);
        }
        return false;
    }

    private void revokeAccess()
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

    private void signOut()
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    @Subscribe
    public void restartActivity(String action)
    {
        if (RESTART_ACTIVITY.equals(action))
        {
            startActivity(new Intent(this, StoreActivity.class));
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_CODE_SDCARD_PERMISSION == requestCode)
        {
            boolean result = true;
            for (int grantResult : grantResults)
            {
                if (grantResult != PackageManager.PERMISSION_GRANTED)
                {
                    result &= false;
                }
            }
            if (result)
            {
                Otto.post(ON_REQUEST_PERMISSION_RESULT);
            }
            else
            {
                snack(R.string.permissionRequiredToUploadPhotos);
                snack(R.string.please_try_again);
            }
        }
    }

    public void requestSdCardPermission()
    {
        String[] permissionTemp = {
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
        };
        ActivityCompat.requestPermissions(this, permissionTemp, REQUEST_CODE_SDCARD_PERMISSION);
    }

    public boolean isSdcardPermissionAvailable()
    {
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    @Subscribe
    public void snack(Integer resId)
    {
        Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), resId, Snackbar.LENGTH_SHORT);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        layout.setGravity(CENTER);

        TextView textView = (TextView) layout.findViewById(android.support.design.R.id.snackbar_text);
        textView.setGravity(CENTER_HORIZONTAL);
        textView.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        layout.setBackgroundResource(R.color.colorAccent);
        snackbar.show();
    }
}