package com.example.gurukul;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.UUID;

public class AddVideoActivity extends AppCompatActivity {

    private TextInputEditText titleInput, descriptionInput;
    private TextView uploadThumbnailTv, uploadVideoTv;
    private ImageView thumbnailImageView;
    private PlayerView videoPlayerView;
    private MaterialButton addVideoBtn;

    private Uri thumbnailUri, videoUri;
    private ExoPlayer exoPlayer;

    private static final int THUMBNAIL_REQUEST_CODE = 100;
    private static final int VIDEO_REQUEST_CODE = 200;

    private ProgressDialog progressDialog;
    private StorageReference storageRef;
    private FirebaseFirestore firestore;

    private String courseId; // <-- holds courseId passed from intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);

        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        uploadThumbnailTv = findViewById(R.id.uploadThumbnailTv);
        uploadVideoTv = findViewById(R.id.uploadVideoTv);
        thumbnailImageView = findViewById(R.id.thumbnailImageView);
        videoPlayerView = findViewById(R.id.videoPlayerView);
        addVideoBtn = findViewById(R.id.addVideoBtn);

        progressDialog = new ProgressDialog(this);
        storageRef = FirebaseStorage.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        // Get courseId from intent (or fallback to dummy)
        courseId = getIntent().getStringExtra("courseId");
        if (courseId == null) {
            courseId = "dummyCourseId"; // fallback if not passed
        }

        uploadThumbnailTv.setOnClickListener(v -> pickImage());
        uploadVideoTv.setOnClickListener(v -> pickVideo());

        addVideoBtn.setOnClickListener(v -> {
            if (validateInputs()) {
                uploadData();
            }
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, THUMBNAIL_REQUEST_CODE);
    }

    private void pickVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent, VIDEO_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == THUMBNAIL_REQUEST_CODE) {
                thumbnailUri = data.getData();
                thumbnailImageView.setImageURI(thumbnailUri);
                thumbnailImageView.setVisibility(View.VISIBLE);
                uploadThumbnailTv.setText("Re-upload Thumbnail");
            } else if (requestCode == VIDEO_REQUEST_CODE) {
                videoUri = data.getData();
                setupExoPlayer(videoUri);
                videoPlayerView.setVisibility(View.VISIBLE);
                uploadVideoTv.setText("Re-upload Video");
            }
        }
    }

    private void setupExoPlayer(Uri uri) {
        if (exoPlayer != null) {
            exoPlayer.release();
        }

        exoPlayer = new ExoPlayer.Builder(this).build();
        videoPlayerView.setPlayer(exoPlayer);

        MediaItem mediaItem = MediaItem.fromUri(uri);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.pause(); // Initially paused
    }

    private boolean validateInputs() {
        String title = titleInput.getText().toString().trim();
        String desc = descriptionInput.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Title and Description are required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (thumbnailUri == null || videoUri == null) {
            Toast.makeText(this, "Please upload both thumbnail and video", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Optional file type validation
        if (!getContentResolver().getType(videoUri).startsWith("video/")) {
            Toast.makeText(this, "Invalid video format", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void uploadData() {
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String ownerId = user.getUid();
        String videoId = UUID.randomUUID().toString();

        // Upload thumbnail
        StorageReference thumbRef = storageRef.child("thumbnails/" + videoId + ".jpg");
        thumbRef.putFile(thumbnailUri).addOnSuccessListener(taskSnapshot ->
                thumbRef.getDownloadUrl().addOnSuccessListener(thumbUri -> {

                    // Upload video
                    StorageReference videoRef = storageRef.child("videos/" + videoId + ".mp4");
                    videoRef.putFile(videoUri).addOnSuccessListener(taskSnapshot1 ->
                            videoRef.getDownloadUrl().addOnSuccessListener(videoDownloadUri -> {

                                // Save to Firestore collection
                                HashMap<String, Object> videoData = new HashMap<>();
                                videoData.put("videoId", videoId);
                                videoData.put("title", titleInput.getText().toString().trim());
                                videoData.put("description", descriptionInput.getText().toString().trim());
                                videoData.put("thumbnailUrl", thumbUri.toString());
                                videoData.put("videoUrl", videoDownloadUri.toString());
                                videoData.put("ownerId", ownerId);
                                videoData.put("courseId", courseId);  // Add courseId here
                                videoData.put("timestamp", System.currentTimeMillis());

                                firestore.collection("videos")
                                        .document(videoId)
                                        .set(videoData)
                                        .addOnSuccessListener(unused -> {
                                            progressDialog.dismiss();
                                            Toast.makeText(this, "Video uploaded successfully", Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            progressDialog.dismiss();
                                            Toast.makeText(this, "Failed to save video info", Toast.LENGTH_SHORT).show();
                                        });

                            }).addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(this, "Failed to upload video", Toast.LENGTH_SHORT).show();
                            }));

                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to upload thumbnail", Toast.LENGTH_SHORT).show();
                }));
    }

    @Override
    protected void onDestroy() {
        if (exoPlayer != null) {
            exoPlayer.release();
        }
        super.onDestroy();
    }
}
