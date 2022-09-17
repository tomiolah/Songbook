package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.BaseEntity;

import java.util.List;

public interface BaseService<M extends BaseEntity> {
    M findOne(Long id);

    List<M> findAll();

    void delete(Long id);

    void delete(List<Long> ids);

    M save(M model);

    Iterable<M> save(List<M> models);

    void saveAllByRepository(List<M> models);
}
