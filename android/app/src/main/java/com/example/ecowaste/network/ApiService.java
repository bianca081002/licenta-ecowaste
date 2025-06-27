package com.example.ecowaste.network;

import com.example.ecowaste.models.ClassificationResponse;
import com.example.ecowaste.models.RecyclingCenter;
import com.example.ecowaste.models.ReportItem;
import com.example.ecowaste.models.UserResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.DELETE;
import retrofit2.http.Part;
import retrofit2.http.Path;

import java.util.List;
import java.util.Map;

public interface ApiService {
    @POST("auth/register")
    Call<ResponseBody> registerUser(@Body User user);

    @POST("auth/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);
    @GET("auth/verify-token")
    Call<VerifyResponse> verifyToken(@Header("Authorization") String token);

    @GET("recycling/recycling-centers")
    Call<List<RecyclingCenter>> getRecyclingCenters();

    @GET("admin/users")
    Call<List<UserResponse>> getAllUsers(@Header("Authorization") String token);

    @DELETE("auth/delete-account")
    Call<ResponseBody> deleteAccount(@Header("Authorization") String token);
    @DELETE("admin/users/{id}")
    Call<Void> deleteUser(@Header("Authorization") String token, @Path("id") String userId);

    @Multipart
    @POST("recycling/classify/")
    Call<ClassificationResponse> classifyImage(
            @Part MultipartBody.Part file,
            @Part("user_id") RequestBody userId
    );

    @GET("recycling/reports/{user_id}")
    Call<List<ReportItem>> getUserReport(@Path("user_id") String userId);


    @GET("/api/recycling/community-reports")
    Call<List<CommunityUser>> getCommunityReports();

    @POST("admin/reset-score/{id}")
    Call<ResponseBody> resetUserScore(@Header("Authorization") String token, @Path("id") String userId);
    @POST("admin/update-score/{id}")
    Call<ResponseBody> updateUserScore(@Header("Authorization") String token, @Path("id") String userId, @Body Map<String, Integer> body);


}
