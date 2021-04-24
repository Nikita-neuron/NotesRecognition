package com.example.tabnote.Fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.tabnote.R;
import com.example.tabnote.ServerCommunication.ServerMessages;
import com.example.tabnote.ServerCommunication.User;

public class LogInFragment extends Fragment implements View.OnClickListener{

    EditText editTextInLogin;
    EditText editTextInPassword;

    TextView signUp;

    Button btnLogin;

    ServerMessages serverMessages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment, container, false);

        editTextInLogin = view.findViewById(R.id.editTextInLogin);
        editTextInPassword = view.findViewById(R.id.editTextInPassword);

        signUp = view.findViewById(R.id.signUp);
        signUp.setOnClickListener(this);

        btnLogin = view.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);

        serverMessages = ServerMessages.getInstance();

        return view;
    }

    private void changeFragment(FragmentTransaction fragmentTransaction, Fragment fragment) {
        fragmentTransaction.replace(R.id.login_fragments, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_login) {
            String userName = editTextInLogin.getText().toString();
            String password = editTextInPassword.getText().toString();

            if (!userName.equals("") && !password.equals("")) {
                User user = new User(userName, password);
                serverMessages.login(user, v.getContext());
            }
        }
        else if (v.getId() == R.id.signUp) {
            changeFragment(getFragmentManager().beginTransaction(), new SignUpFragment());
        }
    }
}
