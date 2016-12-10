package com.thnki.gp.fashion.palace.adapters;

import android.app.Activity;
import android.view.View;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import com.thnki.gp.fashion.palace.R;
import com.thnki.gp.fashion.palace.StoreActivity;
import com.thnki.gp.fashion.palace.models.Accounts;
import com.thnki.gp.fashion.palace.utils.ConnectivityUtil;
import com.thnki.gp.fashion.palace.view.holders.AccountsViewHolder;

import static com.thnki.gp.fashion.palace.Brandfever.toast;

public class UsersAdapter extends FirebaseRecyclerAdapter<Accounts, AccountsViewHolder>
{
    private Activity mActivity;
    private DatabaseReference mRootReference;

    public static UsersAdapter getInstance(DatabaseReference reference, Activity activity)
    {
        UsersAdapter adapter = new UsersAdapter(Accounts.class,
                R.layout.user_list_row, AccountsViewHolder.class, reference.orderByChild(Accounts.NAME), activity);
        adapter.mRootReference = FirebaseDatabase.getInstance().getReference();
        return adapter;
    }

    private UsersAdapter(Class<Accounts> modelClass, int modelLayout, Class<AccountsViewHolder> viewHolderClass,
                         Query ref, Activity activity)
    {
        super(modelClass, modelLayout, viewHolderClass, ref);
        mActivity = activity;
    }

    @Override
    protected void populateViewHolder(final AccountsViewHolder viewHolder, final Accounts model, int position)
    {
        String imageUrl = model.photoUrl;
        Glide.with(mActivity).load(imageUrl)
                .asBitmap().placeholder(R.mipmap.user_icon_accent)
                .centerCrop().into(viewHolder.mImageView);

        viewHolder.mUsername.setText(model.name);
        viewHolder.mUserEmail.setText(model.email);
       /* mRootReference.child(model.googleId).child(OrdersUtil.ORDERS).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Log.d("DataSnapshot","dataSnapshot : "+dataSnapshot);
                if (dataSnapshot != null && dataSnapshot.getValue() instanceof Order)
                {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });*/

        viewHolder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (ConnectivityUtil.isConnected())
                {
                    ((StoreActivity) mActivity).showUserOrdersFragment(model.googleId);
                }
                else
                {
                    toast(R.string.noInternet);
                }
            }
        });
    }
}
