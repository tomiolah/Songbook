package com.bence.songbook.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.bence.songbook.R;
import com.bence.songbook.ui.utils.Preferences;

public class SuggestEditsChooseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest_message);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onEditButtonClick(View view) {
        Intent intent = getIntent();
        intent.putExtra("method", "EDIT");
        intent.setClass(this, SuggestEditsActivity.class);
        startActivity(intent);
    }

    public void onOtherButtonClick(View view) {
        Intent intent = getIntent();
        intent.putExtra("method", "OTHER");
        intent.setClass(this, SuggestEditsActivity.class);
        startActivity(intent);
    }
}
