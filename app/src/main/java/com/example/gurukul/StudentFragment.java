package com.example.gurukul;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class StudentFragment extends Fragment implements StudentAdapter.OnStudentActionListener {

    private RecyclerView recyclerStudents;
    private StudentAdapter adapter;
    private List<StudentModel> studentList;

    public StudentFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_student, container, false);

        recyclerStudents = view.findViewById(R.id.recyclerStudents);
        recyclerStudents.setLayoutManager(new LinearLayoutManager(getContext()));

        studentList = new ArrayList<>();
        loadSampleStudents();

        adapter = new StudentAdapter(getContext(), studentList, this);
        recyclerStudents.setAdapter(adapter);

        return view;
    }

    private void loadSampleStudents() {
        studentList.add(new StudentModel("Aarav Mehta", "aarav.mehta@example.com"));
        studentList.add(new StudentModel("Diya Sharma", "diya.sharma@example.com"));
        studentList.add(new StudentModel("Kunal Verma", "kunal.verma@example.com"));
        studentList.add(new StudentModel("Riya Kapoor", "riya.kapoor@example.com"));
        studentList.add(new StudentModel("Ishaan Singh", "ishaan.singh@example.com"));
        studentList.add(new StudentModel("Sneha Nair", "sneha.nair@example.com"));
        studentList.add(new StudentModel("Vivaan Joshi", "vivaan.joshi@example.com"));
        studentList.add(new StudentModel("Anika Reddy", "anika.reddy@example.com"));
        studentList.add(new StudentModel("Tanishq Roy", "tanishq.roy@example.com"));
        studentList.add(new StudentModel("Meher Khan", "meher.khan@example.com"));
    }

    @Override
    public void onDeleteStudent(int position) {
        studentList.remove(position);
        adapter.notifyItemRemoved(position);
        Toast.makeText(getContext(), "Student deleted", Toast.LENGTH_SHORT).show();
    }
}