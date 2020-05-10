package com.vespa.baek.cafeoma.main.view.shop.data;

public class Shop {
    private String shopName;
    private String shopUrl;

    public Shop() {
    }

    public Shop(String shopName, String shopUrl) {
        this.shopName = shopName;
        this.shopUrl = shopUrl;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopUrl() {
        return shopUrl;
    }

    public void setShopUrl(String shopUrl) {
        this.shopUrl = shopUrl;
    }
}
