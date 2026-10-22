package com.example.lab6.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6.R;
import com.example.lab6.model.ContactModel;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    public interface OnContactClickListener {
        void onContactClick(ContactModel contact);
        void onCallClick(ContactModel contact);
    }

    private List<ContactModel> fullList = new ArrayList<>();
    private List<ContactModel> displayList = new ArrayList<>();
    private OnContactClickListener listener;

    public ContactAdapter(OnContactClickListener listener) {
        this.listener = listener;
    }

    public void setContacts(List<ContactModel> contacts) {
        this.fullList = new ArrayList<>(contacts);
        this.displayList = new ArrayList<>(contacts);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        displayList.clear();
        if (query == null || query.trim().isEmpty()) {
            displayList.addAll(fullList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (ContactModel item : fullList) {
                if (item.getDisplayName().toLowerCase().contains(filterPattern) ||
                    item.getPhoneNumber().toLowerCase().contains(filterPattern)) {
                    displayList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContactModel contact = displayList.get(position);
        holder.tvContactName.setText(contact.getDisplayName());
        holder.tvContactPhone.setText(contact.getPhoneNumber());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onContactClick(contact);
        });

        holder.btnCall.setOnClickListener(v -> {
            if (listener != null) listener.onCallClick(contact);
        });
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvContactName, tvContactPhone;
        ImageButton btnCall;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContactName = itemView.findViewById(R.id.tvContactName);
            tvContactPhone = itemView.findViewById(R.id.tvContactPhone);
            btnCall = itemView.findViewById(R.id.btnCall);
        }
    }
}