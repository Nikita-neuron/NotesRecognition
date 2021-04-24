package com.example.tabnote.Adapters;

import android.app.AlertDialog;
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

import com.example.tabnote.DBManager;
import com.example.tabnote.R;
import com.example.tabnote.ServerMessages;
import com.example.tabnote.Tab;
import com.example.tabnote.TabSavedActivity;

import java.util.ArrayList;

public class UserTabsAdapter extends RecyclerView.Adapter<UserTabsAdapter.ViewHolder> implements View.OnClickListener{

    private final ArrayList<String> tabArrayList;
    String userName;

    DBManager dbManager;

    Context context;
    TextView noTabs;

    // массив карточек
    ArrayList<View> cardsView = new ArrayList<>();

    public UserTabsAdapter(ArrayList<String> tabArrayList, Context context, TextView noTabs, String userName) {
        this.tabArrayList = tabArrayList;
        this.context = context;
        this.noTabs = noTabs;
        this.userName = userName;
        dbManager = DBManager.getInstance(this.context);
    }

    @NonNull
    @Override
    public UserTabsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_users_layout, parent, false);
        return new UserTabsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserTabsAdapter.ViewHolder holder, int position) {

        String tabName = tabArrayList.get(position);

        TextView userName = holder.userName;
        TextView title = holder.title;
        ImageView tabDelete = holder.tabDelete;
        ImageView tabShare = holder.tabShare;

        View view = holder.view;

        userName.setText("");
        title.setText(tabName);

        view.setTag(R.string.cardFile, tabName);
        view.setOnClickListener(this);

        tabDelete.setTag(R.string.cardDeleteID, position);
        tabDelete.setOnClickListener(this);

        tabShare.setTag(R.string.userTabShare, position);
        tabShare.setOnClickListener(this);

        cardsView.add(view);
    }

    @Override
    public int getItemCount() {

        if (tabArrayList.size() == 0) {
            noTabs.setVisibility(View.VISIBLE);
        } else {
            noTabs.setVisibility(View.INVISIBLE);
        }
        return tabArrayList.size();
    }

    @Override
    public void onClick(View v) {
        if (v.getTag(R.string.cardDeleteID) != null) {
            // при нажатии удалить

            // получение карточки табулатуры
            View card = cardsView.get((Integer) v.getTag(R.string.cardDeleteID));

            // удаление карточки
            cardsView.remove(card);
            String tabName = (String) card.getTag(R.string.cardFile);
            dbManager.deleteTab(tabName);

            Toast.makeText(v.getContext(), "Файл удалён", Toast.LENGTH_LONG).show();

            notifyDataSetChanged();
        }
        else if (v.getTag(R.string.cardFile) != null) {
            // при нажатии на кнопку табулатуры
            Intent in = new Intent(v.getContext(), TabSavedActivity.class);
            in.putExtra("tabType", "local");
            in.putExtra("userName", userName);
            in.putExtra("name", v.getTag(R.string.cardFile)+"");
            context.startActivity(in);
        }
        else if (v.getTag(R.string.userTabShare) != null) {
            View card = cardsView.get((Integer) v.getTag(R.string.userTabShare));
            String tabName = (String) card.getTag(R.string.cardFile);

            if (userName.equals("none")) {
                createDialog("unLogIn", v.getContext(), tabName);
            } else {
                createDialog("shareTab", v.getContext(), tabName);
            }
        }
    }

    private void createDialog(String nameDialog, Context context, String cardTitle) {
        switch (nameDialog){
            case "shareTab": {
                // диалоговое окно опубликования табулатуры

                ArrayList<Tab> tabArrayList = new ArrayList<>();

                ServerMessages serverMessages = new ServerMessages();
                serverMessages.getAll(tabArrayList);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setMessage("Другие пользователи смогут увидеть вашу табулатуру");
                builder.setNegativeButton("Отмена", (dialog, which) -> { });
                builder.setPositiveButton("OK", (dialog, which) -> {
                    boolean indName = true;

                    for(Tab tab: tabArrayList) {
                        if(tab.userName.equals(userName) && tab.title.equals(cardTitle)) {
                            createDialog("shareExist", context, cardTitle);
                            indName = false;
                        }
                    }
                    if(indName) {
                        String jsonText = dbManager.getTab(cardTitle);
                        Tab tab = new Tab(0, userName, cardTitle, jsonText);
                        serverMessages.addTab(tab, context);
                        Toast.makeText(context, "Ваша табулатура опубликована", Toast.LENGTH_LONG).show();
                    }
                });

                builder.show();
                break;
            }
            case "shareExist": {
                // диалоговое окно для опубликованных табулатур
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setMessage("Данная табулатура уже опубликована");
                builder.setPositiveButton("OK", (dialog, which) -> { });

                builder.show();
                break;
            }
            case "unLogIn":
                // диалоговое окно если пользователь не авторизован
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setTitle("Вы не авторизованы");
                builder.setMessage("Войдите, чтобы опубликовать табулатуру");
                builder.setPositiveButton("OK", (dialog, which) -> { });

                builder.show();
                break;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView userName;
        private final TextView title;
        private final ImageView tabDelete;
        private final ImageView tabShare;
        private final View view;

        public ViewHolder(View view) {
            super(view);
            userName = view.findViewById(R.id.userNameTab);
            title = view.findViewById(R.id.userTabName);
            tabDelete = view.findViewById(R.id.usersTabDelete);
            tabShare = view.findViewById(R.id.share);
            this.view = view;
        }
    }
}