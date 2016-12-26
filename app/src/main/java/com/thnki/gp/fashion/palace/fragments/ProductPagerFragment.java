package com.thnki.gp.fashion.palace.fragments;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.thnki.gp.fashion.palace.Brandfever;
import com.thnki.gp.fashion.palace.R;
import com.thnki.gp.fashion.palace.adapters.SectionsPagerAdapter;
import com.thnki.gp.fashion.palace.models.Accounts;
import com.thnki.gp.fashion.palace.models.GalleryImage;
import com.thnki.gp.fashion.palace.models.Products;
import com.thnki.gp.fashion.palace.singletons.Otto;
import com.thnki.gp.fashion.palace.utils.ConnectivityUtil;
import com.thnki.gp.fashion.palace.utils.ImageUtil;

import java.io.File;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;
import static com.thnki.gp.fashion.palace.Brandfever.toast;
import static com.thnki.gp.fashion.palace.fragments.ProductsFragment.PICK_IMAGE_MULTIPLE;

public class ProductPagerFragment extends Fragment implements ViewPager.OnPageChangeListener
{
    @Bind(R.id.productImagePager)
    ViewPager mProductImagePager;

    @Bind(R.id.pageIndicatorContainer)
    LinearLayout mPageIndicatorContainer;

    @Bind(R.id.editGalleryImages)
    FloatingActionButton mDeletePhotoImageView;

    @Bind(R.id.uploadMorePhotos)
    FloatingActionButton mUploadPhotoImageView;

    private SectionsPagerAdapter mAdapter;
    private Products mProduct;

    public ArrayList<GalleryImage> mGalleryImages;

    private ProgressDialog mProgressDialog;
    private StorageReference mProductStorageRef;
    private DatabaseReference mProductDbRef;
    private volatile boolean mIsCancelled = false;
    private DatabaseReference mGalleryDbRef;

    public ProductPagerFragment()
    {
        // Required empty public constructor
    }

    public void updateFragmentData(Products product, StorageReference productStorageRef,
                                   DatabaseReference productDbRef)
    {
        mProductStorageRef = productStorageRef;
        mProductDbRef = productDbRef;
        mGalleryDbRef = mProductDbRef.child("galleryImagesList");
        mProduct = product;
        mGalleryImages = product.getGalleryImagesList();
        if (mGalleryImages == null)
        {
            Otto.post(R.string.productNotAvailable);
            getActivity().finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_product_pager, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);
        updateEditingOptionsUi();
        mAdapter = new SectionsPagerAdapter(getActivity().getSupportFragmentManager());

        updateImagePager();
        mProductImagePager.setAdapter(mAdapter);
        mProductImagePager.addOnPageChangeListener(this);
        return parentView;
    }

    private void updateEditingOptionsUi()
    {
        if (Brandfever.getPreferences().getBoolean(Accounts.IS_OWNER, false))
        {
            mDeletePhotoImageView.setVisibility(View.VISIBLE);
            mUploadPhotoImageView.setVisibility(View.VISIBLE);
        }
        mProductImagePager.setVisibility(View.VISIBLE);
    }

    private void updateImagePager()
    {
        mAdapter.mIsDataSetUpdated = true;
        mAdapter.updateDataSet(mGalleryImages);
        updatePageIndicator(mGalleryImages.size());
        mProductImagePager.setCurrentItem(0, true);
    }

    private void uploadMorePhotos(ArrayList<String> mImagesEncodedList)
    {
        final int currentSize = mGalleryImages.size();
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
            StorageReference reference = mProductStorageRef.child(photoName);
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
        int listSize = mGalleryImages.size();
        int size = listSize - currentSize + 1;

        if (downloadUri != null)
        {
            String url = downloadUri.toString();
            if (!url.trim().isEmpty())
            {
                mGalleryImages.add(new GalleryImage(url, photoName));

                mProductDbRef.child(Products.GALLERY_IMAGES_LIST).setValue(mGalleryImages);
                mProduct.setGalleryImagesList(mGalleryImages);
            }
        }
        Log.d("PhotoUploadFlow", "listSize before : " + listSize + ", after : " + mGalleryImages.size());
        Log.d("PhotoUploadFlow", "Size : " + size + ",currentSize : " + currentSize + ", noOfUploadingPhoto : " + noOfUploadingPhoto);

        if (size >= noOfUploadingPhoto)
        {
            if (getActivity() != null)
            {
                if (!mIsCancelled)
                {
                    mProgressDialog.dismiss();
                }
                else
                {
                    toast(R.string.uploaded);
                }
                updateImagePager();
                mProductImagePager.setCurrentItem(0, false);
                mProductImagePager.setCurrentItem(mGalleryImages.size() - 1, true);
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
        updateImagePager();
    }

    private void setupProgressDialog(int noOfUploadingPhoto)
    {
        mIsCancelled = false;
        if (mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(getActivity());
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

    @OnClick(R.id.editGalleryImages)
    public void editGalleryImages()
    {
        if (ConnectivityUtil.isConnected() && mGalleryImages != null)
        {
            EditGalleryFragment editGalleryFragment = new EditGalleryFragment();
            editGalleryFragment.setGalleryImageList(mGalleryImages, mGalleryDbRef);
            editGalleryFragment.show(getActivity().getSupportFragmentManager(),
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
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE);
        }
        else
        {
            Otto.post(R.string.noInternet);
        }
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

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {

    }

    @Override
    public void onPageSelected(final int position)
    {
        highLightPageIndicator(position);
    }

    public View addIndicator()
    {
        return LayoutInflater.from(getActivity()).inflate(R.layout.indicator, mPageIndicatorContainer);
    }

    private void highLightPageIndicator(int position)
    {
        for (int i = 0; i < mPageIndicatorContainer.getChildCount(); i++)
        {
            ImageView indicator = (ImageView) ((LinearLayout) (mPageIndicatorContainer.getChildAt(i))).getChildAt(0);
            indicator.setImageResource(R.drawable.dot);
        }

        ImageView indicator = (ImageView) ((LinearLayout) (mPageIndicatorContainer.getChildAt(position))).getChildAt(0);
        indicator.setImageResource(R.drawable.accent_dot);
    }


    @Override
    public void onPageScrollStateChanged(int state)
    {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        try
        {
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK && null != data)
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
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Otto.unregister(this);
    }
}
