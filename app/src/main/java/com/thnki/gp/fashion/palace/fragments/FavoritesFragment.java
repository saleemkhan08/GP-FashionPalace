package com.thnki.gp.fashion.palace.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
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
import com.thnki.gp.fashion.palace.adapters.FavoritesAdapter;
import com.thnki.gp.fashion.palace.firebase.database.models.Accounts;
import com.thnki.gp.fashion.palace.firebase.database.models.FavoriteProduct;
import com.thnki.gp.fashion.palace.utils.FavoritesUtil;
import com.thnki.gp.fashion.palace.view.holders.WishListProductViewHolder;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FavoritesFragment extends Fragment
{

    @Bind(R.id.favoritesRecyclerView)
    RecyclerView mFavoritesRecyclerView;

    @Bind(R.id.recyclerProgress)
    View mProgress;

    @Bind(R.id.noProductFoundContainer)
    View mNoProductFoundContainer;

    public static final String TAG = "FavoritesFragment";
    private DatabaseReference mFavoritesDbRef;
    private FirebaseRecyclerAdapter<FavoriteProduct, WishListProductViewHolder> mAdapter;

    public FavoritesFragment()
    {
        // Required empty public constructor
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        SharedPreferences preferences = Brandfever.getPreferences();
        mFavoritesDbRef = rootRef.child(preferences.getString(Accounts.GOOGLE_ID, "")).child(FavoritesUtil.FAV_LIST);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parent = inflater.inflate(R.layout.fragment_favorites, container, false);
        ButterKnife.bind(this, parent);
        mAdapter = FavoritesAdapter.getInstance(mFavoritesDbRef, getActivity());
        mFavoritesRecyclerView.setAdapter(mAdapter);
        mFavoritesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFavoritesDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                try
                {
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

    @Override
    public void onResume()
    {
        super.onResume();
        Activity activity = getActivity();
        if (activity instanceof StoreActivity)
        {
            ((StoreActivity) activity).setToolBarTitle(getString(R.string.favorite));
        }
    }

    private void updateUi()
    {
        mProgress.setVisibility(View.GONE);
        int size = mAdapter.getItemCount();
        Log.d("updateUi", "updateUi" + size + ", visible : " + mNoProductFoundContainer.isShown());
        if (size < 1)
        {
            mNoProductFoundContainer.setVisibility(View.VISIBLE);
        }
        else
        {
            mNoProductFoundContainer.setVisibility(View.GONE);
        }
        Log.d("updateUi", "updateUi" + size + ", visible : " + mNoProductFoundContainer.isShown());
    }
}
