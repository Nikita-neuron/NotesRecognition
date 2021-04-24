package com.example.tabnote.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tabnote.R;
import com.example.tabnote.ServerMessages;
import com.example.tabnote.Tab;
import com.example.tabnote.TabSavedActivity;

import java.util.ArrayList;

public class UsersTabsAdapter extends RecyclerView.Adapter<UsersTabsAdapter.ViewHolder> implements View.OnClickListener{

    private final ArrayList<Tab> tabArrayList;
    String userName;

    public UsersTabsAdapter(Context context, ArrayList<Tab> tabArrayList, String userName) {
        this.tabArrayList = tabArrayList;
        this.userName = userName;
    }

    @Override
    public void onClick(View v) {
        if (v.getTag(R.string.usersTabDeleteId) != null) {
            Tab tab = tabArrayList.get((Integer) v.getTag(R.string.usersTabDeleteId));

            if (!tab.userName.equals(userName)) {
                Toast.makeText(v.getContext(), "Вы не можете удалить данную табулатуру", Toast.LENGTH_LONG).show();
                return;
            }

            ServerMessages serverMessages = new ServerMessages();
            serverMessages.deleteTab(tab, v.getContext());
            Toast.makeText(v.getContext(), "Ваша табулатура удалена", Toast.LENGTH_LONG).show();

            tabArrayList.remove(tab);
            notifyDataSetChanged();
        }
        else if (v.getTag(R.string.usersTabId) != null) {
            Tab tab = tabArrayList.get((Integer) v.getTag(R.string.usersTabId));
            Intent in = new Intent(v.getContext(), TabSavedActivity.class);
            in.putExtra("tabType", "out");
            in.putExtra("name", tab.body);
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

        Tab tab = tabArrayList.get(position);

        TextView userName = holder.userName;
        TextView title = holder.title;
        ImageView tabDelete = holder.tabDelete;

        userName.setText(tab.userName);
        title.setText(tab.title);

        holder.view.setTag(R.string.usersTabId, position);
        tabDelete.setTag(R.string.usersTabDeleteId, position);

        tabDelete.setOnClickListener(this);
        holder.view.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return tabArrayList.size();
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

    public void swap(ArrayList<Tab> tabs) {
        tabArrayList.clear();
        tabArrayList.addAll(tabs);
        notifyDataSetChanged();
    }
}
