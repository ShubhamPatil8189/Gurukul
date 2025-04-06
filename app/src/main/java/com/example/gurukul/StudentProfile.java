package com.example.gurukul;

import android.os.Bundle;
import android.view.MenuInflater;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentProfile extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private StudentTabAdapter tabAdapter;
    private ImageView imgStudentMenu;
    private ImageView imgStudentProfile;

    private TextView tvStudentName, tvStudentEmail, tvStudentGender, tvStudentDob, tvStudentMobile, tvStudentCourses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        // Link views
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        imgStudentMenu = findViewById(R.id.imgStudentMenu);
        imgStudentProfile = findViewById(R.id.imgStudentProfile);

        tvStudentName = findViewById(R.id.tvStudentName);
        tvStudentEmail = findViewById(R.id.tvStudentEmail);
        tvStudentGender = findViewById(R.id.tvStudentGender);
        tvStudentDob = findViewById(R.id.tvStudentDob);
        tvStudentMobile = findViewById(R.id.tvStudentMobile);
        tvStudentCourses = findViewById(R.id.tvStudentCourses);

        tabAdapter = new StudentTabAdapter(this);
        viewPager.setAdapter(tabAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Enrolled");
                    break;
                case 1:
                    tab.setText("Completed");
                    break;
                case 2:
                    tab.setText("Alerts");
                    break;
            }
        }).attach();

        fetchStudentData();
        setupMenu();
    }

    private void fetchStudentData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            FirebaseFirestore.getInstance()
                    .collection("students")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String email = documentSnapshot.getString("email");
                            String gender = documentSnapshot.getString("gender");
                            String dob = documentSnapshot.getString("dob");
                            String mobile = documentSnapshot.getString("mobile");
                            String courses = documentSnapshot.getString("courses");
                            String profileUrl = documentSnapshot.getString("profileImageUrl");

                            tvStudentName.setText(name != null ? name : "N/A");
                            tvStudentEmail.setText(email != null ? email : "N/A");
                            tvStudentGender.setText("Gender: " + (gender != null ? gender : "N/A"));
                            tvStudentDob.setText("DOB: " + (dob != null ? dob : "N/A"));
                            tvStudentMobile.setText("Mobile: " + (mobile != null ? mobile : "N/A"));
                            tvStudentCourses.setText("Courses: " + (courses != null ? courses : "N/A"));

                            if (profileUrl != null && !profileUrl.isEmpty()) {
                                Glide.with(this).load(profileUrl).into(imgStudentProfile);
                            }
                        } else {
                            Toast.makeText(this, "Student data not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load student data", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void setupMenu() {
        imgStudentMenu.setOnClickListener(view -> {
            androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(StudentProfile.this, view);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.student_profile_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_edit_profile) {
                    Toast.makeText(StudentProfile.this, "Edit Profile Clicked", Toast.LENGTH_SHORT).show();
                    // Intent intent = new Intent(StudentProfile.this, EditProfileActivity.class);
                    // startActivity(intent);
                    return true;
                } else if (id == R.id.menu_logout) {
                    showLogoutDialog();
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                    // FirebaseAuth.getInstance().signOut();
                    // startActivity(new Intent(this, LoginActivity.class));
                    // finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
