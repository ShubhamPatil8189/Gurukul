package com.example.gurukul;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class EditTeacherProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditTeacherProfile";

    private TextInputEditText etName, etEmail, etQualification, etExperience, etExpertise;
    private Spinner spinnerGender;
    private Button btnEdit;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_teacher_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etQualification = findViewById(R.id.etQualification);
        etExperience = findViewById(R.id.etExperience);
        etExpertise = findViewById(R.id.etExpertise);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnEdit = findViewById(R.id.btnEditProfile);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        loadTeacherData();

        btnEdit.setOnClickListener(v -> updateTeacherProfile());
    }

    private void loadTeacherData() {
        db.collection("teachers").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        etName.setText(document.getString("name"));
                        etEmail.setText(document.getString("email"));
                        etQualification.setText(document.getString("qualification"));
                        etExperience.setText(document.getString("experience"));
                        etExpertise.setText(document.getString("expertise"));

                        String gender = document.getString("gender");
                        if (gender != null) {
                            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerGender.getAdapter();
                            int spinnerPosition = adapter.getPosition(gender);
                            spinnerGender.setSelection(spinnerPosition);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading teacher data", e);
                });
    }

    private void updateTeacherProfile() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String qualification = etQualification.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();
        String expertise = etExpertise.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();

        if (name.isEmpty() || email.isEmpty() || qualification.isEmpty() || experience.isEmpty() || expertise.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updated = new HashMap<>();
        updated.put("name", name);
        updated.put("email", email);
        updated.put("qualification", qualification);
        updated.put("experience", experience);
        updated.put("expertise", expertise);
        updated.put("gender", gender);

        db.collection("teachers").document(uid)
                .update(updated)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Teacher profile updated");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Update failed", e);
                });
    }
}