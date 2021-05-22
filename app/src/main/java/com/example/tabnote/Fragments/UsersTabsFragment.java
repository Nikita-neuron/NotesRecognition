package com.example.tabnote.Fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
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

    Spinner tabSort;
    String[] sortList = new String[] {"названию", "имени пользователя"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.users_tabs, container, false);

        assert getArguments() != null;
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

        tabSort = view.findViewById(R.id.tabSort);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, sortList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tabSort.setAdapter(adapter);

        tabSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String type = sortList[position];

                if (type.equals("имени пользователя")) {
                    usersTabsAdapter.sort("userName");
                }
                else if (type.equals("названию")) {
                    usersTabsAdapter.sort("name");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
