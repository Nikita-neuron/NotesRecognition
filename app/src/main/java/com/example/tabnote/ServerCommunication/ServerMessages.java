package com.example.tabnote.ServerCommunication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.tabnote.Adapters.UsersTabsAdapter;
import com.example.tabnote.database.DBUserManager;
import com.example.tabnote.MainActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;

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

    public void login(User user, Context context, ProgressBar progressBar, DBUserManager dbUserManager) {

        progressBar.setVisibility(ProgressBar.VISIBLE);

        Call<Token> callAsync = service.login(user);

        callAsync.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {

                if (response.code() != 200){
                    try {
                        assert response.errorBody() != null;
                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();
                        MessageBody messageBody = gson.fromJson(response.errorBody().string(), MessageBody.class);
                        String error = messageBody.getMessage();

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
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                    return;
                }

                Token token = response.body();
                System.out.println(token);

                progressBar.setVisibility(ProgressBar.INVISIBLE);

                dbUserManager.addUser(user.getUsername(), user.getPassword());

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

    public void registration(User user, Context context, ProgressBar progressBar, DBUserManager dbUserManager) {

        progressBar.setVisibility(ProgressBar.VISIBLE);

        Call<Token> callAsync = service.registration(user);

        callAsync.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {

                if (response.code() != 200){
                    try {
                        assert response.errorBody() != null;
                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();
                        MessageBody messageBody = gson.fromJson(response.errorBody().string(), MessageBody.class);
                        String error = messageBody.getMessage();

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
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                    return;
                }

                Token token = response.body();
                System.out.println(token);

                progressBar.setVisibility(ProgressBar.INVISIBLE);

                dbUserManager.addUser(user.getUsername(), user.getPassword());

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

    public void getTabs(Context context, UsersTabsAdapter usersTabsAdapter, ProgressBar progressBar) {

        progressBar.setVisibility(ProgressBar.VISIBLE);

        Call<List<Tab>> callAsync = service.getTabs();

        callAsync.enqueue(new Callback<List<Tab>>() {
            @Override
            public void onResponse(Call<List<Tab>> call, Response<List<Tab>> response) {

                if (response.code() != 200){
                    try {
                        assert response.errorBody() != null;
                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();
                        MessageBody messageBody = gson.fromJson(response.errorBody().string(), MessageBody.class);
                        String error = messageBody.getMessage();

                        Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();

                        System.out.println(error);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                    return;
                }

                progressBar.setVisibility(ProgressBar.INVISIBLE);

                List<Tab> tabs = response.body();
                usersTabsAdapter.swap(tabs);
            }

            @Override
            public void onFailure(Call<List<Tab>> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    public void addTab(Tab tab, Context context) {
        Call<MessageBody> callAsync = service.addTab(tab);

        callAsync.enqueue(new Callback<MessageBody>() {
            @Override
            public void onResponse(Call<MessageBody> call, Response<MessageBody> response) {

                if (response.code() != 200){
                    try {
                        assert response.errorBody() != null;
                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();
                        MessageBody messageBody = gson.fromJson(response.errorBody().string(), MessageBody.class);
                        String error = messageBody.getMessage();

                        if (error.contains("exists")) {
                            AlertDialog.Builder dialog = new AlertDialog.Builder(context);

                            dialog.setMessage("Данная табулатура уже опубликована");
                            dialog.setPositiveButton("OK", (d, which) -> { });

                            dialog.show();
                        }

                        System.out.println(error);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                MessageBody messageBody = response.body();

                assert messageBody != null;
                if (messageBody.getMessage().contains("successful")) {
                    Toast.makeText(context, "Ваша табулатура опубликована", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MessageBody> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    public void removeTab(Tab tab, Context context) {
        Call<MessageBody> callAsync = service.removeTab(tab);

        callAsync.enqueue(new Callback<MessageBody>() {
            @Override
            public void onResponse(Call<MessageBody> call, Response<MessageBody> response) {

                if (response.code() != 200){
                    try {
                        assert response.errorBody() != null;
                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();
                        MessageBody messageBody = gson.fromJson(response.errorBody().string(), MessageBody.class);
                        String error = messageBody.getMessage();

                        System.out.println(error);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                MessageBody messageBody = response.body();

                assert messageBody != null;
                if (messageBody.getMessage().contains("successful")) {
                    Toast.makeText(context, "Ваша табулатура удалена", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MessageBody> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    public void editTab(Tab tab, Context context) {
        Call<MessageBody> callAsync = service.editTab(tab);

        callAsync.enqueue(new Callback<MessageBody>() {
            @Override
            public void onResponse(Call<MessageBody> call, Response<MessageBody> response) {

                if (response.code() != 200){
                    try {
                        assert response.errorBody() != null;
                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();
                        MessageBody messageBody = gson.fromJson(response.errorBody().string(), MessageBody.class);
                        String error = messageBody.getMessage();

                        System.out.println(error);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                MessageBody messageBody = response.body();

                assert messageBody != null;
                if (messageBody.getMessage().contains("successful")) {
                    Toast.makeText(context, "Ваша табулатура изменена", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MessageBody> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }
}
