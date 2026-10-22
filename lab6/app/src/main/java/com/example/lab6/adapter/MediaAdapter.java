package com.example.lab6.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lab6.R;
import com.example.lab6.model.MediaModel;

import java.util.ArrayList;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

    public interface OnMediaClickListener {
        void onMediaClick(MediaModel media, int position);
        void onMediaLongClick(MediaModel media, int position);
        void onSelectionChanged(int selectedCount);
    }

    private List<MediaModel> mediaList = new ArrayList<>();
    private final OnMediaClickListener listener;
    private boolean isSelectionMode = false;

    public MediaAdapter(OnMediaClickListener listener) {
        this.listener = listener;
    }

    public void setMediaList(List<MediaModel> list) {
        this.mediaList = new ArrayList<>(list);
        this.isSelectionMode = false;
        notifyDataSetChanged();
    }

    public List<MediaModel> getSelectedMedia() {
        List<MediaModel> selected = new ArrayList<>();
        for (MediaModel item : mediaList) {
            if (item.isSelected()) {
                selected.add(item);
            }
        }
        return selected;
    }

    public void clearSelection() {
        for (MediaModel item : mediaList) {
            item.setSelected(false);
        }
        isSelectionMode = false;
        notifyDataSetChanged();
        if (listener != null) listener.onSelectionChanged(0);
    }

    public void toggleSelection(int position) {
        if (position >= 0 && position < mediaList.size()) {
            MediaModel item = mediaList.get(position);
            item.setSelected(!item.isSelected());

            int selectedCount = getSelectedMedia().size();
            isSelectionMode = selectedCount > 0;

            notifyItemChanged(position);
            if (listener != null) listener.onSelectionChanged(selectedCount);
        }
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaModel media = mediaList.get(position);

        Glide.with(holder.itemView.getContext())
                .load(media.getUri())
                .centerCrop()
                .placeholder(android.R.color.darker_gray)
                .into(holder.ivThumbnail);

        if (media.isVideo()) {
            holder.llVideoBadge.setVisibility(View.VISIBLE);
            holder.tvDuration.setText(media.getFormattedDuration());
        } else {
            holder.llVideoBadge.setVisibility(View.GONE);
        }

        if (media.isSelected()) {
            holder.vSelectionOverlay.setVisibility(View.VISIBLE);
            holder.ivCheckMark.setVisibility(View.VISIBLE);
        } else {
            holder.vSelectionOverlay.setVisibility(View.GONE);
            holder.ivCheckMark.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (isSelectionMode) {
                toggleSelection(pos);
            } else {
                if (listener != null) listener.onMediaClick(media, pos);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return true;
            if (!isSelectionMode) {
                isSelectionMode = true;
                toggleSelection(pos);
                if (listener != null) listener.onMediaLongClick(media, pos);
            } else {
                toggleSelection(pos);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail, ivCheckMark;
        View vSelectionOverlay;
        LinearLayout llVideoBadge;
        TextView tvDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            ivCheckMark = itemView.findViewById(R.id.ivCheckMark);
            vSelectionOverlay = itemView.findViewById(R.id.vSelectionOverlay);
            llVideoBadge = itemView.findViewById(R.id.llVideoBadge);
            tvDuration = itemView.findViewById(R.id.tvDuration);
        }
    }
}