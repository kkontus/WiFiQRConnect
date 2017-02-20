package com.kkontus.wifiqr.interfaces;

import android.net.wifi.ScanResult;

import java.util.List;

public interface NetworkScanner {
    void onScanFinished(List<ScanResult> scanResults);
}
