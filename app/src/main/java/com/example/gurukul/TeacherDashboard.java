package com.example.gurukul;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TeacherDashboard extends AppCompatActivity {

    private RecyclerView organizationsRecyclerView;
    private OrganizationAdapter organizationAdapter;
    private LinearLayoutManager layoutManager;

    private static Handler autoScrollHandler;
    private static Runnable autoScrollRunnable;

    private int currentPosition = 0;

    private LinearLayout pageIndicatorsLayout;
    private ImageView[] indicators;
    private int scrollSpeed = 1500;

    Button btnAddOrg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pageIndicatorsLayout = findViewById(R.id.pageIndicatorsLayout);
        organizationsRecyclerView = findViewById(R.id.organizationsRecyclerView);
        btnAddOrg=findViewById(R.id.btnAddOrg);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        organizationsRecyclerView.setLayoutManager(layoutManager);

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(organizationsRecyclerView);

        fetchOrganizationsFromFirestore();
        setupAutoScroll();

        btnAddOrg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(TeacherDashboard.this,AddOrganizationActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fetchOrganizationsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("organizations")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Organization> organizations = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String id = doc.getId();
                        String title = doc.getString("title"); // ✅ Corrected from 'name' to 'title'
                        String description = doc.getString("description");
                        String imageUrl = doc.getString("imageUrl"); // ✅ For Glide
                        organizations.add(new Organization(id, title, description, imageUrl));
                    }

                    organizationAdapter = new OrganizationAdapter(organizations);
                    organizationsRecyclerView.setAdapter(organizationAdapter);
                    setupPageIndicators(organizations.size());

                    organizationsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                currentPosition = layoutManager.findFirstVisibleItemPosition();
                                updatePageIndicators(currentPosition);
                            }
                        }
                    });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch organizations: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


    private void setupPageIndicators(int count) {
        indicators = new ImageView[count];
        pageIndicatorsLayout.removeAllViews();

        for (int i = 0; i < count; i++) {
            indicators[i] = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            indicators[i].setLayoutParams(params);
            indicators[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.indicator_inactive));
            pageIndicatorsLayout.addView(indicators[i]);
        }

        if (count > 0) {
            indicators[0].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.indicator_active));
        }
    }

    private void updatePageIndicators(int position) {
        for (int i = 0; i < indicators.length; i++) {
            indicators[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.indicator_inactive));
        }
        if (position >= 0 && position < indicators.length) {
            indicators[position].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.indicator_active));
        }
    }

    private void smoothScrollToPositionWithSpeed(int position, int durationMs) {
        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(this) {
            @Override
            protected int calculateTimeForScrolling(int dx) {
                return Math.min(durationMs, super.calculateTimeForScrolling(dx));
            }
        };
        smoothScroller.setTargetPosition(position);
        layoutManager.startSmoothScroll(smoothScroller);
    }

    private void setupAutoScroll() {
        autoScrollHandler = new Handler(Looper.getMainLooper());
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (organizationAdapter != null && organizationAdapter.getItemCount() > 0) {
                    currentPosition = (currentPosition + 1) % organizationAdapter.getItemCount();
                    smoothScrollToPositionWithSpeed(currentPosition, 1000);
                    autoScrollHandler.postDelayed(() -> updatePageIndicators(currentPosition), 700);
                    autoScrollHandler.postDelayed(this, 2000);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        autoScrollHandler.postDelayed(autoScrollRunnable, scrollSpeed);
    }

    @Override
    protected void onPause() {
        super.onPause();
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
    }

    public static void pauseAutoScroll() {
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    public static void resumeAutoScroll() {
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.postDelayed(autoScrollRunnable, 2000);
        }
    }
}
