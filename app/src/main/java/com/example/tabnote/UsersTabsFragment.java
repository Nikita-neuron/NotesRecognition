package com.example.tabnote;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UsersTabsFragment extends Fragment {

    View view;
    UsersTabsAdapter usersTabsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.users_tabs, container, false);

        setTabs();

        return view;
    }

    public void setTabs() {

        ArrayList<Tab> tabArrayList = new ArrayList<>();

        RecyclerView usersTabsList = (RecyclerView) view.findViewById(R.id.usersTabs);

        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        usersTabsAdapter = new UsersTabsAdapter(view.getContext(), tabArrayList);
        usersTabsList.setAdapter(usersTabsAdapter);
        usersTabsList.setLayoutManager(new LinearLayoutManager(view.getContext()));

        ServerMessages serverMessages = new ServerMessages();
        serverMessages.showUsersTabs(usersTabsAdapter, progressBar);
    }
}
