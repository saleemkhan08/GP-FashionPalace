package com.thnki.gp.fashion.palace.fragments;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.otto.Subscribe;
import com.thnki.gp.fashion.palace.Brandfever;
import com.thnki.gp.fashion.palace.R;
import com.thnki.gp.fashion.palace.StoreActivity;
import com.thnki.gp.fashion.palace.adapters.ShopGalleryPagerAdapter;
import com.thnki.gp.fashion.palace.models.Accounts;
import com.thnki.gp.fashion.palace.models.GalleryImage;
import com.thnki.gp.fashion.palace.models.Products;
import com.thnki.gp.fashion.palace.singletons.Otto;
import com.thnki.gp.fashion.palace.utils.ConnectivityUtil;
import com.thnki.gp.fashion.palace.utils.ImageUtil;
import com.thnki.gp.fashion.palace.utils.UserUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;
import static com.thnki.gp.fashion.palace.Brandfever.toast;

public class GalleryFragment extends Fragment implements ViewPager.OnPageChangeListener
{
    private static final String SHOP_GALLERY_IMAGES = "shopGallery";
    public static final int PICK_IMAGE_MULTIPLE_GALLERY = 125;
    @Bind(R.id.galleryImagePager)
    ViewPager mGalleryImagePager;

    private RemindTask mTask;
    @Bind(R.id.pageIndicatorContainer)
    LinearLayout mPageIndicatorContainer;

    @Bind(R.id.editPhotos)
    FloatingActionButton mEditPhotos;

    @Bind(R.id.uploadMorePhotos)
    FloatingActionButton mUploadPhotoImageView;

    private ShopGalleryPagerAdapter mAdapter;
    public ArrayList<GalleryImage> mGalleryImagesList;

    private ProgressDialog mProgressDialog;
    private StorageReference mStorageRef;
    private DatabaseReference mDbRef;
    private volatile boolean mIsCancelled = false;

    private Timer mTimer;
    private int mPageNum = 1;

    private Handler mHandler = new Handler();
    private StoreActivity mActivity;
    private boolean mIsRequestingPermission;

    public GalleryFragment()
    {
        mStorageRef = FirebaseStorage.getInstance().getReference().child(SHOP_GALLERY_IMAGES);
        mDbRef = FirebaseDatabase.getInstance().getReference().child(UserUtil.APP_DATA).child(SHOP_GALLERY_IMAGES);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_gallery, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);
        return parentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mActivity = (StoreActivity) getActivity();
        updateEditingOptionsUi(StoreActivity.OWNER_PROFILE_UPDATED);
        mAdapter = new ShopGalleryPagerAdapter(mActivity.getSupportFragmentManager());
        mDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                try
                {
                    if (dataSnapshot != null && dataSnapshot.getChildrenCount() >= 1 && getActivity() != null)
                    {
                        Log.d("mGalleryImagesList", "" + dataSnapshot.getChildrenCount());
                        mGalleryImagesList = new ArrayList<>();
                        Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                        for (DataSnapshot snapshot : snapshots)
                        {
                            mGalleryImagesList.add(snapshot.getValue(GalleryImage.class));
                        }
                        mAdapter.updateDataSet(mGalleryImagesList);
                        mGalleryImagePager.setAdapter(mAdapter);
                        mGalleryImagePager.addOnPageChangeListener(GalleryFragment.this);
                        mGalleryImagePager.setVisibility(View.VISIBLE);
                        updatePageIndicator(mGalleryImagesList.size());
                        //mEditGalleryFragment.setGalleryImageList(mGalleryImagesList, mDbRef);
                        pageSwitcher();

                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.d("mGalleryImagesList", "" + e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mGalleryImagePager.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                {
                    Log.d("TouchLog", "ACTION_BUTTON_PRESS");
                    if (mTimer != null)
                    {
                        mTimer.cancel();
                    }
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                {

                    pageSwitcher();
                    Log.d("TouchLog", "ACTION_BUTTON_RELEASE");
                }
                return false;
            }
        });
    }

