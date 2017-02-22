package com.kkontus.wifiqr.filters;

import android.net.wifi.ScanResult;
import android.widget.Filter;

import com.kkontus.wifiqr.adapters.NetworkScanArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class NeworkScanFilter extends Filter {
    private NetworkScanArrayAdapter mNetworkScanArrayAdapter;
    private List<ScanResult> mScanResults;
    private List<ScanResult> mSuggestions;
    private List<ScanResult> mTempScanResults;

    public NeworkScanFilter(NetworkScanArrayAdapter networkScanArrayAdapter, List<ScanResult> objects) {
        this.mNetworkScanArrayAdapter = networkScanArrayAdapter;
        this.mScanResults = objects;
        this.mTempScanResults = new ArrayList<>(mScanResults);
        this.mSuggestions = new ArrayList<>();
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        if (charSequence != null) {
            mSuggestions.clear();

            for (ScanResult scanResult : mTempScanResults) {
                if (scanResult.SSID.toLowerCase().contains(charSequence.toString().toLowerCase())) {
                    mSuggestions.add(scanResult);
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = mSuggestions;
            filterResults.count = mSuggestions.size();

            return filterResults;
        } else {
            return new FilterResults();
        }
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        List<ScanResult> scanResults = (List<ScanResult>) filterResults.values;
        if (filterResults != null && filterResults.count > 0) {
            mNetworkScanArrayAdapter.clear();

            for (ScanResult scanResult : scanResults) {
                mNetworkScanArrayAdapter.add(scanResult);
                mNetworkScanArrayAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public CharSequence convertResultToString(Object resultValue) {
        return ((ScanResult) resultValue).SSID;
    }

}