package com.bence.songbook.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.models.SongList;
import com.bence.songbook.repository.impl.ormLite.SongListRepositoryImpl;
import com.bence.songbook.ui.utils.Preferences;
import com.bence.songbook.ui.utils.SongListAdapter;

import java.util.List;

public class LibraryActivity extends AppCompatActivity {
    public static final String TAG = LibraryActivity.class.getSimpleName();
    private final int NEW_SONG_LIST_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.song_lists);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        ListView listView = findViewById(R.id.listView);
        SongListRepositoryImpl songListRepository = new SongListRepositoryImpl(this);
        final List<SongList> songLists = songListRepository.findAll();
        SongListAdapter songListAdapter = new SongListAdapter(this, R.layout.song_list_row, songLists);
        listView.setAdapter(songListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                SongList songList = songLists.get(position);
                Intent intent = new Intent(LibraryActivity.this, SongListActivity.class);
                Memory.getInstance().setPassingSongList(songList);
                startActivityForResult(intent, NEW_SONG_LIST_REQUEST_CODE);
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.content_library_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_newSongList:
                Intent intent = new Intent(this, NewSongListActivity.class);
                startActivityForResult(intent, NEW_SONG_LIST_REQUEST_CODE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case NEW_SONG_LIST_REQUEST_CODE:
                if (resultCode == 1) {
                    recreate();
                }
                if (resultCode == 2) {
                    recreate();
                    Intent intent = new Intent(LibraryActivity.this, SongListActivity.class);
                    startActivityForResult(intent, NEW_SONG_LIST_REQUEST_CODE);
                }
                break;
        }
    }

}
