package com.example.myapplication;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PhoneAdapter adapter;
    private List<PhoneModel> phoneList;
    private boolean isAllChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        phoneList = new ArrayList<>();
        phoneList.add(new PhoneModel("Apple", R.drawable.ic_phone, false));
        phoneList.add(new PhoneModel("Samsung", R.drawable.ic_phone, false));
        phoneList.add(new PhoneModel("Nokia", R.drawable.ic_phone, false));
        phoneList.add(new PhoneModel("Oppo", R.drawable.ic_phone, false));

        adapter = new PhoneAdapter(phoneList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem checkAllItem = menu.findItem(R.id.action_check_all);
        if (checkAllItem != null) {
            checkAllItem.setTitle(isAllChecked ? "Uncheck all" : "Check all");
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_check_all) {
            isAllChecked = !isAllChecked;
            for (PhoneModel phone : phoneList) {
                phone.setChecked(isAllChecked);
            }
            adapter.notifyDataSetChanged();
            invalidateOptionsMenu(); // Update menu title
            return true;
        } else if (id == R.id.action_delete_selected) {
            showDeleteConfirmDialog(false);
            return true;
        } else if (id == R.id.action_delete_all) {
            showDeleteConfirmDialog(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmDialog(boolean deleteAll) {
        if (!deleteAll) {
            boolean hasSelected = false;
            for (PhoneModel phone : phoneList) {
                if (phone.isChecked()) {
                    hasSelected = true;
                    break;
                }
            }
            if (!hasSelected) return; // Nothing to delete
        } else {
            if (phoneList.isEmpty()) return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (deleteAll) {
                        phoneList.clear();
                    } else {
                        Iterator<PhoneModel> iterator = phoneList.iterator();
                        while (iterator.hasNext()) {
                            PhoneModel phone = iterator.next();
                            if (phone.isChecked()) {
                                iterator.remove();
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    
                    // Update check all state
                    if (phoneList.isEmpty()) {
                        isAllChecked = false;
                        invalidateOptionsMenu();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}