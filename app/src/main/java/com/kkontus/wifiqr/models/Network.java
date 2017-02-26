package com.kkontus.wifiqr.models;

public class Network {
    private String SSID;

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Network)) return false;

        Network network = (Network) o;

        return getSSID() != null ? getSSID().equals(network.getSSID()) : network.getSSID() == null;

    }

    @Override
    public int hashCode() {
        return getSSID() != null ? getSSID().hashCode() : 0;
    }
}
