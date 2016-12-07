package com.thnki.gp.fashion.palace.view.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.thnki.gp.fashion.palace.R;


public class DrawerCategoryViewHolder extends RecyclerView.ViewHolder
{
    public TextView mCategory;
    public ImageView mImageView;
    public View mItemView;

    public DrawerCategoryViewHolder(View itemView)
    {
        super(itemView);
        mItemView = itemView;
        mCategory = (TextView) itemView.findViewById(R.id.category);
        mImageView = (ImageView) itemView.findViewById(R.id.imageView);
    }
}
