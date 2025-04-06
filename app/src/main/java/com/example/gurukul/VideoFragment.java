package com.example.gurukul;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class VideoFragment extends Fragment {

    private RecyclerView recyclerView;
    private VideoAdapter adapter;
    private List<VideoModel> videoList;
    private String courseId;  // Variable to store courseId

    public VideoFragment() {}

    // Method to set courseId from outside the fragment
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for the fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        // Get courseId from Fragment arguments
        if (getArguments() != null) {
            courseId = getArguments().getString("courseId");
            Log.d("VideoFragment", "Received courseId: " + courseId);  // Log courseId for debugging
        } else {
            Log.d("VideoFragment", "No courseId received");
            // Don't show toast for null courseId here - we'll handle that in fetchVideosData
        }

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerStudents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the video list
        videoList = new ArrayList<>();

        // Set up adapter
        adapter = new VideoAdapter(videoList);
        recyclerView.setAdapter(adapter);

        // Fetch video data from Firestore
        fetchVideosData();

        return view;
    }

    private void fetchVideosData() {
        // Check if courseId is available
        if (courseId == null || courseId.isEmpty()) {
            Toast.makeText(getContext(), "Course ID not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch videos from Firestore filtered by courseId
        db.collection("videos")
                .whereEqualTo("courseId", courseId)  // Filter by courseId
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        // Clear the current video list
                        videoList.clear();

                        // Loop through the documents in the snapshot
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String title = document.getString("title");
                            String description = document.getString("description");
                            String thumbnailUrl = document.getString("thumbnailUrl");
                            String videoUrl = document.getString("videoUrl");

                            // Create VideoModel instance and add to the list
                            VideoModel video = new VideoModel(title, description, thumbnailUrl, videoUrl, courseId);
                            videoList.add(video);
                        }

                        // Notify adapter of data change
                        adapter.notifyDataSetChanged();

                        if (videoList.isEmpty()) {
                            Toast.makeText(getContext(), "No videos found for this course", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "No videos found for this course", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("VideoFragment", "Error fetching videos: ", e);
                    Toast.makeText(getContext(), "Error fetching videos", Toast.LENGTH_SHORT).show();
                });
    }
}