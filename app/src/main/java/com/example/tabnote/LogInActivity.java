package com.example.tabnote;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tabnote.Fragments.LogInFragment;

public class LogInActivity extends AppCompatActivity implements View.OnClickListener{

    LinearLayout loginFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.log_in_layout);

        loginFragments = findViewById(R.id.login_fragments);

        LogInFragment logInFragment = new LogInFragment();
        changeFragment(getSupportFragmentManager().beginTransaction(), logInFragment);
    }

    private void changeFragment(FragmentTransaction fragmentTransaction, Fragment fragment) {
        fragmentTransaction.replace(R.id.login_fragments, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onClick(View v) {

    }
}
