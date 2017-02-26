package com.kkontus.wifiqr.interfaces;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;

import java.util.List;

public interface NetworkScanner {
    void onScanFinished(List<ScanResult> scanResults);
    void onScanConfiguredFinished(List<WifiConfiguration> scanConfiguredNetworksResults);
}
