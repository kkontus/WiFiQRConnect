package com.kkontus.wifiqr.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.kkontus.wifiqr.R;
import com.kkontus.wifiqr.helpers.SharedPreferencesHelper;

public class SettingsActivity extends AppCompatActivity {
    private Switch mSwitchConfigureWifi;
    private TextView mTextViewConfigureWifiStatus;
    private Switch mSwitchShowData;
    private TextView mTextViewShowDataStatus;
    private SeekBar mSeekBarImageSize;
    private TextView mTextViewSeekBarStatus;
    private SharedPreferencesHelper mSharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        findViews();

        mSharedPreferencesHelper = new SharedPreferencesHelper(this);

        mSwitchConfigureWifi.setChecked(false);
        mSwitchConfigureWifi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSharedPreferencesHelper.setConfigureWiFiAutomatically(isChecked);
                setSwitchConfigureWifiValue(isChecked);
            }
        });
        boolean configureWiFiAutomatically = mSharedPreferencesHelper.getConfigureWiFiAutomatically();
        setSwitchConfigureWifiValue(configureWiFiAutomatically);


        mSwitchShowData.setChecked(false);
        mSwitchShowData.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSharedPreferencesHelper.setIncludeNetworkInfo(isChecked);
                setSwitchShowDataValue(isChecked);
            }
        });
        boolean includeNetworkInfo = mSharedPreferencesHelper.getIncludeNetworkInfo();
        setSwitchShowDataValue(includeNetworkInfo);


        mSeekBarImageSize.setPadding(25, 0, 25, 0);
        mSeekBarImageSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSharedPreferencesHelper.setQrCodeImageSize(progress);
                setSeekBarProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        int imageSize = mSharedPreferencesHelper.getQrCodeImageSize();
        setSeekBarProgress(imageSize);
    }

    private void findViews() {
        mSwitchConfigureWifi = (Switch) findViewById(R.id.switchConfigureWifi);
        mTextViewConfigureWifiStatus = (TextView) findViewById(R.id.textViewConfigureWifiStatus);
        mSwitchShowData = (Switch) findViewById(R.id.switchShowData);
        mTextViewShowDataStatus = (TextView) findViewById(R.id.textViewShowDataStatus);
        mSeekBarImageSize = (SeekBar) findViewById(R.id.seekBarImageSize);
        mTextViewSeekBarStatus = (TextView) findViewById(R.id.textViewSeekBarStatus);
    }

    private void setSeekBarProgress(int imageSize) {
        mSeekBarImageSize.setProgress(imageSize);

        if (imageSize == 0) {
            mTextViewSeekBarStatus.setText(getString(R.string.image_small));
        } else if (imageSize == 1) {
            mTextViewSeekBarStatus.setText(getString(R.string.image_medium));
        } else {
            mTextViewSeekBarStatus.setText(getString(R.string.image_large));
        }
    }

    private void setSwitchShowDataValue(boolean isChecked) {
        mSwitchShowData.setChecked(isChecked);

        if (isChecked) {
            mTextViewShowDataStatus.setText(getString(R.string.enabled));
        } else {
            mTextViewShowDataStatus.setText(getString(R.string.disabled));
        }
    }

    private void setSwitchConfigureWifiValue(boolean isChecked) {
        mSwitchConfigureWifi.setChecked(isChecked);

        if (isChecked) {
            mTextViewConfigureWifiStatus.setText(getString(R.string.enabled));
        } else {
            mTextViewConfigureWifiStatus.setText(getString(R.string.disabled));
        }
    }

}