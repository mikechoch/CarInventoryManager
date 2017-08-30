package com.choch.michaeldicioccio.myapplication;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


public class CustomFragmentPagerAdapter extends FragmentPagerAdapter {

    Context context;

    public CustomFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = Fragment.instantiate(context, CarsFragment.class.getName());
                break;
            case 1:
                fragment = Fragment.instantiate(context, CarsSoldFragment.class.getName());
                break;
            case 2:
                fragment = Fragment.instantiate(context, AllCarsFragment.class.getName());
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
