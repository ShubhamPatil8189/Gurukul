package com.example.gurukul;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class CoursePagerAdapter extends FragmentStateAdapter {

    private String courseId;

    public CoursePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    // Add a method to set courseId
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;

        switch (position) {
            case 0:
                // Videos tab
                fragment = new VideoFragment();
                break;
            case 1:
                // Students tab
                fragment = new StudentFragment();
                break;
            case 2:
                // Alerts tab
                fragment = new AlertFragment();
                break;
            case 3:
                // Notes tab
                fragment = new CourseFragment();
                break;
            default:
                fragment = new VideoFragment();
                break;
        }

        // Pass courseId to all fragments
        if (courseId != null) {
            Bundle args = new Bundle();
            args.putString("courseId", courseId);
            fragment.setArguments(args);
        }

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 4; // Number of tabs
    }
}