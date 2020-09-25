package com.applex.utsav.adapters;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class HomeTabAdapter extends FragmentPagerAdapter {

    private final ArrayList<String> fragmentTitle = new ArrayList<>();
    private final ArrayList<Fragment> fragmentList = new ArrayList<>();

    public HomeTabAdapter(FragmentManager fm) {
        super(fm);
    }

    @NotNull
    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentTitle.get(position);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return fragmentList.size();
    }

    public void addFragment(Fragment fragment, String title){
        fragmentList.add(fragment);
        fragmentTitle.add(title);
    }

}