package com.kkontus.wifiqr.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.kkontus.wifiqr.fragments.CreateQRFragment;
import com.kkontus.wifiqr.fragments.LoadQRFragment;
import com.kkontus.wifiqr.fragments.ReadQRFragment;

public class ActionFragmentPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 3;
    private String tabTitles[] = new String[]{"Read QR", "Load QR", "Create QR"};

    public ActionFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return ReadQRFragment.newInstance(position);
        } else if (position == 1) {
            return LoadQRFragment.newInstance(position);
        } else if (position == 2) {
            return CreateQRFragment.newInstance(position);
        } else {
            return ReadQRFragment.newInstance(position);
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
