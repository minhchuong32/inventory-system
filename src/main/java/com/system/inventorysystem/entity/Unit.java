package com.system.inventorysystem.entity;

import com.system.inventorysystem.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "units")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Unit extends BaseEntity {
    @NotBlank(message = "Tên đơn vị không được để trống")
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "abbreviation", length = 20)
    private String abbreviation;

    @Column(name = "description", length = 255)
    private String description;

    @Override
    public boolean isValid() {
        return super.isValid() && name != null && !name.isBlank();
    }
}
