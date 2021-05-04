package com.example.tabnote.ServerCommunication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {
    private static final String BASE_URL = "https://tabnotejs.herokuapp.com/";
//    private static final String BASE_URL = "http://192.168.1.9:8000/";

    // Logging
//    HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//    interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//    OkHttpClient.Builder httpClient = new OkHttpClient.Builder().addInterceptor(interceptor);

//    private static final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    private static final OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS);
    private static final Gson gson = new GsonBuilder().setLenient().create();

    private static final Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient.build());

    private static final Retrofit retrofit = builder.build();

    public static <S> S createService(Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }
}
