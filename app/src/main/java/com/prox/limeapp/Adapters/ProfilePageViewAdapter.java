package com.prox.limeapp.Adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.prox.limeapp.Fragments.ProfileFragment;

public class ProfilePageViewAdapter extends FragmentPagerAdapter {

    String uid = "0";
    String current_state = "0";

    int size;

    public ProfilePageViewAdapter(FragmentManager fm, int size,String uid,String current_state) {
        super(fm);
        this.size = size;
        this.uid = uid;
        this.current_state = current_state;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                ProfileFragment profileFragment = new ProfileFragment();
                Bundle bundle = new Bundle();
                bundle.putString("uid",uid);
                bundle.putString("current_state",current_state);
                profileFragment.setArguments(bundle);
                return profileFragment;
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return size;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "posts";
            default:
                return null;
        }
    }
}
