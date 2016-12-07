package com.thnki.gp.fashion.palace.view.holders;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.thnki.gp.fashion.palace.R;
import com.thnki.gp.fashion.palace.fragments.NotificationListFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NotificationViewHolder extends RecyclerView.ViewHolder
{
    @Bind(R.id.userImage)
    public ImageView mUserImageView;

    @Bind(R.id.notificationTypeImage)
    public ImageView mNotificationImageView;

    @Bind(R.id.username)
    public TextView mUsername;

    @Bind(R.id.notificationMsg)
    public TextView mNotificationMsg;

    public View mItemView;

    public NotificationViewHolder(View itemView)
    {
        super(itemView);
        mItemView = itemView;
        ButterKnife.bind(this, itemView);
        Log.d(NotificationListFragment.TAG, "NotificationViewHolder");
    }
}
