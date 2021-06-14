package com.bence.songbook.api.retrofit;

import com.bence.projector.common.dto.FavouriteSongDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface FavouriteSongApi {
    @POST("/user/api/favouriteSongs")
    Call<List<FavouriteSongDTO>> uploadFavouriteSong(@Body List<FavouriteSongDTO> favouriteSongDTO);
}
