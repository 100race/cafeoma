package com.vespa.baek.cafeoma.inventory.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.inventory.data.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.SelectionTracker;

import static com.facebook.FacebookSdk.getApplicationContext;

public class InventoryAdapter extends FirestoreRecyclerAdapter<Item, InventoryViewHolder> {

    private ArrayList<Item> items;
    private HashMap<Long,Boolean> selectedItems;
    private Context context;

    @Nullable
    private SelectionTracker<Long> selectionTracker;

    public InventoryAdapter(@NonNull FirestoreRecyclerOptions<Item> options, Context context,ArrayList<Item> items) {
        super(options);
        setHasStableIds(true); // Id를 이용해 아이디를 식별하겠다고 알려줌
        this.context = context;
        this.items = items;
        selectedItems = new HashMap<>();

    }

    public HashMap<Long, Boolean> getSelectedItems() {
        return selectedItems;
    }


//    @Override
//    public void onChildChanged(@NonNull ChangeEventType type, @NonNull DocumentSnapshot snapshot, int newIndex, int oldIndex) {
//        super.onChildChanged(type, snapshot, newIndex, oldIndex);
//
//    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }


    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @NonNull
        @Override
        public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {  //뷰홀더를 최초로 만들어내는곳 인플레이터활용
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            Log.d("재고", "인플레이터실행됨");
            return new InventoryViewHolder(view,selectedItems);
        }

        @Override
        protected void onBindViewHolder(@NonNull InventoryViewHolder holder, int position, @NonNull Item model) {  // 각 아이템에 대한 매칭을 하는것
            //이미지를 받아와서 이미지뷰에 넣어주는 모습 . null일 경우 디폴트 이미지 그대로 출력된다.
            if (model.getImage() != null) {
                Glide.with(holder.itemView)
                        .load(model.getImage())
                        .into(holder.iv_image);
            }
            holder.tv_name.setText(model.getName()); //현재 이 model은 Item 타입임
            holder.tv_remark.setText(model.getRemark());
            holder.tv_quantity.setText(String.valueOf(model.getQuantity())); // long타입을 받아서 텍스트로 넣어주기
            //버튼을 누를 시 url로 연결
            holder.btn_order.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        //*****여기서 등록된 Url에 대한 유효성 테스트를 하던지, 처음에 Url을 넣을 때 테스트를 하던지 해야함. https:// 안넣은 상태로 www.naver.com 하니까 안되더라고 *****
                                                        if (model.getShopUrl() != null) {
                                                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(model.getShopUrl()));
                                                            v.getContext().startActivity(intent);
                                                        } else {
                                                            Toast.makeText(getApplicationContext(), "주문 사이트를 등록해주세요", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }
            );
            //여기서도 selectionTracker을 정의해줘서 뭐가 일어날지 지정해줌
            holder.setSelectionTracker(selectionTracker);
//            if(selectionTracker.isSelected(Long.valueOf(position))){ //현 position의 뷰가 선택되어있으면
//
//            }


        }

    //여기서 setSelectionTracker 정의해주는건 전달해주는거
    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
        }

//    public void deleteItem(HashMap<Long,Boolean> selectedItems, InventoryAdapter adapter) { // 여길 수정해야할듯 position이아니라 selectedItems가 순서랑 stablekey를 갖도록. 그 아이디로 position을 갖고와서 getSnapshot
//        for(long key : selectedItems.keySet()){
//            //stableid로 adpater의 position을 얻어오는 방법?
//            new StableIdKeyProvider()
//            selectionTracker. -> 의 키 프로바이더를 가져와서 .getPosition(key) 하면 position나옴
//            adapter.getSnapshots().getSnapshot().getReference().delete(); //와 개기네 근데 이게맞나?
//        }
//
//    }

}





