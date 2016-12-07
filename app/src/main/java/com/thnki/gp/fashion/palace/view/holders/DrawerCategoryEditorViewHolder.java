package com.thnki.gp.fashion.palace.view.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import com.thnki.gp.fashion.palace.R;


public class DrawerCategoryEditorViewHolder extends RecyclerView.ViewHolder
{
    public CheckedTextView mCheckedTextView;
    public ImageView mImageView;

    public DrawerCategoryEditorViewHolder(View itemView)
    {
        super(itemView);
        mCheckedTextView = (CheckedTextView) itemView.findViewById(R.id.category);
        mImageView = (ImageView) itemView.findViewById(R.id.imageView);
    }
}
