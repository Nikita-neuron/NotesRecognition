package com.example.tabnote.Fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.tabnote.database.DBUserManager;
import com.example.tabnote.R;
import com.example.tabnote.ServerCommunication.ServerMessages;
import com.example.tabnote.ServerCommunication.User;

public class LogInFragment extends Fragment implements View.OnClickListener{

    EditText editTextInLogin;
    EditText editTextInPassword;

    View view;

    TextView signUp;

    Button btnLogin;

    ProgressBar progressBar;

    ServerMessages serverMessages;

    DBUserManager dbUserManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment, container, false);

        dbUserManager = DBUserManager.getInstance(view.getContext());

        editTextInLogin = view.findViewById(R.id.editTextInLogin);
        editTextInPassword = view.findViewById(R.id.editTextInPassword);

        signUp = view.findViewById(R.id.signUp);
        signUp.setOnClickListener(this);

        btnLogin = view.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);

        progressBar = view.findViewById(R.id.progressBarLogIn);
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        serverMessages = ServerMessages.getInstance();

        this.view = view;

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
                if (internetConnection()) {
                    serverMessages.login(user, v.getContext(), progressBar, dbUserManager);
                } else {
                    Toast.makeText(view.getContext(), "Нет подключения к интернету", Toast.LENGTH_LONG).show();
                }
            }
        }
        else if (v.getId() == R.id.signUp) {
            changeFragment(getFragmentManager().beginTransaction(), new SignUpFragment());
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
