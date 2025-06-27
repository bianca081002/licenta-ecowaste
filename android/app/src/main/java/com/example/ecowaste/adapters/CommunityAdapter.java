package com.example.ecowaste.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.network.CommunityUser;

import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.ViewHolder> {
    private List<CommunityUser> userList;

    public CommunityAdapter(List<CommunityUser> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public CommunityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_community_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommunityAdapter.ViewHolder holder, int position) {
        CommunityUser user = userList.get(position);

        // Set rank emoji or number
        switch (position) {
            case 0:
                holder.rank.setText("🥇");
                break;
            case 1:
                holder.rank.setText("🥈");
                break;
            case 2:
                holder.rank.setText("🥉");
                break;
            default:
                holder.rank.setText((position + 1) + ".");
                break;
        }

        holder.username.setText(user.getUsername());
        holder.userId.setText("ID: " + user.getUser_id());
        holder.scanCount.setText(user.getScanCount() + " scans");
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rank, username, userId, scanCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rank = itemView.findViewById(R.id.textRank);
            username = itemView.findViewById(R.id.textUsername);
            userId = itemView.findViewById(R.id.textUserId);
            scanCount = itemView.findViewById(R.id.textScans);
        }
    }
}
