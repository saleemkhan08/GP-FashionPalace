package com.thnki.gp.fashion.palace.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.thnki.gp.fashion.palace.fragments.ImagePagerFragment;

import java.util.ArrayList;

public class ShopGalleryPagerAdapter extends FragmentStatePagerAdapter
{
    private ArrayList<String> mPhotoUrls;

    public boolean mIsDataSetUpdated;
    public ShopGalleryPagerAdapter(FragmentManager fragmentManager)
    {
        super(fragmentManager);
    }

    public void updateDataSet(ArrayList<String> photoUrls)
    {
        mPhotoUrls = photoUrls;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position)
    {
        String url = mPhotoUrls.get(position);
        Log.d("FirstImageIssue", "mPhotoUrlKeys : " + mPhotoUrls.toString() + ", mPhotoUrls : " + mPhotoUrls.toString());
        return ImagePagerFragment.getInstance(url);
    }

    @Override
    public int getItemPosition(Object object)
    {
        if(mIsDataSetUpdated)
        {
            mIsDataSetUpdated = false;
            return POSITION_NONE;
        }
        return super.getItemPosition(object);
    }

    @Override
    public int getCount()
    {
        return mPhotoUrls.size();
    }

    public String getItemUrl(int position)
    {
        return mPhotoUrls.get(position);
    }
}
