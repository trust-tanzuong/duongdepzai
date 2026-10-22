package com.example.lab6.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.lab6.ContactsFragment;
import com.example.lab6.MediaFragment;

public class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new ContactsFragment();
        } else {
            return new MediaFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}