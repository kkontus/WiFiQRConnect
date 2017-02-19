package com.kkontus.wifiqr.helpers;

import android.support.annotation.Nullable;

import com.kkontus.wifiqr.utils.ConnectionManagerUtils;

import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QRCodeFormatter {

    public String formatWiFiQRCode(String networkSSID, String networkPassword, String networkType) {
        if (networkType == null) {
            return "WIFI:S:" + networkSSID + ";P:" + networkPassword + ";";
        } else {
            return "WIFI:S:" + networkSSID + ";T:" + networkType + ";P:" + networkPassword + ";";
        }
    }

    public LinkedHashMap extractWiFiCredentials(String extractedQRCodeContent) {
        String networkSSID = matchNetworkSSID(extractedQRCodeContent);
        String networkPassword = matchNetworkPassword(extractedQRCodeContent);
        String networkType = matchNetworkType(extractedQRCodeContent);

        LinkedHashMap networkCredentials = new LinkedHashMap();
        networkCredentials.put(ConnectionManagerUtils.NETWORK_SSID, networkSSID);
        networkCredentials.put(ConnectionManagerUtils.NETWORK_PASSWORD, networkPassword);
        networkCredentials.put(ConnectionManagerUtils.NETWORK_TYPE, networkType);

        return networkCredentials;
    }

    private String matchBrackets(String string) {
        //String regex = "\\[(.*?)\\]";
        String regex = "\"([^\"]*)\"";
        return matchString(regex, string);
    }

    private String matchNetworkSSID(String string) {
        String regex = "(?<=S:)((?:[^\\;\\?\\\"\\$\\[\\\\\\]\\+])|(?:\\\\[\\\\;,:]))+(?<!\\\\;)(?=;)";
        return matchString(regex, string);
    }

    private String matchNetworkPassword(String string) {
        String regex = "(?<=P:)((?:\\\\[\\\\;,:])|(?:[^;]))+(?<!\\\\;)(?=;)";
        return matchString(regex, string);
    }

    private String matchNetworkType(String string) {
        String regex = "(?<=T:)[a-zA-Z]+(?=;)";
        return matchString(regex, string);
    }

    @Nullable
    private String matchString(String regex, String string) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);
        String matchedValue = null;
        if (matcher.find()) {
            matchedValue = matcher.group(0);
        }
        return matchedValue;
    }

}