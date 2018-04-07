package com.bence.songbook.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.LongSparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.api.SongApiBean;
import com.bence.songbook.models.Language;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongCollection;
import com.bence.songbook.models.SongCollectionElement;
import com.bence.songbook.models.SongVerse;
import com.bence.songbook.repository.SongRepository;
import com.bence.songbook.repository.impl.ormLite.LanguageRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongCollectionRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongRepositoryImpl;
import com.bence.songbook.ui.utils.Preferences;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings("ConstantConditions")
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final Memory memory = Memory.getInstance();
    private List<Song> songs;
    private List<Song> values;
    private Switch inSongSearchSwitch;
    private String lastSearchedText = "";
    private Thread loadSongVersesThread;
    private Toast searchInSongTextIsAvailableToast;
    private boolean searchInSongTextIsAvailable;
    private SongRepository songRepository;
    private LinearLayout linearLayout;
    private PopupWindow filterPopupWindow;
    private PopupWindow selectLanguagePopupWindow;
    private MainActivity mainActivity;
    private List<Language> languages;
    private EditText editText;
    private ListView songListView;
    private TextWatcher previousTextWatcher;
    private PopupWindow sortPopupWindow;
    private int sortMethod;
    private PopupWindow collectionPopupWindow;
    private List<SongCollection> songCollections;
    private SongCollectionAdapter songCollectionAdapter;
    private ListView collectionListView;
    private LanguageRepositoryImpl languageRepository;
    private SongCollectionRepositoryImpl songCollectionRepository;
    private int collectionPosition;
    private SongAdapter adapter;
    private boolean reverseSortMethod;

    public static String stripAccents(String s) {
        String nfdNormalizedString = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        s = pattern.matcher(nfdNormalizedString).replaceAll("");
        s = s.replaceAll("[^a-zA-Z0-9]", "");
        return s;
    }

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSortMethod();
        editText = findViewById(R.id.titleSearchEditText);
        songListView = findViewById(R.id.listView);
        inSongSearchSwitch = findViewById(R.id.inSongSearchSwitch);
        mainActivity = this;
        linearLayout = findViewById(R.id.mainLinearLayout);
        languageRepository = new LanguageRepositoryImpl(getApplicationContext());
        languages = languageRepository.findAll();
        songCollections = memory.getSongCollections();
        songCollectionRepository = new SongCollectionRepositoryImpl(getApplicationContext());
        if (songCollections == null) {
            songCollections = songCollectionRepository.findAll();
            memory.setSongCollections(songCollections);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                hideKeyboard();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                hideKeyboard();
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Intent intent = new Intent(this, ExplanationActivity.class);
                startActivity(intent);
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET}, 1);
        }
        songs = memory.getSongs();
        createLoadSongVerseThread();
        songRepository = new SongRepositoryImpl(getApplicationContext());
        searchInSongTextIsAvailableToast = Toast.makeText(getApplicationContext(), R.string.SearchInSongTextIsAvailable, Toast.LENGTH_LONG);
        if (songs != null) {
            filter();
            loadAll();
        } else {
            songs = songRepository.findAll();
            if (songs.size() > 0) {
                Memory memory = Memory.getInstance();
                memory.setSongs(songs);
                filter();
                loadAll();
//                TODO
//                new Downloader().execute();
                loadSongVersesThread.start();
                Thread uploadViews = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        Date lastUploadedViewsDate = new Date(sharedPreferences.getLong("lastUploadedViewsDate", 0));
                        Date now = new Date();
                        long lastInterval = 86400000; // one day
                        long nowTime = now.getTime();
                        long lastUploadedViewsDateTime = lastUploadedViewsDate.getTime();
                        SongApiBean songApiBean = new SongApiBean();
                        if (nowTime - lastUploadedViewsDateTime > lastInterval) {
                            List<Song> uploadingSongs = new ArrayList<>();
                            for (Song song : songs) {
                                if (song.getLastAccessed().getTime() > lastUploadedViewsDateTime && song.getModifiedDate().getTime() != 123L) {
                                    uploadingSongs.add(song);
                                }
                            }
                            Collections.sort(uploadingSongs, new Comparator<Song>() {
                                @Override
                                public int compare(Song song1, Song song2) {
                                    return song1.getLastAccessed().compareTo(song2.getLastAccessed());
                                }
                            });
                            boolean oneUploaded = false;
                            boolean successfully = true;
                            for (Song song : uploadingSongs) {
                                if (songApiBean.uploadView(song) == null) {
                                    successfully = false;
                                    break;
                                } else {
                                    oneUploaded = true;
                                    lastUploadedViewsDateTime = song.getLastAccessed().getTime();
                                }
                            }
                            if (successfully) {
                                lastUploadedViewsDateTime = nowTime;
                            }
                            if (oneUploaded) {
                                sharedPreferences.edit().putLong("lastUploadedViewsDate", lastUploadedViewsDateTime).apply();
                            }
                        }

                        // upload songs
                        List<Song> uploadingSongs = new ArrayList<>();
                        for (Song song : songs) {
                            if (song.getModifiedDate().getTime() == 123L) {
                                uploadingSongs.add(song);
                            }
                        }
                        for (Song song : uploadingSongs) {
                            final Song uploadedSong = songApiBean.uploadSong(song);
                            if (uploadedSong != null && !uploadedSong.getUuid().trim().isEmpty()) {
                                song.setUuid(uploadedSong.getUuid());
                                song.setModifiedDate(uploadedSong.getModifiedDate());
                                songRepository.save(song);
                            }
                        }
                    }
                });
                uploadViews.start();
            } else {
                Intent loadIntent = new Intent(this, LanguagesActivity.class);
                startActivityForResult(loadIntent, 1);
            }
        }
    }

    private void initSortMethod() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sortMethod = sharedPreferences.getInt("sortMethod", 5);
        reverseSortMethod = sharedPreferences.getBoolean("reverseSortMethod", false);
    }

    private void createLoadSongVerseThread() {
        loadSongVersesThread = new Thread() {
            @Override
            public void run() {
                try {
                    for (Song song : songs) {
                        song.fetchVerses();
                    }
                    searchInSongTextIsAvailableToast.show();
                    searchInSongTextIsAvailable = true;
                } catch (Exception ignored) {
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode >= 1) {
                songs = songRepository.findAll();
                memory.setSongs(songs);
                languages = languageRepository.findAll();
                songCollections = songCollectionRepository.findAll();
                memory.setSongCollections(songCollections);
                selectLanguagePopupWindow = null;
                collectionPopupWindow = null;
                filterPopupWindow = null;
                createLoadSongVerseThread();
                loadSongVersesThread.start();
                filter();
                loadAll();
                Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.downloaded) + " " + (resultCode - 1), Toast.LENGTH_SHORT);
                toast.show();
            }
        } else if (requestCode == 2) {
            recreate();
        } else if (requestCode == 3 && resultCode == 1) {
            values.clear();
            values.addAll(memory.getValues());
        } else if (requestCode == 4 && resultCode == 1) {
            songs = memory.getSongs();
            values.clear();
            values.add(songs.get(songs.size() - 1));
            adapter.setSongList(values);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    loadAll();
                } else {
                    Intent intent = new Intent(this, ExplanationActivity.class);
                    startActivity(intent);
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissPopups();
    }

    private void dismissPopups() {
        if (sortPopupWindow != null) {
            sortPopupWindow.dismiss();
        }
        if (filterPopupWindow != null) {
            filterPopupWindow.dismiss();
        }
        if (selectLanguagePopupWindow != null) {
            selectLanguagePopupWindow.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissPopups();
    }

    public void loadAll() {
        if (songs != null) {
            sortSongs(songs);
            values = new ArrayList<>();
            values.addAll(songs);
            adapter = new SongAdapter(this, R.layout.content_song_list_row, values);
            songListView.setAdapter(adapter);
            titleSearch("");
            songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Song tmp = values.get(position);
                    showSongFullscreen(tmp);
                }

            });

            TextWatcher watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    System.out.println(editable.toString());
                    String enteredText = editable.toString().trim();
                    search(enteredText, adapter);
                    lastSearchedText = enteredText;
                    if (enteredText.equals("show similar")) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        sharedPreferences.edit().putBoolean("show_similar", true).apply();
                    }
                }
            };
            if (previousTextWatcher != null) {
                editText.removeTextChangedListener(previousTextWatcher);
            }
            editText.addTextChangedListener(watcher);
            previousTextWatcher = watcher;
            inSongSearchSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (searchInSongTextIsAvailable) {
                        search(lastSearchedText, adapter);
                    } else {
                        if (!loadSongVersesThread.isAlive()) {
                            try {
                                loadSongVersesThread.start();
                            } catch (IllegalThreadStateException e) {
                                createLoadSongVerseThread();
                                loadSongVersesThread.start();
                            }
                        }
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.You_need_to_wait_for_this_feature, Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            });
        }
    }

    public void search(String text, SongAdapter adapter) {
        if (inSongSearchSwitch.isChecked() && (searchInSongTextIsAvailable)) {
            inSongSearch(text);
        } else {
            titleSearch(text);
        }
        adapter.setSongList(values);
    }

    public void titleSearch(String title) {
        values.clear();
        String text = stripAccents(title.toLowerCase());
        for (int i = 0; i < songs.size(); ++i) {
            Song song = songs.get(i);
            SongCollectionElement songCollectionElement = song.getSongCollectionElement();
            if (song.getStrippedTitle().contains(text) ||
                    (songCollectionElement != null && songCollectionElement.getOrdinalNumber().contains(text.split(" ")[0]))) {
                values.add(song);
            }
        }
    }

    public void inSongSearch(String title) {
        if (!searchInSongTextIsAvailable) {
            titleSearch(title);
            return;
        }
        values.clear();
        String text = stripAccents(title.toLowerCase());
        for (int i = 0; i < songs.size(); ++i) {
            Song song = songs.get(i);
            boolean contains = song.getStrippedTitle().contains(text);
            if (!contains) {
                for (SongVerse verse : song.getVerses()) {
                    if (verse.getStrippedText().contains(text)) {
                        contains = true;
                        break;
                    }
                }
            }
            if (contains) {
                values.add(song);
            }
        }
    }

    public void showSongFullscreen(Song song) {
        Intent intent = new Intent(this, SongActivity.class);
        Song copiedSong = new Song();
        copiedSong.setUuid(song.getUuid());
        copiedSong.setId(song.getId());
        copiedSong.setTitle(song.getTitle());
        copiedSong.setVerses(song.getVerses());
        copiedSong.setAccessedTimes(song.getAccessedTimes());
        copiedSong.setAccessedTimeAverage(song.getAccessedTimeAverage());
        copiedSong.setLastAccessed(song.getLastAccessed());
        copiedSong.setSongCollection(song.getSongCollection());
        copiedSong.setSongCollectionElement(song.getSongCollectionElement());
        intent.putExtra("Song", copiedSong);
        intent.putExtra("verseIndex", 0);
        startActivityForResult(intent, 3);
    }

    private void sortSongs(List<Song> all) {
        if (sortMethod == 0) {
            Collections.sort(all, new Comparator<Song>() {
                @Override
                public int compare(Song lhs, Song rhs) {
                    return rhs.getModifiedDate().compareTo(lhs.getModifiedDate());
                }
            });
        } else if (sortMethod == 1) {
            Collections.sort(all, new Comparator<Song>() {
                @Override
                public int compare(Song lhs, Song rhs) {
                    return lhs.getStrippedTitle().compareTo(rhs.getStrippedTitle());
                }
            });
        } else if (sortMethod == 3) {
            Collections.sort(all, new Comparator<Song>() {
                @Override
                public int compare(Song lhs, Song rhs) {
                    return lhs.getCreatedDate().compareTo(rhs.getCreatedDate());
                }
            });
        } else if (sortMethod == 5) {
            Collections.sort(all, new Comparator<Song>() {
                @Override
                public int compare(Song lhs, Song rhs) {
                    return rhs.getLastAccessed().compareTo(lhs.getLastAccessed());
                }
            });
        } else if (sortMethod == 6) {
            Collections.sort(all, new Comparator<Song>() {
                @Override
                public int compare(Song lhs, Song rhs) {
                    try {
                        SongCollection lhsSongCollection = lhs.getSongCollection();
                        SongCollection rhsSongCollection = rhs.getSongCollection();
                        if (lhsSongCollection == null) {
                            if (rhsSongCollection == null) {
                                return lhs.getTitle().compareTo(rhs.getTitle());
                            } else {
                                return 1;
                            }
                        } else {
                            if (rhsSongCollection == null) {
                                return -1;
                            } else {
                                int compareTo = lhsSongCollection.getName().compareTo(rhsSongCollection.getName());
                                if (compareTo == 0) {
                                    String lhsOrdinalNumber = lhs.getSongCollectionElement().getOrdinalNumber();
                                    String rhsOrdinalNumber = rhs.getSongCollectionElement().getOrdinalNumber();
                                    return lhsOrdinalNumber.compareTo(rhsOrdinalNumber);
                                }
                                return compareTo;
                            }
                        }
                    } catch (Exception e) {
                        return 0;
                    }
                }
            });
        }
        if (reverseSortMethod) {
            Collections.reverse(all);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (sortPopupWindow != null && sortPopupWindow.isShowing()) {
            sortPopupWindow.dismiss();
        } else if (selectLanguagePopupWindow != null && selectLanguagePopupWindow.isShowing()) {
            selectLanguagePopupWindow.dismiss();
        } else if (collectionPopupWindow != null && collectionPopupWindow.isShowing()) {
            collectionPopupWindow.dismiss();
        } else if (filterPopupWindow != null && filterPopupWindow.isShowing()) {
            filterPopupWindow.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        hideKeyboard();
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_download_songs) {
            Intent loadIntent = new Intent(this, LanguagesActivity.class);
            startActivityForResult(loadIntent, 1);
        } else if (id == R.id.nav_settings) {
            Intent loadIntent = new Intent(this, SettingsActivity.class);
            startActivityForResult(loadIntent, 2);
        } else if (id == R.id.nav_new_song) {
            Intent loadIntent = new Intent(this, NewSongActivity.class);
            startActivityForResult(loadIntent, 4);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onSortButtonClick(View view) {
        if (sortPopupWindow != null) {
            sortPopupWindow.dismiss();
        } else {
            createSortPopup();
        }
        hideKeyboard();
        sortPopupWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);
    }

    private void createSortPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View customView = inflater.inflate(R.layout.content_sort, null);
        sortPopupWindow = new PopupWindow(
                customView,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        final Switch reverseSwitch = customView.findViewById(R.id.reverseSwitch);
        RadioGroup radioGroup = customView.findViewById(R.id.radioSort);
        if (sortMethod == 0) {
            radioGroup.check(R.id.modifiedDateRadioButton);
        } else if (sortMethod == 1) {
            radioGroup.check(R.id.byTitleRadioButton);
        } else if (sortMethod == 3) {
            radioGroup.check(R.id.byCreatedDateRadioButton);
        } else if (sortMethod == 5) {
            radioGroup.check(R.id.recentlyViewedRadioButton);
        } else if (sortMethod == 6) {
            radioGroup.check(R.id.byCollectionRadioButton);
        } else if (sortMethod == 2) {
            reverseSortMethod = true;
            sortMethod = 1;
            saveReverseSortMethod();
            radioGroup.check(R.id.byTitleRadioButton);
        } else if (sortMethod == 4) {
            reverseSortMethod = true;
            sortMethod = 3;
            saveReverseSortMethod();
            radioGroup.check(R.id.byCreatedDateRadioButton);
        }
        reverseSwitch.setChecked(reverseSortMethod);
        reverseSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reverseSortMethod = reverseSwitch.isChecked();
                saveReverseSortMethod();
                loadAll();
                sortPopupWindow.dismiss();
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.modifiedDateRadioButton) {
                    sortMethod = 0;
                } else if (checkedId == R.id.byTitleRadioButton) {
                    sortMethod = 1;
                } else if (checkedId == R.id.byCreatedDateRadioButton) {
                    sortMethod = 3;
                } else if (checkedId == R.id.recentlyViewedRadioButton) {
                    sortMethod = 5;
                } else if (checkedId == R.id.byCollectionRadioButton) {
                    sortMethod = 6;
                }
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                sharedPreferences.edit().putInt("sortMethod", sortMethod).apply();
                loadAll();
                sortPopupWindow.dismiss();
            }
        });
        if (Build.VERSION.SDK_INT >= 21) {
            sortPopupWindow.setElevation(5.0f);
        }
        sortPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        sortPopupWindow.setOutsideTouchable(true);
    }

    private void saveReverseSortMethod() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        sharedPreferences.edit().putBoolean("reverseSortMethod", reverseSortMethod).apply();
    }

    public void onFilterButtonClick(View view) {
        if (filterPopupWindow != null) {
            filterPopupWindow.dismiss();
        } else {
            createFilterPopupWindow();
        }
        hideKeyboard();
        filterPopupWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);
    }

    private void createFilterPopupWindow() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.content_filter, null);
        filterPopupWindow = new PopupWindow(
                customView,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        if (Build.VERSION.SDK_INT >= 21) {
            filterPopupWindow.setElevation(5.0f);
        }
        ImageButton closeButton = customView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterPopupWindow.dismiss();
            }
        });
        ListView listView = customView.findViewById(R.id.listView);
        ArrayList<String> values = new ArrayList<>();
        int k = 0;
        final int languagePosition = k;
        values.add(getString(R.string.language));
        collectionPosition = -1;
        if (songCollections.size() > 0) {
            collectionPosition = ++k;
            values.add(getString(R.string.collection));
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == languagePosition) {
                    if (selectLanguagePopupWindow != null) {
                        selectLanguagePopupWindow.dismiss();
                    } else {
                        createSelectLanguagePopup();
                    }
                    hideKeyboard();
                    selectLanguagePopupWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);
                } else if (position == collectionPosition) {
                    if (collectionPopupWindow != null) {
                        collectionPopupWindow.dismiss();
                    } else {
                        createCollectionPopup();
                    }
                    hideKeyboard();
                    sortCollectionBySelectedLanguages();
                    collectionPopupWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);
                }
            }
        });
        filterPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        filterPopupWindow.setOutsideTouchable(true);
    }

    private void sortCollectionBySelectedLanguages() {
        boolean oneLanguageSelected = isOneLanguageSelected();
        LongSparseArray<Object> languageHashMap = new LongSparseArray<>(languages.size());
        for (Language language : languages) {
            if (oneLanguageSelected) {
                if (language.isSelected()) {
                    languageHashMap.put(language.getId(), true);
                }
            } else {
                languageHashMap.put(language.getId(), false);
            }
        }
        List<SongCollection> filteredSongCollections = new ArrayList<>();
        for (SongCollection songCollection : songCollections) {
            if (songCollection.getLanguage() == null || languageHashMap.get(songCollection.getLanguage().getId()) != null) {
                filteredSongCollections.add(songCollection);
            }
        }
        for (SongCollection songCollection : songCollections) {
            if (!filteredSongCollections.contains(songCollection)) {
                filteredSongCollections.add(songCollection);
            }
        }
        songCollectionAdapter = new SongCollectionAdapter(mainActivity,
                R.layout.activity_language_checkbox_row, filteredSongCollections);
        collectionListView.setAdapter(songCollectionAdapter);
    }

    private void createCollectionPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.content_select_collection, null);
        collectionPopupWindow = new PopupWindow(
                customView,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        if (Build.VERSION.SDK_INT >= 21) {
            collectionPopupWindow.setElevation(5.0f);
        }
        Button selectButton = customView.findViewById(R.id.selectButton);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter();
                loadAll();
                collectionPopupWindow.dismiss();
                filterPopupWindow.dismiss();
            }
        });
        songCollectionAdapter = new SongCollectionAdapter(mainActivity,
                R.layout.activity_language_checkbox_row, songCollections);
        collectionListView = customView.findViewById(R.id.listView);
        collectionListView.setAdapter(songCollectionAdapter);
        collectionPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        collectionPopupWindow.setOutsideTouchable(true);
    }

    private void filter() {
        filterSongsByLanguage();
        filterSongsByCollection();
    }

    private void filterSongsByCollection() {
        HashMap<String, Song> hashMap = new HashMap<>(songs.size());
        for (Song song : songs) {
            hashMap.put(song.getUuid(), song);
        }
        songs.clear();
        if (ifOneSelected()) {
            for (SongCollection songCollection : songCollections) {
                if (songCollection.isSelected()) {
                    for (SongCollectionElement songCollectionElement : songCollection.getSongCollectionElements()) {
                        String songUuid = songCollectionElement.getSongUuid();
                        if (hashMap.containsKey(songUuid)) {
                            Song song = hashMap.get(songUuid);
                            song.setSongCollection(songCollection);
                            song.setSongCollectionElement(songCollectionElement);
                            songs.add(song);
                        }
                    }
                }
            }
        } else {
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
        }
    }

    private boolean ifOneSelected() {
        for (SongCollection songCollection : songCollections) {
            if (songCollection.isSelected()) {
                return true;
            }
        }
        return false;
    }

    private void createSelectLanguagePopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.content_select_languages, null);
        selectLanguagePopupWindow = new PopupWindow(
                customView,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        if (Build.VERSION.SDK_INT >= 21) {
            selectLanguagePopupWindow.setElevation(5.0f);
        }
        Button selectButton = customView.findViewById(R.id.selectButton);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                languageRepository.save(languages);
                filter();
                loadAll();
                selectLanguagePopupWindow.dismiss();
                filterPopupWindow.dismiss();
            }
        });
        LanguageAdapter dataAdapter = new LanguageAdapter(mainActivity,
                R.layout.activity_language_checkbox_row, languages);
        ListView listView = customView.findViewById(R.id.listView);
        listView.setAdapter(dataAdapter);
        selectLanguagePopupWindow.setBackgroundDrawable(new BitmapDrawable());
        selectLanguagePopupWindow.setOutsideTouchable(true);
    }

    private void filterSongsByLanguage() {
        songs.clear();
        if (isOneLanguageSelected()) {
            for (Language language : languages) {
                if (language.isSelected()) {
                    songs.addAll(language.getSongs());
                }
            }
        }
        if (songs.size() == 0) {
            songs = songRepository.findAll();
        }
    }

    private boolean isOneLanguageSelected() {
        for (Language language : languages) {
            if (language.isSelected()) {
                return true;
            }
        }
        return false;
    }

    private class LanguageAdapter extends ArrayAdapter<Language> {

        private List<Language> languageList;

        LanguageAdapter(Context context, int textViewResourceId,
                        List<Language> languageList) {
            super(context, textViewResourceId, languageList);
            this.languageList = new ArrayList<>();
            this.languageList.addAll(languageList);
        }

        @SuppressLint({"InflateParams", "SetTextI18n"})
        @SuppressWarnings("ConstantConditions")
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            LanguageAdapter.ViewHolder holder;

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.activity_language_checkbox_row, null);

                holder = new LanguageAdapter.ViewHolder();
                holder.textView = convertView.findViewById(R.id.code);
                holder.checkBox = convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);

                holder.checkBox.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        CheckBox checkBox = (CheckBox) view;
                        Language language = (Language) checkBox.getTag();
                        language.setSelected(checkBox.isChecked());
                    }
                });
            } else {
                holder = (LanguageAdapter.ViewHolder) convertView.getTag();
            }

            Language language = languageList.get(position);
            holder.textView.setText(" (" + language.getNativeName() + ")");
            holder.checkBox.setText(language.getEnglishName());
            holder.checkBox.setChecked(language.isSelected());
            holder.checkBox.setTag(language);

            return convertView;
        }

        private class ViewHolder {
            TextView textView;
            CheckBox checkBox;
        }

    }

    private class SongCollectionAdapter extends ArrayAdapter<SongCollection> {

        private List<SongCollection> songCollectionList;

        SongCollectionAdapter(Context context, int textViewResourceId,
                              List<SongCollection> songCollectionList) {
            super(context, textViewResourceId, songCollectionList);
            this.songCollectionList = new ArrayList<>();
            this.songCollectionList.addAll(songCollectionList);
        }

        @SuppressLint({"InflateParams", "SetTextI18n"})
        @SuppressWarnings("ConstantConditions")
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            SongCollectionAdapter.ViewHolder holder;

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.checkbox_row, null);

                holder = new SongCollectionAdapter.ViewHolder();
                holder.textView = convertView.findViewById(R.id.code);
                holder.checkBox = convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);

                holder.checkBox.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        CheckBox checkBox = (CheckBox) view;
                        SongCollection songCollection = (SongCollection) checkBox.getTag();
                        songCollection.setSelected(checkBox.isChecked());
                    }
                });
            } else {
                holder = (SongCollectionAdapter.ViewHolder) convertView.getTag();
            }
            if (0 <= position && position < songCollectionList.size()) {
                SongCollection songCollection = songCollectionList.get(position);
                holder.checkBox.setText(songCollection.getName());
                holder.checkBox.setChecked(songCollection.isSelected());
                holder.checkBox.setTag(songCollection);
            }
            return convertView;
        }

        public void setList(List<SongCollection> filteredSongCollections) {
            songCollectionList.clear();
            songCollectionList.addAll(filteredSongCollections);
            this.notifyDataSetChanged();
        }

        private class ViewHolder {
            TextView textView;
            CheckBox checkBox;
        }

    }

    private class SongAdapter extends ArrayAdapter<Song> {

        private List<Song> songList;

        SongAdapter(Context context, int textViewResourceId,
                    List<Song> songList) {
            super(context, textViewResourceId, songList);
            this.songList = new ArrayList<>();
            this.songList.addAll(songList);
        }

        @SuppressLint({"InflateParams", "SetTextI18n"})
        @SuppressWarnings("ConstantConditions")
        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

            SongAdapter.ViewHolder holder;

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.content_song_list_row, null);

                holder = new SongAdapter.ViewHolder();
                holder.ordinalNumberTextView = convertView.findViewById(R.id.ordinalNumberTextView);
                holder.titleTextView = convertView.findViewById(R.id.titleTextView);
                convertView.setTag(holder);
            } else {
                holder = (SongAdapter.ViewHolder) convertView.getTag();
            }

            Song song = songList.get(position);
            SongCollection songCollection = song.getSongCollection();
            if (songCollection != null) {
                String text = songCollection.getName() + " " + song.getSongCollectionElement().getOrdinalNumber();
                holder.ordinalNumberTextView.setText(text);
//                For some reason this is not working...
//                holder.ordinalNumberTextView.setVisibility(View.VISIBLE);
            } else {
                holder.ordinalNumberTextView.setText("");
//                For some reason this is not working...
//                holder.ordinalNumberTextView.setVisibility(View.GONE);
            }
            holder.titleTextView.setText(song.getTitle());
            holder.titleTextView.setTag(song);

            return convertView;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        void setSongList(List<Song> songs) {
            songList.clear();
            songList.addAll(songs);
            this.notifyDataSetChanged();
        }

        private class ViewHolder {
            TextView ordinalNumberTextView;
            TextView titleTextView;
        }

    }

    @SuppressLint("StaticFieldLeak")
    class Downloader extends AsyncTask<Void, Integer, Void> {

        private List<Song> newSongs;
        private List<Song> songsAfterModifiedDate;

        @Override
        protected Void doInBackground(Void... voids) {
            final SongApiBean songApiBean = new SongApiBean();
            Date modifiedDate = songs.get(0).getModifiedDate();
            songsAfterModifiedDate = songApiBean.getSongsAfterModifiedDate(modifiedDate);
            if (songsAfterModifiedDate != null) {
                newSongs = new ArrayList<>();
                for (Song modifiedSong : songsAfterModifiedDate) {
                    Song song = songRepository.findByUUID(modifiedSong.getUuid());
                    if (song == null && !modifiedSong.isDeleted()) {
                        newSongs.add(modifiedSong);
                    } else {
                        songRepository.delete(song);
                        if (!modifiedSong.isDeleted()) {
                            songRepository.save(modifiedSong);
                        }
                    }
                }
                songRepository.save(newSongs);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (newSongs != null && songsAfterModifiedDate != null && songsAfterModifiedDate.size() > 0) {
                if (newSongs.size() == songsAfterModifiedDate.size()) {
                    songs.addAll(newSongs);
                } else {
                    songs = songRepository.findAll();
                }
                memory.setSongs(songs);
                loadAll();
                if (loadSongVersesThread.isAlive()) {
                    loadSongVersesThread.interrupt();
                }
                createLoadSongVerseThread();
                loadSongVersesThread.start();
            }
        }
    }

}
