package com.thnki.gp.fashion.palace.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thnki.gp.fashion.palace.R;
import com.thnki.gp.fashion.palace.utils.ImageUtil;
import com.thnki.gp.fashion.palace.view.SquareImageView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SquareImagePagerFragment extends Fragment
{
    @Bind(R.id.productImage)
    SquareImageView mProductImage;

    String mUrl;

    public SquareImagePagerFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_square_image_pager, container, false);
        ButterKnife.bind(this, parentView);
        ImageUtil.displayImage(mUrl, mProductImage);
        return parentView;
    }

    public static SquareImagePagerFragment getInstance(String url)
    {
        SquareImagePagerFragment fragment = new SquareImagePagerFragment();
        fragment.mUrl = url;
        return fragment;
    }
}