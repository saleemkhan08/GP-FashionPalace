package com.thnki.gp.fashion.palace.fragments;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.thnki.gp.fashion.palace.R;
import com.thnki.gp.fashion.palace.singletons.Otto;
import com.thnki.gp.fashion.palace.view.SquareImageView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SquareImagePagerFragment extends Fragment
{
    public static final String IMAGE_LOADED = "imageLoaded";
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
        Glide.with(this).load(mUrl)
                .asBitmap().placeholder(R.mipmap.price_tag)
                .listener(new RequestListener<String, Bitmap>()
                {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource)
                    {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource)
                    {
                        Otto.post(IMAGE_LOADED);
                        return false;
                    }
                })
                .centerCrop().into(mProductImage);
        return parentView;
    }

    public static SquareImagePagerFragment getInstance(String url)
    {
        SquareImagePagerFragment fragment = new SquareImagePagerFragment();
        fragment.mUrl = url;
        return fragment;
    }
}
