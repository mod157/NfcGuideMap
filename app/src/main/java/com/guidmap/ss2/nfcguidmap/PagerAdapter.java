package com.guidmap.ss2.nfcguidmap;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import java.util.List;

/**
 * Created by SunJae on 2016-10-30.
 */

public class PagerAdapter extends FragmentPagerAdapter {
    List<Fragment> listFragments;

    public PagerAdapter(FragmentManager fm, List<Fragment> listFragments){
        super(fm);
        this.listFragments = listFragments;
        Log.v("Fragment","Ok");
    }
    @Override
    public Fragment getItem(int position) {
        return listFragments.get(position);
    }

    @Override
    public int getCount() {
        return listFragments.size();
    }
}
