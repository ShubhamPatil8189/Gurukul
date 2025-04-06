package com.example.gurukul;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gurukul.Course1;
import com.example.gurukul.R;

import java.util.List;

public class CourseAdapter1 extends RecyclerView.Adapter<CourseAdapter1.CourseViewHolder> {
    private Context context;
    private List<Course1> courseList;

    public CourseAdapter1(Context context, List<Course1> courseList) {
        this.context = context;
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course1 course = courseList.get(position);
        holder.tvCourseName.setText(course.getCourseName());
        holder.tvInstructorName.setText("Instructor: " + course.getInstructorName());
        holder.tvCourseDuration.setText("Duration: " + course.getDuration());
        holder.tvProgressText.setText("Progress: " + course.getProgress() + "%");
        holder.progressBar.setProgress(course.getProgress());
        holder.imgThumbnail.setImageResource(course.getImageResId());
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseName, tvInstructorName, tvCourseDuration, tvProgressText;
        ImageView imgThumbnail;
        ProgressBar progressBar;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvInstructorName = itemView.findViewById(R.id.tvInstructorName);
            tvCourseDuration = itemView.findViewById(R.id.tvCourseDuration);
            tvProgressText = itemView.findViewById(R.id.tvProgressText);
            progressBar = itemView.findViewById(R.id.progressBar);
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
        }
    }
}