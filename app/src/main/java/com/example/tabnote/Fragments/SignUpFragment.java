package com.example.tabnote.Fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.tabnote.MainActivity;
import com.example.tabnote.R;
import com.example.tabnote.ServerCommunication.ServerMessages;
import com.example.tabnote.ServerCommunication.User;

public class SignUpFragment extends Fragment implements View.OnClickListener{
    EditText editTextSignLogin;
    EditText editTextSignPassword;

    Button btnSignUp;

    ServerMessages serverMessages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup_fragment, container, false);

        editTextSignLogin = view.findViewById(R.id.editTextSignLogin);
        editTextSignPassword = view.findViewById(R.id.editTextSignPassword);

        btnSignUp = view.findViewById(R.id.btn_signUp);
        btnSignUp.setOnClickListener(this);

        serverMessages = ServerMessages.getInstance();

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_signUp) {
            String userName = editTextSignLogin.getText().toString();
            String password = editTextSignPassword.getText().toString();

            if (!userName.equals("") && !password.equals("")) {
                User user = new User(userName, password);
                serverMessages.registration(user, v.getContext());
            }
        }
    }
}
