package com.vespa.baek.cafeoma.inventory.data;

public class Item { //내부 멤버변수를 private로 선언했음 보안을 위하여
    private String image;
    private String name;
    private long quantity;
    private String remark; // 비고
    private String shopUrl;

    public Item(){}

    public Item(String image, String name, long quantity, String remark, String shopUrl) {
        this.image = image;
        this.name = name;
        this.quantity = quantity;
        this.remark = remark;
        this.shopUrl = shopUrl;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getQuantity() { return quantity; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getShopUrl() {
        return shopUrl;
    }

    public void setShopUrl(String shopUrl) {
        this.shopUrl = shopUrl;
    }
}

