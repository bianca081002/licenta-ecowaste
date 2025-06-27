package com.example.ecowaste.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecowaste.R;
import com.example.ecowaste.models.UserResponse;
import com.example.ecowaste.network.ApiClient;
import com.example.ecowaste.network.ApiService;
import com.example.ecowaste.utils.SharedPreferencesManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final List<UserResponse> userList;
    private final Context context;
    private final ApiService apiService;

    public UsersAdapter(List<UserResponse> userList, Context context) {
        this.userList = userList;
        this.context = context;
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserResponse user = userList.get(position);

        holder.usernameTextView.setText(user.getUsername());
        holder.emailTextView.setText(user.getEmail());
        holder.roleTextView.setText(user.isIs_admin() ? "Admin" : "User");

        holder.buttonDeleteUser.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Confirmare ștergere")
                    .setMessage("Ești sigur că vrei să ștergi acest utilizator?")
                    .setPositiveButton("Șterge", (dialog, which) -> {
                        int currentPosition = holder.getAdapterPosition();
                        if (currentPosition == RecyclerView.NO_POSITION) return;

                        UserResponse selectedUser = userList.get(currentPosition);
                        String token = SharedPreferencesManager.getToken();

                        if (token == null) {
                            Toast.makeText(context, "Token lipsă", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String authHeader = "Bearer " + token;

                        apiService.deleteUser(authHeader, selectedUser.getId()).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    userList.remove(currentPosition);
                                    notifyItemRemoved(currentPosition);
                                    notifyItemRangeChanged(currentPosition, userList.size());
                                    Toast.makeText(context, "Utilizator șters", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Eroare la ștergere", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(context, "Eroare rețea: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Anulează", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        TextView emailTextView;
        TextView roleTextView;
        ImageButton buttonDeleteUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.textUsername);
            emailTextView = itemView.findViewById(R.id.textEmail);
            roleTextView = itemView.findViewById(R.id.textRole);
            buttonDeleteUser = itemView.findViewById(R.id.buttonDeleteUser);
        }
    }
}
