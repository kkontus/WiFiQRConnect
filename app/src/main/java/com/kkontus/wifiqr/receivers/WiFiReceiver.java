package com.kkontus.wifiqr.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.kkontus.wifiqr.fragments.CreateQRFragment;

import java.util.List;

public class WiFiReceiver extends BroadcastReceiver {
    private CreateQRFragment mCreateQRFragment;

    public WiFiReceiver(CreateQRFragment createQRFragment) {
        this.mCreateQRFragment = createQRFragment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//            List<ScanResult> scanResults = wifiManager.getScanResults();
//            mCreateQRFragment.onScanFinished(scanResults);

            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            mCreateQRFragment.onScanConfiguredFinished(configuredNetworks);
        }
    }
}
