package com.kkontus.wifiqr.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.kkontus.wifiqr.R;
import com.kkontus.wifiqr.helpers.Config;
import com.kkontus.wifiqr.helpers.QRCodeFormatter;
import com.kkontus.wifiqr.helpers.QRCodeSize;
import com.kkontus.wifiqr.helpers.SystemGlobal;
import com.kkontus.wifiqr.interfaces.OnFragmentInteractionListener;
import com.kkontus.wifiqr.utils.ConnectionManagerUtils;
import com.kkontus.wifiqr.utils.ImageUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreateQRFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateQRFragment extends Fragment {
    private static final String ARG_TAB_POSITION = "tabPosition";
    private int tabPosition;
    private OnFragmentInteractionListener mListener;

    // Create QR tab
    private LinearLayout mLinearLayoutForLoad;
    private EditText mEditTextNetworkSSID;
    private EditText mEditTextNetworkPassword;
    private Spinner mSpinnerNetworkMethods;
    private Button mButtonGenerateQRCode;
    private ImageView mImageViewGeneratedQR;
    private Bitmap mQRCodeGeneratedBitmap;

    private String mNetworkSSID;
    private String mNetworkPassword;
    private String mNetworkType;

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

        mButtonGenerateQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEditTextNetworkSSID.getText().toString().trim().equals("")) {
                    mEditTextNetworkSSID.setError(getString(R.string.required_ssid));
                } else {
                    mNetworkSSID = mEditTextNetworkSSID.getText().toString();
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
                    String content = new QRCodeFormatter().formatWiFiQRCode(mNetworkSSID, mNetworkPassword, mNetworkType);
                    ImageUtils imageUtils = new ImageUtils(getActivity());
                    Bitmap bitmap = imageUtils.generateQRCode(content, QRCodeSize.LARGE);
                    drawSSIDAndPass(bitmap);
                    mQRCodeGeneratedBitmap = bitmap;
                    mImageViewGeneratedQR.setImageBitmap(bitmap);
                    onImageLoaded(bitmap);

                    //requestPermission();
                    new SystemGlobal().handleSavingImage(getActivity(), bitmap, Config.SAVE_IMAGE_NAME);
                }
            }
        });

        return view;
    }

    private void findViews(View view) {
        mLinearLayoutForLoad = (LinearLayout) view.findViewById(R.id.linearLayoutForLoad);
        mEditTextNetworkSSID = (EditText) view.findViewById(R.id.editTextNetworkSSID);
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
    }

    private void drawSSIDAndPass(Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);
        Rect bounds = new Rect();

        // draw text for network ssid and network password
        drawNetworkSSID(canvas, bitmap, bounds, 10, mNetworkSSID);
        drawNetworkPassword(canvas, bitmap, bounds, 8, mNetworkPassword);
    }

    private void drawNetworkSSID(Canvas canvas, Bitmap bitmap, Rect bounds, float textSize, String textToDraw) {
        // set Paint settings
        Paint paint = getPaintSettings(textSize);
        paint.getTextBounds(textToDraw, 0, textToDraw.length(), bounds);

        int x = (bitmap.getWidth() - bounds.width()) / 2;
        canvas.drawText(textToDraw, x, 35, paint);
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
        // get display density
        float scale = getResources().getDisplayMetrics().density;

        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color
        paint.setColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        // text size in pixels
        paint.setTextSize((int) (textSize * scale));
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

}