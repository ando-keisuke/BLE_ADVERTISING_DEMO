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

        // 初期値を設定
        this.initializeAdvertiseData();
        this.initializeAdvertiseSetting();
        this.initializeAdvertiseCallback();
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

    // こっから下は初期設定をするメソッド冗長だから消すかもしれない
    // 読まなくてもいい
    // ------------------------------

    // 初期値を設定するメソッド
    private void initializeAdvertiseData() {
        // Hello world! という文字列を送信する
        this.advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addManufacturerData(100, "Hello world!".getBytes())
                .build();
    }

    private void initializeAdvertiseSetting() {
        this.AdvertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(android.bluetooth.le.AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(true)
                .setTxPowerLevel(android.bluetooth.le.AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .build();
    }

    private void initializeAdvertiseCallback() {
        this.advertiseCallback = new AdvertiseCallback() {
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
        };
    }

}