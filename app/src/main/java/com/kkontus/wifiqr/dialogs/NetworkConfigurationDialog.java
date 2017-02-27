package com.kkontus.wifiqr.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.kkontus.wifiqr.R;
import com.kkontus.wifiqr.utils.ConnectionManagerUtils;

public class NetworkConfigurationDialog {
    private Context mContext;

    public NetworkConfigurationDialog(Context mContext) {
        this.mContext = mContext;
    }

    public void showNetworkConfigurationDialog(final String networkSSID, final String networkPassword, final String networkType) {
        String dialogTitle = mContext.getString(R.string.configure_wifi);
        String dialogMessage = mContext.getString(R.string.configure_wifi_with_name);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(dialogTitle).setMessage(dialogMessage + " " + networkSSID)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ConnectionManagerUtils connectionManagerUtils = new ConnectionManagerUtils(mContext, networkSSID, networkPassword, networkType);
                        connectionManagerUtils.establishConnection();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

}