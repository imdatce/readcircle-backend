package com.readcircle.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
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

    @Override
    public String toString() {
        return "ResourceTranslation(id=" + id + ", name=" + name + ", langCode=" + langCode + ")";
    }
}