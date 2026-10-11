package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PhoneAdapter extends RecyclerView.Adapter<PhoneAdapter.PhoneViewHolder> {

    private List<PhoneModel> phoneList;

    public PhoneAdapter(List<PhoneModel> phoneList) {
        this.phoneList = phoneList;
    }

    @NonNull
    @Override
    public PhoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_phone, parent, false);
        return new PhoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhoneViewHolder holder, int position) {
        PhoneModel phone = phoneList.get(position);
        if (phone == null) return;

        holder.imgPhone.setImageResource(phone.getImageResId());
        holder.tvName.setText(phone.getName());
        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(phone.isChecked());
        
        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            phone.setChecked(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        if (phoneList != null) {
            return phoneList.size();
        }
        return 0;
    }

    public static class PhoneViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPhone;
        TextView tvName;
        CheckBox cbSelect;

        public PhoneViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPhone = itemView.findViewById(R.id.imgPhone);
            tvName = itemView.findViewById(R.id.tvName);
            cbSelect = itemView.findViewById(R.id.cbSelect);
        }
    }
}