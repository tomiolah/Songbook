package com.bence.songbook.ui.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bence.projector.common.dto.SuggestionDTO;
import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.api.SuggestionApiBean;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongVerse;
import com.bence.songbook.ui.utils.Preferences;
import com.bence.songbook.utils.Config;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import static com.bence.songbook.ui.activity.YoutubeActivity.RECOVERY_REQUEST;

public class SuggestYouTubeActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private Song song;
    private YouTubePlayerView youTubeView;
    private EditText youtubeEditText;
    private YouTubePlayer youtubePlayer;
    private boolean activityPaused = false;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest_youtube);
        youTubeView = findViewById(R.id.youtube_view);
        youTubeView.initialize(Config.YOUTUBE_API_KEY, this);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });
        song = Memory.getInstance().getPassingSong();
        youtubeEditText = findViewById(R.id.youtubeUrl);
        youtubeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                final String youtubeId = parseYoutubeUrl(String.valueOf(s));
                if (youtubeId.length() < 21 && youtubeId.length() > 9) {
                    youTubeView.setVisibility(View.VISIBLE);
                    if (youtubePlayer == null) {
                        final Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (youtubePlayer == null) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (youtubePlayer != null) {
                                    youtubePlayer.cueVideo(youtubeId);
                                    youtubePlayer.play();
                                }
                            }
                        });
                        thread.start();
                    } else {
                        youtubePlayer.cueVideo(youtubeId);
                        youtubePlayer.play();
                    }
                }
            }
        });
        EditText titleEditText = findViewById(R.id.title);
        EditText textEditText = findViewById(R.id.text);
        titleEditText.setText(song.getTitle());
        textEditText.setText(getText(song));
        titleEditText.setKeyListener(null);
        titleEditText.setFocusable(false);
        titleEditText.setCursorVisible(false);
        textEditText.setKeyListener(null);
        textEditText.setFocusable(false);
        textEditText.setCursorVisible(false);
        Toast.makeText(this, getString(R.string.select_youtube_video), Toast.LENGTH_LONG).show();
        try {
            Intent youtubeIntent = new Intent(Intent.ACTION_SEARCH);
            youtubeIntent.setPackage("com.google.android.youtube");
            youtubeIntent.putExtra("query", song.getTitle());
            youtubeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(youtubeIntent);
        } catch (Exception e) {
            Uri parse = Uri.parse("http://www.youtube.com/results?search_query=" + song.getTitle().replace(" ", "+"));
            startActivity(new Intent(Intent.ACTION_VIEW, parse));
        }
    }

    private String getText(Song song) {
        StringBuilder text = new StringBuilder();
        for (SongVerse songVerse : song.getVerses()) {
            if (text.length() > 0) {
                text.append("\n\n");
            }
            text.append(songVerse.getText().trim());
        }
        return text.toString();
    }

    private void submit() {
        final SuggestionDTO suggestionDTO = new SuggestionDTO();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String gmail = sharedPreferences.getString("gmail", "");
        if (!gmail.isEmpty()) {
            suggestionDTO.setCreatedByEmail(gmail);
        } else {
            String email = sharedPreferences.getString("email", "");
            suggestionDTO.setCreatedByEmail(email);
        }
        String url = youtubeEditText.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, R.string.no_change, Toast.LENGTH_SHORT).show();
            return;
        }
        String youtubeId = parseYoutubeUrl(url);
        if (youtubeId.length() < 21 && youtubeId.length() > 9) {
            suggestionDTO.setYoutubeUrl(youtubeId);
        } else {
            Toast.makeText(this, R.string.Cannot_parse_YouTube_Url, Toast.LENGTH_SHORT).show();
            return;
        }
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
                            Toast.makeText(SuggestYouTubeActivity.this, R.string.successfully_uploaded, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SuggestYouTubeActivity.this, R.string.upload_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        thread.start();
        finish();
    }

    private String parseYoutubeUrl(String url) {
        String youtubeUrl = url.replace("https://www.youtube.com/watch?v=", "");
        youtubeUrl = youtubeUrl.replace("https://www.youtube.com/embed/", "");
        youtubeUrl = youtubeUrl.replace("https://youtu.be/", "");
        return youtubeUrl;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                        final YouTubePlayer player, boolean wasRestored) {
        player.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
            @Override
            public void onLoading() {
            }

            @Override
            public void onLoaded(String s) {
            }

            @Override
            public void onAdStarted() {
            }

            @Override
            public void onVideoStarted() {
            }

            @Override
            public void onVideoEnded() {
            }

            @Override
            public void onError(YouTubePlayer.ErrorReason errorReason) {
                if (errorReason == YouTubePlayer.ErrorReason.UNAUTHORIZED_OVERLAY) {
                    player.play();
//                } else {
//                    System.out.println(errorReason);
                }
            }
        });
        youtubePlayer = player;
        if (!wasRestored) {
            player.cueVideo(song.getYoutubeUrl());
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider
                                                provider, YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show();
        } else {
            String error = String.format("Error initializing YouTube player: %s", errorReason.toString());
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + song.getYoutubeUrl())));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(Config.YOUTUBE_API_KEY, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (activityPaused) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboard != null) {
                try {
                    ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                    CharSequence pasteData = item.getText();
                    if (pasteData != null) {
                        youtubeEditText.setText(pasteData);
                    }
                } catch (Exception ignored) {
                }
            }
            activityPaused = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityPaused = true;
    }

    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return youTubeView;
    }
}
