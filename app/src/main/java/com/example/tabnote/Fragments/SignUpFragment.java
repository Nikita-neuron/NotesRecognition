package com.example.tabnote.Fragments;

import androidx.fragment.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.tabnote.database.DBUserManager;
import com.example.tabnote.R;
import com.example.tabnote.ServerCommunication.ServerMessages;
import com.example.tabnote.ServerCommunication.User;

public class SignUpFragment extends Fragment implements View.OnClickListener{
    EditText editTextSignLogin;
    EditText editTextSignPassword;

    Button btnSignUp;

    ProgressBar progressBar;

    View view;

    ServerMessages serverMessages;

    DBUserManager dbUserManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup_fragment, container, false);

        dbUserManager = DBUserManager.getInstance(view.getContext());

        editTextSignLogin = view.findViewById(R.id.editTextSignLogin);
        editTextSignPassword = view.findViewById(R.id.editTextSignPassword);

        btnSignUp = view.findViewById(R.id.btn_signUp);
        btnSignUp.setOnClickListener(this);

        progressBar = view.findViewById(R.id.progressBarSignUp);
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        serverMessages = ServerMessages.getInstance();

        this.view = view;

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_signUp) {
            String userName = editTextSignLogin.getText().toString();
            String password = editTextSignPassword.getText().toString();

            if (!userName.equals("") && !password.equals("")) {
                User user = new User(userName, password);
                if (internetConnection()) {
                    serverMessages.registration(user, v.getContext(), progressBar, dbUserManager);
                } else {
                    Toast.makeText(view.getContext(), "Нет подключения к интернету", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private boolean internetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) view.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return (connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED)
                || (connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null && connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState() == NetworkInfo.State.CONNECTED);
    }
}
