package com.example.gurukul;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private final Context context;
    private final List<Course> courses;
    private final OnCourseActionListener listener;

    public CourseAdapter(Context context, List<Course> courses, OnCourseActionListener listener) {
        this.context = context;
        this.courses = courses;
        this.listener = listener;
    }

    public interface OnCourseActionListener {
        void onCourseClicked(Course course);
        void onDeleteCourse(Course course, int position);
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.bind(course, position);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CourseDetails.class);
            intent.putExtra("courseId", course.getCourseId()); // pass courseId
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    class CourseViewHolder extends RecyclerView.ViewHolder {
        private final ImageView courseImage;
        private final TextView courseTitle;
        private final TextView courseDescription;
        private final TextView instructorName;
        private final RatingBar courseRating;
        private final ImageView menuIcon;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            courseImage = itemView.findViewById(R.id.courseImage);
            courseTitle = itemView.findViewById(R.id.courseTitle);
            courseDescription = itemView.findViewById(R.id.courseDescription);
            instructorName = itemView.findViewById(R.id.instructorName);
            courseRating = itemView.findViewById(R.id.courseRating);
            menuIcon = itemView.findViewById(R.id.menuIcon);
        }

        public void bind(Course course, int position) {
            Log.d("CourseAdapter", "Course Name: " + course.getCourseName());
            Log.d("CourseAdapter", "Image URL: " + course.getImageUrl());

            courseTitle.setText(course.getCourseName());
            courseDescription.setText(course.getDescription());
            instructorName.setText("Instructor: " + course.getInstructor());

            try {
                courseRating.setRating(Float.parseFloat(course.getRating()));
            } catch (Exception e) {
                courseRating.setRating(0f);
            }

            Glide.with(context)
                    .load(course.getImageUrl())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.profile_empty)
                    .error(R.drawable.profile_empty)
                    .into(courseImage);

            menuIcon.setOnClickListener(view -> showPopupMenu(view, course, position));
        }

        private void showPopupMenu(View view, Course course, int position) {
            PopupMenu popupMenu = new PopupMenu(context, view);
            popupMenu.getMenuInflater().inflate(R.menu.menu_organization_options, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_delete_course) {
                    if (listener != null) {
                        listener.onDeleteCourse(course, position);
                    }
                    return true;
                }
                return false;
            });

            popupMenu.show();
        }
    }
}
