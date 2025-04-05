package com.example.gurukul;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText etUsername, etEmail, etMobile, etPassword, etDOB;
    private Spinner spinnerGender;
    private Button btnSignUp;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase init
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Init views
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        etDOB = findViewById(R.id.etDOB);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnSignUp = findViewById(R.id.btnSignUp);

        // Gender spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        // Date Picker for DOB
        etDOB.setOnClickListener(v -> showDatePicker());

        btnSignUp.setOnClickListener(v -> registerStudent());
    }

    private void registerStudent() {
        String name = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String dob = etDOB.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || mobile.isEmpty() || password.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Validation failed: one or more fields are empty.");
            return;
        }

        // Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    Log.d(TAG, "Firebase Auth Success. UID: " + uid);

                    // Create student map
                    Map<String, Object> student = new HashMap<>();
                    student.put("uid", uid);  // ✅ Store UID in Firestore
                    student.put("name", name);
                    student.put("email", email);
                    student.put("mobile", mobile);
                    student.put("gender", gender);
                    student.put("dob", dob);
                    student.put("enrolledCourses", new ArrayList<String>());
                    student.put("completedCourses", new ArrayList<String>());

                    // Save to Firestore
                    db.collection("students").document(uid)
                            .set(student)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Student Registered!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Student document created successfully in Firestore.");
                                Intent intent = new Intent(this, MainActivity.class);
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to save student info", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Firestore document write failed: ", e);
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Registration failed: " + e, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Firebase Auth failed: ", e);
                });
    }


    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String dob = dayOfMonth + "/" + (month + 1) + "/" + year;
                    etDOB.setText(dob);
                    Log.d(TAG, "DOB selected: " + dob);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }
}
