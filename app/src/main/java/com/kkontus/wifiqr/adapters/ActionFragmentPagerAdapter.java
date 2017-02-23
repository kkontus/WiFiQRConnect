package com.kkontus.wifiqr.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.kkontus.wifiqr.R;
import com.kkontus.wifiqr.fragments.CreateQRFragment;
import com.kkontus.wifiqr.fragments.LoadQRFragment;
import com.kkontus.wifiqr.fragments.ReadQRFragment;
import com.kkontus.wifiqr.helpers.SystemGlobal;

public class ActionFragmentPagerAdapter extends FragmentPagerAdapter {
    private final int PAGE_COUNT = 3;
    private Context mContext;

    public ActionFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);

        this.mContext = context;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);

        boolean fragmentCheck = object instanceof Fragment && (object instanceof ReadQRFragment || object instanceof LoadQRFragment || object instanceof CreateQRFragment);

        if (fragmentCheck) {
            new SystemGlobal().hideKeyboard((Fragment) object);
        }
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ReadQRFragment.newInstance(position);
            case 1:
                return LoadQRFragment.newInstance(position);
            case 2:
                return CreateQRFragment.newInstance(position);
        }

        return null;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.read_qr_code);
            case 1:
                return mContext.getString(R.string.load_qr_code);
            case 2:
                return mContext.getString(R.string.create_qr_code);
        }

        return null;
    }
}
