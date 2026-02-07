package com.readcircle.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "assignments")
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    private String guestName;

    private int participantNumber;
    private int startUnit;
    private int endUnit;

    @JsonProperty("isTaken")
    private boolean isTaken = false;

    // --- YENİ EKLENEN KISIM BAŞLANGIÇ ---
    @Column(nullable = false, columnDefinition = "boolean default false")
    @JsonProperty("isCompleted") // <--- BU SATIR ÇOK ÖNEMLİ (Frontend ile uyum için)
    private boolean isCompleted = false;
    // --- YENİ EKLENEN KISIM BİTİŞ ---

    private String assignedToName;

    @ManyToOne
    @JoinColumn(name = "session_id")
    @JsonIgnore
    private DistributionSession session;

    @ManyToOne
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @Column(nullable = true)
    private Integer currentCount;

    // --- GETTER & SETTER ---

    // Bu getter'a da anotasyon ekleyelim garanti olsun
    @JsonProperty("isCompleted")
    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    // Diğer Getter Setterlar (Aynı kalacak)
    public Integer getCurrentCount() { return currentCount; }
    public void setCurrentCount(Integer currentCount) { this.currentCount = currentCount; }
    public Resource getResource() { return resource; }
    public void setResource(Resource resource) { this.resource = resource; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getParticipantNumber() { return participantNumber; }
    public void setParticipantNumber(int participantNumber) { this.participantNumber = participantNumber; }
    public int getStartUnit() { return startUnit; }
    public void setStartUnit(int startUnit) { this.startUnit = startUnit; }
    public int getEndUnit() { return endUnit; }
    public void setEndUnit(int endUnit) { this.endUnit = endUnit; }

    @JsonProperty("isTaken")
    public boolean isTaken() { return isTaken; }

    public void setTaken(boolean taken) { isTaken = taken; }
    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }
    public DistributionSession getSession() { return session; }
    public void setSession(DistributionSession session) { this.session = session; }
}