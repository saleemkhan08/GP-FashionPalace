package com.thnki.gp.fashion.palace.fragments;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Subscribe;
import com.thnki.gp.fashion.palace.Brandfever;
import com.thnki.gp.fashion.palace.R;
import com.thnki.gp.fashion.palace.StoreActivity;
import com.thnki.gp.fashion.palace.interfaces.Const;
import com.thnki.gp.fashion.palace.interfaces.DrawerItemClickListener;
import com.thnki.gp.fashion.palace.models.Accounts;
import com.thnki.gp.fashion.palace.models.Category;
import com.thnki.gp.fashion.palace.models.NotificationModel;
import com.thnki.gp.fashion.palace.singletons.Otto;
import com.thnki.gp.fashion.palace.utils.ConnectivityUtil;
import com.thnki.gp.fashion.palace.utils.ImageUtil;
import com.thnki.gp.fashion.palace.utils.NotificationsUtil;
import com.thnki.gp.fashion.palace.view.holders.DrawerCategoryViewHolder;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.thnki.gp.fashion.palace.interfaces.Const.AVAILABLE_;
import static com.thnki.gp.fashion.palace.interfaces.Const.CATEGORY_ID;
import static com.thnki.gp.fashion.palace.interfaces.DrawerItemClickListener.ENTER;

/**
 * Fragment handles the drawer menu.
 */

public class CategoryDrawerFragment extends Fragment implements ValueEventListener
{
    private static final String LOGIN = "Login";
    public static final String CATEGORY_CHILD = "categoryChild";
    private static final String TAG = "CategoryDrawerFragment";
    private boolean mIsFirstLevelCategory = false;

    @Bind(R.id.categoryRecyclerView)
    RecyclerView mCategoriesRecyclerView;

    SharedPreferences mPreferences;
    @Bind(R.id.customerListButton)
    View customerListButton;

    @Bind(R.id.myOrders)
    View myOrders;

    @Bind(R.id.backIcon)
    View mHeaderBack;

    @Bind(R.id.profile)
    View mHeaderProfile;

    @Bind(R.id.editCategories)
    View mEditButton;

    @Bind(R.id.profileName)
    TextView mProfileName;

    @Bind(R.id.profilePic)
    ImageView mProfilePic;

    @Bind(R.id.categoryName)
    TextView mCategory;

    @Bind(R.id.notificationCount)
    TextView mNotificationCountTextView;

    private List<String> mFirstLevelArray;
    public String mCurrentCategory = "";
    private DatabaseReference mAvailableCategoriesRef;
    private DrawerItemClickListener mItemClickListener;

    private Resources mResources;

    public static CategoryDrawerFragment getInstance(String categoryChild, DrawerItemClickListener listener)
    {
        CategoryDrawerFragment fragment = new CategoryDrawerFragment();
        fragment.mItemClickListener = listener;
        fragment.setArguments(categoryChild);
        return fragment;
    }

    private void setArguments(String categoryChild)
    {
        Bundle bundle = new Bundle();
        bundle.putString(CATEGORY_CHILD, categoryChild);
        setArguments(bundle);
    }

    private String mCategoryChild;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View layout = inflater.inflate(R.layout.fragment_drawer_category, container, false);
        ButterKnife.bind(this, layout);

