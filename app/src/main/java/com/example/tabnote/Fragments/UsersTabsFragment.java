package com.example.tabnote.Fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import android.app.Fragment;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tabnote.Adapters.UsersTabsAdapter;
import com.example.tabnote.R;
import com.example.tabnote.ServerCommunication.ServerMessages;
import com.example.tabnote.ServerCommunication.Tab;

import java.util.List;

public class UsersTabsFragment extends Fragment {

    View view;
    UsersTabsAdapter usersTabsAdapter;

    List<Tab> tabList;

    String userName;

    ServerMessages serverMessages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.users_tabs, container, false);

        userName = getArguments().getString("userName");

        serverMessages = ServerMessages.getInstance();

        RecyclerView usersTabsList = view.findViewById(R.id.usersTabs);

        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        TextView internetConnection = view.findViewById(R.id.no_internet_users);

        usersTabsAdapter = new UsersTabsAdapter(tabList, userName, view.getContext());
        usersTabsList.setAdapter(usersTabsAdapter);
        usersTabsList.setLayoutManager(new LinearLayoutManager(view.getContext()));

        if (internetConnection()) {
            internetConnection.setVisibility(TextView.INVISIBLE);
            serverMessages.getTabs(view.getContext(), usersTabsAdapter, progressBar);
        }

        return view;
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
