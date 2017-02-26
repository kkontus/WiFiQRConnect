package com.kkontus.wifiqr.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private static SharedPreferences.Editor mEditor;

    private static final String INCLUDE_NETWORK_INFO = "includeNetworkInfo";
    private static final String QR_CODE_IMAGE_SIZE = "qrCodeImageSize";

    public SharedPreferencesHelper(Context context) {
        this.mContext = context;
        this.mSharedPreferences = mContext.getSharedPreferences(Config.PREFS_NAME, Context.MODE_PRIVATE);
        this.mEditor = mSharedPreferences.edit();
    }

    public boolean getIncludeNetworkInfo() {
        return mSharedPreferences.getBoolean(INCLUDE_NETWORK_INFO, false);
    }

    public void setIncludeNetworkInfo(boolean includeNetworkInfo) {
        if (mEditor != null) {
            mEditor.putBoolean(INCLUDE_NETWORK_INFO, includeNetworkInfo);
            mEditor.apply();
        }
    }

    public int getQrCodeImageSize() {
        return mSharedPreferences.getInt(QR_CODE_IMAGE_SIZE, 3);
    }

    public void setQrCodeImageSize(int imageSize) {
        if (mEditor != null) {
            mEditor.putInt(QR_CODE_IMAGE_SIZE, imageSize);
            mEditor.apply();
        }
    }

}
