package com.example.gurukul;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class CourseDetails extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private CoursePagerAdapter adapter;

    private ImageView imgCourseMenu, imgCourseThumbnail;
    private TextView tvCourseTitle, tvCourseDesc, tvInstructor;
    private RatingBar courseRating;

    private String courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);

        // Get course ID from intent
        courseId = getIntent().getStringExtra("courseId");
        if (courseId == null) {
            Toast.makeText(this, "Course ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        imgCourseMenu = findViewById(R.id.imgCourseMenu);
        imgCourseThumbnail = findViewById(R.id.imgCourseThumbnail);
        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        tvCourseDesc = findViewById(R.id.tvCourseDesc);
        tvInstructor = findViewById(R.id.tvTeacherName);
        courseRating = findViewById(R.id.courseRating);

        // Set up pager adapter
        adapter = new CoursePagerAdapter(this);
        adapter.setCourseId(courseId); // Set courseId to adapter
        viewPager.setAdapter(adapter);

        // Tabs
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Videos"); break;
                case 1: tab.setText("Students"); break;
                case 2: tab.setText("Alerts"); break;
                case 3: tab.setText("Notes"); break;
            }
        }).attach();

        // Load course data
        fetchCourseDetails();

        // Set up menu actions
        setupCourseMenu();
    }

    private void fetchCourseDetails() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("courses").document(courseId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("courseName");
                        String desc = documentSnapshot.getString("description");
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        String teacherId = documentSnapshot.getString("teacherId");
                        String rating = "0"; // Default rating

                        tvCourseTitle.setText(title);
                        tvCourseDesc.setText(desc);

                        try {
                            courseRating.setRating(Float.parseFloat(rating));
                        } catch (Exception e) {
                            courseRating.setRating(0);
                        }

                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.profile_empty)
                                .error(R.drawable.profile_empty)
                                .into(imgCourseThumbnail);

                        fetchTeacherName(teacherId);

                    } else {
                        Toast.makeText(this, "Course not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load course", Toast.LENGTH_SHORT).show();
                    Log.e("CourseDetails", "Error fetching course: ", e);
                });
    }

    private void fetchTeacherName(String teacherId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("teachers").document(teacherId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String teacherName = documentSnapshot.getString("name");
                        tvInstructor.setText("Instructor: " + (teacherName != null ? teacherName : "Unknown"));
                    } else {
                        tvInstructor.setText("Instructor: Unknown");
                    }
                })
                .addOnFailureListener(e -> {
                    tvInstructor.setText("Instructor: Unknown");
                    Log.e("CourseDetails", "Error fetching teacher name: ", e);
                });
    }

    private void setupCourseMenu() {
        imgCourseMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(CourseDetails.this, v);
            popupMenu.getMenuInflater().inflate(R.menu.menu_course_options1, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.action_edit_course) {
                    Toast.makeText(this, "Edit Course clicked", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.action_add_video) {
                    Intent intent = new Intent(CourseDetails.this, AddVideoActivity.class);
                    intent.putExtra("courseId", courseId);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_add_student) {
                    showAddStudentDialog();
                    return true;
                } else {
                    return false;
                }
            });

            popupMenu.show();
        });
    }

    private void showAddStudentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Student");

        final EditText input = new EditText(this);
        input.setHint("Enter student email");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            enrollStudentByEmail(email);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void enrollStudentByEmail(String email) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Enrolling student...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("students")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot studentDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String studentId = studentDoc.getId();

                        db.collection("students").document(studentId)
                                .update("enrolledCourses", FieldValue.arrayUnion(courseId))
                                .addOnSuccessListener(aVoid -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(this, "Student enrolled successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(this, "Failed to enroll student", Toast.LENGTH_SHORT).show();
                                    Log.e("EnrollStudent", "Update failed: ", e);
                                });

                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Student not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error fetching student", Toast.LENGTH_SHORT).show();
                    Log.e("EnrollStudent", "Fetch failed: ", e);
                });
    }
}
