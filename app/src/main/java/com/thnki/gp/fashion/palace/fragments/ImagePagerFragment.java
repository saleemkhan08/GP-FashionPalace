package com.thnki.gp.fashion.palace.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.thnki.gp.fashion.palace.R;
import com.thnki.gp.fashion.palace.utils.ImageUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ImagePagerFragment extends Fragment
{
    @Bind(R.id.galleryImage)
    ImageView mGalleryImage;

    String mUrl;

    public ImagePagerFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_image_pager, container, false);
        ButterKnife.bind(this, parentView);
        ImageUtil.displayImage(mUrl, mGalleryImage);
        return parentView;
    }

    public static ImagePagerFragment getInstance(String url)
    {
        ImagePagerFragment fragment = new ImagePagerFragment();
        fragment.mUrl = url;
        return fragment;
    }
}
