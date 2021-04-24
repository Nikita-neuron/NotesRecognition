package com.example.tabnote;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

public class StartActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_AUDIO_RECORD = 2;

    LinearLayout rootLayout;

    Button login;
    TextView withoutAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_satrt);

        withoutAuth = findViewById(R.id.withoutAuth);
        withoutAuth.setOnClickListener(this);

        login = findViewById(R.id.button_login);
        login.setOnClickListener(this);

        // проверка разрешений
        // запись с микрофона
        int permissionStatusAudio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        // запись файлов
        int permissionStatusStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionStatusAudio != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO},
                    REQUEST_AUDIO_RECORD);
        }

        if (permissionStatusStorage != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_AUDIO_RECORD);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_login) {
            Intent intent = new Intent(this, LogInActivity.class);
            startActivity(intent);
        }
        else if (v.getId() == R.id.withoutAuth) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("userName", "none");
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // проверка ответа пользователя по разрешениям
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_EXTERNAL_STORAGE) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Предоставляет дополнительную информацию, если разрешение
                // не было дано, а пользователь должен получить разъяснения
                Snackbar.make(rootLayout, "Без данного разрешения, вы не сможете сохранять и читать табулатуры", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", view -> ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_EXTERNAL_STORAGE)).show();
            }

        } else if (requestCode == REQUEST_AUDIO_RECORD){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                // Предоставляет дополнительную информацию, если разрешение
                // не было дано, а пользователь должен получить разъяснения
                Snackbar.make(rootLayout, "Без данного разрешения, приложение не сможет распозновать ноты", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", view -> ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_RECORD)).show();
            }
        }
    }
}
