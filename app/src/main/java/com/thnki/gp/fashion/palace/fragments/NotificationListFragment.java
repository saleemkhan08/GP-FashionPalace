package com.thnki.gp.fashion.palace.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thnki.gp.fashion.palace.Brandfever;
import com.thnki.gp.fashion.palace.R;
import com.thnki.gp.fashion.palace.StoreActivity;
import com.thnki.gp.fashion.palace.adapters.NotificationsAdapter;
import com.thnki.gp.fashion.palace.models.Accounts;
import com.thnki.gp.fashion.palace.models.NotificationModel;
import com.thnki.gp.fashion.palace.utils.NotificationsUtil;
import com.thnki.gp.fashion.palace.view.holders.NotificationViewHolder;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NotificationListFragment extends Fragment
{
    @Bind(R.id.notificationListRecyclerView)
    RecyclerView mNotificationListRecyclerView;

    @Bind(R.id.recyclerProgress)
    View mProgress;

    @Bind(R.id.noNotificationsFoundContainer)
    View mNoNotificationsFoundContainer;

    public static final String TAG = "NotificationListFragmnt";
    private FirebaseRecyclerAdapter<NotificationModel, NotificationViewHolder> mAdapter;
    private String mGoogleId;
    private DatabaseReference mNotificationsDbRef;
    private boolean mIsNotificationFragmentShown;

    public NotificationListFragment()
    {
        // Required empty public constructor
        Log.d(TAG, "NotificationListFragment");
        mGoogleId = Brandfever.getPreferences().getString(Accounts.GOOGLE_ID, "dummyId");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView");
        View parent = inflater.inflate(R.layout.fragment_notifiaction_list, container, false);
        ButterKnife.bind(this, parent);
        mNotificationsDbRef = FirebaseDatabase.getInstance().getReference()
                .child(mGoogleId).child(NotificationsUtil.TAG);

        mAdapter = NotificationsAdapter.getInstance(mNotificationsDbRef, getActivity());
        mNotificationListRecyclerView.setAdapter(mAdapter);
        mNotificationListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mIsNotificationFragmentShown = true;
        mNotificationsDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                try
                {
                    mProgress.setVisibility(View.GONE);
                    updateNotificationReadStatus(dataSnapshot);
                    updateUi();
                }
                catch (Exception e)
                {
                    Log.d("Exception", e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
        return parent;
    }

    private void updateNotificationReadStatus(DataSnapshot dataSnapshot)
    {
        if (mIsNotificationFragmentShown)
        {
            Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
            for (DataSnapshot snapshot : snapshots)
            {
                NotificationModel model = snapshot.getValue(NotificationModel.class);
                model.isRead = true;
                mNotificationsDbRef.child(snapshot.getKey()).setValue(model);
            }
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Activity activity = getActivity();
        if (activity instanceof StoreActivity)
        {
            ((StoreActivity) activity).setToolBarTitle(getString(R.string.notifications));
        }
    }

    private void updateUi()
    {
        mProgress.setVisibility(View.GONE);
        int size = mAdapter.getItemCount();
        Log.d("updateUi", "updateUi" + size + ", visible : " + mNoNotificationsFoundContainer.isShown());
        if (size < 1)
        {
            mNoNotificationsFoundContainer.setVisibility(View.VISIBLE);
        }
        else
        {
            mNoNotificationsFoundContainer.setVisibility(View.GONE);
        }
        Log.d("updateUi", "updateUi" + size + ", visible : " + mNoNotificationsFoundContainer.isShown());
    }

    @Override
    public void onStop()
    {
        super.onStop();
        mIsNotificationFragmentShown = false;
    }
}
