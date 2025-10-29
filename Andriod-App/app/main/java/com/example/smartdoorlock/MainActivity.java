package com.example.smartdoorlock;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private Handler disconnectHandler = new Handler(Looper.getMainLooper());
    private static final long DISCONNECT_TIMEOUT = 2 * 60 * 1000; // 2 minutes
    private Runnable disconnectRunnable = new Runnable() {
        @Override
        public void run() {
            disconnectBluetoothDevice();
        }
    };
    private static final int REQUEST_ENABLE_BT = 1;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;

    private Button btnPair, btnLock, btnUnlock;
    private TextView statusTextView;
    private boolean isDoorLocked = false;
    private Handler connectionCheckHandler = new Handler(Looper.getMainLooper());
    private static final int CONNECTION_CHECK_INTERVAL = 1000; // Check every second

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupBluetoothAdapter();
        setupButtonListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startConnectionCheck();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopConnectionCheck();
    }

    private Runnable connectionCheckRunnable = new Runnable() {
        @Override
        public void run() {
            checkBluetoothConnection();
            connectionCheckHandler.postDelayed(this, CONNECTION_CHECK_INTERVAL);
        }
    };

    private void initializeViews() {
        btnPair = findViewById(R.id.btnPair);
        btnLock = findViewById(R.id.btnLock);
        btnUnlock = findViewById(R.id.btnUnlock);
        statusTextView = findViewById(R.id.statusTextView);
    }

    private void setupBluetoothAdapter() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void setupButtonListeners() {
        btnPair.setOnClickListener(v -> pairBluetoothDevice());

        btnLock.setOnClickListener(v -> {
            sendBluetoothCommand('L');
            updateDoorStatus(true);
        });

        btnUnlock.setOnClickListener(v -> {
            sendBluetoothCommand('U');
            updateDoorStatus(false);
        });

        Button btnViewLogs = findViewById(R.id.btnViewLogs);
        btnViewLogs.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LogActivity.class);
            startActivity(intent);
        });
    }

    private void startDisconnectTimer() {
        // Remove any existing callbacks
        disconnectHandler.removeCallbacks(disconnectRunnable);
        // Start new disconnect timer
        disconnectHandler.postDelayed(disconnectRunnable, DISCONNECT_TIMEOUT);
    }

    private void resetDisconnectTimer() {
        // Reset the timer on any user interaction
        startDisconnectTimer();
    }

    private void disconnectBluetoothDevice() {
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Always clean up resources and update UI regardless of exceptions
            bluetoothSocket = null;
            outputStream = null;

            runOnUiThread(() -> {
                btnPair.setText("Pair Bluetooth Device");
                btnLock.setEnabled(false);
                btnUnlock.setEnabled(false);
                statusTextView.setText("Door Status: Unknown");

                LogEntry logEntry = new LogEntry("Bluetooth Connection Lost");
                List<LogEntry> logs = LogManager.loadLogs(this);
                logs.add(0, logEntry);
                LogManager.saveLogs(this, logs);

                Toast.makeText(this, "Bluetooth Connection Lost", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // Reset disconnect timer on any touch event
        resetDisconnectTimer();
        return super.dispatchTouchEvent(event);
    }

    private void startConnectionCheck() {
        connectionCheckHandler.postDelayed(connectionCheckRunnable, CONNECTION_CHECK_INTERVAL);
    }

    private void stopConnectionCheck() {
        connectionCheckHandler.removeCallbacks(connectionCheckRunnable);
    }

    private void pairBluetoothDevice() {
        // If already connected, disconnect
        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            disconnectBluetoothDevice();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    REQUEST_ENABLE_BT);
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().contains("HC-05")) {
                connectToBluetoothDevice(device);
                break;
            }
        }
    }

    private void connectToBluetoothDevice(BluetoothDevice device) {
        if (device == null) {
            Toast.makeText(this, "No compatible device found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Run connection in a separate thread to avoid ANR
        new Thread(() -> {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);

                // Set a connection timeout
                runOnUiThread(() -> Toast.makeText(this, "Attempting to connect...", Toast.LENGTH_SHORT).show());

                bluetoothSocket.connect();
                outputStream = bluetoothSocket.getOutputStream();

                runOnUiThread(() -> {
                    Toast.makeText(this, "Connected to " + device.getName(), Toast.LENGTH_SHORT).show();
                    btnLock.setEnabled(true);
                    btnUnlock.setEnabled(true);
                    btnPair.setText("Disconnect");
                    startDisconnectTimer();
                });
            } catch (IOException e) {
                e.printStackTrace();
                // Clean up resources
                try {
                    if (bluetoothSocket != null) {
                        bluetoothSocket.close();
                        bluetoothSocket = null;
                    }
                    outputStream = null;
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "Connection failed. Please try again.", Toast.LENGTH_SHORT).show();
                    btnLock.setEnabled(false);
                    btnUnlock.setEnabled(false);
                    btnPair.setText("Pair Bluetooth Device");
                });
            }
        }).start();
    }

    private void checkBluetoothConnection() {
        if (bluetoothSocket != null && outputStream != null) {
            try {
                // Try to write a dummy byte to check connection
                outputStream.write(0);
            } catch (IOException e) {
                e.printStackTrace();
                // Connection lost - handle on UI thread
                runOnUiThread(this::disconnectBluetoothDevice);
            }
        }
    }

    private void sendBluetoothCommand(char command) {
        if (bluetoothSocket != null && outputStream != null && bluetoothSocket.isConnected()) {
            try {
                outputStream.write(command);
                Toast.makeText(this, command == 'L' ? "Door Locked" : "Door Unlocked", Toast.LENGTH_SHORT).show();
                resetDisconnectTimer();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to send command. Reconnecting...", Toast.LENGTH_SHORT).show();
                disconnectBluetoothDevice();
            }
        } else {
            Toast.makeText(this, "Not connected. Please pair device first.", Toast.LENGTH_SHORT).show();
            disconnectBluetoothDevice(); // Clean up any partial connections
        }
    }

    private void updateDoorStatus(boolean locked) {
        isDoorLocked = locked;
        statusTextView.setText("Door Status: " + (locked ? "Locked" : "Unlocked"));
        btnLock.setEnabled(!locked);
        btnUnlock.setEnabled(locked);

        // Add log entry
        LogEntry logEntry = new LogEntry(locked ? "Door Locked" : "Door Unlocked");

        // Load existing logs, add new entry, save
        List<LogEntry> logs = LogManager.loadLogs(this);
        logs.add(0, logEntry);
        LogManager.saveLogs(this, logs);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopConnectionCheck();
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}