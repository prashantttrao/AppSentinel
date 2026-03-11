package com.example.appsentinel.api;

import com.example.appsentinel.models.ScanResult;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ScanService {

    @Multipart
    @POST("/scan")
    Call<ScanResult> scanApp(
            @Part MultipartBody.Part apk,
            @Part("package_name") RequestBody packageName
    );

}