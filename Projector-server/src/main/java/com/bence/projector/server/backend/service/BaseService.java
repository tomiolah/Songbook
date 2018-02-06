package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.BaseEntity;

import java.util.List;

public interface BaseService<M extends BaseEntity> {
    M findOne(String id);

    List<M> findAll();

    void delete(String id);

    void delete(List<String> ids);

    M save(M model);

    Iterable save(List<M> models);
}
