package com.bence.songbook.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
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
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.models.FavouriteSong;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongVerse;
import com.bence.songbook.network.ProjectionTextChangeListener;
import com.bence.songbook.repository.FavouriteSongRepository;
import com.bence.songbook.repository.SongRepository;
import com.bence.songbook.repository.impl.ormLite.FavouriteSongRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongRepositoryImpl;
import com.bence.songbook.service.SongService;
import com.bence.songbook.ui.utils.GoogleSignInIntent;
import com.bence.songbook.ui.utils.Preferences;
import com.bence.songbook.ui.utils.SaveFavouriteInGoogleDrive;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bence.songbook.ui.utils.SaveFavouriteInGoogleDrive.REQUEST_CODE_SIGN_IN;

public class SongActivity extends AppCompatActivity {
    private static final String TAG = "SongActivity";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private Song song;
    private Memory memory;
    private MenuItem favouriteMenuItem;
    private SaveFavouriteInGoogleDrive saveFavouriteInGoogleDrive;
    private Intent signInIntent;
    private View mainLayout;
    private PopupWindow googleSignInPopupWindow;

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
        loadSongView(song);
    }

    private void loadSongView(Song song) {
        setToolbarTitleAndSize();
        TextView collectionTextView = findViewById(R.id.collectionTextView);
        if (song.getSongCollection() != null) {
            String text = song.getSongCollection().getName() + " " + song.getSongCollectionElement().getOrdinalNumber();
            collectionTextView.setText(text);
            collectionTextView.setVisibility(View.VISIBLE);
        } else {
            collectionTextView.setVisibility(View.GONE);
        }

        final Intent fullScreenIntent = new Intent(this, FullscreenActivity.class);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullScreenIntent.putExtra("verseIndex", 0);
                startActivity(fullScreenIntent);
            }
        });

        MyCustomAdapter dataAdapter = new MyCustomAdapter(this,
                R.layout.content_song_verse, song.getVerses());
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(dataAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                fullScreenIntent.putExtra("verseIndex", position);
                startActivity(fullScreenIntent);
            }

        });
        if (favouriteMenuItem != null) {
            if (song.isFavourite()) {
                favouriteMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_star_black_24dp));
            }
        }
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
        } else if (itemId == R.id.action_versions) {
            Intent intent = new Intent(this, VersionsActivity.class);
            startActivityForResult(intent, 1);
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        onBackButtonClick(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == 1) {
                    song = memory.getPassingSong();
                    loadSongView(song);
                }
                break;
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
                    saveGmail(result, getApplicationContext());
                    saveFavouriteInGoogleDrive.initializeDriveClient(result);
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
                favourite.setFavouritePublished(!favourite.isFavouritePublished());
                FavouriteSongRepository favouriteSongRepository = new FavouriteSongRepositoryImpl(context);
                favouriteSongRepository.save(favourite);
                favouriteMenuItem.setIcon(ResourcesCompat.getDrawable(getResources(), song.isFavourite() ?
                        R.drawable.ic_star_black_24dp : R.drawable.ic_star_border_black_24dp, null));
                syncFavouriteInGoogleDrive();
                return false;
            }
        });
        final MenuItem versionsMenuItem = menu.findItem(R.id.action_versions);
        versionsMenuItem.setVisible(false);
        final SongRepository songRepository = new SongRepositoryImpl(this);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String versionGroup = song.getVersionGroup();
                if (versionGroup == null) {
                    versionGroup = song.getUuid();
                }
                boolean was = false;
                if (versionGroup != null) {
                    List<Song> allByVersionGroup = songRepository.findAllByVersionGroup(versionGroup);
                    for (Song song1 : allByVersionGroup) {
                        if (!song1.getUuid().equals(song.getUuid())) {
                            was = true;
                            break;
                        }
                    }
                }
                final boolean finalWas = was;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalWas) {
                            versionsMenuItem.setVisible(true);
                        } else {
                            menu.removeItem(versionsMenuItem.getItemId());
                        }
                    }
                });
            }
        });
        thread.start();
        return true;
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

    @SuppressWarnings("ConstantConditions")
    private class MyCustomAdapter extends ArrayAdapter<SongVerse> {

        private List<SongVerse> songVerses;

        MyCustomAdapter(Context context, int textViewResourceId,
                        List<SongVerse> songVerses) {
            super(context, textViewResourceId, songVerses);
            this.songVerses = new ArrayList<>();
            this.songVerses.addAll(songVerses);
        }

        @SuppressLint({"InflateParams", "SetTextI18n"})
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            MyCustomAdapter.ViewHolder holder;

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.content_song_verse, null);

                holder = new MyCustomAdapter.ViewHolder();
                holder.textView = convertView.findViewById(R.id.textView);
                holder.chorusTextView = convertView.findViewById(R.id.chorusTextView);
                convertView.setTag(holder);
            } else {
                holder = (MyCustomAdapter.ViewHolder) convertView.getTag();
            }

            SongVerse songVerse = songVerses.get(position);
            holder.textView.setText(songVerse.getText());
            if (!songVerse.isChorus()) {
                holder.chorusTextView.setVisibility(View.GONE);
            } else {
                holder.chorusTextView.setVisibility(View.VISIBLE);
            }
            return convertView;
        }

        private class ViewHolder {
            TextView textView;
            TextView chorusTextView;
        }

    }
}
