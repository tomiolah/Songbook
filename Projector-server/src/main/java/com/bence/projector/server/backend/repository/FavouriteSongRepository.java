package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.FavouriteSong;
import org.springframework.data.repository.CrudRepository;

public interface FavouriteSongRepository extends CrudRepository<FavouriteSong, Long> {
}
