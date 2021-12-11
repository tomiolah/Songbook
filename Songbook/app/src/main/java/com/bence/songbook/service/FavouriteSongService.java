package com.bence.songbook.service;

import android.content.Context;

import com.bence.projector.common.dto.FavouriteSongDTO;
import com.bence.songbook.api.FavouriteSongApiBean;
import com.bence.songbook.models.FavouriteSong;
import com.bence.songbook.repository.impl.ormLite.FavouriteSongRepositoryImpl;

import java.util.ArrayList;
import java.util.List;

public class FavouriteSongService {

    private static FavouriteSongService instance;

    private FavouriteSongService() {

    }

    public static FavouriteSongService getInstance() {
        if (instance == null) {
            instance = new FavouriteSongService();
        }
        return instance;
    }

    public void syncFavourites(final Context context) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (!UserService.getInstance().isLoggedIn(context)) {
                    return;
                }
                FavouriteSongRepositoryImpl favouriteSongRepository = new FavouriteSongRepositoryImpl(context);
                List<FavouriteSong> favouriteSongs = favouriteSongRepository.findAll();
                List<FavouriteSong> notUploadedFavouriteSongs = getNotUploadedFavouriteSongs(favouriteSongs);
                if (notUploadedFavouriteSongs.size() == 0) {
                    return;
                }
                FavouriteSongApiBean favouriteSongApiBean = new FavouriteSongApiBean(context);
                List<FavouriteSongDTO> favouriteSongDTOS = favouriteSongApiBean.uploadFavouriteSongs(notUploadedFavouriteSongs);
                if (favouriteSongDTOS != null) {
                    setUploadedToServer(notUploadedFavouriteSongs, favouriteSongRepository);
                }
            }
        });
        thread.start();
    }

    private void setUploadedToServer(List<FavouriteSong> favouriteSongs, FavouriteSongRepositoryImpl favouriteSongRepository) {
        for (FavouriteSong favouriteSong : favouriteSongs) {
            favouriteSong.setUploadedToServer(true);
        }
        favouriteSongRepository.save(favouriteSongs);
    }

    private List<FavouriteSong> getNotUploadedFavouriteSongs(List<FavouriteSong> favouriteSongs) {
        List<FavouriteSong> notUploadedFavouriteSongs = new ArrayList<>();
        for (FavouriteSong favouriteSong : favouriteSongs) {
            if (!favouriteSong.isUploadedToServer()) {
                notUploadedFavouriteSongs.add(favouriteSong);
            }
        }
        return notUploadedFavouriteSongs;
    }
}
