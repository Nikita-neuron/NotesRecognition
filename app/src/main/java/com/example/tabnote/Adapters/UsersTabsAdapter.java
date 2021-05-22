package com.example.tabnote.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tabnote.R;
import com.example.tabnote.ServerCommunication.ServerMessages;
import com.example.tabnote.ServerCommunication.Tab;
import com.example.tabnote.TabActivity;
import com.example.tabnote.TabComparator;
import com.example.tabnote.TabPreview;

import org.json.JSONException;

import java.util.List;

public class UsersTabsAdapter extends RecyclerView.Adapter<UsersTabsAdapter.ViewHolder> implements View.OnClickListener{

    private List<Tab> tabList;
    String userName;

    ServerMessages serverMessages;

    Context context;

    TabComparator tabComparator;

    public UsersTabsAdapter(List<Tab> tabList, String userName, Context context) {
        this.tabList = tabList;
        this.userName = userName;

        serverMessages = ServerMessages.getInstance();

        this.context = context;
        tabComparator = new TabComparator();
    }

    @Override
    public void onClick(View v) {
        if (v.getTag(R.string.usersTabDeleteId) != null) {
            Tab tab = tabList.get((Integer) v.getTag(R.string.usersTabDeleteId));

            if (!tab.getUsername().equals(userName)) {
                Toast.makeText(v.getContext(), "Вы не можете удалить данную табулатуру", Toast.LENGTH_LONG).show();
                return;
            }
            if (internetConnection(v.getContext())) {
                serverMessages.removeTab(tab, v.getContext());
            } else {
                Toast.makeText(v.getContext(), "Нет подключения к интернету", Toast.LENGTH_LONG).show();
            }

            tabList.remove(tab);
            notifyDataSetChanged();
        }
        else if (v.getTag(R.string.usersTabId) != null) {
            Tab tab = tabList.get((Integer) v.getTag(R.string.usersTabId));
            Intent in = new Intent(v.getContext(), TabActivity.class);
            in.putExtra("tabType", "out");
            in.putExtra("body", tab.getBody());
            in.putExtra("userName", userName);
            v.getContext().startActivity(in);
        }
    }

    @NonNull
    @Override
    public UsersTabsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_users_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersTabsAdapter.ViewHolder holder, int position) {

        Tab tab = tabList.get(position);

        TextView userNameText = holder.userName;
        TextView title = holder.title;
        ImageView tabDelete = holder.tabDelete;

        userNameText.setText(tab.getUsername());
        title.setText(tab.getTitle());

        holder.view.setTag(R.string.usersTabId, position);
        tabDelete.setTag(R.string.usersTabDeleteId, position);

        if (tab.getUsername().equals(userName)) {
            tabDelete.setOnClickListener(this);
        } else {
            tabDelete.setVisibility(ImageView.INVISIBLE);
        }
        holder.view.setOnClickListener(this);

        String jsonText = tab.getBody();

        TabPreview tabPreview = new TabPreview(context, holder.view);
        try {
            tabPreview.readJSONFile(jsonText);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return tabList == null ? 0 : tabList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView userName;
        private final TextView title;
        private final ImageView tabDelete;
        private final ImageView share;
        private final View view;

        public ViewHolder(View view) {
            super(view);
            userName = view.findViewById(R.id.userNameTab);
            title = view.findViewById(R.id.userTabName);
            tabDelete = view.findViewById(R.id.usersTabDelete);
            share = view.findViewById(R.id.share);

            ((ViewGroup) share.getParent()).removeView(share);

            this.view = view;
        }
    }

    public void swap(List<Tab> tabs) {
        tabList = tabs;
        notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sort(String type) {

        if (tabList != null && tabList.size() > 0) {
            if (type.equals("name")) {
                tabList.sort(tabComparator.sortByName());
            }
            else if (type.equals("userName")) {
                tabList.sort(tabComparator.sortByUserName());
            }
            notifyDataSetChanged();
        }
    }

    private boolean internetConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
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
