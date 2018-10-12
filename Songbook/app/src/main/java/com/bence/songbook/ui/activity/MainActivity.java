package com.bence.songbook.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.api.SongApiBean;
import com.bence.songbook.models.FavouriteSong;
import com.bence.songbook.models.Language;
import com.bence.songbook.models.QueueSong;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongCollection;
import com.bence.songbook.models.SongCollectionElement;
import com.bence.songbook.models.SongVerse;
import com.bence.songbook.repository.FavouriteSongRepository;
import com.bence.songbook.repository.SongRepository;
import com.bence.songbook.repository.impl.ormLite.FavouriteSongRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.LanguageRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.QueueSongRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongCollectionRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongRepositoryImpl;
import com.bence.songbook.ui.utils.DynamicListView;
import com.bence.songbook.ui.utils.GoogleSignInIntent;
import com.bence.songbook.ui.utils.Preferences;
import com.bence.songbook.ui.utils.QueueSongAdapter;
import com.bence.songbook.ui.utils.SyncFavouriteInGoogleDrive;
import com.bence.songbook.ui.utils.SyncInBackground;
import com.bence.songbook.utils.Utility;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import static com.bence.songbook.ui.activity.SongActivity.saveGmail;
import static com.bence.songbook.ui.activity.SongActivity.showGoogleSignIn;
import static com.bence.songbook.ui.utils.SaveFavouriteInGoogleDrive.REQUEST_CODE_SIGN_IN;

