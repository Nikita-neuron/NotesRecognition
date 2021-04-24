package com.example.tabnote.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import android.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tabnote.Adapters.UsersTabsAdapter;
import com.example.tabnote.R;
import com.example.tabnote.ServerMessages;
import com.example.tabnote.Tab;

import java.util.ArrayList;

public class UsersTabsFragment extends Fragment {

    View view;
    UsersTabsAdapter usersTabsAdapter;

    String userName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.users_tabs, container, false);

        userName = getArguments().getString("userName");

        setTabs();

        return view;
    }

    public void setTabs() {

        ArrayList<Tab> tabArrayList = new ArrayList<>();

        RecyclerView usersTabsList = view.findViewById(R.id.usersTabs);

        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        usersTabsAdapter = new UsersTabsAdapter(view.getContext(), tabArrayList, userName);
        usersTabsList.setAdapter(usersTabsAdapter);
        usersTabsList.setLayoutManager(new LinearLayoutManager(view.getContext()));

        ServerMessages serverMessages = new ServerMessages();
        serverMessages.showUsersTabs(usersTabsAdapter, progressBar);
    }
}
