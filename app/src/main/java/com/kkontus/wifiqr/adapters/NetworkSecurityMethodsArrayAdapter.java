package com.kkontus.wifiqr.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kkontus.wifiqr.R;

import java.util.List;

public class NetworkSecurityMethodsArrayAdapter extends ArrayAdapter {
    private Context mContext;
    private int mResource;
    private List<String> mNetworkSecurityMethods;

    public NetworkSecurityMethodsArrayAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);

        this.mContext = context;
        this.mResource = resource;
        this.mNetworkSecurityMethods = objects;
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return mNetworkSecurityMethods.get(position);
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

            viewHolder.networkSecurityMethod = (TextView) view.findViewById(R.id.spinnerText);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        String methodType = mNetworkSecurityMethods.get(position);
        if (methodType != null) {
            viewHolder.networkSecurityMethod.setText(methodType);
        }

        return view;
    }

    static class ViewHolder {
        TextView networkSecurityMethod;
    }

}