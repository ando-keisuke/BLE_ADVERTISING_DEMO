package com.example.ble_advertising_demo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private BLEAdvertiseController advertiseController;

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

        // ボタンを押した時の動作を定義する
        // ボタンを押したらeditTextに入力された文字列をアドバタイズする
        findViewById(R.id.button1).setOnClickListener(v -> {
            // editTextを取得する
            EditText editText = findViewById(R.id.data);
            // 文字を抜き出す
            String data = editText.getText().toString();

            // アドバタイズデータを作成する
            AdvertiseData advertiseData = new AdvertiseData.Builder()
                    .setIncludeDeviceName(true)
                    .addManufacturerData(100, data.getBytes())
                    .build();

            // アドバタイズコントローラが初期化されているかチェックする
            if (advertiseController == null) {
                initializeBluetooth();
            }
            // 初期化が成功したかチェック
            if (advertiseController == null) {
                Toast.makeText(this,"コントローラの初期化に失敗しました",Toast.LENGTH_SHORT).show();
            }

            // アドバタイズデータをControllerにセットする
            advertiseController.setAdvertiseData(advertiseData);

            // アドバタイズを再起動
            // 今のアドバタイズを停止
            advertiseController.stopAdvertising();

            // 新しいアドバタイズを開始
            boolean res = advertiseController.startAdvertising();

            if (res) {
                Log.i("BLE_DBG", "update advertise data: " + data);
                Toast.makeText(this, data + " をセットしました", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("BLE_DBG", "failed to update advertise data: " + data);
                Toast.makeText(this, "データを更新できませんでした。", Toast.LENGTH_SHORT).show();
            }
        });
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
                    for (int i = 0; i < result.size();i++){
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
            Log.d("BLE_DBG", "please activate bluetooth");
            Toast.makeText(this, "Bluetoothを有効にしてください", Toast.LENGTH_SHORT).show();
            return;
        }

        BluetoothLeAdvertiser advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

        // デバイスがBluetooth LE Advertiserをサポートしているか確認
        if (advertiser == null) {
            Log.d("BLD_DBG", "BLE Advertising is not supported on this device.");
            Toast.makeText(this, "このデバイスはBluetooth LE Advertiseをサポートしていません", Toast.LENGTH_SHORT).show();
            return;
        }

        // コントローラが既に初期化されている場合は何もしない
        if (advertiseController != null) {
            Log.d("BLE_DBG","BLEAdvertiseController was already initialized!");
            return;
        };

        // コントローラを作成する
        advertiseController = new BLEAdvertiseController(this, advertiser);

        // コントローラに初期値を設定する
        // AdvertiseSetting
        // AdvertiseData
        // AdvertiseCallback
        // この三つが必要
        advertiseController.setAdvertiseSettings(new AdvertiseSettings.Builder()
                .setAdvertiseMode(android.bluetooth.le.AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(true)
                .setTxPowerLevel(android.bluetooth.le.AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .build());

        advertiseController.setAdvertiseData(new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addManufacturerData(100, "this is initial value!".getBytes())
                .build());

        advertiseController.setAdvertiseCallback(new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.i("BLE-DBG", "Controller: start advertising!");
            }

            @Override
            public void onStartFailure(int errorCode) {
                String errorMessage;
                switch (errorCode) {
                    case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                        errorMessage = "callback: Data is too large";
                        break;
                        case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                            errorMessage = "callback: Too many advertisers";
                            break;
                        case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                            errorMessage = "callback: Already started";
                            break;
                        case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                            errorMessage = "callback: Internal error";
                            break;
                        case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                            errorMessage = "callback: Feature unsupported";
                            break;
                        default:
                            errorMessage = "callback: Unknown error";
                            break;
                    }
                    Log.e("BLE-DBG", "Controller: CODE: " + errorCode + "MSG: " + errorMessage);
            }
        });
    }

}

