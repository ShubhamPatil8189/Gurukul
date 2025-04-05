package com.example.gurukul;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.*;

public class AddOrganizationActivity extends AppCompatActivity {

    private TextInputEditText orgNameInput, descriptionInput, teacherEmail1Input, teacherEmail2Input;
    private ImageView orgImageView;
    private TextView imageUploadBtn;
    private Uri selectedImageUri;
    private Button addOrgBtn;
    private ProgressDialog progressDialog;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    orgImageView.setImageURI(selectedImageUri);
                    orgImageView.setVisibility(View.VISIBLE);
                    imageUploadBtn.setText("Re-upload Image");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_organization);

        orgNameInput = findViewById(R.id.orgNameInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        teacherEmail1Input = findViewById(R.id.teacherEmail1Input);
        teacherEmail2Input = findViewById(R.id.teacherEmail2Input);
        orgImageView = findViewById(R.id.orgImageView);
        imageUploadBtn = findViewById(R.id.imageUploadBtn);
        addOrgBtn = findViewById(R.id.addOrgBtn);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        imageUploadBtn.setOnClickListener(v -> openImagePicker());
        addOrgBtn.setOnClickListener(v -> uploadOrganization());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadOrganization() {
        String name = orgNameInput.getText().toString().trim();
        String desc = descriptionInput.getText().toString().trim();
        String email1 = teacherEmail1Input.getText().toString().trim();
        String email2 = teacherEmail2Input.getText().toString().trim();

        if (name.isEmpty() || desc.isEmpty() || email1.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        String imagePath = "organization_images/" + UUID.randomUUID() + ".jpg";
        storage.getReference(imagePath).putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> storage.getReference(imagePath).getDownloadUrl()
                        .addOnSuccessListener(imageUri -> {
                            List<String> emailList = new ArrayList<>();
                            emailList.add(email1);
                            if (!email2.isEmpty()) emailList.add(email2);

                            fetchTeacherUids(emailList, uids -> {
                                Map<String, Object> orgData = new HashMap<>();
                                orgData.put("title", name);
                                orgData.put("description", desc);
                                orgData.put("imageUrl", imageUri.toString());
                                orgData.put("ownerId", auth.getUid());
                                orgData.put("teachers", uids);

                                db.collection("organizations").add(orgData)
                                        .addOnSuccessListener(docRef -> {
                                            progressDialog.dismiss();
                                            Toast.makeText(this, "Organization added successfully", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            progressDialog.dismiss();
                                            Toast.makeText(this, "Failed to save organization: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            });
                        }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchTeacherUids(List<String> emails, OnUidsFetched callback) {
        List<String> uids = new ArrayList<>();
        db.collection("teachers")
                .whereIn("email", emails)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (var doc : snapshot.getDocuments()) {
                        uids.add(doc.getId());
                    }
                    callback.onFetched(uids);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to fetch teachers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    interface OnUidsFetched {
        void onFetched(List<String> uids);
    }
}
