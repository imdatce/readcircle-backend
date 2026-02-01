package com.readcircle.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "sessions")
public class DistributionSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private String description;

    private String creatorName;

     private int participants;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Assignment> assignments;

    public int getParticipants() { return participants; }
    public void setParticipants(int participants) { this.participants = participants; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public List<Assignment> getAssignments() { return assignments; }
    public void setAssignments(List<Assignment> assignments) { this.assignments = assignments; }
}