package com.kkontus.wifiqr.filters;

import android.widget.Filter;

import com.kkontus.wifiqr.models.Network;
import com.kkontus.wifiqr.adapters.NetworkScanArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class NetworkScanFilter extends Filter {
    private NetworkScanArrayAdapter mNetworkScanArrayAdapter;
    private List<Network> mScanResults;
    private List<Network> mSuggestions;
    private List<Network> mTempScanResults;

    public NetworkScanFilter(NetworkScanArrayAdapter networkScanArrayAdapter, List<Network> objects) {
        this.mNetworkScanArrayAdapter = networkScanArrayAdapter;
        this.mScanResults = objects;
        this.mTempScanResults = new ArrayList<>(mScanResults);
        this.mSuggestions = new ArrayList<>();
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        if (charSequence != null) {
            mSuggestions.clear();

            for (Network scanResult : mTempScanResults) {
                if (scanResult.getSSID().toLowerCase().contains(charSequence.toString().toLowerCase())) {
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
        List<Network> scanResults = (List<Network>) filterResults.values;
        if (filterResults != null && filterResults.count > 0) {
            mNetworkScanArrayAdapter.clear();

            for (Network scanResult : scanResults) {
                mNetworkScanArrayAdapter.add(scanResult);
                mNetworkScanArrayAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public CharSequence convertResultToString(Object resultValue) {
        return ((Network) resultValue).getSSID();
    }

}