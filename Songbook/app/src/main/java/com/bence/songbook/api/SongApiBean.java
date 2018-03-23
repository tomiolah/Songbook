package com.bence.songbook.api;

import android.util.Log;

import com.bence.projector.common.dto.SongDTO;
import com.bence.songbook.ProgressMessage;
import com.bence.songbook.api.assembler.SongAssembler;
import com.bence.songbook.api.retrofit.ApiManager;
import com.bence.songbook.api.retrofit.SongApi;
import com.bence.songbook.models.Language;
import com.bence.songbook.models.Song;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import retrofit2.Call;

public class SongApiBean {
    private static final String TAG = SongApiBean.class.getName();
    private SongApi songApi;
    private SongAssembler songAssembler;

    public SongApiBean() {
        songApi = ApiManager.getClient().create(SongApi.class);
        songAssembler = SongAssembler.getInstance();
    }

    public List<Song> getSongs(final ProgressMessage progressMessage) {
        Call<List<SongDTO>> call = songApi.getSongs();
        try {
            List<SongDTO> songDTOs = call.execute().body();
            return songAssembler.createModelList(songDTOs, progressMessage);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public List<Song> getSongsAfterModifiedDate(final ProgressMessage progressMessage, Date modifiedDate) {
        Call<List<SongDTO>> call = songApi.getSongsAfterModifiedDate(modifiedDate.getTime());
        try {
            List<SongDTO> songDTOs = call.execute().body();
            return songAssembler.createModelList(songDTOs, progressMessage);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public List<Song> getSongsAfterModifiedDate(Date modifiedDate) {
        Call<List<SongDTO>> call = songApi.getSongsAfterModifiedDate(modifiedDate.getTime());
        try {
            List<SongDTO> songDTOs = call.execute().body();
            return songAssembler.createModelList(songDTOs);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public List<Song> getSongsByLanguage(Language language, ProgressMessage progressMessage) {
        Call<List<SongDTO>> call = songApi.getSongsByLanguage(language.getUuid());
        List<Song> songs = null;
        try {
            List<SongDTO> songDTOs = call.execute().body();
            if (songDTOs != null) {
                progressMessage.onSetMax(songDTOs.size());
            }
            songs = songAssembler.createModelList(songDTOs, progressMessage);
            System.out.println("songs = " + songs);
            return songs;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return songs;
    }

    public List<Song> getSongsByLanguageAndAfterModifiedDate(Language language, Long modifiedDate, ProgressMessage progressMessage) {
        Call<List<SongDTO>> call = songApi.getSongsByLanguageAndAfterModifiedDate(language.getUuid(), modifiedDate);
        List<Song> songs = null;
        try {
            List<SongDTO> songDTOs = call.execute().body();
            if (songDTOs != null) {
                progressMessage.onSetMax(songDTOs.size());
            }
            songs = songAssembler.createModelList(songDTOs, progressMessage);
            return songs;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return songs;
    }

    public SongDTO uploadView(Song song) {
        Call<SongDTO> call = songApi.uploadView(song.getUuid());
        try {
            return call.execute().body();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }
}
