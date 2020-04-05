package com.vespa.baek.cafeoma.inventory.adapter;

import com.vespa.baek.cafeoma.inventory.data.Item;

import java.util.ArrayList;

public interface InventoryAdapterContract {

    interface View{
        void notifyAdapter();
    }

    interface Model{
        void addItems(ArrayList<Item> items);
        void clearItems();
    }
}
