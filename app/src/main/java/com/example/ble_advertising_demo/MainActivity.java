package com.example.ble_advertising_demo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    // 必要なパーミッションリスト
    String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // パーミッションの確認とリクエスト
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            initializeBluetooth();
        }
    }

    // パーミッションチェック
    private boolean checkPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // パーミッションのリクエスト
    private void requestPermissions() {
        ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    for (int i = 0; i <= result.size();i++){
                        Log.d("BLE_PERM",REQUIRED_PERMISSIONS[i] + " : " + result.get(REQUIRED_PERMISSIONS[i]));
                    }
                    boolean allGranted = true;
                    for (Boolean granted : result.values()) {
                        if (!granted) {
                            allGranted = false;
                            break;
                        }
                    }
                    if (allGranted) {
                        initializeBluetooth();
                    } else {
                        Toast.makeText(this, "すべての権限を許可してください", Toast.LENGTH_SHORT).show();
                    }
                });
        permissionLauncher.launch(REQUIRED_PERMISSIONS);
    }

    // Bluetoothの初期化
    private void initializeBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        // Bluetoothがサポートされているか確認
        if (bluetoothAdapter == null) {
            Log.d("BLE_DBG", "Bluetooth was not supported in on device.");
            Toast.makeText(this, "Bluetoothがサポートされていません", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bluetoothが有効か確認
        if (!bluetoothAdapter.isEnabled()) {
            Log.d("BLE_DBG","please activate bluetooth");
            Toast.makeText(this, "Bluetoothを有効にしてください", Toast.LENGTH_SHORT).show();
            return;
        }

        BluetoothLeAdvertiser advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

        // デバイスがBluetooth LE Advertiserをサポートしているか確認
        if (advertiser == null) {
            Log.d("BLD_DBG","BLE Advertising is not supported on this device.");
            Toast.makeText(this, "このデバイスはBluetooth LE Advertiseをサポートしていません", Toast.LENGTH_SHORT).show();
            return;
        }

        startAdvertising(advertiser);
    }

    // アドバタイズの開始
    private void startAdvertising(BluetoothLeAdvertiser advertiser) {
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(true)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .build();

        byte[] helloWorldBytes = {72, 69, 76, 76, 79, 32, 87, 79, 82, 76, 68};  // 'HELLO WORLD' のバイト列


        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addManufacturerData(100,helloWorldBytes)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        advertiser.startAdvertising(settings, data, new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.d("BLE-DBG", "start advertising!");
                Toast.makeText(MainActivity.this, "アドバタイズ開始", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.d("BLE-DBG", "ERROR CODE: " + errorCode);
                String errorMessage;
                switch (errorCode) {
                    case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                        errorMessage = "データが大きすぎます";
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                        errorMessage = "アドバタイズスロットが不足しています";
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                        errorMessage = "すでにアドバタイズが開始されています";
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                        errorMessage = "内部エラーが発生しました";
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                        errorMessage = "機能がサポートされていません";
                        break;
                    default:
                        errorMessage = "未知のエラー";
                        break;
                }
                Toast.makeText(MainActivity.this, "アドバタイズ失敗: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