@SuppressWarnings({"ConstantConditions", "deprecation"})
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final Memory memory = Memory.getInstance();
    private final String TAG = "MainActivity";
    private final int DOWNLOAD_SONGS_REQUEST_CODE = 1;
    private List<Song> songs;
    private List<Song> values;
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
    private ListView songListView;
    private PopupWindow sortPopupWindow;
    private int sortMethod;
    private PopupWindow collectionPopupWindow;
    private List<SongCollection> songCollections;
    private ListView collectionListView;
    private LanguageRepositoryImpl languageRepository;
    private SongCollectionRepositoryImpl songCollectionRepository;
    private SongAdapter adapter;
    private boolean reverseSortMethod;
    private boolean shortCollectionName;
    private boolean light_theme_switch;
    private boolean wasOrdinalNumber;
    private Switch containingVideosSwitch;
    private Switch favouriteSwitch;
    private List<FavouriteSong> favouriteSongs;
    private SyncFavouriteInGoogleDrive syncFavouriteInGoogleDrive;
    private Date lastDatePressedAtEnd;
    private PopupWindow googleSignInPopupWindow;
    private boolean gSignIn;
    private MenuItem signInMenuItem;
    private boolean inSongSearchSwitch = false;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private DynamicListView queueListView;
    private MenuItem searchItem;
    private QueueSongRepositoryImpl queueSongRepository;
    private View clearAllButton;

    public static String stripAccents(String s) {
        String nfdNormalizedString = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        s = pattern.matcher(nfdNormalizedString).replaceAll("");
        s = s.replaceAll("[^a-zA-Z0-9]", "");
        return s;
    }

    @SuppressLint({"ShowToast", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int queueIndex = sharedPreferences.getInt("queueIndex", -1);
        memory.setQueueIndex(queueIndex, this);
        queueListView = findViewById(R.id.queueList);
        queueListView.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow NestedScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow NestedScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });

        queueListView.setListener(new DynamicListView.Listener() {
            @Override
            public void swapElements(int indexOne, int indexTwo) {
                List<QueueSong> values = memory.getQueue();
                QueueSong temp = values.get(indexOne);
                QueueSong secondTmp = values.get(indexTwo);
                int queueNumber = temp.getQueueNumber();
                temp.setQueueNumber(secondTmp.getQueueNumber());
                secondTmp.setQueueNumber(queueNumber);
                values.set(indexOne, secondTmp);
                values.set(indexTwo, temp);
                queueSongRepository.save(temp);
                queueSongRepository.save(secondTmp);
            }

            @Override
            public void deleteElement(int originalItem) {
                List<QueueSong> values = memory.getQueue();
                QueueSong temp = values.get(originalItem);
                System.out.println(temp.getSong().getTitle());
                memory.removeQueueSong(temp);
                queueSongRepository.delete(temp);
                queueListView.invalidateViews();
                queueListView.refreshDrawableState();
            }
        });
        queueSongRepository = new QueueSongRepositoryImpl(this);
        final LinearLayout llBottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        clearAllButton = findViewById(R.id.clearAllButton);
        clearAllButton.setVisibility(View.INVISIBLE);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    searchItem.collapseActionView();
                    clearAllButton.setVisibility(View.VISIBLE);
                } else {
                    clearAllButton.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        memory.addOnQueueChangeListener(new Memory.Listener() {
            @Override
            public void onAdd(QueueSong queueSong) {
                if (memory.getQueue().size() > 0) {
                    if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                    bottomSheetBehavior.setHideable(false);
                    bottomSheetBehavior.setSkipCollapsed(false);
                }
            }

            @Override
            public void onRemove(QueueSong queueSong) {
                if (memory.getQueue().size() < 1) {
                    setBottomSheetHideable();
                    if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    }
                }
            }
        });

        initPreferences();
        memory.setMainActivity(this);
        songListView = findViewById(R.id.listView);
        mainActivity = this;
        linearLayout = findViewById(R.id.mainLinearLayout);
        languageRepository = new LanguageRepositoryImpl(getApplicationContext());
        languages = languageRepository.findAll();
        songCollections = memory.getSongCollections();
        favouriteSongs = memory.getFavouriteSongs();
        songCollectionRepository = new SongCollectionRepositoryImpl(getApplicationContext());
        if (songCollections == null) {
            songCollections = songCollectionRepository.findAll();
            setShortNamesForSongCollections(songCollections);
            memory.setSongCollections(songCollections);
        }
        if (favouriteSongs == null) {
            FavouriteSongRepository favouriteSongRepository = new FavouriteSongRepositoryImpl(this);
            favouriteSongs = favouriteSongRepository.findAll();
            memory.setFavouriteSongs(favouriteSongs);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        //noinspection NullableProblems
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

        Menu menu = navigationView.getMenu();

        gSignIn = sharedPreferences.getBoolean("gSignIn", false);
        signInMenuItem = menu.findItem(R.id.nav_sign_in);
        if (gSignIn) {
            if (signInMenuItem != null) {
                signInMenuItem.setTitle(getString(R.string.sign_out));
            }
        }
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
            songs = new ArrayList<>();
            filter();
            if (songs.size() > 0) {
                setDataToQueueSongs();
                Memory memory = Memory.getInstance();
                memory.setSongs(songs);
                loadAll();
                loadSongVersesThread.start();
                uploadViewsFavourites();
            } else {
                Intent loadIntent = new Intent(this, LanguagesActivity.class);
                startActivityForResult(loadIntent, 1);
            }
        }
        Intent appLinkIntent = getIntent();
        Uri appLinkData = appLinkIntent.getData();
        if (appLinkData != null) {
            try {
                final Toast this_song_is_not_saved = Toast.makeText(this, R.string.this_song_is_not_saved, Toast.LENGTH_LONG);
                String text = appLinkData.toString();
                String[] str = {"/#/song/", "/song/"};
                if (text != null) {
                    for (String s : str) {
                        if (text.contains(s)) {
                            final String songUuid = text.substring(text.lastIndexOf(s) + s.length(), text.length());
                            Song song = null;
                            for (Song song1 : songs) {
                                if (song1.getUuid().equals(songUuid)) {
                                    song = song1;
                                    break;
                                }
                            }
                            if (song != null) {
                                showSongFullscreen(song);
                            } else {
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SongApiBean songApiBean = new SongApiBean();
                                        Song newSong = songApiBean.getSong(songUuid);
                                        if (newSong != null) {
                                            this_song_is_not_saved.show();
                                            showSongFullscreen(newSong);
                                        }
                                    }
                                });
                                thread.start();
                            }
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        syncDatabase();
    }

    private void setBottomSheetHideable() {
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setSkipCollapsed(true);
    }

    private void uploadViewsFavourites() {
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

                //upload inc favourites
                List<FavouriteSong> favouriteUploadingSongs = new ArrayList<>();
                for (FavouriteSong favouriteSong : favouriteSongs) {
                    if (favouriteSong.isFavourite() && favouriteSong.isFavouriteNotPublished()) {
                        favouriteUploadingSongs.add(favouriteSong);
                    }
                }
                FavouriteSongRepository favouriteSongRepository = new FavouriteSongRepositoryImpl(MainActivity.this);
                for (FavouriteSong favouriteSong : favouriteUploadingSongs) {
                    Song song = favouriteSong.getSong();
                    if (song != null && song.getUuid() != null && songApiBean.uploadIncFavourite(song) != null) {
                        favouriteSong.setFavouritePublished(true);
                        favouriteSongRepository.save(favouriteSong);
                    } else {
                        break;
                    }
                }
            }
        });
        uploadViews.start();
    }

    private void setFavouritesForSongs() {
        HashMap<String, Song> hashMap = new HashMap<>(songs.size());
        for (Song song : songs) {
            hashMap.put(song.getUuid(), song);
        }
        for (FavouriteSong favouriteSong : favouriteSongs) {
            if (favouriteSong.getSong() != null) {
                String songUuid = favouriteSong.getSong().getUuid();
                if (hashMap.containsKey(songUuid)) {
                    Song song = hashMap.get(songUuid);
                    song.setFavourite(favouriteSong);
                }
            }
        }
    }

    private void syncDatabase() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean syncAutomatically = sharedPreferences.getBoolean(LanguagesActivity.syncAutomatically, true);
        if (syncAutomatically) {
            String syncDateTime = "lastSyncDateTime";
            long lastSyncDateTime = sharedPreferences.getLong(syncDateTime, 0);
            Date date = new Date();
            if (date.getTime() - 1000 * 60 * 60 * 12 > lastSyncDateTime) {
                SyncInBackground syncInBackground = SyncInBackground.getInstance();
                if (lastSyncDateTime == 0) {
                    syncInBackground.setSyncFrom();
                }
                syncInBackground.sync(getApplicationContext());
                sharedPreferences.edit().putLong(syncDateTime, date.getTime()).apply();
            }
        }
        if (sharedPreferences.getBoolean("YoutubeUrl", true)) {
            SyncInBackground syncInBackground = SyncInBackground.getInstance();
            syncInBackground.syncYoutubeUrl(getApplicationContext());
            sharedPreferences.edit().putBoolean("YoutubeUrl", false).apply();
        }
        syncDrive();
    }

    private void syncDrive() {
        syncFavouriteInGoogleDrive = new SyncFavouriteInGoogleDrive(new GoogleSignInIntent() {
            @Override
            public void task(Intent signInIntent) {
            }
        }, this, songs, favouriteSongs);
        syncFavouriteInGoogleDrive.signIn();
    }

    private void initPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sortMethod = sharedPreferences.getInt("sortMethod", 5);
        reverseSortMethod = sharedPreferences.getBoolean("reverseSortMethod", false);
        shortCollectionName = sharedPreferences.getBoolean("shortCollectionName", false);
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
        switch (requestCode) {
            case DOWNLOAD_SONGS_REQUEST_CODE:
                if (resultCode >= 1) {
                    songs = songRepository.findAll();
                    memory.setSongs(songs);
                    List<QueueSong> queue = memory.getQueue();
                    if (queue == null) {
                        queue = queueSongRepository.findAll();
                        Collections.sort(queue, new Comparator<QueueSong>() {
                            @Override
                            public int compare(QueueSong o1, QueueSong o2) {
                                return Utility.compare(o1.getQueueNumber(), o2.getQueueNumber());
                            }
                        });
                        memory.setQueue(queue);
                    }
                    if (queue.size() < 1) {
                        hideBottomSheet();
                    }
                    languages = languageRepository.findAll();
                    songCollections = songCollectionRepository.findAll();
                    setShortNamesForSongCollections(songCollections);
                    memory.setSongCollections(songCollections);
                    selectLanguagePopupWindow = null;
                    collectionPopupWindow = null;
                    filterPopupWindow = null;
                    createLoadSongVerseThread();
                    loadSongVersesThread.start();
                    filter();
                    syncDrive();
                    loadAll();
                    Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.downloaded) + " " + (resultCode - 1), Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            case 2:
                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (light_theme_switch != sharedPreferences.getBoolean("light_theme_switch", false)) {
                    recreate();
                }
                break;
            case 3:
                if (resultCode == 1) {
                    values.clear();
                    values.addAll(memory.getValues());
                }
                break;
            case 4:
                if (resultCode == 1) {
                    songs = memory.getSongs();
                    values.clear();
                    values.add(songs.get(songs.size() - 1));
                    adapter.setSongList(values);
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
                    saveGmail(result, getApplicationContext());
                    syncFavouriteInGoogleDrive.initializeDriveClient(result);
                    if (signInMenuItem != null) {
                        signInMenuItem.setTitle(getString(R.string.sign_out));
                    }
                    gSignIn = true;
                } else {
                    Log.e(TAG, "Sign-in failed.");
                    showToaster("Sign-in failed.", Toast.LENGTH_LONG);
                }
                break;
        }
        songListView.invalidateViews();
        queueListView.invalidateViews();
    }

    private void setShortNamesForSongCollections(List<SongCollection> songCollections) {
        HashMap<String, SongCollection> hashMap = new HashMap<>();
        List<SongCollection> collectionList = new ArrayList<>(songCollections.size());
        collectionList.addAll(songCollections);
        Collections.sort(collectionList, new Comparator<SongCollection>() {
            @Override
            public int compare(SongCollection lhs, SongCollection rhs) {
                return Utility.compare(rhs.getSongCollectionElements().size(), lhs.getSongCollectionElements().size());
            }
        });
        for (SongCollection songCollection : collectionList) {
            String shortName = songCollection.getShortName();
            if (hashMap.containsKey(shortName)) {
                SongCollection sameShortNameSongCollection = hashMap.get(shortName);
                String a = sameShortNameSongCollection.getName();
                String b = songCollection.getName();
                String newA = removeCommonString(a, b);
                if (newA == null) {
                    String newB = removeCommonString(b, a);
                    sameShortNameSongCollection.setShortName(newB);
                } else {
                    shortName = newA;
                }
            }
            songCollection.setShortName(shortName);
            hashMap.put(shortName, songCollection);
        }
    }

    private String removeCommonString(String a, String b) {
        StringBuilder newA;
        int k = 1;
        String[] splitA = a.split(" ");
        String[] splitB = b.split(" ");
        int i;
        for (i = 0; i < splitA.length && i < splitB.length; ++i) {
            try {
                String sA = splitA[i];
                String sB = splitB[i];
                if (sA.length() > k && sB.length() > k && sA.charAt(k) != sB.charAt(k)) {
                    newA = new StringBuilder();
                    for (int j = 0; j < splitB.length; ++j) {
                        newA.append((splitB[j].charAt(0) + "").toUpperCase());
                        if (j == i) {
                            newA.append(splitB[j].substring(1, k + 1).toLowerCase());
                        }
                    }
                    return newA.toString();
                }
            } catch (StringIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        return null;
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

    @SuppressLint("ClickableViewAccessibility")
    public void loadAll() {
        if (songs != null) {
            sortSongs(songs);
            values = new ArrayList<>();
            values.addAll(songs);
            adapter = new SongAdapter(this, R.layout.content_song_list_row, values);

            QueueSongAdapter queueSongAdapter = new QueueSongAdapter(this, R.layout.list_row, memory.getQueue(), new Listener() {
                @Override
                public void onGrab(int position, LinearLayout row) {
                    queueListView.onGrab(position, row);
                }
            }, shortCollectionName);

            queueListView.setAdapter(queueSongAdapter);
            queueListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    List<QueueSong> queue = memory.getQueue();
                    Song tmp = queue.get(position).getSong();
                    if (position + 1 < queue.size()) {
                        memory.setQueueIndex(position + 1, MainActivity.this);
                    } else {
                        memory.setQueueIndex(0, MainActivity.this);
                    }
                    showSongFullscreen(tmp);
                }

            });
            songListView.setAdapter(adapter);
            titleSearch("");
            songListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (values.size() > 0) {
                        hideKeyboard();
                    }
                    return false;
                }
            });
            songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Song tmp = values.get(position);
                    showSongFullscreen(tmp);
                }

            });
            songListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    QueueSong queueSong = new QueueSong();
                    queueSong.setQueueNumber(memory.getQueue().size());
                    queueSong.setSong(values.get(position));
                    memory.addSongToQueue(queueSong);
                    queueSongRepository.save(queueSong);
                    queueListView.invalidateViews();
                    showToaster(getString(R.string.added_to_queue), Toast.LENGTH_LONG);
                    return true;
                }
            });
        }
    }

    public void search(String text, SongAdapter adapter) {
        if (inSongSearchSwitch && (searchInSongTextIsAvailable)) {
            inSongSearch(text);
        } else {
            titleSearch(text);
        }
        adapter.setSongList(values);
    }

    public void titleSearch(String title) {
        values.clear();
        String text = title.toLowerCase();
        String stripped = stripAccents(text);
        String firstWord = stripAccents(text.split(" ")[0].toLowerCase());
        String other;
        if (text.length() > firstWord.length()) {
            other = stripAccents(text.substring(firstWord.length() + 1, text.length()).toLowerCase());
        } else {
            other = "";
        }
        String ordinalNumber = firstWord;
        String collectionName = "";
        if (!firstWord.matches("^[0-9]+.*")) {
            char[] chars = firstWord.toCharArray();
            int i;
            for (i = 0; i < chars.length; ++i) {
                if (chars[i] >= '0' && chars[i] <= '9') {
                    break;
                }
            }
            collectionName = stripAccents(firstWord.substring(0, i).toLowerCase());
            ordinalNumber = firstWord.substring(i, firstWord.length());
        }
        int ordinalNumberInt = Integer.MIN_VALUE;
        try {
            ordinalNumberInt = Integer.parseInt(ordinalNumber);
        } catch (Exception ignored) {
        }
        wasOrdinalNumber = false;
        for (int i = 0; i < songs.size(); ++i) {
            Song song = songs.get(i);
            if (containsInTitle(stripped, song, other, collectionName, ordinalNumber, ordinalNumberInt)) {
                values.add(song);
            }
        }
        if (wasOrdinalNumber) {
            Collections.sort(values, new Comparator<Song>() {
                @Override
                public int compare(Song l, Song r) {
                    SongCollectionElement lSongCollectionElement = l.getSongCollectionElement();
                    SongCollectionElement rSongCollectionElement = r.getSongCollectionElement();
                    if (lSongCollectionElement != null && rSongCollectionElement != null) {
                        Integer ordinalNumberInt = lSongCollectionElement.getOrdinalNumberInt();
                        return ordinalNumberInt.compareTo(rSongCollectionElement.getOrdinalNumberInt());
                    } else {
                        return 1;
                    }
                }
            });
        }
    }

    private boolean containsInTitle(String stripped, Song song, String other, String collectionName, String ordinalNumber, int ordinalNumberInt) {
        String strippedTitle = song.getStrippedTitle();
        if (strippedTitle.contains(stripped)) {
            return true;
        }
        SongCollectionElement songCollectionElement = song.getSongCollectionElement();
        SongCollection songCollection = song.getSongCollection();
        if (songCollection == null) {
            return false;
        }
        String songOrdinalNumber = songCollectionElement.getOrdinalNumber().toLowerCase();
        boolean equals = songOrdinalNumber.equals(ordinalNumber);
        boolean contains = songOrdinalNumber.contains(ordinalNumber) || ordinalNumberInt == songCollectionElement.getOrdinalNumberInt();
        if (collectionName.isEmpty()) {
            boolean b = (!ordinalNumber.isEmpty() && contains) || equals;
            if (other.isEmpty()) {
                if (b && equals) {
                    wasOrdinalNumber = true;
                }
                return b;
            }
            boolean b1 = b && strippedTitle.contains(other);
            if (b1 && equals) {
                wasOrdinalNumber = true;
            }
            return b1;
        }
        String name = songCollection.getStripedName();
        String shortName = songCollection.getStrippedShortName();
        boolean b = name.contains(collectionName) || shortName.contains(collectionName);
        if (ordinalNumber.isEmpty()) {
            if (other.isEmpty()) {
                if (b && equals) {
                    wasOrdinalNumber = true;
                }
                return b;
            }
            boolean b1 = b && strippedTitle.contains(other);
            if (b1 && equals) {
                wasOrdinalNumber = true;
            }
            return b1;
        }
        if (other.isEmpty()) {
            boolean b2 = b && contains;
            if (b2 && equals) {
                wasOrdinalNumber = true;
            }
            return b2;
        }
        boolean b2 = b && contains && strippedTitle.contains(other);
        if (b2 && equals) {
            wasOrdinalNumber = true;
        }
        return b2;
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
        memory.setPassingSong(song);
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
                                    Integer lhsOrdinalNumber = lhs.getSongCollectionElement().getOrdinalNumberInt();
                                    int rhsOrdinalNumber = rhs.getSongCollectionElement().getOrdinalNumberInt();
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
        } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (sortPopupWindow != null && sortPopupWindow.isShowing()) {
            sortPopupWindow.dismiss();
        } else if (selectLanguagePopupWindow != null && selectLanguagePopupWindow.isShowing()) {
            selectLanguagePopupWindow.dismiss();
        } else if (collectionPopupWindow != null && collectionPopupWindow.isShowing()) {
            collectionPopupWindow.dismiss();
        } else if (filterPopupWindow != null && filterPopupWindow.isShowing()) {
            filterPopupWindow.dismiss();
        } else if (googleSignInPopupWindow != null && googleSignInPopupWindow.isShowing()) {
            googleSignInPopupWindow.dismiss();
        } else {
            Date now = new Date();
            int interval = 777;
            if (lastDatePressedAtEnd != null) {
                if (now.getTime() - lastDatePressedAtEnd.getTime() >= interval) {
                    Toast.makeText(this, R.string.press_twice_to_exit, Toast.LENGTH_SHORT).show();
                } else {
                    lastDatePressedAtEnd = null;
                    super.onBackPressed();
                    return;
                }
            }
            lastDatePressedAtEnd = now;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        hideKeyboard();
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        searchItem = menu.findItem(R.id.action_search);
        final MenuItem searchInTextMenuItem = menu.findItem(R.id.action_search_in_text);
        searchInTextMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (!inSongSearchSwitch) {
                    Drawable drawable = item.getIcon();
                    if (drawable != null) {
                        drawable.mutate();
                        drawable.setColorFilter(Color.rgb(153, 175, 174), PorterDuff.Mode.SRC_ATOP);
                    }
                    inSongSearchSwitch = true;
                } else {
                    Drawable drawable = item.getIcon();
                    if (drawable != null) {
                        drawable.mutate();
                        drawable.setColorFilter(Color.rgb(94, 89, 94), PorterDuff.Mode.SRC_ATOP);
                    }
                    inSongSearchSwitch = false;
                }
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
                return false;
            }
        });

        final SearchView mSearchView = (SearchView) searchItem.getActionView();
        searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS |
                MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        mSearchView.setQueryHint(getString(R.string.search));
        mSearchView.setIconified(false);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String enteredText = newText.trim();
                search(enteredText, adapter);
                lastSearchedText = enteredText;
                if (enteredText.equals("show similar")) {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    sharedPreferences.edit().putBoolean("show_similar", true).apply();
                }
                return false;
            }
        });
        searchItem.expandActionView();
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchItem.getActionView().requestFocusFromTouch();
                mSearchView.setIconified(false);
                showKeyboard();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                hideKeyboard();
                return true;
            }
        });
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

    private void showKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_download_songs) {
            Intent loadIntent = new Intent(this, LanguagesActivity.class);
            startActivityForResult(loadIntent, DOWNLOAD_SONGS_REQUEST_CODE);
        } else if (id == R.id.nav_settings) {
            Intent loadIntent = new Intent(this, SettingsActivity.class);
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            light_theme_switch = sharedPreferences.getBoolean("light_theme_switch", false);
            startActivityForResult(loadIntent, 2);
        } else if (id == R.id.nav_new_song) {
            Intent loadIntent = new Intent(this, NewSongActivity.class);
            startActivityForResult(loadIntent, 4);
        } else if (id == R.id.nav_privacy_policy) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://comsongbook.firebaseapp.com/privacy_policy.html")));
        } else if (id == R.id.nav_sign_in) {
            if (!gSignIn) {
                googleSignInPopupWindow = showGoogleSignIn((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE), true);
                if (googleSignInPopupWindow != null) {
                    googleSignInPopupWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);
                }
            } else {
                new SyncFavouriteInGoogleDrive(null, this, null, null).signOut();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                sharedPreferences.edit().putBoolean("gSignIn", false).apply();
                if (signInMenuItem != null) {
                    signInMenuItem.setTitle(getString(R.string.sign_in));
                }
            }
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
                LayoutParams.MATCH_PARENT,
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
        if (songCollections.size() == 0) {
            customView.findViewById(R.id.collectionButton).setVisibility(View.GONE);
        }
        containingVideosSwitch = customView.findViewById(R.id.containingVideosSwitch);
        containingVideosSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                hideKeyboard();
                filter();
                loadAll();
            }
        });
        favouriteSwitch = customView.findViewById(R.id.favouriteSwitch);
        favouriteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                hideKeyboard();
                filter();
                loadAll();
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
        SongCollectionAdapter songCollectionAdapter = new SongCollectionAdapter(mainActivity,
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
                filterPopupWindow.dismiss();
                collectionPopupWindow.dismiss();
            }
        });
        Collections.sort(songCollections, new Comparator<SongCollection>() {
            @Override
            public int compare(SongCollection o1, SongCollection o2) {
                return Utility.compare(o2.getSongCollectionElements().size(), o1.getSongCollectionElements().size());
            }
        });
        collectionListView = customView.findViewById(R.id.listView);
        collectionPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        collectionPopupWindow.setOutsideTouchable(true);
    }

    private void filter() {
        filterSongsByLanguage();
        filterSongsByCollection();
        filterSongsByVideos();
        filterSongsByFavourites();
        setFavouritesForSongs();
    }

    private void setDataToQueueSongs() {
        LongSparseArray<Song> sparseArray = new LongSparseArray<>(songs.size());
        for (Song song : songs) {
            sparseArray.put(song.getId(), song);
        }
        List<QueueSong> queue = memory.getQueue();
        if (queue == null) {
            queue = queueSongRepository.findAll();
            Collections.sort(queue, new Comparator<QueueSong>() {
                @Override
                public int compare(QueueSong o1, QueueSong o2) {
                    return Utility.compare(o1.getQueueNumber(), o2.getQueueNumber());
                }
            });
            memory.setQueue(queue);
            if (queue.size() < 1) {
                hideBottomSheet();
            }
        }
        List<Song> songs = new ArrayList<>();
        for (QueueSong queueSong : queue) {
            if (queueSong.getSong() != null) {
                Long id = queueSong.getSong().getId();
                Song song = sparseArray.get(id);
                if (song != null) {
                    queueSong.setSong(song);
                } else {
                    song = songRepository.findOne(id);
                    if (song != null) {
                        queueSong.setSong(song);
                        sparseArray.put(id, song);
                    }
                }
                songs.add(song);
            }
        }
        HashMap<String, Song> hashMap = new HashMap<>();
        for (Song song : songs) {
            if (song.getUuid() != null) {
                hashMap.put(song.getUuid(), song);
            }
        }
        for (SongCollection songCollection : songCollections) {
            for (SongCollectionElement songCollectionElement : songCollection.getSongCollectionElements()) {
                String songUuid = songCollectionElement.getSongUuid();
                if (hashMap.containsKey(songUuid)) {
                    Song song = hashMap.get(songUuid);
                    song.setSongCollection(songCollection);
                    song.setSongCollectionElement(songCollectionElement);
                }
            }
        }
        for (FavouriteSong favouriteSong : favouriteSongs) {
            if (favouriteSong.getSong() != null) {
                String songUuid = favouriteSong.getSong().getUuid();
                if (hashMap.containsKey(songUuid)) {
                    Song song = hashMap.get(songUuid);
                    song.setFavourite(favouriteSong);
                }
            }
        }
    }

    private void filterSongsByFavourites() {
        if (favouriteSwitch != null && favouriteSwitch.isChecked()) {
            ArrayList<Song> tmpSongs = new ArrayList<>(songs);
            songs.clear();
            for (Song song : tmpSongs) {
                if (song.isFavourite()) {
                    songs.add(song);
                }
            }
        }
    }

    private void filterSongsByVideos() {
        if (containingVideosSwitch != null && containingVideosSwitch.isChecked()) {
            ArrayList<Song> tmpSongs = new ArrayList<>(songs);
            songs.clear();
            for (Song song : tmpSongs) {
                if (song.getYoutubeUrl() != null) {
                    songs.add(song);
                }
            }
        }
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

    public void setShortCollectionName(boolean shortCollectionName) {
        this.shortCollectionName = shortCollectionName;
    }

    public void onLanguageButtonClick(View view) {
        if (selectLanguagePopupWindow != null) {
            selectLanguagePopupWindow.dismiss();
        } else {
            createSelectLanguagePopup();
        }
        hideKeyboard();
        selectLanguagePopupWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);
    }

    public void onCollectionButtonClick(View view) {
        if (collectionPopupWindow != null) {
            collectionPopupWindow.dismiss();
        } else {
            createCollectionPopup();
        }
        hideKeyboard();
        sortCollectionBySelectedLanguages();
        collectionPopupWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);
    }

    public void onGoogleSignIn(View view) {
        SyncFavouriteInGoogleDrive syncFavouriteInGoogleDrive = new SyncFavouriteInGoogleDrive(new GoogleSignInIntent() {
            @Override
            public void task(Intent signInIntent) {
                startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
            }
        }, this, songs, favouriteSongs);
        syncFavouriteInGoogleDrive.signIn();
        googleSignInPopupWindow.dismiss();
    }

    private void showToaster(String s, int lengthLong) {
        Toast.makeText(this, s, lengthLong).show();
    }

    public void onClearAllQueueClick(View view) {
        List<QueueSong> all = queueSongRepository.findAll();
        queueSongRepository.deleteAll(all);
        memory.getQueue().clear();
        queueListView.invalidateViews();
        hideBottomSheet();
    }

    private void hideBottomSheet() {
        setBottomSheetHideable();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public void onExpandBottomSheetClick(View view) {
        if (memory.getQueue().size() < 1) {
            hideBottomSheet();
            return;
        }
        bottomSheetBehavior.setState(
                bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED ?
                        BottomSheetBehavior.STATE_COLLAPSED :
                        BottomSheetBehavior.STATE_EXPANDED);
    }

    public interface Listener {
        void onGrab(int position, LinearLayout row);
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
                holder.imageView = convertView.findViewById(R.id.starImageView);
                convertView.setTag(holder);
            } else {
                holder = (SongAdapter.ViewHolder) convertView.getTag();
            }

            Song song = songList.get(position);
            holder.imageView.setVisibility(song.isFavourite() ? View.VISIBLE : View.INVISIBLE);
            SongCollection songCollection = song.getSongCollection();
            if (songCollection != null) {
                String collectionName = songCollection.getName();
                if (shortCollectionName) {
                    collectionName = songCollection.getShortName();
                }
                String text = collectionName + " " + song.getSongCollectionElement().getOrdinalNumber();
                holder.ordinalNumberTextView.setText(text);
            } else {
                holder.ordinalNumberTextView.setText("");
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
            View imageView;
        }

    }
}
