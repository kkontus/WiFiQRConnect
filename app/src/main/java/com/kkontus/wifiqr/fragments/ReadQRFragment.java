package com.kkontus.wifiqr.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.kkontus.wifiqr.R;
import com.kkontus.wifiqr.helpers.QRCodeFormatter;
import com.kkontus.wifiqr.interfaces.OnFragmentInteractionListener;
import com.kkontus.wifiqr.utils.ConnectionManagerUtils;

import java.util.LinkedHashMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReadQRFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReadQRFragment extends Fragment {
    private static final String ARG_TAB_POSITION = "tabPosition";
    private int tabPosition;
    private OnFragmentInteractionListener mListener;

    // Read QR tab
    private LinearLayout mLinearLayoutForRead;
    private Button mButtonScanQRCode;
    private TextView mTextViewConnectionStatus;
    private TextView mTextViewConnectionData;

    private String mNetworkSSID;
    private String mNetworkPassword;
    private String mNetworkType;

    public ReadQRFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param tabPosition Parameter 1.
     * @return A new instance of fragment ReadQRFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReadQRFragment newInstance(int tabPosition) {
        ReadQRFragment fragment = new ReadQRFragment();
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
        View view = inflater.inflate(R.layout.fragment_read_qr, container, false);
        findViews(view);

        mButtonScanQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQRScanner();
            }
        });

        return view;
    }

    private void findViews(View view) {
        mLinearLayoutForRead = (LinearLayout) view.findViewById(R.id.linearLayoutForRead);
        mButtonScanQRCode = (Button) view.findViewById(R.id.buttonScanQRCode);
        mTextViewConnectionStatus = (TextView) view.findViewById(R.id.textViewConnectionStatus);
        mTextViewConnectionData = (TextView) view.findViewById(R.id.textViewConnectionData);
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                mTextViewConnectionData.setText(getString(R.string.qr_scanner_canceled));
            } else {
                mTextViewConnectionData.setText(result.getContents().toString());

                LinkedHashMap networkCredentials = new QRCodeFormatter().extractWiFiCredentials(result.getContents().toString());
                mNetworkSSID = (String) networkCredentials.get(ConnectionManagerUtils.NETWORK_SSID);
                mNetworkPassword = (String) networkCredentials.get(ConnectionManagerUtils.NETWORK_PASSWORD);
                mNetworkType = (String) networkCredentials.get(ConnectionManagerUtils.NETWORK_TYPE);

                // don't check for the condition "mNetworkType != null" since it's null for the open network
                if (mNetworkSSID != null && mNetworkPassword != null) {
                    ConnectionManagerUtils connectionManagerUtils = new ConnectionManagerUtils(getActivity(), mNetworkSSID, mNetworkPassword, mNetworkType);
                    connectionManagerUtils.establishConnection();
                } else {
                    System.out.println("Some credentials are null");
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void startQRScanner() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt(getString(R.string.qr_scanner_hint));
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

}