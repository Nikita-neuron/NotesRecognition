// http://learn-android.ru/news/sozdanie_sostavnykh_view_obektov/2015-03-08-63.html

// идеи:
// 1. заменить запись в файл на запись в БД
// 2. заменить кнопку плея при записи на значок микрофона
// 3. может быть поменять белый фон на картинку гитары

package com.example.tabnote;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Fragment;
import android.widget.TextView;

import com.example.tabnote.Fragments.UserFragment;
import com.example.tabnote.Fragments.UsersTabsFragment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    RelativeLayout fragmentsChange;
    RelativeLayout btnUsersTabs;
    RelativeLayout btnHome;
    RelativeLayout btnTabAdd;
    RelativeLayout bottomMenu;
    RelativeLayout topMenuUser;
    RelativeLayout userLogin;

    Button btnUserLogin;

    TextView userTitleType;
    TextView userNameView;

    UserFragment userFragment;
    UsersTabsFragment usersTabsFragment;

    String userName;

    @SuppressLint({"HandlerLeak", "WrongViewCast", "CommitTransaction"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userName = getIntent().getExtras().getString("userName");

        fragmentsChange = findViewById(R.id.fragmentsChange);

        btnUsersTabs = findViewById(R.id.btn_users_tabs);
        btnUsersTabs.setOnClickListener(this);

        btnHome = findViewById(R.id.btn_home);
        btnHome.setOnClickListener(this);

        btnTabAdd = findViewById(R.id.btn_tab_add);
        btnTabAdd.setOnClickListener(this);

        bottomMenu = findViewById(R.id.bottom_menu);

        userTitleType = findViewById(R.id.user_title_type);
        userNameView = findViewById(R.id.user_name);

        topMenuUser = findViewById(R.id.topMenuUser);
        userLogin = findViewById(R.id.user_login);

        Bundle bundle = new Bundle();
        bundle.putString("userName", userName);

        userFragment = new UserFragment();
        userFragment.setArguments(bundle);

        usersTabsFragment = new UsersTabsFragment();
        usersTabsFragment.setArguments(bundle);

        activeMenuItem(btnHome);

        changeFragment(getFragmentManager().beginTransaction(), userFragment);

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

                userTitleType.setText("Табулатуры пользователей");

                changeFragment(getFragmentManager().beginTransaction(), usersTabsFragment);
                break;
            case R.id.btn_tab_add:
                activeMenuItem(btnTabAdd);

                Intent intent = new Intent(this, NewTabActivity.class);
                intent.putExtra("userName", userName);
                startActivity(intent);
                break;
            case R.id.btn_home:
                activeMenuItem(btnHome);

                userTitleType.setText("Ваши табулатуры");

                changeFragment(getFragmentManager().beginTransaction(), userFragment);
                break;
            case R.id.btn_user_login:
                Intent intent1 = new Intent(this, LogInActivity.class);
                startActivity(intent1);
                break;
            case R.id.topMenuUser:
                showPopupMenu(v.getContext(), v);
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
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }
}