package com.thnki.gp.fashion.palace.view.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.thnki.gp.fashion.palace.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WishListProductViewHolder extends RecyclerView.ViewHolder
{
    @Bind(R.id.productImage)
    public ImageView mImageView;

    @Bind(R.id.productBrand)
    public TextView mBrand;

    @Bind(R.id.productPriceAfter)
    public TextView mPriceAfter;

    @Bind(R.id.productPriceBefore)
    public TextView mPriceBefore;

    @Bind(R.id.deleteFav)
    public ImageView mDeleteFav;

    public View mItemView;

    public WishListProductViewHolder(View itemView)
    {
        super(itemView);
        mItemView = itemView;
        ButterKnife.bind(this, itemView);
    }
}
