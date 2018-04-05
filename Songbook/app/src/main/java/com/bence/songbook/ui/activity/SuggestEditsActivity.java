package com.bence.songbook.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bence.projector.common.dto.SuggestionDTO;
import com.bence.songbook.R;
import com.bence.songbook.api.SuggestionApiBean;
import com.bence.songbook.models.Song;
import com.bence.songbook.ui.utils.Preferences;

public class SuggestEditsActivity extends AppCompatActivity {

    private Song song;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest_edits);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.new_song);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });
        Intent intent = getIntent();
        song = (Song) intent.getSerializableExtra("Song");
    }

    private void submit() {
        final SuggestionDTO suggestionDTO = new SuggestionDTO();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String email = sharedPreferences.getString("email", "");
        suggestionDTO.setCreatedByEmail(email);
        final EditText suggestionEditText = findViewById(R.id.suggestion);
        suggestionDTO.setDescription(suggestionEditText.getText().toString().trim());
        suggestionDTO.setSongId(song.getUuid());
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SuggestionApiBean suggestionApiBean = new SuggestionApiBean();
                final SuggestionDTO uploadedSuggestion = suggestionApiBean.uploadSuggestion(suggestionDTO);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (uploadedSuggestion != null && !uploadedSuggestion.getUuid().trim().isEmpty()) {
                            song.setUuid(uploadedSuggestion.getUuid());
                            Toast.makeText(SuggestEditsActivity.this, R.string.successfully_uploaded, Toast.LENGTH_SHORT).show();
                            setResult(1);
                            finish();
                        } else {
                            Toast.makeText(SuggestEditsActivity.this, R.string.upload_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        thread.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
