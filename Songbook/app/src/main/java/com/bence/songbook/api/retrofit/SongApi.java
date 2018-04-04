package com.bence.songbook.api.retrofit;

import com.bence.projector.common.dto.SongDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface SongApi {
    @GET("/api/songs")
    Call<List<SongDTO>> getSongs();

    @GET("/api/songsAfterModifiedDate/{modifiedDate}")
    Call<List<SongDTO>> getSongsAfterModifiedDate(@Path("modifiedDate") Long modifiedDate);

    @GET("/api/songs/language/{language}")
    Call<List<SongDTO>> getSongsByLanguage(@Path("language") String language);

    @GET("/api/songs/language/{language}/modifiedDate/{modifiedDate}")
    Call<List<SongDTO>> getSongsByLanguageAndAfterModifiedDate(@Path("language") String languageUuid, @Path("modifiedDate") Long modifiedDate);

    @PUT("/api/song/{uuid}/incViews")
    Call<SongDTO> uploadView(@Path("uuid") String uuid);

    @POST("/api/song/upload")
    Call<SongDTO> uploadSong(@Body SongDTO songDTO);
}
