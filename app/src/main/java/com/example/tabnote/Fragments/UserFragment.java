package com.example.tabnote.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tabnote.Adapters.UserTabsAdapter;
import com.example.tabnote.database.DBManager;
import com.example.tabnote.R;

import java.util.ArrayList;

public class UserFragment extends Fragment {

    RecyclerView userTabs;

    TextView noTabs;

    DBManager dbManager;

    String userName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_fragment, container, false);

        userName = getArguments().getString("userName");

        dbManager = DBManager.getInstance(view.getContext());

        noTabs = view.findViewById(R.id.no_tabs);

        userTabs = view.findViewById(R.id.userTabs);

        ArrayList<String> tabNames = dbManager.getTabNames();

        UserTabsAdapter userTabsAdapter = new UserTabsAdapter(tabNames, view.getContext(), noTabs, userName);
        userTabs.setAdapter(userTabsAdapter);
        userTabs.setLayoutManager(new LinearLayoutManager(view.getContext()));

        return view;
    }

}
