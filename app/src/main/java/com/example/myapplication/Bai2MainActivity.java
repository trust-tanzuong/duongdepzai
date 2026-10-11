package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Bai2MainActivity extends AppCompatActivity implements EventAdapter.EventClickListener {

    private EventAdapter adapter;
    private ArrayList<EventModel> eventList;
    private SwitchCompat switchFilter;
    
    // Using new ActivityResult API instead of deprecated startActivityForResult
    private final ActivityResultLauncher<Intent> addEventLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    EventModel newEvent = (EventModel) result.getData().getSerializableExtra("NEW_EVENT");
                    int positionToEdit = result.getData().getIntExtra("EDIT_POSITION", -1);
                    
                    if (newEvent != null) {
                        if (positionToEdit != -1) {
                            // Update existing event
                            eventList.set(positionToEdit, newEvent);
                        } else {
                            // Add new event
                            eventList.add(newEvent);
                        }
                        adapter.updateData(eventList);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bai2_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        eventList = new ArrayList<>();
        // Mock data as in Figure 1
        eventList.add(new EventModel("Sinh hoat chu nhiem", "C120", "09/03/2020", "04:43", false));
        eventList.add(new EventModel("Huong dan luan van", "C120", "09/03/2020", "04:43", true));

        RecyclerView recyclerView = findViewById(R.id.recyclerViewEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        
        adapter = new EventAdapter(eventList, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bai2_main, menu);
        
        MenuItem switchItem = menu.findItem(R.id.action_switch);
        if (switchItem != null) {
            View actionView = switchItem.getActionView();
            if (actionView instanceof SwitchCompat) {
                switchFilter = (SwitchCompat) actionView;
                switchFilter.setChecked(false); // Default OFF
                
                switchFilter.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    adapter.setFilter(isChecked);
                });
                
                // Initialize filter
                adapter.setFilter(false);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add) {
            Intent intent = new Intent(this, AddEventActivity.class);
            addEventLauncher.launch(intent);
            return true;
        } else if (id == R.id.action_remove_all) {
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure to remove all events?")
                    .setPositiveButton("YES", (dialog, which) -> {
                        eventList.clear();
                        adapter.updateData(eventList);
                    })
                    .setNegativeButton("NO", null)
                    .show();
            return true;
        } else if (id == R.id.action_about) {
            // Optional About info
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEnableToggled(EventModel event, boolean isEnabled) {
        // Handle logic if needed, adapter handles UI state
        // If switch is OFF, and we disable an event, we might want to refresh list
        if (switchFilter != null && !switchFilter.isChecked()) {
            adapter.updateData(eventList);
        }
    }

    // Handle Context Menu for items
    private EventModel selectedEvent;
    private int selectedPosition;

    @Override
    public void onEventLongClick(EventModel event, int position) {
        this.selectedEvent = event;
        this.selectedPosition = position;
        
        // Find the view to anchor context menu if using modern approach, or just show Dialog for simplicity
        // The assignment asks for "Gồm 2 lựa chọn Delete và Edit". We can use an AlertDialog array or standard ContextMenu.
        // Let's use a simple AlertDialog for consistency and simplicity.
        String[] options = {"Delete", "Edit"};
        new AlertDialog.Builder(this)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Delete
                        eventList.remove(selectedPosition);
                        adapter.updateData(eventList);
                    } else if (which == 1) {
                        // Edit
                        Intent intent = new Intent(this, AddEventActivity.class);
                        intent.putExtra("EDIT_EVENT", selectedEvent);
                        intent.putExtra("EDIT_POSITION", selectedPosition);
                        addEventLauncher.launch(intent);
                    }
                })
                .show();
    }
}