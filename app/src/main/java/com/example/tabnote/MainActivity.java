package com.example.tabnote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.tabnote.Fragments.InfoFragment;
import com.example.tabnote.Fragments.UserFragment;
import com.example.tabnote.Fragments.UsersTabsFragment;
import com.example.tabnote.database.DBUserManager;
import com.google.android.material.snackbar.Snackbar;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_AUDIO_RECORD = 2;

    RelativeLayout fragmentsChange;
    RelativeLayout btnUsersTabs;
    RelativeLayout btnHome;
    RelativeLayout btnTabAdd;
    RelativeLayout bottomMenu;
    RelativeLayout topMenuUser;
    RelativeLayout userLogin;

    ImageView tabInfo;

    Button btnUserLogin;

    TextView userNameView;

    UserFragment userFragment;
    UsersTabsFragment usersTabsFragment;
    InfoFragment infoFragment;

    String userName;

    DBUserManager dbUserManager;

    @SuppressLint({"HandlerLeak", "WrongViewCast", "CommitTransaction"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        dbUserManager = DBUserManager.getInstance(this);

        userName = "none";

        String user = dbUserManager.existUser();

        if (!user.equals("")) {
            userName = user;
        }

        fragmentsChange = findViewById(R.id.fragmentsChange);

        btnUsersTabs = findViewById(R.id.btn_users_tabs);
        btnUsersTabs.setOnClickListener(this);

        btnHome = findViewById(R.id.btn_home);
        btnHome.setOnClickListener(this);

        btnTabAdd = findViewById(R.id.btn_tab_add);
        btnTabAdd.setOnClickListener(this);

        bottomMenu = findViewById(R.id.bottom_menu);

        userNameView = findViewById(R.id.user_name);

        topMenuUser = findViewById(R.id.topMenuUser);
        userLogin = findViewById(R.id.user_login);

        tabInfo = findViewById(R.id.tab_info);
        tabInfo.setOnClickListener(this);

        Bundle bundle = new Bundle();
        bundle.putString("userName", userName);

        userFragment = new UserFragment();
        userFragment.setArguments(bundle);

        usersTabsFragment = new UsersTabsFragment();
        usersTabsFragment.setArguments(bundle);

        infoFragment = new InfoFragment();

        activeMenuItem(btnHome);
        changeFragment(getSupportFragmentManager().beginTransaction(), userFragment);

        if (userName.equals("none")) {
            topMenuUser.setVisibility(View.INVISIBLE);

            btnUserLogin = findViewById(R.id.btn_user_login);
            btnUserLogin.setOnClickListener(this);
        } else {
            userLogin.setVisibility(View.INVISIBLE);
            userNameView.setText(userName);

            topMenuUser.setOnClickListener(this);
        }
    }

    private void changeFragment(FragmentTransaction fragmentTransaction, Fragment fragment) {
        fragmentTransaction.replace(R.id.fragmentsChange, fragment);
        fragmentTransaction.commit();
    }

    private void changeMenuColor(RelativeLayout item, boolean active) {
        int color = active ? Color.WHITE : Color.BLACK;
        item.getChildAt(0).getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        ((TextView) item.getChildAt(1)).setTextColor(color);
    }

    private void activeMenuItem(RelativeLayout item) {
        for (int i = 0; i < bottomMenu.getChildCount(); i++) {
            changeMenuColor((RelativeLayout) bottomMenu.getChildAt(i), false);
        }
        changeMenuColor(item, true);
    }

    @SuppressLint({"CommitTransaction", "NonConstantResourceId"})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_users_tabs:
                activeMenuItem(btnUsersTabs);

                changeFragment(getSupportFragmentManager().beginTransaction(), usersTabsFragment);
                break;
            case R.id.btn_tab_add:
                activeMenuItem(btnTabAdd);

                Intent intent = new Intent(this, TabActivity.class);
                intent.putExtra("userName", userName);
                intent.putExtra("tabType", "new");
                startActivity(intent);
                break;
            case R.id.btn_home:
                activeMenuItem(btnHome);

                changeFragment(getSupportFragmentManager().beginTransaction(), userFragment);
                break;
            case R.id.btn_user_login:
                Intent intent1 = new Intent(this, LogInActivity.class);
                startActivity(intent1);
                break;
            case R.id.topMenuUser:
                showPopupMenu(v.getContext(), v);
                break;
            case R.id.tab_info:
                changeFragment(getSupportFragmentManager().beginTransaction(), infoFragment);
                break;
        }
    }

    public void showPopupMenu(Context context, View view) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    assert menuPopupHelper != null;
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        popupMenu.inflate(R.menu.popupmenu);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.userLogout) {
            dbUserManager.deleteUser(userName);
            topMenuUser.setVisibility(View.INVISIBLE);
            userLogin.setVisibility(View.VISIBLE);

            btnUserLogin = findViewById(R.id.btn_user_login);
            btnUserLogin.setOnClickListener(this);

            userName = "none";
            return true;
        }
        return false;
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
                Snackbar.make(fragmentsChange, "Без данного разрешения, вы не сможете сохранять и читать табулатуры", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", view -> ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_EXTERNAL_STORAGE)).show();
            }

        } else if (requestCode == REQUEST_AUDIO_RECORD){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                // Предоставляет дополнительную информацию, если разрешение
                // не было дано, а пользователь должен получить разъяснения
                Snackbar.make(fragmentsChange, "Без данного разрешения, приложение не сможет распозновать ноты", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", view -> ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_RECORD)).show();
            }
        }
    }
}