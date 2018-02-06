package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.BaseEntity;
import com.bence.projector.server.backend.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public abstract class BaseServiceImpl<M extends BaseEntity> implements BaseService<M> {
    @Autowired
    private CrudRepository<M, String> repository;

    @Override
    public M findOne(String id) {
        return repository.findOne(id);
    }

    @Override
    public List<M> findAll() {
        return (List<M>) repository.findAll();
    }

    @Override
    public void delete(String id) {
        repository.delete(id);
    }

    @Override
    public void delete(final List<String> ids) {
        ids.forEach(id -> repository.delete(id));
    }

    @Override
    public M save(final M model) {
        return repository.save(model);
    }

    @Override
    public Iterable save(final List<M> models) {
        return repository.save(models);
    }
}
