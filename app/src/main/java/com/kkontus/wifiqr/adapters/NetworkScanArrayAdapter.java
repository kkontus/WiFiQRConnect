package com.kkontus.wifiqr.adapters;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kkontus.wifiqr.R;

import java.util.List;

public class NetworkScanArrayAdapter extends ArrayAdapter {
    private Context mContext;
    private int mResource;
    private List<ScanResult> mScanResults;

    public NetworkScanArrayAdapter(Context context, int resource, List<ScanResult> objects) {
        super(context, resource, objects);

        this.mContext = context;
        this.mResource = resource;
        this.mScanResults = objects;
    }

    @Nullable
    @Override
    public ScanResult getItem(int position) {
        return mScanResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder;

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(mResource, null);

            viewHolder = new ViewHolder();

            viewHolder.networkSSID = (TextView) view.findViewById(R.id.autocompleteText);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        ScanResult scanResult = mScanResults.get(position);
        if (scanResult != null && scanResult.SSID != null) {
            viewHolder.networkSSID.setText(scanResult.SSID.toString());
        }

        return view;
    }

    static class ViewHolder {
        TextView networkSSID;
    }

}
