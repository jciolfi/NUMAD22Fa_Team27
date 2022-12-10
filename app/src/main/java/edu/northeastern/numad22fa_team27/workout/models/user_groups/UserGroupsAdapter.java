package edu.northeastern.numad22fa_team27.workout.models.user_groups;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Objects;

import edu.northeastern.numad22fa_team27.R;
import edu.northeastern.numad22fa_team27.workout.models.Group;

public class UserGroupsAdapter extends RecyclerView.Adapter<UserGroupsViewHolder> {
    private final String TAG = "UserGroupsAdapter";
    private final List<Group> userGroups;

    public UserGroupsAdapter(List<Group> userGroups) {
        this.userGroups = userGroups;
    }

    @NonNull
    @Override
    public UserGroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserGroupsViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_user_group, null));
    }

    @Override
    public void onBindViewHolder(@NonNull UserGroupsViewHolder holder, int position) {
        Group group = userGroups.get(position);
        holder.groupName.setText(group.getGroupName());
        holder.numMembers.setText(String.format("Members: %s", group.getMembers().size()));

        // set public/private switch if admin
        if (group.getAdminID().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
            if (group.getAcceptingMembers()) {
                holder.publicSwitch.setChecked(true);
            } else {
                holder.publicSwitch.setChecked(false);
            }
            holder.publicSwitch.setVisibility(View.VISIBLE);
        } else {
            holder.publicSwitch.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return userGroups.size();
    }
}
