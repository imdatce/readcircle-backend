package com.readcircle.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
@Table(name = "resource_translations")
public class ResourceTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String langCode;
    private String name;
    private String unitName;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;
    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLangCode() {
        return langCode;
    }

    @ManyToOne
    @JoinColumn(name = "resource_id")
    @JsonIgnore
    private Resource resource;

    public void setLangCode(String tr) {
    }
}