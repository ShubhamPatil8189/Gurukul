package com.example.gurukul;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EnrolledCourseAdapter extends RecyclerView.Adapter<EnrolledCourseAdapter.CourseViewHolder> {

    private Context context;
    private List<EnrolledCourse> courseList;

    public EnrolledCourseAdapter(Context context, List<EnrolledCourse> courseList) {
        this.context = context;
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_enrolled_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        EnrolledCourse course = courseList.get(position);

        holder.tvCourseName.setText(course.getCourseName());
        holder.tvInstructorName.setText("Instructor: " + course.getInstructorName());
        holder.tvDuration.setText("Duration: " + course.getDuration());
        holder.imgThumbnail.setImageResource(course.getImageResId());
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseName, tvInstructorName, tvDuration;
        ImageView imgThumbnail;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvInstructorName = itemView.findViewById(R.id.tvInstructorName);
            tvDuration = itemView.findViewById(R.id.tvCourseDuration);
            imgThumbnail = itemView.findViewById(R.id.imgCourseThumbnail);
        }
    }
}