package com.kkontus.wifiqr.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.kkontus.wifiqr.models.Network;
import com.kkontus.wifiqr.R;
import com.kkontus.wifiqr.filters.NetworkScanFilter;

import java.util.List;

public class NetworkScanArrayAdapter extends ArrayAdapter {
    private Context mContext;
    private int mResource;
    private List<Network> mScanResults;

    public NetworkScanArrayAdapter(Context context, int resource, List<Network> objects) {
        super(context, resource, objects);

        this.mContext = context;
        this.mResource = resource;
        this.mScanResults = objects;
    }

    @Nullable
    @Override
    public Network getItem(int position) {
        return mScanResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new NetworkScanFilter(this, mScanResults);
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

        Network scanResult = mScanResults.get(position);
        if (scanResult != null && scanResult.getSSID() != null) {
            viewHolder.networkSSID.setText(scanResult.getSSID().toString());
        }

        return view;
    }

    static class ViewHolder {
        TextView networkSSID;
    }

}
