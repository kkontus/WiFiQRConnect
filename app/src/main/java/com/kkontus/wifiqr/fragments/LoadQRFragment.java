package com.kkontus.wifiqr.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.Result;
import com.kkontus.wifiqr.R;
import com.kkontus.wifiqr.helpers.Config;
import com.kkontus.wifiqr.helpers.QRCodeFormatter;
import com.kkontus.wifiqr.interfaces.NetworkScanner;
import com.kkontus.wifiqr.interfaces.OnFragmentInteractionListener;
import com.kkontus.wifiqr.utils.ConnectionManagerUtils;
import com.kkontus.wifiqr.utils.ImageUtils;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoadQRFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoadQRFragment extends Fragment implements NetworkScanner {
    private static final String ARG_TAB_POSITION = "tabPosition";
    private int tabPosition;
    private OnFragmentInteractionListener mListener;

    // Load QR tab
    private LinearLayout mLinearLayoutForLoad;
    private Button mButtonLoadQRCode;
    private TextView mTextViewLoadedData;
    private ImageView mImageViewLoadedQR;

    private Bitmap mQRCodeLoadedBitmap;

    private BroadcastReceiver wiFiReceiver;

    public LoadQRFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param tabPosition Parameter 1.
     * @return A new instance of fragment LoadQRFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoadQRFragment newInstance(int tabPosition) {
        LoadQRFragment fragment = new LoadQRFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_POSITION, tabPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabPosition = getArguments().getInt(ARG_TAB_POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_load_qr, container, false);
        findViews(view);

        mButtonLoadQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // image picker to load image from gallery
                launchImagePicker();

                // we need to request user permission for loading available networks
                requestUserPermission();
            }
        });

        return view;
    }

    private void findViews(View view) {
        mLinearLayoutForLoad = (LinearLayout) view.findViewById(R.id.linearLayoutForLoad);
        mButtonLoadQRCode = (Button) view.findViewById(R.id.buttonLoadQRCode);
        mTextViewLoadedData = (TextView) view.findViewById(R.id.textViewLoadedData);
        mImageViewLoadedQR = (ImageView) view.findViewById(R.id.imageViewLoadedQR);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

        System.out.println("onPause");
        if (wiFiReceiver != null) {
            System.out.println("onPause unregisterReceiver");
            getActivity().unregisterReceiver(wiFiReceiver);
            wiFiReceiver = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Config.REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the task you need to do.
                    handleScanningNetwork();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private void requestUserPermission() {
        // check if user permission is already granted
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), getString(R.string.save_image_permission_rationale), Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Request the permission again.
                        // Don't use ActivityCompat.requestPermissions since it goes through parent
                        // activity and we don't need that, so we need to use requestPermissions
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Config.REQUEST_ACCESS_COARSE_LOCATION);
                    }
                }).show();
            } else {
                // No explanation needed, we can request the permission.
                // Don't use ActivityCompat.requestPermissions since it goes through parent
                // activity and we don't need that, so we need to use requestPermissions
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Config.REQUEST_ACCESS_COARSE_LOCATION);
            }
        } else {
            // user permission has already been granted so we can continue with saving the image
            handleScanningNetwork();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Config.RESULT_PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();

                    ImageUtils imageUtils = new ImageUtils(getActivity());
                    Bitmap immutableBitmap = imageUtils.getBitmapFromUri(uri);
                    Bitmap bitmap = immutableBitmap.copy(Bitmap.Config.RGB_565, true);
                    mQRCodeLoadedBitmap = bitmap;

                    mImageViewLoadedQR.setImageBitmap(mQRCodeLoadedBitmap);
                    onImageLoaded(mQRCodeLoadedBitmap);
                    Result result = imageUtils.readQRCodeImage(mQRCodeLoadedBitmap);
                    if (result != null && result.getText() != null) {
                        mTextViewLoadedData.setText(result.getText());

                        LinkedHashMap networkCredentials = new QRCodeFormatter().extractWiFiCredentials(result.getText().toString());
                        String mNetworkSSID = (String) networkCredentials.get(ConnectionManagerUtils.NETWORK_SSID);
                        String mNetworkPassword = (String) networkCredentials.get(ConnectionManagerUtils.NETWORK_PASSWORD);
                        String mNetworkType = (String) networkCredentials.get(ConnectionManagerUtils.NETWORK_TYPE);

                        // don't check for the condition "mNetworkType != null" since it's null for the open network
                        if (mNetworkSSID != null && mNetworkPassword != null) {
                            ConnectionManagerUtils connectionManagerUtils = new ConnectionManagerUtils(getActivity(), mNetworkSSID, mNetworkPassword, mNetworkType);
                            connectionManagerUtils.establishConnection();
                        } else {
                            System.out.println("Some credentials are null");
                        }
                    } else {
                        mTextViewLoadedData.setText(getString(R.string.result_unable_to_read));
                    }
                }
                break;
        }
    }

    @Override
    public void onScanFinished(List<ScanResult> scanResults) {
        System.out.println("Read Fragment onScanFinished");

        for (int i = 0; i < scanResults.size(); i++){
            System.out.println(scanResults.get(i).SSID);
            System.out.println("\n");
        }
    }

    private void handleScanningNetwork() {
        // turn on WiFi if not already enabled
        final WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        boolean wifiEnabled = wifiManager.isWifiEnabled();
        if (!wifiEnabled) {
            wifiManager.setWifiEnabled(true);
        }

        final LoadQRFragment readQRFragment = this;

        if (wiFiReceiver == null) {
            wiFiReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                        List<ScanResult> scanResults = wifiManager.getScanResults();

                        readQRFragment.onScanFinished(scanResults);
                    }
                }
            };

            getActivity().registerReceiver(wiFiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
        wifiManager.startScan();
    }

    private void launchImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, Config.RESULT_PICK_IMAGE);
    }

    public void onImageLoaded(Bitmap bitmap) {
        if (mListener != null) {
            mListener.onImageLoaded(bitmap);
        }
    }

}