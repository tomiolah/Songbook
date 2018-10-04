package com.bence.songbook.ui.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bence.songbook.models.FavouriteSong;
import com.bence.songbook.models.Song;
import com.bence.songbook.repository.FavouriteSongRepository;
import com.bence.songbook.repository.impl.ormLite.FavouriteSongRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongRepositoryImpl;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncFavouriteInGoogleDrive extends FavouriteInGoogleDrive {
    public static final int REQUEST_CODE_SIGN_IN = 474;
    private static final String TAG = "SyncFavouriteInGoogle";
    private final List<Song> songs;
    private final List<FavouriteSong> localFavourites;

    public SyncFavouriteInGoogleDrive(GoogleSignInIntent googleSignInIntent, Activity activity,
                                      List<Song> songs, List<FavouriteSong> favouriteSongs) {
        this.googleSignInIntent = googleSignInIntent;
        this.activity = activity;
        this.songs = songs;
        this.localFavourites = favouriteSongs;
    }

    @Override
    void readingFavouritesFromDrive(DriveResourceClient mDriveResourceClient) {
        Query query = new Query.Builder().addFilter(Filters.ownedByMe()).build();
        this.mDriveResourceClient = mDriveResourceClient;
        this.mDriveResourceClient.query(query).addOnSuccessListener(activity, new OnSuccessListener<MetadataBuffer>() {
            @Override
            public void onSuccess(MetadataBuffer metadata) {
                getFileInFolder(metadata);
            }
        }).addOnFailureListener(activity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    @Override
    void retrieveContents(final DriveFile file) {
        Task<DriveContents> openFileTask =
                mDriveResourceClient.openFile(file, DriveFile.MODE_READ_WRITE);
        openFileTask
                .continueWithTask(new Continuation<DriveContents, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                        DriveContents contents = task.getResult();
                        BufferedReader reader = null;
                        InputStream inputStream = null;
                        List<FavouriteSong> favouriteSongs;
                        try {
                            inputStream = new FileInputStream(contents.getParcelFileDescriptor().getFileDescriptor());
                            reader = new BufferedReader(new InputStreamReader(inputStream));
                            StringBuilder builder = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                builder.append(line).append("\n");
                            }
                            System.out.println("builder = " + builder.toString());

                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                            Type listType = new TypeToken<ArrayList<FavouriteSong>>() {
                            }.getType();
                            favouriteSongs = gson.fromJson(builder.toString(), listType);
                            if (favouriteSongs != null) {
                                System.out.println("o.size() = " + favouriteSongs.size());
                                Map<String, FavouriteSong> map = new HashMap<>(favouriteSongs.size());
                                for (FavouriteSong favouriteSong : favouriteSongs) {
                                    String uuid = favouriteSong.getSong().getUuid();
                                    map.put(uuid, favouriteSong);
                                }
                                Map<String, Song> songMap = new HashMap<>(songs.size());
                                for (Song song : songs) {
                                    songMap.put(song.getUuid(), song);
                                }
                                FavouriteSongRepository favouriteSongRepository = new FavouriteSongRepositoryImpl(activity);
                                List<FavouriteSong> modifiedFavourites = new ArrayList<>();
                                for (FavouriteSong favouriteSong : map.values()) {
                                    String uuid = favouriteSong.getSong().getUuid();
                                    Song song = null;
                                    if (!songMap.containsKey(uuid)) {
                                        SongRepositoryImpl songRepository = new SongRepositoryImpl(activity);
                                        Song songByUUID = songRepository.findByUUID(uuid);
                                        if (songByUUID != null) {
                                            FavouriteSong favouriteSongBySongUuid = favouriteSongRepository.findFavouriteSongBySongUuid(uuid);
                                            songByUUID.setFavourite(favouriteSongBySongUuid);
                                            song = songByUUID;
                                        }
                                    } else {
                                        song = songMap.get(uuid);
                                    }
                                    if (song != null) {
                                        if (song.getFavourite() == null) {
                                            if (favouriteSong.isFavourite()) {
                                                song.setFavourite(true);
                                                FavouriteSong favourite = song.getFavourite();
                                                favourite.setModifiedDate(favouriteSong.getModifiedDate());
                                                favourite.setFavouritePublishedToDrive(true);
                                                modifiedFavourites.add(favourite);
                                            }
                                        } else {
                                            FavouriteSong favourite = song.getFavourite();
                                            if (favourite.getModifiedDate().before(favouriteSong.getModifiedDate())) {
                                                song.setFavourite(favouriteSong.isFavourite());
                                                favourite.setModifiedDate(favouriteSong.getModifiedDate());
                                                favourite.setFavouritePublishedToDrive(true);
                                                modifiedFavourites.add(favourite);
                                            }
                                        }
                                    }
                                }
                                favouriteSongRepository.save(modifiedFavourites);
                                List<FavouriteSong> unpublishedList = new ArrayList<>();
                                for (FavouriteSong favouriteSong : localFavourites) {
                                    if (!favouriteSong.isFavouritePublishedToDrive()) {
                                        unpublishedList.add(favouriteSong);
                                    }
                                }
                                if (unpublishedList.size() > 0) {
                                    for (FavouriteSong favourite : unpublishedList) {
                                        String uuid = favourite.getSong().getUuid();
                                        if (!map.containsKey(uuid)) {
                                            map.put(uuid, favourite);
                                        } else {
                                            FavouriteSong favouriteSong = map.get(uuid);
                                            if (favourite.getModifiedDate().after(favouriteSong.getModifiedDate())) {
                                                map.put(uuid, favourite);
                                            }
                                        }
                                    }
                                    unpublishedList.clear();
                                    unpublishedList.addAll(map.values());
                                    rewriteContents(file, unpublishedList);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            rewriteContents(file, new ArrayList<FavouriteSong>());
                        } finally {
                            if (reader != null) {
                                reader.close();
                            }
                            if (inputStream != null) {
                                inputStream.close();
                            }
                        }
                        return mDriveResourceClient.discardContents(contents);
                    }
                })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Unable to update contents", e);
                    }
                });
    }

}
