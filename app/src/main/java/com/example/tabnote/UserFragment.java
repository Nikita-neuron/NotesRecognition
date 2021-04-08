package com.example.tabnote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class UserFragment extends Fragment implements View.OnClickListener{
    LinearLayout rootLayout;
    LinearLayout tabCards;

    Button createTab;

    TextView textViewCards;

    DBManager dbManager;

    // массив карточек
    ArrayList<TabCardView> cardsView = new ArrayList<>();

    String userName;

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_actifity, container, false);

        dbManager = DBManager.getInstance(view.getContext());
        userName = "Test";

        // корневой элемент, карточки, кнопка новой табулатуры
        rootLayout = view.findViewById(R.id.rootLayout);
        tabCards = view.findViewById(R.id.tabCards);
        createTab = view.findViewById(R.id.createTab);

        createTab.setOnClickListener(this);

        // все файлы в папке
        ArrayList<String> tabNames = dbManager.getTabNames();

        textViewCards = view.findViewById(R.id.noTabs);

        if (tabNames.size() == 0) {
            textViewCards.setText("У вас нет сохранённых табулатур");
        } else {
            textViewCards.setText("Сохранённые табулатуры");
            // создание карточек с табулатурами
            int i = 0;
            for(String tabName: tabNames) {

                TabCardView cardView = new TabCardView(view.getContext(), tabName);
                cardView.setTag(R.string.cardFile, tabName);
                cardView.setOnClickListener(this);

                cardView.cardDeleteTab.setTag(R.string.cardDeleteID, i);
                cardView.cardDeleteTab.setOnClickListener(this);

                cardView.share.setTag(R.string.userTabShare, i);
                cardView.share.setOnClickListener(this);

                tabCards.addView(cardView);

                cardsView.add(cardView);
                i++;
            }
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.createTab) {
            // при нажатии на карточку
            Intent in = new Intent(v.getContext(), NewTabActivity.class);
            startActivity(in);
        }
        else if (v.getTag(R.string.cardDeleteID) != null) {
            // при нажатии удалить

            // получение карточки табулатуры
            TabCardView card = cardsView.get((Integer) v.getTag(R.string.cardDeleteID));

            // удаление карточки
            cardsView.remove(card);
            tabCards.removeView(card);
            String tabName = (String) card.getTag(R.string.cardFile);
            dbManager.deleteTab(tabName);

            Toast.makeText(v.getContext(), "Файл удалён", Toast.LENGTH_LONG).show();

            if (cardsView.size() == 0) {
                textViewCards.setText("У вас нет сохранённых табулатур");
            }
        }
        else if (v.getTag(R.string.cardFile) != null) {
            // при нажатии на кнопку новой табулатуры
            Intent in = new Intent(v.getContext(), TabSavedActivity.class);
            in.putExtra("tabType", "local");
            in.putExtra("name", v.getTag(R.string.cardFile)+"");
            startActivity(in);
        }
        else if (v.getTag(R.string.userTabShare) != null) {
            TabCardView card = cardsView.get((Integer) v.getTag(R.string.userTabShare));
            createDialog("shareTab", v.getContext(), card.tabName);
        }
    }

    public void createDialog(String nameDialog, Context context, String cardTitle) {
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
        }
    }
}
