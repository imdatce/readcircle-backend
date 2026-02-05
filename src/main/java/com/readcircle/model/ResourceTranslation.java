package com.readcircle.model;

import com.fasterxml.jackson.annotation.JsonIgnore; // BU EKLENDİ
import jakarta.persistence.*;
import lombok.Getter; // BU EKLENDİ
import lombok.Setter; // BU EKLENDİ

@Entity
@Getter // @Data yerine bunları kullanıyoruz (Döngüyü önler)
@Setter
@Table(name = "resource_translations")
public class ResourceTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String langCode;
    private String name;
    private String unitName;

     @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    @JsonIgnore
    private Resource resource;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

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

     @Override
    public String toString() {
        return "ResourceTranslation(id=" + id + ", name=" + name + ", langCode=" + langCode + ")";
    }
}