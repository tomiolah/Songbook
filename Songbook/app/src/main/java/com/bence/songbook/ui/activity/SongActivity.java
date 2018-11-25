package com.bence.songbook.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.models.FavouriteSong;
import com.bence.songbook.models.QueueSong;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongCollection;
import com.bence.songbook.models.SongCollectionElement;
import com.bence.songbook.models.SongList;
import com.bence.songbook.models.SongListElement;
import com.bence.songbook.network.ProjectionTextChangeListener;
import com.bence.songbook.repository.FavouriteSongRepository;
import com.bence.songbook.repository.SongCollectionRepository;
import com.bence.songbook.repository.SongRepository;
import com.bence.songbook.repository.impl.ormLite.FavouriteSongRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.QueueSongRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongCollectionRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongListElementRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongListRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongRepositoryImpl;
import com.bence.songbook.service.SongService;
import com.bence.songbook.ui.utils.GoogleSignInIntent;
import com.bence.songbook.ui.utils.PageAdapter;
import com.bence.songbook.ui.utils.Preferences;
import com.bence.songbook.ui.utils.SaveFavouriteInGoogleDrive;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.bence.songbook.ui.activity.VersionsActivity.getSongFromMemory;
import static com.bence.songbook.ui.utils.SaveFavouriteInGoogleDrive.REQUEST_CODE_SIGN_IN;

public class SongActivity extends AppCompatActivity {
    private static final String TAG = "SongActivity";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    TabLayout tabLayout;
    ViewPager viewPager;
    PageAdapter pageAdapter;
    private Song song;
    private Memory memory;
    private MenuItem favouriteMenuItem;
    private SaveFavouriteInGoogleDrive saveFavouriteInGoogleDrive;
    private Intent signInIntent;
    private View mainLayout;
    private PopupWindow googleSignInPopupWindow;
    private Menu menu;
    private PopupWindow saveToSongListPopupWindow;
    private List<Song> allByVersionGroup = new ArrayList<>();

    public static void saveGmail(GoogleSignInAccount result, Context context) {
        String email = result.getEmail();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString("gmail", email).apply();
        sharedPreferences.edit().putBoolean("gSignIn", true).apply();
    }

