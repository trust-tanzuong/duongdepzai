package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<EventModel> eventList;
    private List<EventModel> filteredList;
    private boolean isFilterOn = false;
    private EventClickListener listener;

    public interface EventClickListener {
        void onEnableToggled(EventModel event, boolean isEnabled);
        void onEventLongClick(EventModel event, int position);
    }

    public EventAdapter(List<EventModel> eventList, EventClickListener listener) {
        this.eventList = eventList;
        this.filteredList = new ArrayList<>(eventList);
        this.listener = listener;
    }

    public void updateData(List<EventModel> newEvents) {
        this.eventList = newEvents;
        filterList();
    }

    public void setFilter(boolean isOn) {
        this.isFilterOn = isOn;
        filterList();
    }

    private void filterList() {
        filteredList.clear();
        if (isFilterOn) {
            filteredList.addAll(eventList); // Show all if ON
        } else {
            // Show only enabled if OFF
            for (EventModel event : eventList) {
                if (event.isEnabled()) {
                    filteredList.add(event);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventModel event = filteredList.get(position);

        holder.tvEventName.setText(event.getName());
        holder.tvEventPlace.setText(event.getPlace());
        holder.tvEventDateTime.setText(event.getDate() + " " + event.getTime());

        // Remove listener before setting checked state to prevent unwanted callbacks
        holder.swEnableEvent.setOnCheckedChangeListener(null);
        holder.swEnableEvent.setChecked(event.isEnabled());

        holder.swEnableEvent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            event.setEnabled(isChecked);
            if (listener != null) {
                listener.onEnableToggled(event, isChecked);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onEventLongClick(event, position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return filteredList != null ? filteredList.size() : 0;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName;
        TextView tvEventPlace;
        TextView tvEventDateTime;
        SwitchCompat swEnableEvent;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventPlace = itemView.findViewById(R.id.tvEventPlace);
            tvEventDateTime = itemView.findViewById(R.id.tvEventDateTime);
            swEnableEvent = itemView.findViewById(R.id.swEnableEvent);
        }
    }
}