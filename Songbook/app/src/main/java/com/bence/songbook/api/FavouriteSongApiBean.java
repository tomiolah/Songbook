package com.bence.songbook.api;

import android.content.Context;
import android.util.Log;

import com.bence.projector.common.dto.FavouriteSongDTO;
import com.bence.songbook.api.assembler.FavouriteSongAssembler;
import com.bence.songbook.api.retrofit.ApiManager;
import com.bence.songbook.api.retrofit.FavouriteSongApi;
import com.bence.songbook.models.FavouriteSong;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class FavouriteSongApiBean {
    private static final String TAG = FavouriteSongApiBean.class.getName();
    private final FavouriteSongAssembler favouriteSongAssembler;
    private final FavouriteSongApi favouriteSongApi;

    public FavouriteSongApiBean(Context context) {
        favouriteSongApi = ApiManager.getClient().create(FavouriteSongApi.class);
        favouriteSongAssembler = FavouriteSongAssembler.getInstance(context);
    }

    public List<FavouriteSongDTO> uploadFavouriteSongs(List<FavouriteSong> favouriteSongs) {
        List<FavouriteSongDTO> dtos = favouriteSongAssembler.createDTOS(favouriteSongs);
        Call<List<FavouriteSongDTO>> call = favouriteSongApi.uploadFavouriteSong(dtos);
        try {
            Response<List<FavouriteSongDTO>> favouriteSongDTOResponse = call.execute();
            if (favouriteSongDTOResponse.isSuccessful()) {
                return favouriteSongDTOResponse.body();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }
}
