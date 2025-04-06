package com.example.gurukul;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class StudentTabAdapter extends FragmentStateAdapter {

    public StudentTabAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new EnrolledFragment();
            case 1:
                return new CompleteFragment();
            case 2:
                return new AlertFragment();
            default:
                return new EnrolledFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}