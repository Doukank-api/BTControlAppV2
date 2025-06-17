package com.example.btcontrolappv2

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.OutputStream
import java.util.*
import android.view.MotionEvent

class MainActivity : AppCompatActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var isConnected = false

    private lateinit var btnConnect: Button
    private lateinit var btnForward: Button
    private lateinit var btnBackward: Button
    private lateinit var btnLeft: Button
    private lateinit var btnRight: Button

    private val UUID_STRING = "00001101-0000-1000-8000-00805F9B34FB"

    // ... existing code ...

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnConnect = findViewById(R.id.btnBluetooth)
        btnForward = findViewById(R.id.btnForward)
        btnBackward = findViewById(R.id.btnBackward)
        btnLeft = findViewById(R.id.btnLeft)
        btnRight = findViewById(R.id.btnRight)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        btnConnect.setOnClickListener {
            if (!isConnected) {
                checkBluetoothPermissions()
            } else {
                disconnectBluetooth()
            }
        }

        // İleri tuşu için basılı tutma kontrolü
        btnForward.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isConnected) sendCommand("FORWARD")
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isConnected) sendCommand("STOP")
                    true
                }
                else -> false
            }
        }

        // Geri tuşu için basılı tutma kontrolü
        btnBackward.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isConnected) sendCommand("BACKWARD")
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isConnected) sendCommand("STOP")
                    true
                }
                else -> false
            }
        }

        // Sol tuşu için basılı tutma kontrolü
        btnLeft.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isConnected) sendCommand("LEFT")
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isConnected) sendCommand("STOP")
                    true
                }
                else -> false
            }
        }

        // Sağ tuşu için basılı tutma kontrolü
        btnRight.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isConnected) sendCommand("RIGHT")
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isConnected) sendCommand("STOP")
                    true
                }
                else -> false
            }
        }
    }

// ... existing code ...

    private fun checkBluetoothPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, 1)
        } else {
            connectToBluetooth()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun connectToBluetooth() {
        val pairedDevices = bluetoothAdapter.bondedDevices
        val arduinoDevice = pairedDevices.find { it.name.contains("MAVERA") } ?: return
        try {
            bluetoothSocket = arduinoDevice.createRfcommSocketToServiceRecord(UUID.fromString(UUID_STRING))
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            isConnected = true
            btnConnect.text = "Bağlantıyı Kes"
        } catch (_: IOException) {
            disconnectBluetooth()
        }
    }

    private fun disconnectBluetooth() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (_: IOException) {}
        isConnected = false
        outputStream = null
        bluetoothSocket = null
        btnConnect.text = "Bluetooth Bağlan"
    }

    private fun sendCommand(command: String) {
        try {
            outputStream?.write("$command\n".toByteArray())
        } catch (_: IOException) {
            disconnectBluetooth()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectBluetooth()
    }


}