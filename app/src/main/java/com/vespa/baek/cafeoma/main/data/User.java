package com.vespa.baek.cafeoma.main.data;

public class User {
    private String email;
    private String inventoryid;

    public User(String email, String inventoryid) {
        this.email = email;
        this.inventoryid = inventoryid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setInventoryid(String inventoryid) {
        this.inventoryid = inventoryid;
    }

    public String getEmail() {
        return email;
    }

    public String getInventoryid() {
        return inventoryid;
    }


}
