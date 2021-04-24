package com.example.tabnote.ServerCommunication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {
    private static final String BASE_URL = "https://tabnotejs.herokuapp.com/";

    // Logging
//    HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//    interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//    OkHttpClient.Builder httpClient = new OkHttpClient.Builder().addInterceptor(interceptor);

    private static final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
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