    public static PopupWindow showGoogleSignIn(LayoutInflater inflater, boolean main) {
        if (inflater != null) {
            @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.content_ask_google_sign_in, null);
            if (main) {
                View viewById = customView.findViewById(R.id.dontShowButton);
                viewById.setVisibility(View.GONE);
            }
            PopupWindow googleSignInPopupWindow = new PopupWindow(
                    customView,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            if (Build.VERSION.SDK_INT >= 21) {
                googleSignInPopupWindow.setElevation(5.0f);
            }
            //noinspection deprecation
            googleSignInPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            googleSignInPopupWindow.setOutsideTouchable(true);
            return googleSignInPopupWindow;
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        memory = Memory.getInstance();
        setContentView(R.layout.activity_song);
        mainLayout = findViewById(R.id.main_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        song = memory.getPassingSong();
        if (song == null) {
            finish();
            return;
        }
        tabLayout = findViewById(R.id.tablayout);
        viewPager = findViewById(R.id.viewPager);
        final SongRepository songRepository = new SongRepositoryImpl(this);
        allByVersionGroup.clear();
        String versionGroup = song.getVersionGroup();
        String uuid = song.getUuid();
        if (versionGroup == null) {
            versionGroup = uuid;
        }
        if (versionGroup != null) {
            allByVersionGroup.addAll(songRepository.findAllByVersionGroup(versionGroup));
        }
        final List<Song> songs = new ArrayList<>(allByVersionGroup.size());
        songs.add(song);
        HashMap<String, Song> hashMap = new HashMap<>(songs.size());
        for (Song song : allByVersionGroup) {
            if (!song.getUuid().equals(uuid)) {
                hashMap.put(song.getUuid(), getSongFromMemory(song));
            }
        }
        SongCollectionRepository songCollectionRepository = new SongCollectionRepositoryImpl(this);
        List<SongCollection> songCollections = songCollectionRepository.findAll();
        for (SongCollection songCollection : songCollections) {
            for (SongCollectionElement songCollectionElement : songCollection.getSongCollectionElements()) {
                String songUuid = songCollectionElement.getSongUuid();
                if (hashMap.containsKey(songUuid)) {
                    Song song = hashMap.get(songUuid);
                    song.setSongCollection(songCollection);
                    song.setSongCollectionElement(songCollectionElement);
                    songs.add(song);
                    hashMap.remove(songUuid);
                }
            }
        }
        songs.addAll(hashMap.values());
        Collections.sort(songs, new Comparator<Song>() {
            @Override
            public int compare(Song lhs, Song rhs) {
                Integer scoreL = lhs.getScore();
                if (lhs.getLanguage().getId().equals(song.getLanguage().getId())) {
                    scoreL += 1;
                }
                Integer scoreR = rhs.getScore();
                if (rhs.getLanguage().getId().equals(song.getLanguage().getId())) {
                    scoreR += 1;
                }
                if (scoreL.equals(scoreR)) {
                    return rhs.getModifiedDate().compareTo(lhs.getModifiedDate());
                }
                return scoreR.compareTo(scoreL);
            }
        });
        for (Song song : songs) {
            tabLayout.addTab(tabLayout.newTab().setText(song.getTitle()));
        }
        if (songs.size() == 1) {
            tabLayout.setVisibility(View.GONE);
        }
        pageAdapter = new PageAdapter(getSupportFragmentManager(), songs);
        viewPager.setAdapter(pageAdapter);

        int position = songs.indexOf(song);
        viewPager.setCurrentItem(position, true);
        tabLayout.setScrollPosition(position, 0f, true);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                viewPager.setCurrentItem(position);
                song = songs.get(position);
                memory.setPassingSong(song);
                setToolbarTitleAndSize();
                setUpMenu();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        final Intent fullScreenIntent = new Intent(this, FullscreenActivity.class);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullScreenIntent.putExtra("verseIndex", -1);
                startActivity(fullScreenIntent);
            }
        });
        setToolbarTitleAndSize();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        invalidateOptionsMenu();
    }

    private void setToolbarTitleAndSize() {
        android.support.v7.app.ActionBar actionbar = getSupportActionBar();
        if (actionbar == null) {
            return;
        }
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setCustomView(R.layout.song_activity_title_bar);
        final TextView title = actionbar.getCustomView().findViewById(R.id.toolbarTitle);
        title.setText(song.getTitle());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            setBlank();
        } else if (itemId == R.id.action_similar) {
            List<Song> allSimilar = SongService.findAllSimilar(song, memory.getSongs());
            memory.setValues(allSimilar);
            if (allSimilar.size() > 0) {
                setResult(1);
                finish();
            } else {
                showToaster("No similar found", Toast.LENGTH_SHORT);
            }
        } else if (itemId == R.id.action_suggest_edits) {
            Intent intent = new Intent(this, SuggestEditsChooseActivity.class);
            Song copiedSong = new Song();
            copiedSong.setUuid(song.getUuid());
            copiedSong.setId(song.getId());
            copiedSong.setTitle(song.getTitle());
            copiedSong.setVerses(song.getVerses());
            copiedSong.setSongCollection(song.getSongCollection());
            copiedSong.setSongCollectionElement(song.getSongCollectionElement());
            startActivityForResult(intent, 2);
        } else if (itemId == R.id.action_share) {
            Intent share = new Intent(android.content.Intent.ACTION_SEND);
            share.setType("text/plain");
            share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            share.putExtra(Intent.EXTRA_SUBJECT, song.getTitle());
            share.putExtra(Intent.EXTRA_TITLE, song.getTitle());
            share.putExtra(Intent.EXTRA_TEXT, song.getTitle() + ":\nhttp://192.168.100.4:8080/song/" + song.getUuid());
            startActivity(Intent.createChooser(share, "Share song!"));
        } else if (itemId == R.id.action_youtube) {
            Intent intent = new Intent(this, YoutubeActivity.class);
            Song copiedSong = new Song();
            copiedSong.setUuid(song.getUuid());
            copiedSong.setId(song.getId());
            copiedSong.setTitle(song.getTitle());
            copiedSong.setVerses(song.getVerses());
            copiedSong.setSongCollection(song.getSongCollection());
            copiedSong.setSongCollectionElement(song.getSongCollectionElement());
            copiedSong.setYoutubeUrl(song.getYoutubeUrl());
            startActivity(intent);
        } else if (itemId == R.id.action_add_to_queue) {
            QueueSongRepositoryImpl queueSongRepository = new QueueSongRepositoryImpl(this);
            QueueSong model = new QueueSong();
            model.setSong(song);
            memory.addSongToQueue(model);
            queueSongRepository.save(model);
            showToaster(getString(R.string.added_to_queue), Toast.LENGTH_SHORT);
        } else if (itemId == R.id.action_save_to_song_list) {
            saveToSongList();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("InflateParams")
    private void saveToSongList() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View customView;
        if (inflater == null) {
            return;
        }
        customView = inflater.inflate(R.layout.content_save_queue, null);
        saveToSongListPopupWindow = new PopupWindow(
                customView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        if (Build.VERSION.SDK_INT >= 21) {
            saveToSongListPopupWindow.setElevation(5.0f);
        }
        Button closeButton = customView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveToSongListPopupWindow.dismiss();
            }
        });
        ListView listView = customView.findViewById(R.id.listView);
        SongListRepositoryImpl songListRepository = new SongListRepositoryImpl(this);
        final List<SongList> songLists = songListRepository.findAll();
        List<String> all = new ArrayList<>(songLists.size());
        for (SongList songList : songLists) {
            all.add(songList.getTitle());
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                all);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SongList songList = songLists.get(position);
                List<SongListElement> songListElements = songList.getSongListElements();
                SongListElement songListElement = new SongListElement();
                songListElement.setSong(song);
                songListElement.setNumber(songListElements.size());
                songListElement.setSongList(songList);
                songListElements.add(songListElement);
                new SongListElementRepositoryImpl(SongActivity.this).save(songListElement);
                saveToSongListPopupWindow.dismiss();
            }
        });
        //noinspection deprecation
        saveToSongListPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        saveToSongListPopupWindow.setOutsideTouchable(true);
        saveToSongListPopupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);
    }

    @Override
    public void onBackPressed() {
        onBackButtonClick(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 2:
                if (resultCode == SuggestEditsChooseActivity.LINKING) {
                    finish();
                }
                break;
            case REQUEST_CODE_SIGN_IN:
                if (resultCode != RESULT_OK) {
                    Log.e(TAG, "Sign-in failed.");
                    showToaster("Sign-in failed.", Toast.LENGTH_LONG);
                    return;
                }
                Task<GoogleSignInAccount> getAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
                if (getAccountTask.isSuccessful()) {
                    GoogleSignInAccount result = getAccountTask.getResult();
                    if (result != null) {
                        saveGmail(result, getApplicationContext());
                        saveFavouriteInGoogleDrive.initializeDriveClient(result);
                    }
                } else {
                    Log.e(TAG, "Sign-in failed.");
                    showToaster("Sign-in failed.", Toast.LENGTH_LONG);
                }
                break;
        }
    }

    private void showToaster(String s, int lengthLong) {
        Toast.makeText(this, s, lengthLong).show();
    }

    private void setBlank() {
        if (memory.isShareOnNetwork()) {
            List<ProjectionTextChangeListener> projectionTextChangeListeners = memory.getProjectionTextChangeListeners();
            if (projectionTextChangeListeners != null) {
                for (int i = 0; i < projectionTextChangeListeners.size(); ++i) {
                    projectionTextChangeListeners.get(i).onSetText("");
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        setUpMenu();
        return true;
    }

    private void setUpMenu() {
        if (menu == null) {
            return;
        }
        menu.clear();
        getMenuInflater().inflate(R.menu.content_song_menu, menu);
        MenuItem showSimilarMenuItem = menu.findItem(R.id.action_similar);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean show_similar = sharedPreferences.getBoolean("show_similar", false);
        if (!show_similar) {
            showSimilarMenuItem.setVisible(false);
            menu.removeItem(showSimilarMenuItem.getItemId());
        }
        MenuItem youtubeMenuItem = menu.findItem(R.id.action_youtube);
        if (song.getYoutubeUrl() == null) {
            youtubeMenuItem.setVisible(false);
            menu.removeItem(youtubeMenuItem.getItemId());
        }
        if (song.getUuid() == null) {
            MenuItem shareMenuItem = menu.findItem(R.id.action_share);
            shareMenuItem.setVisible(false);
            menu.removeItem(shareMenuItem.getItemId());
        }
        favouriteMenuItem = menu.findItem(R.id.action_favourite);
        final SongActivity context = this;
        if (song.isFavourite()) {
            favouriteMenuItem.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_star_black_24dp, null));
        }
        favouriteMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                song.setFavourite(!song.isFavourite());
                FavouriteSong favourite = song.getFavourite();
                favourite.setModifiedDate(new Date());
                favourite.setFavouritePublished(favourite.isFavouriteNotPublished());
                FavouriteSongRepository favouriteSongRepository = new FavouriteSongRepositoryImpl(context);
                favouriteSongRepository.save(favourite);
                favouriteMenuItem.setIcon(ResourcesCompat.getDrawable(getResources(), song.isFavourite() ?
                        R.drawable.ic_star_black_24dp : R.drawable.ic_star_border_black_24dp, null));
                syncFavouriteInGoogleDrive();
                return false;
            }
        });
    }

    private void syncFavouriteInGoogleDrive() {
        saveFavouriteInGoogleDrive = new SaveFavouriteInGoogleDrive(new GoogleSignInIntent() {
            @Override
            public void task(Intent signInIntent) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SongActivity.this);
                boolean showGoogleSignIn = sharedPreferences.getBoolean("ShowGoogleSignInWhenFavouriteChanges", true);
                if (!showGoogleSignIn) {
                    return;
                }
                SongActivity.this.signInIntent = signInIntent;
                googleSignInPopupWindow = showGoogleSignIn((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE), false);
                if (googleSignInPopupWindow != null) {
                    googleSignInPopupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);
                }
            }
        }, this, song);
        saveFavouriteInGoogleDrive.signIn();
    }

    public void onBackButtonClick(View view) {
        if (googleSignInPopupWindow != null && googleSignInPopupWindow.isShowing()) {
            googleSignInPopupWindow.dismiss();
            return;
        }
        setBlank();
        finish();
    }

    public void onGoogleSignIn(View view) {
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
        googleSignInPopupWindow.dismiss();
    }

    public void onDontShowAgain(View view) {
        googleSignInPopupWindow.dismiss();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putBoolean("ShowGoogleSignInWhenFavouriteChanges", false).apply();
    }

    public void onNewSongListClick(View view) {
        saveToSongListPopupWindow.dismiss();
        Intent intent = new Intent(this, NewSongListActivity.class);
        intent.putExtra("addSongToSongList", true);
        memory.setPassingSong(song);
        startActivity(intent);
    }
}
