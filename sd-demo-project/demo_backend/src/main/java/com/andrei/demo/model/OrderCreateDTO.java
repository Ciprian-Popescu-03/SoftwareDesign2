package com.andrei.demo.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class OrderCreateDTO {

    @NotNull(message = "Person ID is required")
    private UUID personId;

    @NotEmpty(message = "At least one Product ID is required")
    private List<UUID> productIds;

    public UUID getPersonId() {
        return personId;
    }

    public void setPersonId(UUID personId) {
        this.personId = personId;
    }

    public List<UUID> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<UUID> productIds) {
        this.productIds = productIds;
    }
}