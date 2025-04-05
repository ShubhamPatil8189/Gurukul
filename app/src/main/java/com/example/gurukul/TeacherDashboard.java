package com.example.gurukul;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.view.View;

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

import java.util.ArrayList;
import java.util.List;

public class TeacherDashboard extends AppCompatActivity {

    private RecyclerView organizationsRecyclerView;
    private OrganizationAdapter organizationAdapter;
    private LinearLayoutManager layoutManager;

    // Made static for access from adapter
    private static Handler autoScrollHandler;
    private static Runnable autoScrollRunnable;

    private int currentPosition = 0;

    // For page indicators
    private LinearLayout pageIndicatorsLayout;
    private ImageView[] indicators;
    private int scrollSpeed = 1500; // 1.5s + 0.5s delay

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

        // Initialize the page indicators layout
        pageIndicatorsLayout = findViewById(R.id.pageIndicatorsLayout);

        setupOrganizationsRecyclerView();
        setupPageIndicators();
        setupAutoScroll();
    }

    private void smoothScrollToPositionWithSpeed(int position, int durationMs) {
        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(this) {
            @Override
            protected int calculateTimeForScrolling(int dx) {
                // Slight slowdown
                return Math.min(durationMs, super.calculateTimeForScrolling(dx));
            }
        };
        smoothScroller.setTargetPosition(position);
        layoutManager.startSmoothScroll(smoothScroller);
    }

    private void setupOrganizationsRecyclerView() {
        organizationsRecyclerView = findViewById(R.id.organizationsRecyclerView);

        // Set up horizontal layout manager
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        organizationsRecyclerView.setLayoutManager(layoutManager);

        // Apply snap helper for paging effect
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(organizationsRecyclerView);

        // Set adapter with sample data
        List<Organization> organizations = getSampleOrganizations();
        organizationAdapter = new OrganizationAdapter(organizations);
        organizationsRecyclerView.setAdapter(organizationAdapter);

        // Add scroll listener to update page indicators
        organizationsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Update the current position when scrolling stops
                    currentPosition = layoutManager.findFirstVisibleItemPosition();
                    updatePageIndicators(currentPosition);
                }
            }
        });
    }

    private void setupPageIndicators() {
        List<Organization> organizations = getSampleOrganizations();
        indicators = new ImageView[organizations.size()];

        // Clear any existing indicators
        pageIndicatorsLayout.removeAllViews();

        // Create indicators
        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(this);

            // Set indicator size and margins
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            indicators[i].setLayoutParams(params);

            // Set inactive dot image initially
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    this, R.drawable.indicator_inactive));

            // Add to layout
            pageIndicatorsLayout.addView(indicators[i]);
        }

        // Set the first indicator as active
        if (indicators.length > 0) {
            indicators[0].setImageDrawable(ContextCompat.getDrawable(
                    this, R.drawable.indicator_active));
        }
    }

    private void updatePageIndicators(int position) {
        // Reset all indicators to inactive
        for (int i = 0; i < indicators.length; i++) {
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    this, R.drawable.indicator_inactive));
        }

        // Set the current position indicator to active
        if (position >= 0 && position < indicators.length) {
            indicators[position].setImageDrawable(ContextCompat.getDrawable(
                    this, R.drawable.indicator_active));
        }
    }

    private void setupAutoScroll() {
        autoScrollHandler = new Handler(Looper.getMainLooper());
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (organizationAdapter.getItemCount() > 0) {
                    currentPosition = (currentPosition + 1) % organizationAdapter.getItemCount();

                    // Smooth scroll with slight slowdown
                    smoothScrollToPositionWithSpeed(currentPosition, 1000); // slight delay

                    // Sync indicator with scroll
                    autoScrollHandler.postDelayed(() -> {
                        updatePageIndicators(currentPosition);
                    }, 700);

                    // Schedule the next scroll after full duration
                    autoScrollHandler.postDelayed(this, 2000);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start auto-scrolling when activity is resumed
        autoScrollHandler.postDelayed(autoScrollRunnable, scrollSpeed);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop auto-scrolling when activity is paused
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
    }

    private List<Organization> getSampleOrganizations() {
        // Create sample data
        List<Organization> organizations = new ArrayList<>();
        organizations.add(new Organization("ABC University", "Educational Institution", R.drawable.ic_organization));
        organizations.add(new Organization("XYZ Research Center", "Research Institute", R.drawable.ic_organization));
        organizations.add(new Organization("Tech Academy", "Training Institute", R.drawable.ic_organization));
        organizations.add(new Organization("Science Foundation", "Non-profit Organization", R.drawable.ic_organization));
        organizations.add(new Organization("Learning Hub", "Educational Platform", R.drawable.ic_organization));
        return organizations;
    }

    // 👇 ADDED METHOD TO PAUSE AUTO SCROLLING
    public static void pauseAutoScroll() {
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }
}