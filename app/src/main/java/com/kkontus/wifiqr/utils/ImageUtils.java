package com.kkontus.wifiqr.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.kkontus.wifiqr.helpers.QRCodeSize;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageUtils {
    private Context mContext;
    private static final int SMALL_WIDTH = 120;
    private static final int SMALL_HEIGHT = 120;
    private static final int MEDIUM_WIDTH = 230;
    private static final int MEDIUM_HEIGHT = 230;
    private static final int LARGE_WIDTH = 350;
    private static final int LARGE_HEIGHT = 350;

    public ImageUtils(Context context) {
        mContext = context;
    }

    public Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor;
        Bitmap image = null;
        try {
            parcelFileDescriptor = mContext.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public Result readQRCodeImage(Bitmap loadedQRCodeImage) {
        int width = loadedQRCodeImage.getWidth();
        int height = loadedQRCodeImage.getHeight();
        int[] pixels = new int[width * height];
        loadedQRCodeImage.getPixels(pixels, 0, width, 0, 0, width, height);

        LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        Reader reader = new MultiFormatReader();
        Result result = null;
        try {
            result = reader.decode(binaryBitmap);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Bitmap generateQRCode(String content, QRCodeSize outputImageSize) {
        int imageWidth;
        int imageHeight;
        // these are standards for the WiFi QR codes
        if (outputImageSize == QRCodeSize.SMALL) {
            imageWidth = SMALL_WIDTH;
            imageHeight = SMALL_HEIGHT;
        } else if (outputImageSize == QRCodeSize.MEDIUM) {
            imageWidth = MEDIUM_WIDTH;
            imageHeight = MEDIUM_HEIGHT;
        } else {
            imageWidth = LARGE_WIDTH;
            imageHeight = LARGE_HEIGHT;
        }

        QRCodeWriter writer = new QRCodeWriter();
        Bitmap bitmap = null;
        try {
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, imageWidth, imageHeight);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

}