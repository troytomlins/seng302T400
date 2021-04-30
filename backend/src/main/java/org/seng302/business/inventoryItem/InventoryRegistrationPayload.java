package org.seng302.business.inventoryItem;

import org.seng302.business.product.ProductPayload;

import java.time.LocalDate;

public class InventoryRegistrationPayload {

    private String productId;
    private Integer quantity;
    private Double pricePerItem;
    private Double totalPrice;
    private LocalDate manufactured;
    private LocalDate sellBy;
    private LocalDate bestBefore;
    private LocalDate expires;

    //getter
    public String getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Double getPricePerItem() {
        return pricePerItem;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public LocalDate getManufactured() {
        return manufactured;
    }

    public LocalDate getSellBy() {
        return sellBy;
    }

    public LocalDate getBestBefore() {
        return bestBefore;
    }

    public LocalDate getExpires() {
        return expires;
    }
}

