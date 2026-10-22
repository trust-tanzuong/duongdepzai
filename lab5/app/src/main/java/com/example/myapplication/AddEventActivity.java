package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class AddEventActivity extends AppCompatActivity {

    private TextInputEditText edtName;
    private TextInputEditText edtPlace;
    private TextInputEditText edtDate;
    private TextInputEditText edtTime;
    private TextView tvErrorName;

    private int selectedYear, selectedMonth, selectedDay;
    private int selectedHour, selectedMinute;

    private EventModel eventToEdit;
    private int editPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_event);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtName = findViewById(R.id.edtName);
        edtPlace = findViewById(R.id.edtPlace);
        edtDate = findViewById(R.id.edtDate);
        edtTime = findViewById(R.id.edtTime);
        tvErrorName = findViewById(R.id.tvErrorName);

        // Initialize with current date and time
        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
        selectedHour = calendar.get(Calendar.HOUR_OF_DAY);
        selectedMinute = calendar.get(Calendar.MINUTE);

        setupClickListeners();
        checkEditMode();
    }

    private void checkEditMode() {
        if (getIntent().hasExtra("EDIT_EVENT")) {
            eventToEdit = (EventModel) getIntent().getSerializableExtra("EDIT_EVENT");
            editPosition = getIntent().getIntExtra("EDIT_POSITION", -1);
            if (eventToEdit != null) {
                edtName.setText(eventToEdit.getName());
                edtPlace.setText(eventToEdit.getPlace());
                edtDate.setText(eventToEdit.getDate());
                edtTime.setText(eventToEdit.getTime());
            }
        }
    }

    private void setupClickListeners() {
        edtPlace.setOnClickListener(v -> showPlaceDialog());
        
        edtDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        selectedYear = year;
                        selectedMonth = month;
                        selectedDay = dayOfMonth;
                        // Format: DD/MM/YYYY
                        String dateStr = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
                        edtDate.setText(dateStr);
                    }, selectedYear, selectedMonth, selectedDay);
            datePickerDialog.show();
        });

        edtTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        selectedHour = hourOfDay;
                        selectedMinute = minute;
                        // Format: HH:MM
                        String timeStr = String.format("%02d:%02d", hourOfDay, minute);
                        edtTime.setText(timeStr);
                    }, selectedHour, selectedMinute, true); // true for 24 hour time
            timePickerDialog.show();
        });
    }

    private void showPlaceDialog() {
        String[] places = {"C201", "C202", "C203", "C204"};
        int checkedItem = -1;
        String currentPlace = edtPlace.getText() != null ? edtPlace.getText().toString() : "";
        for (int i = 0; i < places.length; i++) {
            if (places[i].equals(currentPlace)) {
                checkedItem = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Select place")
                .setSingleChoiceItems(places, checkedItem, (dialog, which) -> {
                    edtPlace.setText(places[which]);
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bai2_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_save) {
            saveEvent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveEvent() {
        String name = edtName.getText() != null ? edtName.getText().toString().trim() : "";
        String place = edtPlace.getText() != null ? edtPlace.getText().toString().trim() : "";
        String date = edtDate.getText() != null ? edtDate.getText().toString().trim() : "";
        String time = edtTime.getText() != null ? edtTime.getText().toString().trim() : "";

        if (name.isEmpty()) {
            tvErrorName.setVisibility(View.VISIBLE);
            return;
        } else {
            tvErrorName.setVisibility(View.GONE);
        }

        // Create new or update existing event
        boolean isEnabled = (eventToEdit != null) ? eventToEdit.isEnabled() : false; // Default to false when created or preserve old
        EventModel resultEvent = new EventModel(name, place, date, time, isEnabled);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("NEW_EVENT", resultEvent);
        if (editPosition != -1) {
            resultIntent.putExtra("EDIT_POSITION", editPosition);
        }
        
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}