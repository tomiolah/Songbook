package com.bence.songbook.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.LongSparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.models.QueueSong;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongList;
import com.bence.songbook.models.SongListElement;
import com.bence.songbook.repository.impl.ormLite.QueueSongRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongListElementRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongListRepositoryImpl;
import com.bence.songbook.ui.utils.DynamicListView;
import com.bence.songbook.ui.utils.Preferences;
import com.bence.songbook.ui.utils.SongListElementAdapter;
import com.bence.songbook.utils.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class SongListActivity extends AppCompatActivity {
    public static final String TAG = SongListActivity.class.getSimpleName();
    private static final int NEW_SONG_LIST_REQUEST_CODE = 1;
    private final Memory memory = Memory.getInstance();
    private List<SongListElement> songListElements;
    private SongList songList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        songList = memory.getPassingSongList();
        toolbar.setTitle(songList.getTitle());
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        final DynamicListView<SongListElementAdapter> listView = findViewById(R.id.listView);
        songListElements = songList.getSongListElements();
        Collections.sort(songListElements, new Comparator<SongListElement>() {
            @Override
            public int compare(SongListElement o1, SongListElement o2) {
                return Utility.compare(o1.getNumber(), o2.getNumber());
            }
        });
        SongListElementAdapter songListAdapter = new SongListElementAdapter(this, R.layout.list_row, songListElements,
                new MainActivity.Listener() {

                    @Override
                    public void onGrab(int position, LinearLayout row) {
                        listView.onGrab(position, row);
                    }
                }, false);
        fetchSongAttributes();
        final SongListElementRepositoryImpl songListElementRepository = new SongListElementRepositoryImpl(this);
        final SongListRepositoryImpl songListRepository = new SongListRepositoryImpl(this);
        listView.setAdapter(songListAdapter);
        listView.setListener(new DynamicListView.Listener() {
            @Override
            public void swapElements(int indexOne, int indexTwo) {
                SongListElement temp = songListElements.get(indexOne);
                SongListElement secondTmp = songListElements.get(indexTwo);
                int queueNumber = temp.getNumber();
                temp.setNumber(secondTmp.getNumber());
                secondTmp.setNumber(queueNumber);
                songListElements.set(indexOne, secondTmp);
                songListElements.set(indexTwo, temp);
                songListElementRepository.save(temp);
                songListElementRepository.save(secondTmp);
                songList.setModifiedDate(new Date());
                songListRepository.save(songList);
            }

            @Override
            public void deleteElement(int originalItem) {
                SongListElement temp = songListElements.remove(originalItem);
                songListElementRepository.delete(temp);
                songList.setModifiedDate(new Date());
                songListRepository.save(songList);
                listView.invalidateViews();
                listView.refreshDrawableState();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Song song = songListElements.get(position).getSong();
                Intent intent = new Intent(SongListActivity.this, SongActivity.class);
                memory.setPassingSong(song);
                startActivityForResult(intent, NEW_SONG_LIST_REQUEST_CODE);
            }
        });
    }

    private void fetchSongAttributes() {
        List<Song> songs = memory.getSongs();
        LongSparseArray<Song> hashMap = new LongSparseArray<>(songs.size());
        for (Song song : songs) {
            hashMap.put(song.getId(), song);
        }
        for (SongListElement element : songListElements) {
            if (element.getSong() != null) {
                Long songId = element.getSong().getId();
                Song song = hashMap.get(songId);
                if (song != null) {
                    element.setSong(song);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.content_song_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_add_to_queue:
                QueueSongRepositoryImpl queueSongRepository = new QueueSongRepositoryImpl(this);
                List<QueueSong> newQueueSongs = new ArrayList<>(songListElements.size());
                for (SongListElement element : songListElements) {
                    QueueSong queueSong = new QueueSong();
                    queueSong.setSong(element.getSong());
                    memory.addSongToQueue(queueSong);
                    newQueueSongs.add(queueSong);
                }
                queueSongRepository.save(newQueueSongs);
                showToaster(getString(R.string.added_to_queue), Toast.LENGTH_SHORT);
                break;
            case R.id.action_edit:
                Intent intent = new Intent(this, NewSongListActivity.class);
                memory.setEditingSongList(songList);
                intent.putExtra("edit", true);
                startActivityForResult(intent, NEW_SONG_LIST_REQUEST_CODE);
                break;
            case R.id.action_delete:
                SongListRepositoryImpl songListRepository = new SongListRepositoryImpl(this);
                songListRepository.delete(songList);
                setResult(1);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showToaster(String s, int lengthLong) {
        Toast.makeText(this, s, lengthLong).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case NEW_SONG_LIST_REQUEST_CODE:
                if (resultCode == 1) {
                    setResult(2);
                    finish();
                }
                break;
        }
    }

}
