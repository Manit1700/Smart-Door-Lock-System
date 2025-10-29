package com.example.smartdoorlock;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.app.AlertDialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LogActivity extends AppCompatActivity {
    private static final String CORRECT_PIN = "1234";
    private List<LogEntry> logEntries;
    private ArrayAdapter<LogEntry> adapter;
    private ListView logListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        logListView = findViewById(R.id.logListView);
        Button btnClearLogs = findViewById(R.id.btnClearLogs);

        logEntries = LogManager.loadLogs(this);
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                logEntries
        );
        logListView.setAdapter(adapter);

        btnClearLogs.setOnClickListener(v -> showClearLogsDialog());
    }

    private void showClearLogsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.RoundedDialogTheme);
        builder.setTitle("Clear Logs");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_pin_entry, null);
        EditText pinEditText = dialogView.findViewById(R.id.pinEditText);

        builder.setView(dialogView);
        builder.setPositiveButton("Clear", (dialog, which) -> {
            String enteredPin = pinEditText.getText().toString().trim();
            if (enteredPin.equals(CORRECT_PIN)) {
                LogManager.clearLogs(this);
                logEntries.clear();
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Logs cleared successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}