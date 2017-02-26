package com.kkontus.wifiqr.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.Result;
import com.kkontus.wifiqr.R;
import com.kkontus.wifiqr.helpers.Config;
import com.kkontus.wifiqr.helpers.QRCodeFormatter;
import com.kkontus.wifiqr.interfaces.OnFragmentInteractionListener;
import com.kkontus.wifiqr.interfaces.OnImageLoadedListener;
import com.kkontus.wifiqr.utils.ConnectionManagerUtils;
import com.kkontus.wifiqr.utils.ImageUtils;

import java.util.LinkedHashMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoadQRFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoadQRFragment extends Fragment implements OnFragmentInteractionListener {
    private static final String ARG_TAB_POSITION = "tabPosition";
    private int tabPosition;
    private OnImageLoadedListener mOnImageLoadedListener;

    // Load QR tab
    private Button mButtonLoadQRCode;
    private TextView mTextViewLoadedData;
    private ImageView mImageViewLoadedQR;

    private Bitmap mQRCodeLoadedBitmap;
    private float mScale = 1.0f;

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
        View view = inflater.inflate(R.layout.fragment_load_qr, container, false);
        findViews(view);

        if (mQRCodeLoadedBitmap != null) {
            imageViewRequestLayout();
            mImageViewLoadedQR.setImageBitmap(mQRCodeLoadedBitmap);
        }

        mButtonLoadQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // image picker to load image from gallery
                launchImagePicker();
            }
        });

        return view;
    }

    private void findViews(View view) {
        mButtonLoadQRCode = (Button) view.findViewById(R.id.buttonLoadQRCode);
        mTextViewLoadedData = (TextView) view.findViewById(R.id.textViewLoadedData);
        mImageViewLoadedQR = (ImageView) view.findViewById(R.id.imageViewLoadedQR);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnImageLoadedListener = null;
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
                    imageViewRequestLayout();

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

    private void imageViewRequestLayout() {
        // get display density
        mScale = getResources().getDisplayMetrics().density;
        mImageViewLoadedQR.getLayoutParams().width = (int) (mQRCodeLoadedBitmap.getWidth() * mScale);
        mImageViewLoadedQR.getLayoutParams().height = (int) (mQRCodeLoadedBitmap.getHeight() * mScale);
        mImageViewLoadedQR.requestLayout();
    }

    private void launchImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, Config.RESULT_PICK_IMAGE);
    }

    public void onImageLoaded(Bitmap bitmap) {
        if (mOnImageLoadedListener != null) {
            mOnImageLoadedListener.onImageLoaded(bitmap);
        }
    }

    @Override
    public void onFragmentFocusGained(Context context, int position) {
        System.out.println("onFragmentFocusGained " + position);
    }

    @Override
    public void onFragmentFocusLost(int position) {
        System.out.println("onFragmentFocusLost " + position);
    }

}