        Bundle bundle = getArguments();
        mCategoryChild = bundle.getString(CATEGORY_CHILD);
        if (mCategoryChild == null)
        {
            Otto.post(StoreActivity.RESTART_ACTIVITY);
        }
        return layout;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Otto.register(this);
    }


    @Override
    public void onResume()
    {
        super.onResume();
        Log.d(TAG, "categoryChild : " + mCategoryChild);
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        mAvailableCategoriesRef = rootRef.child(mCategoryChild);
        mCurrentCategory = mCategoryChild;
        mPreferences = Brandfever.getPreferences();

        DatabaseReference notificationDbRef = rootRef.child(mPreferences.getString(Accounts.GOOGLE_ID, ""))
                .child(NotificationsUtil.TAG);
        notificationDbRef.addValueEventListener(this);
        mResources = getResources();

        String parentCategory = getParentCategory(mCurrentCategory.replace(AVAILABLE_, ""));
        mCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerView.Adapter mAdapter = getAdapter();
        mCategoriesRecyclerView.setAdapter(mAdapter);
        mFirstLevelArray = getFirstLevelArray();

        if (parentCategory != null)
        {
            mIsFirstLevelCategory = false;
            mHeaderBack.setVisibility(View.VISIBLE);
            mHeaderProfile.setVisibility(View.GONE);
            mCategory.setText(parentCategory);
        }
        else
        {
            mIsFirstLevelCategory = true;
            mHeaderProfile.setVisibility(View.VISIBLE);
            mHeaderBack.setVisibility(View.GONE);
            String imageUrl = mPreferences.getString(Accounts.PHOTO_URL, "");
            if (!imageUrl.isEmpty())
            {
                ImageUtil.displayRoundedImage(imageUrl, R.mipmap.user_icon_accent, mProfilePic);
            }
            mProfileName.setText(mPreferences.getString(Accounts.NAME, LOGIN));
        }
        updateOwnerProfile(StoreActivity.OWNER_PROFILE_UPDATED);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        Otto.unregister(this);
    }

    @Subscribe
    public void updateOwnerProfile(String action)
    {
        if (action.equals(StoreActivity.OWNER_PROFILE_UPDATED))
        {
            if (Brandfever.getPreferences().getBoolean(Accounts.IS_OWNER, false))
            {
                mEditButton.setVisibility(View.VISIBLE);
                customerListButton.setVisibility(View.VISIBLE);
                myOrders.setVisibility(View.GONE);
            }
            else
            {
                mEditButton.setVisibility(View.GONE);
                customerListButton.setVisibility(View.GONE);
                myOrders.setVisibility(View.VISIBLE);
            }
        }
    }

    private String getParentCategory(String currentCategory)
    {
        Log.d("getParentCategory", currentCategory);
        List<String> firstLevelCategory = Arrays.asList(mResources.getStringArray(R.array.firstLevelCategoriesId));
        Log.d("getParentCategory", "" + firstLevelCategory);
        int index = firstLevelCategory.indexOf(currentCategory);
        String[] headerTitle = mResources.getStringArray(R.array.firstLevelCategories);
        if (index >= 0)
        {
            return headerTitle[index];
        }
        return null;
    }

    private List<String> getFirstLevelArray()
    {
        String[] array = getResources().getStringArray(R.array.firstLevelCategoriesId);
        for (int i = 0; i < array.length; i++)
        {
            array[i] = Const.AVAILABLE_ + array[i];
        }
        return Arrays.asList(array);
    }

    private RecyclerView.Adapter getAdapter()
    {
        return new FirebaseRecyclerAdapter<Category, DrawerCategoryViewHolder>(
                Category.class,
                R.layout.category_list_item,
                DrawerCategoryViewHolder.class,
                mAvailableCategoriesRef.orderByChild(CATEGORY_ID))
        {

            @Override
            protected void populateViewHolder(final DrawerCategoryViewHolder viewHolder, final Category model, int position)
            {
                viewHolder.mCategory.setText(model.getCategoryName());
                String imageUrl = model.getCategoryImage();
                if (imageUrl != null && !imageUrl.isEmpty() && mIsFirstLevelCategory)
                {
                    Glide.with(getActivity()).load(imageUrl)
                            .asBitmap().centerCrop().into(new BitmapImageViewTarget(viewHolder.mImageView)
                    {
                        @Override
                        protected void setResource(Bitmap resource)
                        {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(mResources, resource);
                            circularBitmapDrawable.setCircular(true);
                            viewHolder.mImageView.setImageDrawable(circularBitmapDrawable);
                        }
                    });
                }
                else
                {
                    viewHolder.mImageView.setVisibility(View.GONE);
                }
                viewHolder.mItemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if (ConnectivityUtil.isConnected())
                        {
                            String categoryName = getAvailableCategoryName(model);
                            if (mItemClickListener != null)
                            {
                                if (mFirstLevelArray.contains(categoryName))
                                {
                                    //Otto.post(categoryName);
                                    mItemClickListener.onFirstLevelItemClick(categoryName, ENTER);
                                }
                                else
                                {
                                    mItemClickListener.onSecondLevelItemClick(categoryName);
                                }
                            }
                        }
                        else
                        {
                            Otto.post(R.string.noInternet);
                        }
                    }
                });
            }
        };
    }

    private String getAvailableCategoryName(Category model)
    {
        return Const.AVAILABLE_ + model.getCategory();
    }

    private String getCategoryName()
    {
        return mCurrentCategory.replace(Const.AVAILABLE_, "");
    }

    @OnClick(R.id.editCategories)
    public void enableEditing()
    {
        if (ConnectivityUtil.isConnected())
        {
            if (mItemClickListener != null)
            {
                mItemClickListener.onEditClick(getCategoryName());
            }
        }
        else
        {
            Otto.post(R.string.noInternet);
        }
    }

    @OnClick(R.id.backIcon)
    public void back()
    {
        if (mItemClickListener != null)
        {
            mItemClickListener.onBackClick();
        }
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot)
    {
        try
        {
            int notificationCount = 0;
            Iterable<DataSnapshot> children = dataSnapshot.getChildren();
            for (DataSnapshot snapshot : children)
            {
                NotificationModel model = snapshot.getValue(NotificationModel.class);
                if (!model.isRead)
                {
                    notificationCount++;
                }
            }
            if (mNotificationCountTextView != null)
            {
                if (notificationCount > 0)
                {
                    mNotificationCountTextView.setText(String.valueOf(notificationCount));
                }
                else
                {
                    mNotificationCountTextView.setText("");
                }
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
}