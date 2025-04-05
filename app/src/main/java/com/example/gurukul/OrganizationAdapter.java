package com.example.gurukul;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class OrganizationAdapter extends RecyclerView.Adapter<OrganizationAdapter.OrganizationViewHolder> {

    private List<Organization> organizations;

    public OrganizationAdapter(List<Organization> organizations) {
        this.organizations = organizations;
    }

    @NonNull
    @Override
    public OrganizationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_organization, parent, false);
        return new OrganizationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrganizationViewHolder holder, int position) {
        Organization organization = organizations.get(position);

        // ✅ Set title and description
        holder.organizationName.setText(organization.getTitle());
        holder.organizationDescription.setText(organization.getDescription());

        // ✅ Load image from URL using Glide
        Glide.with(holder.organizationImage.getContext())
                .load(organization.getImageUrl())
                .placeholder(R.drawable.ic_organization)
                .into(holder.organizationImage);


        Log.d("mytag",organization.getImageUrl());

        // 3-dot menu handling
        holder.menuIcon.setOnClickListener(view -> {
            TeacherDashboard.pauseAutoScroll();

            PopupMenu popupMenu = new PopupMenu(view.getContext(), holder.menuIcon);
            popupMenu.inflate(R.menu.menu_course);

            popupMenu.setOnDismissListener(menu -> TeacherDashboard.resumeAutoScroll());

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_add_course) {
                    Toast.makeText(view.getContext(), "Add Course clicked", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.menu_edit_org) {
                    showAddTeacherDialog(view.getContext(), organization.getUid());
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return organizations.size();
    }

    // ✅ Show dialog to enter teacher email
    private void showAddTeacherDialog(Context context, String organizationId) {
        EditText input = new EditText(context);
        input.setHint("Enter Teacher Email");
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        new AlertDialog.Builder(context)
                .setTitle("Add Teacher")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String email = input.getText().toString().trim();
                    if (!email.isEmpty()) {
                        addTeacherToOrganization(context, email, organizationId);
                    } else {
                        Toast.makeText(context, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ✅ Add teacher UID to Firestore
    private void addTeacherToOrganization(Context context, String email, String organizationId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("teachers")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String teacherUid = querySnapshot.getDocuments().get(0).getId();

                        db.collection("organizations")
                                .document(organizationId)
                                .update("teachers", FieldValue.arrayUnion(teacherUid))
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(context, "Teacher added successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to update organization: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(context, "Teacher not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error fetching teacher: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ✅ ViewHolder for each organization card
    static class OrganizationViewHolder extends RecyclerView.ViewHolder {
        ImageView organizationImage;
        TextView organizationName;
        TextView organizationDescription;
        ImageView menuIcon;

        public OrganizationViewHolder(@NonNull View itemView) {
            super(itemView);
            organizationImage = itemView.findViewById(R.id.organizationImage);
            organizationName = itemView.findViewById(R.id.organizationName);
            organizationDescription = itemView.findViewById(R.id.organizationDescription);
            menuIcon = itemView.findViewById(R.id.menuIcon);
        }
    }
}
