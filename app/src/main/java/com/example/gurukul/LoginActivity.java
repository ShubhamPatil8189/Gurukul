package com.example.gurukul;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Spinner spinnerRole;
    private Button btnSignIn;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // change if your XML file name is different

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnSignIn = findViewById(R.id.btnSignIn);

        // Firebase setup
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Spinner values (make sure to populate it)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.roles_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        btnSignIn.setOnClickListener(view -> loginUser());
    }

    private void loginUser() {
        String email = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String selectedRole = spinnerRole.getSelectedItem().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedRole.equals("Select Role")) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user == null) {
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String uid = user.getUid();
                        String collectionName = selectedRole.equalsIgnoreCase("Student") ? "students" : "teachers";

                        db.collection(collectionName).document(uid).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        Toast.makeText(this, "Login successful as " + selectedRole, Toast.LENGTH_SHORT).show();
                                        Intent intent;
                                        if (selectedRole != null) {
                                            if (selectedRole.equalsIgnoreCase("Student")) {
                                                Toast.makeText(this, "Student", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(this, "Teacher", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                    } else {
                                        Toast.makeText(this, selectedRole + " not found", Toast.LENGTH_LONG).show();
                                        auth.signOut();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });

                    } else {
                        Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
