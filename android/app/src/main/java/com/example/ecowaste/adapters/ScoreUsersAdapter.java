package com.example.ecowaste.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.models.UserResponse;

import java.util.List;

public class ScoreUsersAdapter extends RecyclerView.Adapter<ScoreUsersAdapter.UserViewHolder> {

    private final List<UserResponse> userList;
    private final Context context;
    private final OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(UserResponse user);
    }

    public ScoreUsersAdapter(List<UserResponse> userList, Context context, OnUserClickListener listener) {
        this.userList = userList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_score, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserResponse user = userList.get(position);
        holder.textUsername.setText(user.getUsername());
        holder.textEmail.setText(user.getEmail());
        holder.textRole.setText(user.isIs_admin() ? "Admin" : "User");

        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textUsername, textEmail, textRole;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textUsername = itemView.findViewById(R.id.textUsername);
            textEmail = itemView.findViewById(R.id.textEmail);
            textRole = itemView.findViewById(R.id.textRole);
        }
    }
}
