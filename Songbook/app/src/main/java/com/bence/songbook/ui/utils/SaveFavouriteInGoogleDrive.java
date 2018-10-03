package com.bence.songbook.ui.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bence.songbook.models.FavouriteSong;
import com.bence.songbook.models.Song;
import com.bence.songbook.repository.impl.ormLite.FavouriteSongRepositoryImpl;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaveFavouriteInGoogleDrive extends FavouriteInGoogleDrive {
    public static final int REQUEST_CODE_SIGN_IN = 473;
    private static final String TAG = "SaveFavouriteInGoogle";
    private Song song;

    public SaveFavouriteInGoogleDrive(GoogleSignInIntent googleSignInIntent, Activity activity, Song song) {
        this.googleSignInIntent = googleSignInIntent;
        this.activity = activity;
        this.song = song;
    }

    private void savingFavourite() {
        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getAppFolder();
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();
        Tasks.whenAll(appFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(@NonNull Task<Void> task) {
                        DriveFolder parent = appFolderTask.getResult();
                        DriveContents contents = createContentsTask.getResult();
                        OutputStream outputStream = contents.getOutputStream();
                        Writer writer = null;
                        try {
                            writer = new OutputStreamWriter(outputStream);
                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                            FavouriteSong favourite = song.getFavourite();
                            favourite.setFavouritePublishedToDrive(true);
                            String str = "[" + gson.toJson(favourite) + "]";
                            System.out.println("str = " + str);
                            writer.write(str);
                            FavouriteSongRepositoryImpl favouriteSongRepository = new FavouriteSongRepositoryImpl(activity);
                            favouriteSongRepository.save(favourite);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (writer != null) {
                                try {
                                    writer.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(FAVOURITES)
                                .setMimeType("application/json")
                                .setStarred(true)
                                .build();

                        return mDriveResourceClient.createFile(parent, changeSet, contents);
                    }
                })
                .addOnSuccessListener(activity,
                        new OnSuccessListener<DriveFile>() {
                            @Override
                            public void onSuccess(DriveFile driveFile) {
                                System.out.println("wrote");
                            }
                        })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Unable to create file", e);
                    }
                });
    }

    @Override
    void readingFavouritesFromDrive(DriveResourceClient mDriveResourceClient) {
        Query query = new Query.Builder().addFilter(Filters.ownedByMe()).build();
        this.mDriveResourceClient = mDriveResourceClient;
        this.mDriveResourceClient.query(query).addOnSuccessListener(activity, new OnSuccessListener<MetadataBuffer>() {
            @Override
            public void onSuccess(MetadataBuffer metadata) {
                getFileInFolder(metadata);
                if (!found) {
                    savingFavourite();
                }
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
                                    if (map.containsKey(uuid)) {
                                        FavouriteSong hash = map.get(uuid);
                                        if (hash.getModifiedDate().before(favouriteSong.getModifiedDate())) {
                                            map.put(uuid, favouriteSong);
                                        }
                                    } else {
                                        map.put(uuid, favouriteSong);
                                    }
                                }
                                String uuid = song.getUuid();
                                FavouriteSong favouriteSong = song.getFavourite();
                                if (map.containsKey(uuid)) {
                                    FavouriteSong hash = map.get(uuid);
                                    if (hash.getModifiedDate().before(favouriteSong.getModifiedDate())) {
                                        map.put(uuid, favouriteSong);
                                    }
                                } else {
                                    map.put(uuid, favouriteSong);
                                }
                                favouriteSongs.clear();
                                favouriteSongs.addAll(map.values());
                                rewriteContents(file, favouriteSongs);
                            } else {
                                savingFavourite();
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
