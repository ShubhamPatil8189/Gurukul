package com.example.gurukul;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeacherRegisterActivity extends AppCompatActivity {

    private static final String TAG = "TeacherRegister";

    private EditText etName, etEmail, etQualification, etExperience, etExpertise, etPassword;
    private Spinner spinnerGender;
    private Button btnRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Init Views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etQualification = findViewById(R.id.etQualification);
        etExperience = findViewById(R.id.etExperience);
        etExpertise = findViewById(R.id.etExpertise);
        etPassword = findViewById(R.id.etPassword);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnRegister = findViewById(R.id.btnRegister);

        // Spinner Setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> registerTeacher());
    }

    private void registerTeacher() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String qualification = etQualification.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();
        String expertise = etExpertise.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();

        if (name.isEmpty() || email.isEmpty() || qualification.isEmpty() || experience.isEmpty()
                || expertise.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    String teacherId = UUID.randomUUID().toString(); // Unique ID

                    Map<String, Object> teacher = new HashMap<>();
                    teacher.put("uid", uid);
                    teacher.put("teacherId", teacherId);
                    teacher.put("name", name);
                    teacher.put("email", email);
                    teacher.put("qualification", qualification);
                    teacher.put("experience", experience);
                    teacher.put("expertise", expertise);
                    teacher.put("gender", gender);
                    teacher.put("role", "Teacher");

                    db.collection("teachers").document(uid)
                            .set(teacher)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Teacher Registered!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Teacher data saved to Firestore.");
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to save teacher info", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Firestore write failed", e);
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Firebase Auth failed: ", e);
                });
    }
}
