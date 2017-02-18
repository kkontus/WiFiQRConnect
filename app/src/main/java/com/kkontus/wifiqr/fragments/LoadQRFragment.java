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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.Result;
import com.kkontus.wifiqr.R;
import com.kkontus.wifiqr.interfaces.OnFragmentInteractionListener;
import com.kkontus.wifiqr.utils.ImageUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoadQRFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoadQRFragment extends Fragment {
    private static final String ARG_TAB_POSITION = "tabPosition";
    private int tabPosition;
    private OnFragmentInteractionListener mListener;

    // Load QR tab
    private LinearLayout mLinearLayoutForLoad;
    private Button mButtonLoadQRCode;
    private TextView mTextViewLoadedData;
    private ImageView mImageViewLoadedQR;

    private Bitmap mQRCodeLoadedBitmap;
    private static final int RESULT_PICK_IMAGE = 1234;

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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();

                    ImageUtils imageUtils = new ImageUtils(getActivity());
                    Bitmap immutableBitmap = imageUtils.getBitmapFromUri(uri);
                    Bitmap bmp = immutableBitmap.copy(Bitmap.Config.RGB_565, true);

                    mQRCodeLoadedBitmap = bmp;
                    mImageViewLoadedQR.setImageBitmap(bmp);
                    Result result = imageUtils.readQRCodeImage(bmp);
                    mTextViewLoadedData.setText(result.getText());
                }
                break;
        }
    }

    private void launchImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, RESULT_PICK_IMAGE);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

}