package com.kkontus.wifiqr.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.kkontus.wifiqr.R;
import com.kkontus.wifiqr.adapters.ActionFragmentPagerAdapter;
import com.kkontus.wifiqr.helpers.Config;
import com.kkontus.wifiqr.helpers.SystemGlobal;
import com.kkontus.wifiqr.interfaces.OnFragmentInteractionListener;
import com.kkontus.wifiqr.interfaces.OnImageLoadedListener;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity implements OnImageLoadedListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private ActionFragmentPagerAdapter mActionFragmentPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private CoordinatorLayout mCoordinatorLayout;
    private FloatingActionButton mFab;
//    private Snackbar mSnackbarLocationPermission;
    private TabLayout mTabLayout;
    private Bitmap mQRCodeLoadedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findViews();

        // hide mFab by default
        mFab.hide();
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mQRCodeLoadedBitmap != null) {
                    // share QR code image
                    shareQRCodeBitmap(mQRCodeLoadedBitmap, "NetworkInfo");
                } else {
                    Snackbar snackbar = Snackbar.make(view, getString(R.string.share_image_not_empty), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });

//        // TODO
//        mSnackbarLocationPermission = makeSnackbar();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mActionFragmentPagerAdapter = new ActionFragmentPagerAdapter(getSupportFragmentManager(), this);

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mActionFragmentPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                clearFocus();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    hideKeyboard();
                }
            }
        });

        // Give the TabLayout the ViewPager
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.colorPrimary));
        mTabLayout.setSelectedTabIndicatorHeight((int) (4 * getResources().getDisplayMetrics().density));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                clearFocus();
                hideKeyboard();

                if (tab.getPosition() == 1 || tab.getPosition() == 2) {
                    mFab.show();
                } else {
                    mFab.hide();
                }

//                // TODO
//                if (tab.getPosition() == 2) {
//                    // make new in case previous one is closed by swipe
//                    mSnackbarLocationPermission = makeSnackbar();
//
//                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
//                            mSnackbarLocationPermission.show();
//                        } else {
//                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Config.REQUEST_ACCESS_COARSE_LOCATION);
//                        }
//                    }
//                } else {
//                    mSnackbarLocationPermission.dismiss();
//                }

                OnFragmentInteractionListener fragmentToShow = (OnFragmentInteractionListener) mActionFragmentPagerAdapter.getItem(tab.getPosition());
                fragmentToShow.onFragmentFocusGained(getApplicationContext(), tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                OnFragmentInteractionListener fragmentToShow = (OnFragmentInteractionListener) mActionFragmentPagerAdapter.getItem(tab.getPosition());
                fragmentToShow.onFragmentFocusLost(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void findViews() {
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_content);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mViewPager = (ViewPager) findViewById(R.id.container);
        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent intent;
        if (id == R.id.action_settings) {
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onImageLoaded(Bitmap bitmap) {
        System.out.println("MainActivity onImageLoaded");

        mQRCodeLoadedBitmap = bitmap;
    }

    private void hideKeyboard() {
        // hide keyboard
        new SystemGlobal().hideKeyboard(MainActivity.this);
    }

    private void clearFocus() {
        if (getCurrentFocus() != null) {
            getCurrentFocus().clearFocus();
        }
    }

    private void shareQRCodeBitmap(Bitmap bitmap, String filename) {
        try {
            File file = new File(getCacheDir(), filename + ".jpeg");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("image/jpeg");
            startActivity(Intent.createChooser(intent, getResources().getText(R.string.send_to)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    // TODO
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        switch (requestCode) {
//            case Config.REQUEST_ACCESS_COARSE_LOCATION: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // permission was granted, yay! Do the task you need to do.
//                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
//                    // Should we show an explanation?
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
//
//                    } else {
//                        //Never ask again selected, or device policy prohibits the app from having that permission.
//                        //So, disable that feature, or fall back to another situation...
//                    }
//                }
//                return;
//            }
//        }
//    }
//
//    @NonNull
//    private Snackbar makeSnackbar() {
//        return Snackbar.make(mCoordinatorLayout, getString(R.string.scan_network_permission_rationale), Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Request the permission again.
//                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Config.REQUEST_ACCESS_COARSE_LOCATION);
//            }
//        });
//    }

}