package com.readcircle.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "resources")
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String codeKey;

    @Enumerated(EnumType.STRING)
    private ResourceType type;

    private int totalUnits;

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL)
    private List<ResourceTranslation> translations;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodeKey() { return codeKey; }
    public void setCodeKey(String codeKey) { this.codeKey = codeKey; }

    public ResourceType getType() { return type; }
    public void setType(ResourceType type) { this.type = type; }

    public int getTotalUnits() { return totalUnits; }
    public void setTotalUnits(int totalUnits) { this.totalUnits = totalUnits; }

    public List<ResourceTranslation> getTranslations() { return translations; }
    public void setTranslations(List<ResourceTranslation> translations) { this.translations = translations; }
}