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
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.kkontus.wifiqr.R;
import com.kkontus.wifiqr.adapters.NetworkScanArrayAdapter;
import com.kkontus.wifiqr.helpers.Config;
import com.kkontus.wifiqr.helpers.QRCodeFormatter;
import com.kkontus.wifiqr.helpers.QRCodeSize;
import com.kkontus.wifiqr.helpers.SharedPreferencesHelper;
import com.kkontus.wifiqr.helpers.SystemGlobal;
import com.kkontus.wifiqr.interfaces.NetworkScanner;
import com.kkontus.wifiqr.interfaces.OnFragmentInteractionListener;
import com.kkontus.wifiqr.utils.ConnectionManagerUtils;
import com.kkontus.wifiqr.utils.ImageUtils;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreateQRFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateQRFragment extends Fragment implements NetworkScanner {
    private static final String ARG_TAB_POSITION = "tabPosition";
    private int tabPosition;
    private OnFragmentInteractionListener mListener;

    // Create QR tab
    private LinearLayout mLinearLayoutForLoad;
    private AutoCompleteTextView mAutoCompleteTextViewNetworkSSID;
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
    // private ArrayAdapter<String> mNetworkScanResultsArrayAdapter;
    private NetworkScanArrayAdapter mNetworkScanResultsArrayAdapter;

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

        mAutoCompleteTextViewNetworkSSID.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ScanResult scanResultFromAdapter = (ScanResult) adapterView.getAdapter().getItem(i);
                if (scanResultFromAdapter != null) {
                    mAutoCompleteTextViewNetworkSSID.setText(scanResultFromAdapter.SSID);
                }
            }
        });

        mButtonGenerateQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAutoCompleteTextViewNetworkSSID.getText().toString().trim().equals("")) {
                    mAutoCompleteTextViewNetworkSSID.setError(getString(R.string.required_ssid));
                } else {
                    mNetworkSSID = mAutoCompleteTextViewNetworkSSID.getText().toString();
                }

                if (mEditTextNetworkPassword.getText().toString().trim().equals("")) {
                    mEditTextNetworkPassword.setError(getString(R.string.required_password));
                } else {
                    mNetworkPassword = mEditTextNetworkPassword.getText().toString();
                }

                ConnectionManagerUtils connectionManagerUtils = new ConnectionManagerUtils();
                mNetworkType = connectionManagerUtils.networkTypeMapper(mSpinnerNetworkMethods.getSelectedItem().toString());
                // don't check for the condition "mNetworkType != null" since it's null for the open network
                if (mNetworkSSID != null && mNetworkPassword != null) {
                    // hide keyboard
                    new SystemGlobal().hideKeyboard(CreateQRFragment.this);

                    // start generating QR code
                    mSharedPreferencesHelper = new SharedPreferencesHelper(getActivity());
                    int imageSize = mSharedPreferencesHelper.getQrCodeImageSize();
                    QRCodeSize qrCodeSize = getQrCodeImageSize(imageSize);

                    String content = new QRCodeFormatter().formatWiFiQRCode(mNetworkSSID, mNetworkPassword, mNetworkType);

                    ImageUtils imageUtils = new ImageUtils(getActivity());
                    Bitmap bitmap = imageUtils.generateQRCode(content, qrCodeSize);
                    mQRCodeGeneratedBitmap = bitmap;

                    // get display density
                    mScale = getResources().getDisplayMetrics().density;
                    mImageViewGeneratedQR.getLayoutParams().width = (int) (mQRCodeGeneratedBitmap.getWidth() * mScale);
                    mImageViewGeneratedQR.getLayoutParams().height = (int) (mQRCodeGeneratedBitmap.getHeight() * mScale);
                    mImageViewGeneratedQR.requestLayout();

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

        // we need to request user permission for loading available networks
        requestUserPermissionNetworkScan();

        return view;
    }

    private void findViews(View view) {
        mLinearLayoutForLoad = (LinearLayout) view.findViewById(R.id.linearLayoutForLoad);
        mAutoCompleteTextViewNetworkSSID = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextViewNetworkSSID);
        mEditTextNetworkPassword = (EditText) view.findViewById(R.id.editTextNetworkPassword);
        mSpinnerNetworkMethods = (Spinner) view.findViewById(R.id.spinnerNetworkMethods);
        mButtonGenerateQRCode = (Button) view.findViewById(R.id.buttonGenerateQRCode);
        mImageViewGeneratedQR = (ImageView) view.findViewById(R.id.imageViewGeneratedQR);
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
        if (mWiFiReceiver != null) {
            System.out.println("onPause unregisterReceiver");
            getActivity().unregisterReceiver(mWiFiReceiver);
            mWiFiReceiver = null;
        }
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

    private void requestUserPermissionSaveToSDCard() {
        // check if user permission is already granted
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), getString(R.string.save_image_permission_rationale), Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
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
        if (mListener != null) {
            mListener.onImageLoaded(bitmap);
        }
    }

    private void requestUserPermissionNetworkScan() {
        // check if user permission is already granted
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), getString(R.string.scan_network_permission_rationale), Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
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
    public void onScanFinished(List<ScanResult> scanResults) {
        System.out.println("Read Fragment onScanFinished");

        mNetworkScanResultsArrayAdapter = new NetworkScanArrayAdapter(getActivity(), R.layout.autocomplete_item, scanResults);
        mAutoCompleteTextViewNetworkSSID.setAdapter(mNetworkScanResultsArrayAdapter);

//        String[] listOfAvailableNetworks = new String[scanResults.size()];
//        for (int i = 0; i < scanResults.size(); i++) {
//            listOfAvailableNetworks[i] = scanResults.get(i).SSID;
//            System.out.println(scanResults.get(i).SSID);
//        }
//
//        mNetworkScanResultsArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, listOfAvailableNetworks);
//        mNetworkScanResultsArrayAdapter.notifyDataSetChanged();
//        mAutoCompleteTextViewNetworkSSID.setAdapter(mNetworkScanResultsArrayAdapter);
    }

    private void handleScanningNetwork() {
        // turn on WiFi if not already enabled
        final WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        boolean wifiEnabled = wifiManager.isWifiEnabled();
        if (!wifiEnabled) {
            wifiManager.setWifiEnabled(true);
        }

        final CreateQRFragment readQRFragment = this;

        if (mWiFiReceiver == null) {
            mWiFiReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                        List<ScanResult> scanResults = wifiManager.getScanResults();

                        readQRFragment.onScanFinished(scanResults);
                    }
                }
            };

            getActivity().registerReceiver(mWiFiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
        wifiManager.startScan();
    }

}