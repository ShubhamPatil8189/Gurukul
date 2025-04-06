package com.example.gurukul;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
        holder.bind(organization);
    }

    @Override
    public int getItemCount() {
        return organizations.size();
    }

    class OrganizationViewHolder extends RecyclerView.ViewHolder {
        ImageView organizationImage, menuIcon;
        TextView organizationName, organizationDescription;

        public OrganizationViewHolder(@NonNull View itemView) {
            super(itemView);
            organizationImage = itemView.findViewById(R.id.organizationImage);
            organizationName = itemView.findViewById(R.id.organizationName);
            organizationDescription = itemView.findViewById(R.id.organizationDescription);
            menuIcon = itemView.findViewById(R.id.menuIcon);

            // Handle item click to open OrganizationDetailsActivity
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Context context = itemView.getContext();
                    Organization organization = organizations.get(position);

                    // Pause auto-scrolling when navigating away
                    TeacherDashboard.pauseAutoScroll();

                    // Start OrganizationDetails activity with all required data
                    Intent intent = new Intent(context, OrganizationDetailsActivity.class);
                    intent.putExtra("organizationId", organization.getUid());
                    intent.putExtra("title", organization.getTitle());
                    intent.putExtra("description", organization.getDescription());
                    intent.putExtra("imageUrl", organization.getImageUrl());
                    intent.putExtra("ownerId", organization.getOwnerId());
                    context.startActivity(intent);
                }
            });

            // Handle 3-dot menu click
            menuIcon.setOnClickListener(view -> {
                TeacherDashboard.pauseAutoScroll();

                PopupMenu popupMenu = new PopupMenu(view.getContext(), menuIcon);
                popupMenu.inflate(R.menu.menu23);

                popupMenu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.action_add) {
                        Toast.makeText(view.getContext(), "delete org", Toast.LENGTH_SHORT).show();
                        return true;
                    } else if (itemId == R.id.action_edit) {
                        // Show dialog to add teacher email
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setTitle("Add Teacher");

                        final EditText input = new EditText(view.getContext());
                        input.setHint("Enter teacher email");
                        builder.setView(input);

                        builder.setPositiveButton("Add", (dialog, which) -> {
                            String email = input.getText().toString().trim();
                            if (email.isEmpty()) {
                                Toast.makeText(view.getContext(), "Email is required", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                Organization organization = organizations.get(position);
                                String organizationId = organization.getUid();

                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("teachers")
                                        .whereEqualTo("email", email)
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                            if (!queryDocumentSnapshots.isEmpty()) {
                                                String teacherUid = queryDocumentSnapshots.getDocuments().get(0).getId();

                                                db.collection("organizations").document(organizationId)
                                                        .update("teachers", FieldValue.arrayUnion(teacherUid))
                                                        .addOnSuccessListener(aVoid -> {
                                                            Toast.makeText(view.getContext(), "Teacher added to organization", Toast.LENGTH_SHORT).show();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(view.getContext(), "Failed to add teacher", Toast.LENGTH_SHORT).show();
                                                        });
                                            } else {
                                                Toast.makeText(view.getContext(), "No teacher found with that email", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(view.getContext(), "Error fetching teacher", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        });

                        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                        builder.show();

                        return true;
                    }
                    return false;
                });

                popupMenu.setOnDismissListener(menu -> {
                    TeacherDashboard.resumeAutoScroll();
                });

                popupMenu.show();
            });
        }

        public void bind(Organization organization) {
            organizationName.setText(organization.getTitle());
            organizationDescription.setText(organization.getDescription());

            Glide.with(itemView.getContext())
                    .load(organization.getImageUrl())
                    .placeholder(R.drawable.profile_empty)
                    .error(R.drawable.profile_empty)
                    .into(organizationImage);
        }
    }
}
