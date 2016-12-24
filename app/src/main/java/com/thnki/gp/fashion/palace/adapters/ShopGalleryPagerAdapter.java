package com.thnki.gp.fashion.palace.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.thnki.gp.fashion.palace.fragments.ImagePagerFragment;
import com.thnki.gp.fashion.palace.models.GalleryImage;

import java.util.ArrayList;

public class ShopGalleryPagerAdapter extends FragmentStatePagerAdapter
{
    private ArrayList<GalleryImage> mGalleryImages;

    public boolean mIsDataSetUpdated;

    public ShopGalleryPagerAdapter(FragmentManager fragmentManager)
    {
        super(fragmentManager);
    }

    public void updateDataSet(ArrayList<GalleryImage> galleryImages)
    {
        mGalleryImages = galleryImages;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position)
    {
        String url = mGalleryImages.get(position).url;
        Log.d("FirstImageIssue", "mPhotoUrlKeys : " + mGalleryImages.toString() + ", mPhotoUrls : " + mGalleryImages.toString());
        return ImagePagerFragment.getInstance(url);
    }

    @Override
    public int getItemPosition(Object object)
    {
        if (mIsDataSetUpdated)
        {
            mIsDataSetUpdated = false;
            return POSITION_NONE;
        }
        return super.getItemPosition(object);
    }

    @Override
    public int getCount()
    {
        return mGalleryImages.size();
    }

    public String getItemUrl(int position)
    {
        return mGalleryImages.get(position).url;
    }
}
