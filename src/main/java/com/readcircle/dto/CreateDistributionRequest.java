package com.readcircle.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

public class CreateDistributionRequest {

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String description;

    @Min(value = 1, message = "Min 1 participate.")
    private int participants;

    @NotEmpty(message = "Min 1 part.")
    private List<Long> resourceIds;

    private Map<Long, Integer> customTotals;

    public int getParticipants() {
        return participants;
    }

    public void setParticipants(int participants) {
        this.participants = participants;
    }

    public List<Long> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List<Long> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public Map<Long, Integer> getCustomTotals() {
        return customTotals;
    }

    public void setCustomTotals(Map<Long, Integer> customTotals) {
        this.customTotals = customTotals;
    }
}