package com.thnki.gp.fashion.palace.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.squareup.otto.Subscribe;
import com.thnki.gp.fashion.palace.Brandfever;
import com.thnki.gp.fashion.palace.R;
import com.thnki.gp.fashion.palace.StoreActivity;
import com.thnki.gp.fashion.palace.fragments.NotificationListFragment;
import com.thnki.gp.fashion.palace.models.Accounts;
import com.thnki.gp.fashion.palace.models.NotificationModel;
import com.thnki.gp.fashion.palace.singletons.Otto;
import com.thnki.gp.fashion.palace.utils.ConnectivityUtil;
import com.thnki.gp.fashion.palace.utils.ImageUtil;
import com.thnki.gp.fashion.palace.utils.OrdersUtil;
import com.thnki.gp.fashion.palace.view.holders.NotificationViewHolder;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindString;

import static com.thnki.gp.fashion.palace.StoreActivity.HIDE_DELETE_ICON;

public class NotificationsAdapter extends FirebaseRecyclerAdapter<NotificationModel, NotificationViewHolder>
{
    private Activity mActivity;

    @BindString(R.string.orderPlaced)
    String ORDER_PLACED;

    @BindString(R.string.packed)
    String ORDER_PACKED;

    @BindString(R.string.shipped)
    String ORDER_SHIPPED;

    @BindString(R.string.delayed)
    String ORDER_DELAYED;

    @BindString(R.string.delivered)
    String ORDER_DELIVERED;

    @BindString(R.string.returnOrder)
    String ORDER_RETURN_REQUESTED;

    @BindString(R.string.returned)
    String ORDER_RETURNED;

    @BindString(R.string.orderCancelled)
    String ORDER_CANCELLED;

    private boolean mIsLongClicked;
    private Map<String, Boolean> mMap;
    private DatabaseReference mRef;

    public static NotificationsAdapter getInstance(DatabaseReference reference, Activity activity)
    {
        Log.d(NotificationListFragment.TAG, "NotificationsAdapter - getInstance");
        NotificationsAdapter adapter = new NotificationsAdapter(NotificationModel.class,
                R.layout.notification_list_row, NotificationViewHolder.class,
                reference.orderByChild(NotificationModel.TIME_STAMP), activity);
        adapter.mRef = reference;
        return adapter;
    }

