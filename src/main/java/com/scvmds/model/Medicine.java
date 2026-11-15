package com.scvmds.model;

public record Medicine(
        int id,
        String name,
        String description,
        String dosageForm,
        int stockQuantity,
        String expirationDate,
        String status
) {
    @Override
    public String toString(){
        return String.format("(%d) %s %s [%s] (Stock: %d) (Expiration: %s) STATUS: %s ", id, name, description, dosageForm, stockQuantity, expirationDate, status );
    }
}