    private void pageSwitcher()
    {
        if (!Brandfever.getPreferences().getBoolean(Accounts.IS_OWNER, false))
        {
            mHandler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        mTimer = new Timer(); // At this line a new Thread will be created
                        if (mTask != null)
                        {
                            mTask.cancel();
                        }
                        mTask = new RemindTask();
                        mTimer.scheduleAtFixedRate(mTask, 0, 3000); // delay
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.d("Exception", "Exception : " + e.getMessage());
                    }
                }
            }, 6000);
        }
    }

    @OnClick(R.id.editPhotos)
    public void editPhotos()
    {
        if (ConnectivityUtil.isConnected() && mGalleryImagesList != null)
        {
            EditGalleryFragment editGalleryFragment = new EditGalleryFragment();
            editGalleryFragment.setGalleryImageList(mGalleryImagesList, mDbRef);
            editGalleryFragment.show(mActivity.getSupportFragmentManager(),
                    EditGalleryFragment.TAG);
        }
        else
        {
            Otto.post(R.string.noInternet);
        }
    }

    @OnClick(R.id.uploadMorePhotos)
    public void uploadMorePhotos()
    {
        if (ConnectivityUtil.isConnected())
        {
            if (mActivity.isSdcardPermissionAvailable())
            {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE_GALLERY);
            }
            else
            {
                mIsRequestingPermission = true;
                mActivity.requestSdCardPermission();
            }
        }
        else
        {
            Otto.post(R.string.noInternet);
        }
    }

    @Subscribe
    public void getImages(String action)
    {
        if (action.equals(StoreActivity.ON_REQUEST_PERMISSION_RESULT) && mIsRequestingPermission)
        {
            mIsRequestingPermission = false;
            uploadMorePhotos();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        try
        {
            if (requestCode == PICK_IMAGE_MULTIPLE_GALLERY && resultCode == RESULT_OK && null != data)
            {
                ArrayList<String> mImagesEncodedList = new ArrayList<>();
                if (data.getData() != null)
                {
                    Uri mImageUri = data.getData();
                    mImagesEncodedList.add(mImageUri.toString());
                }
                else
                {
                    if (data.getClipData() != null)
                    {
                        ClipData mClipData = data.getClipData();
                        for (int i = 0; i < mClipData.getItemCount(); i++)
                        {
                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri mImageUri = item.getUri();
                            mImagesEncodedList.add(mImageUri.toString());
                        }
                    }
                }
                if (ConnectivityUtil.isConnected())
                {
                    uploadMorePhotos(mImagesEncodedList);
                }
                else
                {
                    Otto.post(R.string.noInternet);
                }
            }
            else
            {
                Otto.post(R.string.youHaventPickedAnImage);
            }
        }
        catch (Exception e)
        {
            Otto.post(R.string.something_went_wrong);
            e.printStackTrace();
            if (mProgressDialog != null)
            {
                mProgressDialog.dismiss();
            }
        }
    }

    private void uploadMorePhotos(ArrayList<String> mImagesEncodedList)
    {
        final int currentSize = mGalleryImagesList.size();
        final int noOfUploadingPhoto = mImagesEncodedList.size();
        setupProgressDialog(noOfUploadingPhoto);

        Log.d("PhotoUploadFlow", "mImagesEncodedList : " + mImagesEncodedList.size());
        Log.d("PhotoUploadFlow", "currentSize : " + currentSize);

        for (int productIndex = 0; productIndex < noOfUploadingPhoto && !mIsCancelled; productIndex++)
        {
            String uri = mImagesEncodedList.get(productIndex);
            int uploadIndex = productIndex + currentSize;
            Log.d("PhotoUploadFlow", "uploadIndex : " + uploadIndex);

            /**
             * Put the photo using random key name does not matter as it will be directly accessed using the download URL
             */
            final String photoName = Products.generateRandomKey();

            Uri fileUri = Uri.parse(uri);
            final File destFile = ImageUtil.saveBitmapToFile(fileUri, photoName);
            StorageReference reference = mStorageRef.child(photoName);
            reference.putFile(Uri.fromFile(destFile)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    try
                    {
                        Log.d("PhotoUploadFlow", "onSuccess");
                        updateProgressDialog(taskSnapshot, currentSize,
                                photoName, noOfUploadingPhoto);
                        if (destFile != null && destFile.exists())
                        {
                            destFile.delete();
                        }
                    }
                    catch (Exception e)
                    {
                        Log.d("PhotoUploadFlow", "Exception " + e.getMessage());
                    }
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    mProgressDialog.dismiss();
                    Log.d("PhotoUploadFlow", "onFailure");
                    Otto.post(R.string.please_try_again);
                }
            });
        }
    }

    private void updateProgressDialog(UploadTask.TaskSnapshot taskSnapshot, int currentSize,
                                      String photoName, int noOfUploadingPhoto)
    {
        Log.d("PhotoUploadFlow", "updateProgressDialog");
        Uri downloadUri = taskSnapshot.getDownloadUrl();
        int listSize = mGalleryImagesList.size();
        int size = listSize - currentSize + 1;

        if (downloadUri != null)
        {
            String url = downloadUri.toString();
            if (!url.trim().isEmpty())
            {
                GalleryImage image = new GalleryImage();
                image.url = url;
                image.name = photoName;
                mGalleryImagesList.add(image);
                mDbRef.setValue(mGalleryImagesList);
            }
        }
        Log.d("PhotoUploadFlow", "listSize before : " + listSize + ", after : " + mGalleryImagesList.size());
        Log.d("PhotoUploadFlow", "Size : " + size + ",currentSize : " + currentSize + ", noOfUploadingPhoto : " + noOfUploadingPhoto);

        if (size >= noOfUploadingPhoto)
        {
            if (mActivity != null)
            {
                if (!mIsCancelled)
                {
                    mProgressDialog.dismiss();
                }
                else
                {
                    Otto.post(R.string.uploaded);
                }
                mGalleryImagePager.setCurrentItem(0, false);
                mGalleryImagePager.setCurrentItem(mGalleryImagesList.size() - 1, true);
            }
            return;
        }
        String msg = "Uploading " + (size + 1) + " of " + noOfUploadingPhoto;
        if (!mIsCancelled)
        {
            mProgressDialog.setMessage(msg);
        }
        else
        {
            toast(msg);
        }
    }

    private void setupProgressDialog(int noOfUploadingPhoto)
    {
        mIsCancelled = false;
        if (mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(mActivity);
        }

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialogInterface)
            {
                mIsCancelled = true;
                Otto.post(R.string.photosWillBeUploadedInBackground);
                mProgressDialog.dismiss();
            }
        });

        mProgressDialog.show();
        mProgressDialog.setMessage("Uploading " + 1 + " of " + noOfUploadingPhoto);
    }

    private void updatePageIndicator(final int size)
    {
        mPageIndicatorContainer.removeAllViews();
        if (size > 1)
        {
            for (int i = 0; i < size; i++)
            {
                addIndicator();
            }
            highLightPageIndicator(0);
        }
    }

    class RemindTask extends TimerTask
    {

        @Override
        public void run()
        {
            // As the TimerTask run on a seprate thread from UI thread we have
            // to call runOnUiThread to do work on UI thread.
            mHandler.post(new Runnable()
            {
                public void run()
                {
                    mPageNum++;
                    if (mPageNum == mGalleryImagesList.size())
                    {
                        mPageNum = 0;
                    }
                    mGalleryImagePager.setCurrentItem(mPageNum);
                }
            });
        }
    }

    public View addIndicator()
    {
        return LayoutInflater.from(mActivity).inflate(R.layout.indicator, mPageIndicatorContainer);
    }

    private void highLightPageIndicator(int position)
    {
        int size = mPageIndicatorContainer.getChildCount();
        for (int i = 0; i < size; i++)
        {
            ImageView indicator = (ImageView) ((LinearLayout) (mPageIndicatorContainer.getChildAt(i))).getChildAt(0);
            indicator.setImageResource(R.drawable.dot);
        }

        if (position >= 0 && position < size)
        {
            ImageView indicator = (ImageView) ((LinearLayout) (mPageIndicatorContainer.getChildAt(position))).getChildAt(0);
            indicator.setImageResource(R.drawable.accent_dot);
        }
    }

    @Subscribe
    public void updateEditingOptionsUi(String action)
    {
        if (action.equals(StoreActivity.OWNER_PROFILE_UPDATED))
        {
            Log.d("OWNER_PROFILE_UPDATED", "Action : " + action);
            if (Brandfever.getPreferences().getBoolean(Accounts.IS_OWNER, false))
            {
                mEditPhotos.setVisibility(View.VISIBLE);
                mUploadPhotoImageView.setVisibility(View.VISIBLE);
            }
            else
            {
                mEditPhotos.setVisibility(View.GONE);
                mUploadPhotoImageView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {

    }

    @Override
    public void onPageSelected(int position)
    {
        highLightPageIndicator(position);
        mPageNum = position;
    }

    @Override
    public void onPageScrollStateChanged(int state)
    {

    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (mTimer != null)
        {
            mTimer.cancel();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Otto.unregister(this);
    }
}