    private NotificationsAdapter(Class<NotificationModel> modelClass, int modelLayout, Class<NotificationViewHolder> viewHolderClass,
                                 Query ref, Activity activity)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
        Log.d(NotificationListFragment.TAG, "NotificationsAdapter");
        mActivity = activity;
        mMap = new HashMap<>();
        Otto.register(this);
    }

    @Override
    protected void populateViewHolder(final NotificationViewHolder viewHolder, final NotificationModel model, final int position)
    {
        mMap.put(NotificationsAdapter.this.getRef(position).getKey(), false);
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                Otto.post(StoreActivity.SHOW_DELETE_ICON);
                viewHolder.mParentView.setBackgroundResource(R.color.colorAccentTransparent);
                mIsLongClicked = true;
                return false;
            }
        });

        viewHolder.mNotificationMsg.setText(model.notification);

        final boolean isCurrentUserOwner = Brandfever.getPreferences().getBoolean(Accounts.IS_OWNER, false);

        ImageUtil.displayImage(model.photoUrl, R.mipmap.notification_place_holder, viewHolder.mUserImageView);
        viewHolder.mUsername.setText(model.username);

        Log.d(NotificationListFragment.TAG, "populateViewHolder Before Switch : " + model.action);

        switch (model.action)
        {
            case OrdersUtil.ORDER_PLACED:
                Log.d(NotificationListFragment.TAG, "populateViewHolder before : ORDER_PLACED");
                loadImage(R.mipmap.shopping_cart, viewHolder.mNotificationImageView);
                Log.d(NotificationListFragment.TAG, "populateViewHolder model.action : " + OrdersUtil.ORDER_PLACED);
                break;
            case OrdersUtil.ORDER_CANCELLED:
                Log.d(NotificationListFragment.TAG, "populateViewHolder before : ORDER_CANCELLED");
                loadImage(R.mipmap.remove_shopping_cart_black, viewHolder.mNotificationImageView);
                Log.d(NotificationListFragment.TAG, "populateViewHolder model.action : " + OrdersUtil.ORDER_CANCELLED);
                break;
            case OrdersUtil.ORDER_PACKED:
                Log.d(NotificationListFragment.TAG, "populateViewHolder before : ORDER_PACKED");
                loadImage(R.mipmap.packed, viewHolder.mNotificationImageView);
                Log.d(NotificationListFragment.TAG, "populateViewHolder model.action : " + OrdersUtil.ORDER_PACKED);
                break;
            case OrdersUtil.ORDER_DELIVERED:
                Log.d(NotificationListFragment.TAG, "populateViewHolder before : ORDER_DELIVERED");
                loadImage(R.mipmap.delivered, viewHolder.mNotificationImageView);
                Log.d(NotificationListFragment.TAG, "populateViewHolder model.action : " + OrdersUtil.ORDER_DELIVERED);
                break;
            case OrdersUtil.ORDER_SHIPPED:
                Log.d(NotificationListFragment.TAG, "populateViewHolder before : ORDER_SHIPPED");
                loadImage(R.mipmap.shipped, viewHolder.mNotificationImageView);
                Log.d(NotificationListFragment.TAG, "populateViewHolder model.action : " + OrdersUtil.ORDER_SHIPPED);
                break;
            case OrdersUtil.ORDER_RETURNED:
                Log.d(NotificationListFragment.TAG, "populateViewHolder before : ORDER_RETURNED");
                loadImage(R.mipmap.return_order, viewHolder.mNotificationImageView);
                Log.d(NotificationListFragment.TAG, "populateViewHolder model.action : " + OrdersUtil.ORDER_RETURNED);
                break;
            case OrdersUtil.ORDER_REQUESTED_RETURN:
                Log.d(NotificationListFragment.TAG, "populateViewHolder before : ORDER_REQUESTED_RETURN");
                loadImage(R.mipmap.return_order, viewHolder.mNotificationImageView);
                Log.d(NotificationListFragment.TAG, "populateViewHolder model.action : " + OrdersUtil.ORDER_REQUESTED_RETURN);
                break;
            case OrdersUtil.ORDER_DELAYED:
                Log.d(NotificationListFragment.TAG, "populateViewHolder before : ORDER_DELAYED");
                loadImage(R.mipmap.delayed, viewHolder.mNotificationImageView);
                Log.d(NotificationListFragment.TAG, "populateViewHolder model.action : " + OrdersUtil.ORDER_DELAYED);
                break;
            default:
                Log.d(NotificationListFragment.TAG, "populateViewHolder : default");
                break;
        }
        Log.d(NotificationListFragment.TAG, "populateViewHolder : After Switch");
        viewHolder.mItemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!mIsLongClicked)
                {
                    if (ConnectivityUtil.isConnected())
                    {
                        if (isCurrentUserOwner)
                        {
                            ((StoreActivity) mActivity).showUserOrdersFragment(model.googleId);
                        }
                        else
                        {
                            ((StoreActivity) mActivity).showUserOrdersFragment(Brandfever.getPreferences().getString(Accounts.GOOGLE_ID, ""));
                        }
                    }
                    else
                    {
                        Otto.post(R.string.noInternet);
                    }
                }
                else
                {
                    if (!mMap.get(NotificationsAdapter.this.getRef(position).getKey()))
                    {
                        mMap.put(NotificationsAdapter.this.getRef(position).getKey(), true);
                        viewHolder.mParentView.setBackgroundResource(R.color.colorAccentTransparent);
                    }
                    else
                    {
                        mMap.put(NotificationsAdapter.this.getRef(position).getKey(), false);
                        viewHolder.mParentView.setBackgroundResource(R.color.transparentWhite);
                    }
                }
            }
        });
    }

    @Subscribe
    public void deleteNotifications(String action)
    {
        if (action.equals(StoreActivity.DELETE_ICON_CLICKED))
        {
            mIsLongClicked = false;
        }
        for (Map.Entry<String, Boolean> set : mMap.entrySet())
        {
            if (set.getValue())
            {
                mRef.child(set.getKey()).removeValue();
            }
        }
    }

    private void loadImage(int resId, ImageView view)
    {
        ImageUtil.displayImage(resId, view);
    }

    public void unregister()
    {
        Otto.unregister(this);
        Otto.post(HIDE_DELETE_ICON);
    }
}
