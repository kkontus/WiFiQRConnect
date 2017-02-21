package com.kkontus.wifiqr.utils;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.util.List;

public class ConnectionManagerUtils {
    private Context mContext;

    // WifiConfiguration
    private WifiConfiguration mConf;
    private String mNetworkSSID;
    private String mNetworkPassword;
    private String mNetworkType;
    private static final String OPEN = "OPEN";
    private static final String WEP = "WEP";
    private static final String WPA = "WPA";
    public static final String NETWORK_SSID = "networkSSID";
    public static final String NETWORK_PASSWORD = "networkPassword";
    public static final String NETWORK_TYPE = "networkType";
    public static final String DROPDOWN_VALUE_OPEN = "OPEN";
    public static final String DROPDOWN_VALUE_WEP = "WEP";
    public static final String DROPDOWN_VALUE_WPA = "WPA/WPA2";

    public ConnectionManagerUtils() {

    }

    public ConnectionManagerUtils(Context context, String networkSSID, String networkPassword, String networkType) {
        mContext = context;
        mNetworkSSID = networkSSID;
        mNetworkPassword = networkPassword;
        mNetworkType = networkType;
    }

    public void establishConnection() {
        // turn on WiFi if not already enabled
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        boolean wifiEnabled = wifiManager.isWifiEnabled();
        if (!wifiEnabled) {
            wifiManager.setWifiEnabled(true);
        }

        // create new WifiConfiguration instance and add networkSSID (SSID must contain quotes)
        mConf = new WifiConfiguration();
        mConf.SSID = "\"" + mNetworkSSID + "\"";

        // choose proper security method
        chooseSecurityMethod(mNetworkType);

        // add config to WifiManager
        wifiManager.addNetwork(mConf);

        // run through list of configured networks and enable our
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifiConfiguration : configuredNetworks) {
            if (wifiConfiguration.SSID != null && wifiConfiguration.SSID.equals("\"" + mNetworkSSID + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(wifiConfiguration.networkId, true);
                wifiManager.reconnect();

                break;
            }
        }
    }

    private void chooseSecurityMethod(String method) {
        if (method == null) {
            method = OPEN;
        }

        switch (method) {
            case OPEN:
                configOpen();
                break;
            case WEP:
                configWEP();
                break;
            case WPA:
                configWPA();
                break;
        }
    }

    private void configOpen() {
        // for open network
        mConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        mConf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        mConf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        mConf.allowedAuthAlgorithms.clear();
        mConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        mConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        mConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        mConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        mConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        mConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
    }

    private void configWEP() {
        // for wep
        mConf.wepKeys[0] = "\"" + mNetworkPassword + "\"";
        mConf.wepTxKeyIndex = 0;

        mConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        mConf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        mConf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        mConf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        mConf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
        mConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        mConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        mConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        mConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
    }

    private void configWPA() {
        // for wpa
        mConf.preSharedKey = "\"" + mNetworkPassword + "\"";

        mConf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        mConf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        mConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        mConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        mConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        mConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        mConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        mConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        mConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
    }

    public String networkTypeMapper(String networkMethodType) {
        String mappedNetworkType = null;

        // values inside .equals("value") method must match to values inside strings.xml <string-array name="network_methods_array">
        if (networkMethodType.equals(DROPDOWN_VALUE_OPEN)) {
            mappedNetworkType = null;
        } else if (networkMethodType.equals(DROPDOWN_VALUE_WEP)) {
            mappedNetworkType = ConnectionManagerUtils.WEP;
        } else if (networkMethodType.equals(DROPDOWN_VALUE_WPA)) {
            mappedNetworkType = ConnectionManagerUtils.WPA;
        }

        return mappedNetworkType;
    }

}