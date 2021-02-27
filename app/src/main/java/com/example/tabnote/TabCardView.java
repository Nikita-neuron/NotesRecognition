package com.example.tabnote;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class TabCardView extends LinearLayout{

    TextView tabCardName;
    ImageView cardDeleteTab;

    String tabName;
    Context context;

    public TabCardView(Context context, String tabName) {
        super(context);

        this.tabName = tabName;
        this.context = context;

        initializeViews();
    }

    public TabCardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.tabName = "";
        this.context = context;

        initializeViews();
    }

    public TabCardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.tabName = "";
        this.context = context;

        initializeViews();
    }

    private void initializeViews() {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.tab_btn_layout_view, this);

        cardDeleteTab = findViewById(R.id.cardDeleteTab);
        tabCardName = findViewById(R.id.tabCardName);
        tabCardName.setText(tabName);
    }
}
