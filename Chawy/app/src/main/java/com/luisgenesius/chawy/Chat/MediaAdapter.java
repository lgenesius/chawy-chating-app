package com.luisgenesius.chawy.Chat;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.luisgenesius.chawy.R;

import java.util.ArrayList;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder>{
    private Context context;
    private ArrayList<String> mediaList;

    public MediaAdapter(Context context, ArrayList<String> mediaList) {
        this.context = context;
        this.mediaList = mediaList;
    }

    public class MediaViewHolder extends RecyclerView.ViewHolder {
        ImageView mediaImageView;
        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            mediaImageView = itemView.findViewById(R.id.mediaImageView);
        }
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_detail, parent, false);
        MediaViewHolder mediaVH = new MediaViewHolder(view);
        return mediaVH;
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Glide.with(context).load(Uri.parse(mediaList.get(position))).into(holder.mediaImageView);
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }
}
