package com.example.gurukul;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
        holder.organizationName.setText(organization.getName());
        holder.organizationDescription.setText(organization.getDescription());
        holder.organizationImage.setImageResource(organization.getImageResId());

        // Handle 3-dot menu click
        holder.menuIcon.setOnClickListener(view -> {

            // Pause auto-scroll when 3-dot menu is clicked
            TeacherDashboard.pauseAutoScroll();

            PopupMenu popupMenu = new PopupMenu(view.getContext(), holder.menuIcon);
            popupMenu.inflate(R.menu.menu_course);

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_add_course) {
                    Toast.makeText(view.getContext(), "Delete Organization clicked", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.menu_edit_org) {
                    Toast.makeText(view.getContext(), "Add Teacher clicked", Toast.LENGTH_SHORT).show();
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
            menuIcon = itemView.findViewById(R.id.menuIcon); // Binds the 3-dot icon
        }
    }
}