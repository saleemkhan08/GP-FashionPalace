package com.thnki.gp.fashion.palace.view.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.thnki.gp.fashion.palace.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class OrderListProductViewHolder extends RecyclerView.ViewHolder
{
    @Bind(R.id.productImage)
    public ImageView mImageView;

    @Bind(R.id.productBrand)
    public TextView mBrand;

    @Bind(R.id.productPriceAfter)
    public TextView mPriceAfter;

    @Bind(R.id.productPriceBefore)
    public TextView mPriceBefore;

    @Bind(R.id.productSize)
    public TextView mProductSize;

    @Bind(R.id.orderStatus)
    public TextView mOrderStatus;

    @Bind(R.id.orderOptionsImageView)
    public ImageView mOrderOptions;

    @Bind(R.id.orderOptions)
    public View mOrderOptionsContainer;

    @Bind(R.id.statusImageView)
    public ImageView mStatusImageView;

    @Bind(R.id.itemViewContainer)
    public View mItemViewContainer;

    public OrderListProductViewHolder(View itemView)
    {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
