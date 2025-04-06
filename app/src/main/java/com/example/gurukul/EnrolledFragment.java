package com.example.gurukul;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EnrolledFragment extends Fragment {

    private RecyclerView recyclerView;
    private EnrolledCourseAdapter adapter;
    private List<EnrolledCourse> courseList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enrolled, container, false);

        recyclerView = view.findViewById(R.id.recyclerEnrolledCourses);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        courseList = new ArrayList<>();
        courseList.add(new EnrolledCourse("Android Development", "Mr. Sharma", "6 weeks", R.drawable.sampleimage));
        courseList.add(new EnrolledCourse("Java Fundamentals", "Ms. Singh", "4 weeks", R.drawable.sampleimage));
        courseList.add(new EnrolledCourse("UI/UX Design", "Mr. Khan", "3 weeks", R.drawable.sampleimage));
        courseList.add(new EnrolledCourse("Firebase Integration", "Mr. Joshi", "2 weeks", R.drawable.sampleimage));
        courseList.add(new EnrolledCourse("Machine Learning Basics", "Dr. Mehta", "8 weeks", R.drawable.sampleimage));
        courseList.add(new EnrolledCourse("Python for Data Science", "Ms. Iyer", "5 weeks", R.drawable.sampleimage));
        courseList.add(new EnrolledCourse("Full Stack Web Development", "Mr. Deshmukh", "10 weeks", R.drawable.sampleimage));
        courseList.add(new EnrolledCourse("Cloud Computing with AWS", "Ms. Roy", "6 weeks", R.drawable.sampleimage));
        courseList.add(new EnrolledCourse("Cybersecurity Essentials", "Mr. Kapoor", "4 weeks", R.drawable.sampleimage));
        courseList.add(new EnrolledCourse("C++ Programming Masterclass", "Mr. Verma", "7 weeks", R.drawable.sampleimage));
        courseList.add(new EnrolledCourse("Kotlin for Android", "Ms. Nair", "3 weeks", R.drawable.sampleimage));
        courseList.add(new EnrolledCourse("Data Structures & Algorithms", "Dr. Rao", "6 weeks", R.drawable.sampleimage));


        adapter = new EnrolledCourseAdapter(getContext(), courseList);
        recyclerView.setAdapter(adapter);

        return view;
    }
}