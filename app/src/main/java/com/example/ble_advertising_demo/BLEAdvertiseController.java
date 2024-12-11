package com.example.ble_advertising_demo;

import android.Manifest;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

// BLEAdvertiseControllerクラス
// BLEのアドバタイズの制御をする
// start,stopだけで簡単にアドバタイズを制御できる
public class BLEAdvertiseController {
    private final Context context;
    private final BluetoothLeAdvertiser advertiser;
    private AdvertiseData advertiseData;
    private AdvertiseSettings AdvertiseSettings;
    private AdvertiseCallback advertiseCallback;

    // コンストラクタ
    BLEAdvertiseController(Context context,BluetoothLeAdvertiser advertiser) {
        this.context = context;
        this.advertiser = advertiser;
    }

    // コンストラクタ
    BLEAdvertiseController(Context context,
                           BluetoothLeAdvertiser advertiser,
                           AdvertiseData advertiseData,
                           AdvertiseSettings AdvertiseSettings,
                           AdvertiseCallback advertiseCallback) {
        this.context = context;
        this.advertiser = advertiser;
        this.advertiseData = advertiseData;
        this.AdvertiseSettings = AdvertiseSettings;
        this.advertiseCallback = advertiseCallback;
    }

    // setter
    public void setAdvertiseSettings(AdvertiseSettings AdvertiseSettings) { this.AdvertiseSettings = AdvertiseSettings;}
    public void setAdvertiseData(AdvertiseData advertiseData) { this.advertiseData = advertiseData;}
    public void setAdvertiseCallback(AdvertiseCallback advertiseCallback) {this.advertiseCallback = advertiseCallback;}

    // アドバタイズを開始するメソッド
    // 結果をbooleanで返す
    public boolean startAdvertising() {
        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BLE-DBG", "Controller: Permission denied");
            return false;
        }
        advertiser.startAdvertising(this.AdvertiseSettings, this.advertiseData, advertiseCallback);

        Log.i("BLE-DBG", "Controller: Advertising started");

        return true;
    }

    // アドバタイズを停止するメソッド
    // 結果をbooleanで返す
    public void stopAdvertising() {
        if (advertiser != null) {
            if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            advertiser.stopAdvertising(advertiseCallback);
        }
    }

}
