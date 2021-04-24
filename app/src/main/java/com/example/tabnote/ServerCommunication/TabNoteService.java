package com.example.tabnote.ServerCommunication;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TabNoteService {
    @POST("/auth/login/")
    Call<Token> login(@Body User user);

    @POST("/auth/registration/")
    Call<Token> registration(@Body User user);
}
