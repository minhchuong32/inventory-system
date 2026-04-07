package com.system.inventorysystem.factory;

// T : Entity
// D : DTO
public interface EntityFactory<T, D> {
    D toDTO(T entity);
}
