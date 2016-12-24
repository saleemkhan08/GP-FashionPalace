package com.thnki.gp.fashion.palace.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.google.firebase.database.DatabaseReference;
import com.thnki.gp.fashion.palace.R;
import com.thnki.gp.fashion.palace.adapters.EditGalleryAdapter;
import com.thnki.gp.fashion.palace.interfaces.Const;
import com.thnki.gp.fashion.palace.models.GalleryImage;
import com.thnki.gp.fashion.palace.singletons.Otto;
import com.thnki.gp.fashion.palace.utils.ItemMovementCallbackHelper;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditGalleryFragment extends DialogFragment implements Const
{
    public static final String TAG = "EditAddressDialogFragment";

    @Bind(R.id.editGalleryRecyclerView)
    RecyclerView mEditGalleryRecyclerView;
    private EditGalleryAdapter mAdapter;
    private DatabaseReference mDbRef;

    public EditGalleryFragment()
    {
    }

    public void setGalleryImageList(ArrayList<GalleryImage> galleryImageList, DatabaseReference ref)
    {
        mAdapter = new EditGalleryAdapter(galleryImageList);
        mDbRef = ref;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Window window = getDialog().getWindow();
        if (window != null)
        {
            window.requestFeature(Window.FEATURE_NO_TITLE);
        }

        View parentView = inflater.inflate(R.layout.fragment_edit_gallery, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);
        mEditGalleryRecyclerView.setAdapter(mAdapter);
        mEditGalleryRecyclerView.setLayoutManager(new GridLayoutManager(parentView.getContext(), 2));
        ItemMovementCallbackHelper callbackHelper = new ItemMovementCallbackHelper(mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callbackHelper);
        itemTouchHelper.attachToRecyclerView(mEditGalleryRecyclerView);
        return parentView;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        Otto.unregister(this);
    }

    @OnClick(R.id.closeDialog)
    public void close()
    {
        dismiss();
    }

    @OnClick(R.id.saveGallery)
    public void save()
    {
        mDbRef.setValue(mAdapter.getGalleryImagesList());
        dismiss();
    }
}