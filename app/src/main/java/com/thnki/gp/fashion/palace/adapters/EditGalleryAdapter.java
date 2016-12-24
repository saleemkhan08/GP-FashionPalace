package com.thnki.gp.fashion.palace.adapters;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thnki.gp.fashion.palace.R;
import com.thnki.gp.fashion.palace.interfaces.IOnItemMovedListener;
import com.thnki.gp.fashion.palace.models.GalleryImage;
import com.thnki.gp.fashion.palace.utils.ImageUtil;
import com.thnki.gp.fashion.palace.view.holders.GalleryEditorItemViewHolder;

import java.util.ArrayList;
import java.util.Collections;

import static com.thnki.gp.fashion.palace.Brandfever.toast;

public class EditGalleryAdapter extends RecyclerView.Adapter<GalleryEditorItemViewHolder> implements IOnItemMovedListener
{
    private ArrayList<GalleryImage> mGalleryImagesList;

    public EditGalleryAdapter(ArrayList<GalleryImage> galleryImagesList)
    {
        mGalleryImagesList = new ArrayList<>(galleryImagesList);
    }

    public ArrayList<GalleryImage> getGalleryImagesList()
    {
        return mGalleryImagesList;
    }

    @Override
    public boolean onItemMoved(int fromPosition, int toPosition)
    {
        if (fromPosition < toPosition)
        {
            for (int i = fromPosition; i < toPosition; i++)
            {
                Collections.swap(mGalleryImagesList, i, i + 1);
            }
        }
        else
        {
            for (int i = fromPosition; i > toPosition; i--)
            {
                Collections.swap(mGalleryImagesList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        updateItemNumber();
        return true;
    }

    private void updateItemNumber()
    {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                notifyDataSetChanged();
            }
        }, 1000);
    }

    @Override
    public GalleryEditorItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_gallery_images_row, parent, false);
        return new GalleryEditorItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GalleryEditorItemViewHolder viewHolder, final int position)
    {
        GalleryImage model = mGalleryImagesList.get(position);
        viewHolder.mImageNumber.setText("" + (position + 1));
        ImageUtil.displayImage(model.url, viewHolder.mImageView);
        viewHolder.mDeleteImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (mGalleryImagesList.size() > 1)
                {
                    mGalleryImagesList.remove(position);
                    notifyItemRemoved(position);
                    updateItemNumber();
                }
                else
                {
                    toast(R.string.youCannotDeleteAllTheImages);
                }
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return mGalleryImagesList.size();
    }
}
