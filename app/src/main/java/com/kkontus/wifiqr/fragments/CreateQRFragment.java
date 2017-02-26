package com.kkontus.wifiqr.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.kkontus.wifiqr.R;
import com.kkontus.wifiqr.adapters.NetworkScanArrayAdapter;
import com.kkontus.wifiqr.adapters.NetworkSecurityMethodsArrayAdapter;
import com.kkontus.wifiqr.helpers.Config;
import com.kkontus.wifiqr.helpers.QRCodeFormatter;
import com.kkontus.wifiqr.helpers.QRCodeSize;
import com.kkontus.wifiqr.helpers.SharedPreferencesHelper;
import com.kkontus.wifiqr.helpers.SystemGlobal;
import com.kkontus.wifiqr.interfaces.NetworkScanner;
import com.kkontus.wifiqr.interfaces.OnFragmentInteractionListener;
import com.kkontus.wifiqr.interfaces.OnImageLoadedListener;
import com.kkontus.wifiqr.models.Network;
import com.kkontus.wifiqr.utils.ConnectionManagerUtils;
import com.kkontus.wifiqr.utils.ImageUtils;
import com.kkontus.wifiqr.views.InstantAutoComplete;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreateQRFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateQRFragment extends Fragment implements NetworkScanner, OnFragmentInteractionListener {
    private static final String ARG_TAB_POSITION = "tabPosition";
    private int tabPosition;
    private OnImageLoadedListener mOnImageLoadedListener;

    // Create QR tab
    private CoordinatorLayout mCoordinatorLayout;
    private RelativeLayout mRelativeLayoutCreate;
    private InstantAutoComplete mAutoCompleteTextViewNetworkSSID;
    private EditText mEditTextNetworkPassword;
    private Spinner mSpinnerNetworkMethods;
    private Button mButtonGenerateQRCode;
    private ImageView mImageViewGeneratedQR;
    private Bitmap mQRCodeGeneratedBitmap;

    // network credentials properties
    private String mNetworkSSID;
    private String mNetworkPassword;
    private String mNetworkType;

    // general
    private SharedPreferencesHelper mSharedPreferencesHelper;
    private float mScale = 1.0f;
    private BroadcastReceiver mWiFiReceiver;
    private NetworkScanArrayAdapter mNetworkScanResultsArrayAdapter;
    private List<Network> mScanResults = new ArrayList<>();

    public CreateQRFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param tabPosition Parameter 1.
     * @return A new instance of fragment CreateQRFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateQRFragment newInstance(int tabPosition) {
        CreateQRFragment fragment = new CreateQRFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_POSITION, tabPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnImageLoadedListener) {
            mOnImageLoadedListener = (OnImageLoadedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnImageLoadedListener");
        }
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
        View view = inflater.inflate(R.layout.fragment_create_qr, container, false);
        findViews(view);

        ConnectionManagerUtils connectionManagerUtils = new ConnectionManagerUtils(view.getContext());
        String connectedNetwork = connectionManagerUtils.connectedNetworkSSID();

        // this is just mock that will show open dropdown for autocomplete
        initializeNetworkScanAdapter(view.getContext());

        mRelativeLayoutCreate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                clearFocus();
                hideKeyboard();
                return false;
            }
        });

        // TODO
        mAutoCompleteTextViewNetworkSSID.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    if (isAccessCoarseLocationPermissionGranted(getContext())) {
                        handleScanningNetwork();
                    }
                }
            }
        });

        mAutoCompleteTextViewNetworkSSID.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Network scanResultFromAdapter = (Network) adapterView.getAdapter().getItem(i);
                if (scanResultFromAdapter != null && scanResultFromAdapter.getSSID() != null) {
                    mAutoCompleteTextViewNetworkSSID.setText(scanResultFromAdapter.getSSID());
                    mAutoCompleteTextViewNetworkSSID.setSelection(scanResultFromAdapter.getSSID().length());
                }
            }
        });
        mAutoCompleteTextViewNetworkSSID.setThreshold(1);
        mAutoCompleteTextViewNetworkSSID.setText(connectedNetwork);
        mAutoCompleteTextViewNetworkSSID.setSelection(connectedNetwork.length());

        // initialize drop down adapter
        initializeNetworkSecurityMethodsAdapter(view.getContext());
        mSpinnerNetworkMethods.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });

        if (mQRCodeGeneratedBitmap != null) {
            imageViewRequestLayout();
            mImageViewGeneratedQR.setImageBitmap(mQRCodeGeneratedBitmap);
        }

        mButtonGenerateQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAutoCompleteTextViewNetworkSSID.getText().toString().trim().equals("")) {
                    mAutoCompleteTextViewNetworkSSID.setError(getString(R.string.required_ssid));
                } else {
                    mNetworkSSID = mAutoCompleteTextViewNetworkSSID.getText().toString();
                    mAutoCompleteTextViewNetworkSSID.clearFocus();
                }

                if (mEditTextNetworkPassword.getText().toString().trim().equals("")) {
                    mEditTextNetworkPassword.setError(getString(R.string.required_password));
                } else {
                    mNetworkPassword = mEditTextNetworkPassword.getText().toString();
                    mEditTextNetworkPassword.clearFocus();
                }

                ConnectionManagerUtils connectionManagerUtils = new ConnectionManagerUtils();
                mNetworkType = connectionManagerUtils.networkTypeMapper(mSpinnerNetworkMethods.getSelectedItem().toString());
                // don't check for the condition "mNetworkType != null" since it's null for the open network
                if (mNetworkSSID != null && mNetworkPassword != null) {
                    hideKeyboard();

                    // start generating QR code
                    mSharedPreferencesHelper = new SharedPreferencesHelper(getActivity());
                    int imageSize = mSharedPreferencesHelper.getQrCodeImageSize();
                    QRCodeSize qrCodeSize = getQrCodeImageSize(imageSize);

                    String content = new QRCodeFormatter().formatWiFiQRCode(mNetworkSSID, mNetworkPassword, mNetworkType);

                    ImageUtils imageUtils = new ImageUtils(getActivity());
                    Bitmap bitmap = imageUtils.generateQRCode(content, qrCodeSize);
                    mQRCodeGeneratedBitmap = bitmap;
                    imageViewRequestLayout();

                    boolean includeNetworkInfo = mSharedPreferencesHelper.getIncludeNetworkInfo();
                    if (includeNetworkInfo) {
                        drawSSIDAndPass(mQRCodeGeneratedBitmap, qrCodeSize);
                    }

                    mImageViewGeneratedQR.setImageBitmap(mQRCodeGeneratedBitmap);
                    onImageLoaded(mQRCodeGeneratedBitmap);

                    // we need to request user permission for saving QR code image to external storage
                    requestUserPermissionSaveToSDCard();
                }
            }
        });

        return view;
    }

    private void imageViewRequestLayout() {
        // get display density
        mScale = getResources().getDisplayMetrics().density;
        mImageViewGeneratedQR.getLayoutParams().width = (int) (mQRCodeGeneratedBitmap.getWidth() * mScale);
        mImageViewGeneratedQR.getLayoutParams().height = (int) (mQRCodeGeneratedBitmap.getHeight() * mScale);
        mImageViewGeneratedQR.requestLayout();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && mAutoCompleteTextViewNetworkSSID != null) {
            mAutoCompleteTextViewNetworkSSID.requestFocus();
        }
    }

    private void findViews(View view) {
        mRelativeLayoutCreate = (RelativeLayout) view.findViewById(R.id.fragment_main_create);
        mAutoCompleteTextViewNetworkSSID = (InstantAutoComplete) view.findViewById(R.id.autoCompleteTextViewNetworkSSID);
        mEditTextNetworkPassword = (EditText) view.findViewById(R.id.editTextNetworkPassword);
        mSpinnerNetworkMethods = (Spinner) view.findViewById(R.id.spinnerNetworkMethods);
        mButtonGenerateQRCode = (Button) view.findViewById(R.id.buttonGenerateQRCode);
        mImageViewGeneratedQR = (ImageView) view.findViewById(R.id.imageViewGeneratedQR);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCoordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.main_content);
    }

    @Override
    public void onPause() {
        super.onPause();

        System.out.println("onPause");
        unregisterWiFiReceiver();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnImageLoadedListener = null;

        System.out.println("onDetach");
        unregisterWiFiReceiver();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Config.REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the task you need to do.
                    handleSavingImage();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private void unregisterWiFiReceiver() {
        if (mWiFiReceiver != null) {
            System.out.println("unregisterReceiver");
            getActivity().unregisterReceiver(mWiFiReceiver);
            mWiFiReceiver = null;
        }
    }

    private void hideKeyboard() {
        // hide keyboard
        new SystemGlobal().hideKeyboard(CreateQRFragment.this);
    }

    private void clearFocus() {
        if (getActivity().getCurrentFocus() != null) {
            getActivity().getCurrentFocus().clearFocus();
        }
    }

    private void requestUserPermissionSaveToSDCard() {
        // check if user permission is already granted
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                Snackbar.make(mCoordinatorLayout, getString(R.string.save_image_permission_rationale), Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Request the permission again.
                        // Don't use ActivityCompat.requestPermissions since it goes through parent
                        // activity and we don't need that, so we need to use requestPermissions
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Config.REQUEST_WRITE_EXTERNAL_STORAGE);
                    }
                }).show();
            } else {
                // No explanation needed, we can request the permission.
                // Don't use ActivityCompat.requestPermissions since it goes through parent
                // activity and we don't need that, so we need to use requestPermissions
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Config.REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            // user permission has already been granted so we can continue with saving the image
            handleSavingImage();
        }
    }

    private void handleSavingImage() {
        new SystemGlobal().handleSavingImage(getActivity(), mQRCodeGeneratedBitmap, Config.SAVE_IMAGE_NAME);
    }

    @NonNull
    private QRCodeSize getQrCodeImageSize(int imageSize) {
        QRCodeSize qrCodeSize;
        if (imageSize == 0) {
            qrCodeSize = QRCodeSize.SMALL;
        } else if (imageSize == 1) {
            qrCodeSize = QRCodeSize.MEDIUM;
        } else {
            qrCodeSize = QRCodeSize.LARGE;
        }
        return qrCodeSize;
    }

    private float getDrawTextSize(QRCodeSize outputImageSize) {
        float textSize;
        if (outputImageSize == QRCodeSize.SMALL) {
            textSize = 3.5f;
        } else if (outputImageSize == QRCodeSize.MEDIUM) {
            textSize = 6.5f;
        } else {
            textSize = 10.0f;
        }
        return textSize;
    }

    private void drawSSIDAndPass(Bitmap bitmap, QRCodeSize outputImageSize) {
        float textSize = getDrawTextSize(outputImageSize);

        Canvas canvas = new Canvas(bitmap);
        Rect bounds = new Rect();

        // draw text for network ssid and network password
        drawNetworkSSID(canvas, bitmap, bounds, textSize, mNetworkSSID);
        drawNetworkPassword(canvas, bitmap, bounds, textSize, mNetworkPassword);
    }

    private void drawNetworkSSID(Canvas canvas, Bitmap bitmap, Rect bounds, float textSize, String textToDraw) {
        // set Paint settings
        Paint paint = getPaintSettings(textSize);
        paint.getTextBounds(textToDraw, 0, textToDraw.length(), bounds);

        int x = (bitmap.getWidth() - bounds.width()) / 2;
        canvas.drawText(textToDraw, x, textSize * mScale, paint);
    }

    private void drawNetworkPassword(Canvas canvas, Bitmap bitmap, Rect bounds, float textSize, String textToDraw) {
        // set Paint settings
        Paint paint = getPaintSettings(textSize);
        paint.getTextBounds(textToDraw, 0, textToDraw.length(), bounds);

        int x = (bitmap.getWidth() - bounds.width()) / 2;
        canvas.drawText(textToDraw, x, bitmap.getHeight() - 10, paint);
    }

    @NonNull
    private Paint getPaintSettings(float textSize) {
        // new anti aliased Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color
        paint.setColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        // text size in pixels
        paint.setTextSize(textSize * mScale);
        // text bold
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        return paint;
    }

    public void onImageLoaded(Bitmap bitmap) {
        if (mOnImageLoadedListener != null) {
            mOnImageLoadedListener.onImageLoaded(bitmap);
        }
    }

    private boolean isAccessCoarseLocationPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void initializeNetworkSecurityMethodsAdapter(Context context) {
        List<String> networkMethods = new ArrayList<>();
        networkMethods.add(ConnectionManagerUtils.DROPDOWN_VALUE_OPEN);
        networkMethods.add(ConnectionManagerUtils.DROPDOWN_VALUE_WEP);
        networkMethods.add(ConnectionManagerUtils.DROPDOWN_VALUE_WPA);
        NetworkSecurityMethodsArrayAdapter adapter = new NetworkSecurityMethodsArrayAdapter(context, R.layout.spinner_item, networkMethods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerNetworkMethods.setAdapter(adapter);
    }

    @Override
    public void onScanFinished(List<ScanResult> scanResults) {
        System.out.println("Create Fragment onScanFinished");

        List<Network> loadedNetworks = new ArrayList<>();
        for (ScanResult scanResult : scanResults) {
            Network network = new Network();
            network.setSSID(scanResult.SSID);
            loadedNetworks.add(network);
        }
        reloadNetworkScanAdapter(loadedNetworks);
    }

    @Override
    public void onScanConfiguredFinished(List<WifiConfiguration> scanConfiguredNetworksResults) {
        System.out.println("Create Fragment onScanConfiguredFinished");

        List<Network> loadedNetworks = new ArrayList<>();
        for (WifiConfiguration wifiConfiguration : scanConfiguredNetworksResults) {
            Network network = new Network();
            String connectedNetworkSSID = wifiConfiguration.SSID;
            String SSID = connectedNetworkSSID.replace("\"", "");
            network.setSSID(SSID);
            if (!loadedNetworks.contains(network)) {
                loadedNetworks.add(network);
            }
        }
        reloadNetworkScanAdapter(loadedNetworks);
    }

    private void initializeNetworkScanAdapter(Context context) {
        List<Network> loadingNetworks = new ArrayList<>();
        Network network = new Network();
        if (isAccessCoarseLocationPermissionGranted(context)) {
            network.setSSID("Loading...");
        } else {
            network.setSSID(null);
        }
        loadingNetworks.add(network);
        setNetworkScanAdapter(context, loadingNetworks);
    }

    private void setNetworkScanAdapter(Context context, List<Network> scanResults) {
        mNetworkScanResultsArrayAdapter = new NetworkScanArrayAdapter(context, R.layout.autocomplete_item, scanResults);
        mAutoCompleteTextViewNetworkSSID.setAdapter(mNetworkScanResultsArrayAdapter);
    }

    private void reloadNetworkScanAdapter(List<Network> scanResults) {
        // mScanResults.clear(); // no need for this
        mScanResults = scanResults; // set scanned networks to class property

        mNetworkScanResultsArrayAdapter.clear();
        mNetworkScanResultsArrayAdapter.addAll(mScanResults); // set in the line above
        mAutoCompleteTextViewNetworkSSID.setAdapter(mNetworkScanResultsArrayAdapter);
        mNetworkScanResultsArrayAdapter.notifyDataSetChanged();
    }

    private void handleScanningNetwork() {
        // turn on WiFi if not already enabled
        final WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        boolean wifiEnabled = wifiManager.isWifiEnabled();
        if (!wifiEnabled) {
            wifiManager.setWifiEnabled(true);
        }

        final CreateQRFragment createQRFragment = this;

        if (mWiFiReceiver == null) {
            mWiFiReceiver = new WiFiReceiver(createQRFragment);
            getActivity().registerReceiver(mWiFiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

//            mWiFiReceiver = new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                    if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
//                        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//                        List<ScanResult> scanResults = wifiManager.getScanResults();
//
//                        createQRFragment.onScanFinished(scanResults);
//                    }
//                }
//            };
//
//            getActivity().registerReceiver(mWiFiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
        wifiManager.startScan();
    }

    @Override
    public void onFragmentFocusGained(Context context, int position) {
        System.out.println("onFragmentFocusGained " + position);

    }

    @Override
    public void onFragmentFocusLost(int position) {
        System.out.println("onFragmentFocusLost " + position);

    }


    private class WiFiReceiver extends BroadcastReceiver {
        private CreateQRFragment mCreateQRFragment;

        public WiFiReceiver(CreateQRFragment createQRFragment) {
            this.mCreateQRFragment = createQRFragment;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                //List<ScanResult> scanResults = wifiManager.getScanResults();
                //mCreateQRFragment.onScanFinished(scanResults);

                List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
                mCreateQRFragment.onScanConfiguredFinished(configuredNetworks);
            }
        }
    }

}