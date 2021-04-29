package com.example.tabnote.ServerCommunication;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface TabNoteService {
    @POST("/auth/login/")
    Call<Token> login(@Body User user);

    @POST("/auth/registration/")
    Call<Token> registration(@Body User user);

    @GET("/tabs/")
    Call<List<Tab>> getTabs();

    @POST("/tabs/add")
    Call<MessageBody> addTab(@Body Tab tab);

    @POST("/tabs/remove/")
    Call<MessageBody> removeTab(@Body Tab tab);

    @POST("/tabs/edit/")
    Call<MessageBody> editTab(@Body Tab tab);
}
