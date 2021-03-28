// http://learn-android.ru/news/sozdanie_sostavnykh_view_obektov/2015-03-08-63.html

// идеи:
// 1. заменить запись в файл на запись в БД
// 2. заменить кнопку плея при записи на значок микрофона
// 3. может быть поменять белый фон на картинку гитары

package com.example.tabnote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_AUDIO_RECORD = 2;

    LinearLayout rootLayout;
    LinearLayout tabCards;

    Button createTab;

    TextView textViewCards;

    DBManager dbManager;

    // массив карточек
    ArrayList<TabCardView> cardsView = new ArrayList<>();

    @SuppressLint({"HandlerLeak", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar;
        actionBar = getSupportActionBar();
        @SuppressLint("ResourceAsColor")
        ColorDrawable colorDrawable
                = new ColorDrawable(R.color.topMenuColor);
        assert actionBar != null;
        actionBar.setBackgroundDrawable(colorDrawable);

        dbManager = DBManager.getInstance(this);

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

        // корневой элемент, карточки, кнопка новой табулатуры
        rootLayout = findViewById(R.id.rootLayout);
        tabCards = findViewById(R.id.tabCards);
        createTab = findViewById(R.id.createTab);

        createTab.setOnClickListener(this);

        // все файлы в папке
        ArrayList<String> tabNames = dbManager.getTabNames();

        textViewCards = findViewById(R.id.noTabs);

        if (tabNames.size() == 0) {
            textViewCards.setText("У вас нет сохранённых табулатур");
        } else {
            textViewCards.setText("Сохранённые табулатуры");
            // создание карточек с табулатурами
            int i = 0;
            for(String tabName: tabNames) {

                TabCardView cardView = new TabCardView(this, tabName);
                cardView.setTag(R.string.cardID, "card");
                cardView.setTag(R.string.cardFile, tabName);
                cardView.setOnClickListener(this);

                cardView.cardDeleteTab.setTag(R.string.cardDelete, "delete");
                cardView.cardDeleteTab.setTag(R.string.cardDeleteID, i);
                cardView.cardDeleteTab.setOnClickListener(this);

                tabCards.addView(cardView);

                cardsView.add(cardView);
                i++;
            }
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
                            .setAction("OK", view -> ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                                    REQUEST_EXTERNAL_STORAGE)).show();
                }

        } else if (requestCode == REQUEST_AUDIO_RECORD){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                // Предоставляет дополнительную информацию, если разрешение
                // не было дано, а пользователь должен получить разъяснения
                Snackbar.make(rootLayout, "Без данного разрешения, приложение не сможет распозновать ноты", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", view -> ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_RECORD)).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.createTab) {
            // при нажатии на карточку
            Intent in = new Intent(this, NewTabActivity.class);
            startActivity(in);
        }
        else if (v.getTag(R.string.cardDelete) != null && v.getTag(R.string.cardDelete).equals("delete")) {
            // при нажатии удалить

            // получение карточки табулатуры
            TabCardView card = cardsView.get((Integer) v.getTag(R.string.cardDeleteID));

            // удаление карточки
            cardsView.remove(card);
            tabCards.removeView(card);
            String tabName = (String) card.getTag(R.string.cardFile);
            dbManager.deleteTab(tabName);

            Toast.makeText(this, "Файл удалён", Toast.LENGTH_LONG).show();

            if (cardsView.size() == 0) {
                textViewCards.setText("У вас нет сохранённых табулатур");
            }
        }
        else if (v.getTag(R.string.cardID) != null && v.getTag(R.string.cardID).equals("card")) {
            // при нажатии на кнопку новой табулатуры
            Intent in = new Intent(this, TabSavedActivity.class);
            in.putExtra("file", v.getTag(R.string.cardFile)+"");
            startActivity(in);
        }
    }


}