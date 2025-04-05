package com.example.gurukul;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.*;

public class AddCourseActivity extends AppCompatActivity {

    private TextInputEditText etCourseName, etPrice, etDuration, etDescription, etTeacherEmail;
    private TextView tvUploadImage;
    private ImageView ivCourseImage;
    private MaterialButton btnAddCourse;

    private Uri selectedImageUri;
    private ProgressDialog progressDialog;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    Glide.with(this).load(selectedImageUri).into(ivCourseImage);
                    ivCourseImage.setVisibility(View.VISIBLE);
                    tvUploadImage.setText("Re-upload Course Image");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        etCourseName = findViewById(R.id.etCourseName);
        etPrice = findViewById(R.id.etPrice);
        etDuration = findViewById(R.id.etDuration);
        etDescription = findViewById(R.id.etDescription);
        etTeacherEmail = findViewById(R.id.etTeacherEmail);
        tvUploadImage = findViewById(R.id.tvUploadImage);
        ivCourseImage = findViewById(R.id.ivCourseImage);
        btnAddCourse = findViewById(R.id.btnAddCourse);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        tvUploadImage.setOnClickListener(v -> openImagePicker());

        btnAddCourse.setOnClickListener(v -> uploadCourse());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadCourse() {
        String courseName = etCourseName.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String duration = etDuration.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String teacherEmail = etTeacherEmail.getText().toString().trim();

        if (courseName.isEmpty() || price.isEmpty() || duration.isEmpty() ||
                description.isEmpty() || teacherEmail.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Please fill all fields and upload image", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        String imagePath = "course_images/" + UUID.randomUUID().toString() + ".jpg";
        storage.getReference(imagePath).putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storage.getReference(imagePath).getDownloadUrl()
                                .addOnSuccessListener(imageUri -> {
                                    findTeacherUid(teacherEmail, teacherUid -> {
                                        if (teacherUid == null) {
                                            progressDialog.dismiss();
                                            Toast.makeText(this, "Teacher not found", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        findOrganizationId(teacherUid, orgId -> {
                                            if (orgId == null) {
                                                progressDialog.dismiss();
                                                Toast.makeText(this, "Organization not found for this teacher", Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            Map<String, Object> courseData = new HashMap<>();
                                            courseData.put("courseName", courseName);
                                            courseData.put("price", price);
                                            courseData.put("duration", duration);
                                            courseData.put("description", description);
                                            courseData.put("teacherId", teacherUid);
                                            courseData.put("organizationId", orgId);
                                            courseData.put("ownerId", auth.getUid());
                                            courseData.put("imageUrl", imageUri.toString());

                                            db.collection("courses").add(courseData)
                                                    .addOnSuccessListener(docRef -> {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(this, "Course added successfully", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        });
                                    });
                                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void findTeacherUid(String email, OnTeacherUidFetched callback) {
        db.collection("teachers")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        String uid = snapshot.getDocuments().get(0).getId();
                        callback.onFetched(uid);
                    } else {
                        callback.onFetched(null);
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error fetching teacher: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void findOrganizationId(String teacherUid, OnOrganizationIdFetched callback) {
        db.collection("organizations")
                .whereArrayContains("teachers", teacherUid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        String orgId = snapshot.getDocuments().get(0).getId();
                        callback.onFetched(orgId);
                    } else {
                        callback.onFetched(null);
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error fetching organization: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    interface OnTeacherUidFetched {
        void onFetched(String teacherUid);
    }

    interface OnOrganizationIdFetched {
        void onFetched(String orgId);
    }
}
