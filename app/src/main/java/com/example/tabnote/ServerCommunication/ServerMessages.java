package com.example.tabnote.ServerCommunication;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.tabnote.MainActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServerMessages {
    private final TabNoteService service;

    private static ServerMessages serverMessages;

    public static ServerMessages getInstance() {
        if (serverMessages == null) {
            serverMessages = new ServerMessages();
        }
        return serverMessages;
    }

    private ServerMessages() {
        service = ServiceGenerator.createService(TabNoteService.class);
    }

    public void login(User user, Context context) {
        Call<Token> callAsync = service.login(user);

        callAsync.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {

                if (response.code() != 200){
                    try {
                        assert response.errorBody() != null;
                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();
                        ErrorBody errorBody = gson.fromJson(response.errorBody().string(), ErrorBody.class);
                        String error = errorBody.getMessage();

                        String message = "";

                        if (error.contains("not found")) {
                            message = "Пользователь не найден";
                        }
                        else if (error.contains("Invalid password")) {
                            message = "Неправильный пароль";
                        }
                        if (!message.equals("")) Toast.makeText(context, message, Toast.LENGTH_LONG).show();

                        System.out.println(error);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                Token token = response.body();
                System.out.println(token);

                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("userName", user.getUsername());
                context.startActivity(intent);
            }

            @Override
            public void onFailure(Call<Token> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    public void registration(User user, Context context) {
        System.out.println(user.getUsername());
        Call<Token> callAsync = service.registration(user);

        callAsync.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {

                if (response.code() != 200){
                    try {
                        assert response.errorBody() != null;
                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();
                        ErrorBody errorBody = gson.fromJson(response.errorBody().string(), ErrorBody.class);
                        String error = errorBody.getMessage();

                        String message = "";

                        if (error.contains("exists")) {
                            message = "Подьзователь с таким именем уже существует";
                        }
                        else if (error.contains("Invalid password")) {
                            message = "Неправильный пароль";
                        }
                        if (!message.equals("")) Toast.makeText(context, message, Toast.LENGTH_LONG).show();

                        System.out.println(error);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                Token token = response.body();
                System.out.println(token);

                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("userName", user.getUsername());
                context.startActivity(intent);
            }

            @Override
            public void onFailure(Call<Token> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }
}
