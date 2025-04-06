package com.example.gurukul;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class OrganizationDetailsActivity extends AppCompatActivity {

    private static final String TAG = "OrgDetails";
    private TextView titleTextView, descriptionTextView, ownerNameTextView;
    private ImageView organizationImageView;
    private RecyclerView coursesRecyclerView;
    private CourseAdapter courseAdapter;
    private List<Course> courseList;
    private ImageButton imgMenuTop;

    private FirebaseFirestore db;

    private String title, description, imageUrl, ownerId, organizationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_details);

        // Init views
        titleTextView = findViewById(R.id.tvCourseTitle);
        descriptionTextView = findViewById(R.id.tvCourseDesc);
        ownerNameTextView = findViewById(R.id.tvTeacherName);
        organizationImageView = findViewById(R.id.imgCourseThumbnail);
        coursesRecyclerView = findViewById(R.id.recyclerCourses);
        imgMenuTop = findViewById(R.id.imgMenuTop);
        imgMenuTop.setOnClickListener(this::showTopMenu);

        // Firestore instance
        db = FirebaseFirestore.getInstance();

        // Get data from intent
        Intent intent = getIntent();
        if (intent != null) {
            title = intent.getStringExtra("title");
            description = intent.getStringExtra("description");
            imageUrl = intent.getStringExtra("imageUrl");
            ownerId = intent.getStringExtra("ownerId");
            organizationId = intent.getStringExtra("organizationId");
        }

        // Set default values if data is missing
        if (title == null) title = "Organization";
        if (description == null) description = "No description available";

        // Set data
        titleTextView.setText(title);
        descriptionTextView.setText(description);

        // Load image only if URL is available
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).placeholder(R.drawable.profile_empty).into(organizationImageView);
        } else {
            organizationImageView.setImageResource(R.drawable.profile_empty);
        }

        // Load owner's name
        loadOwnerName();

        // Set up RecyclerView
        courseList = new ArrayList<>();
        courseAdapter = new CourseAdapter(this, courseList, new CourseAdapter.OnCourseActionListener() {
            @Override
            public void onCourseClicked(Course course) {
                if (course != null && course.getCourseId() != null) {
                    Intent intent = new Intent(OrganizationDetailsActivity.this, CourseDetails.class);
                    intent.putExtra("courseId", course.getCourseId());
                    startActivity(intent);
                } else {
                    Toast.makeText(OrganizationDetailsActivity.this, "Course details not available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDeleteCourse(Course course, int position) {
                // Optional: handle delete logic
            }
        });

        coursesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        coursesRecyclerView.setAdapter(courseAdapter);

        // Load courses for this organization
        loadCoursesForOrganization();
    }

    private void loadOwnerName() {
        // Check if ownerId is valid before attempting to load the owner
        if (ownerId == null || ownerId.isEmpty()) {
            ownerNameTextView.setText("Owner: Unknown");
            Log.w(TAG, "Owner ID is missing or empty");
            return;
        }

        db.collection("teachers")
                .document(ownerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String ownerName = documentSnapshot.getString("name");
                        if (ownerName != null && !ownerName.isEmpty()) {
                            ownerNameTextView.setText("Owner: " + ownerName);
                        } else {
                            ownerNameTextView.setText("Owner: Unknown");
                        }
                    } else {
                        ownerNameTextView.setText("Owner: Unknown");
                        Log.w(TAG, "Owner document does not exist");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load owner", e);
                    ownerNameTextView.setText("Owner: Unknown");
                });
    }

    private void loadCoursesForOrganization() {
        // Check if organizationId is valid
        if (organizationId == null || organizationId.isEmpty()) {
            Toast.makeText(this, "Organization ID is missing", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Organization ID is missing or empty");
            return;
        }

        db.collection("courses")
                .whereEqualTo("organizationId", organizationId)  // Filter courses by organization ID
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    courseList.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "No courses found for this organization", Toast.LENGTH_SHORT).show();
                    } else {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            if (doc == null) continue;

                            String courseId = doc.getId();
                            String courseName = doc.getString("courseName");
                            String courseDescription = doc.getString("description");
                            String imageURL = doc.getString("imageUrl");
                            String duration = doc.getString("duration");
                            String rating = doc.getString("rating");
                            String teacherId = doc.getString("teacherId");

                            // Use safe defaults for null values
                            if (courseName == null) courseName = "Unnamed Course";
                            if (courseDescription == null) courseDescription = "No description available";
                            if (duration == null) duration = "Unknown duration";
                            if (rating == null) rating = "Not rated";

                            Course course = new Course(courseId, courseName, courseDescription, imageURL, duration, rating);
                            course.setCourseId(courseId);
                            course.setInstructor("Loading...");
                            courseList.add(course);

                            int index = courseList.size() - 1;
                            loadInstructorName(teacherId, index);
                        }
                    }
                    courseAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load courses", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Course fetch error", e);
                });
    }

    private void loadInstructorName(String teacherId, int index) {
        if (index < 0 || index >= courseList.size()) {
            Log.w(TAG, "Invalid index for instructor name update: " + index);
            return;
        }

        if (teacherId == null || teacherId.isEmpty()) {
            courseList.get(index).setInstructor("Unknown");
            courseAdapter.notifyItemChanged(index);
            return;
        }

        db.collection("teachers")
                .document(teacherId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String instructorName = doc.getString("name");
                        if (instructorName != null && !instructorName.isEmpty()) {
                            courseList.get(index).setInstructor(instructorName);
                        } else {
                            courseList.get(index).setInstructor("Unknown");
                        }
                    } else {
                        courseList.get(index).setInstructor("Unknown");
                        Log.w(TAG, "Teacher document does not exist: " + teacherId);
                    }
                    courseAdapter.notifyItemChanged(index);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load instructor: " + teacherId, e);
                    courseList.get(index).setInstructor("Unknown");
                    courseAdapter.notifyItemChanged(index);
                });
    }
    private void showTopMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_organization_options, popup.getMenu());

        popup.setOnMenuItemClickListener(this::onMenuItemClick);
        popup.show();
    }

    private boolean onMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_add_course) {
            Toast.makeText(this, "Add Course selected", Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(this,AddCourseActivity.class);
            startActivity(intent);
            return true;

        } else if (itemId == R.id.action_edit_organization) {
            Toast.makeText(this, "Edit Organization selected", Toast.LENGTH_SHORT).show();
            return true;

        } else {
            return false;
        }
    }
}