package com.vespa.baek.cafeoma.inventory.view.presenter;

import android.view.MotionEvent;
import android.view.View;

import com.vespa.baek.cafeoma.inventory.adapter.InventoryViewHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

public class InventoryDetailsLookUp extends ItemDetailsLookup<Long> {
    //사용자의 선택과 관련된 항목에 대한 정보를 제공. MotionEvent를 기반하여 선택된 내용을 ViewHolder에 매핑.

    private RecyclerView recyclerView;

    public InventoryDetailsLookUp(RecyclerView recyclerView){
        this.recyclerView = recyclerView;
    }
    @Nullable
    @Override
    public ItemDetails<Long> getItemDetails(@NonNull MotionEvent motionEvent) {
        View view = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
        if (view != null) {
            InventoryViewHolder viewHolder = (InventoryViewHolder) recyclerView.getChildViewHolder(view);
            return viewHolder.getItemDetails();
        }
        return null;
    }
}
