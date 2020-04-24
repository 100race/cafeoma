package com.vespa.baek.cafeoma.inventory.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.inventory.data.Item;


import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

import static com.facebook.FacebookSdk.getApplicationContext;



public class InventoryAdapter extends FilterableFirestoreRecyclerAdapter<Item,InventoryViewHolder> {
    private final static String defaultImage = "";
    private Context context;



    public InventoryAdapter(@NonNull FirestoreRecyclerOptions<Item> options, Context context) {
        super(options,true);
        this.context = context;
        setHasStableIds(true);
    }
    //[filterable해보기]

    @Override
    protected boolean filterCondition(Item model, String filterPattern) {
        return model.getName().toLowerCase().contains(filterPattern);
    }


//    @Override //여기 말고 리스트 있는 어댑터에서 구현해봐야게사다
//    public long getItemId(int position) {
//
//        return itemId;
//    }

    //여기서 아예 position에 따른 db의 document id를 가져와버리면 되지않을까?
    public int getFilteredPos(int position) {
        ArrayList<Integer> filteredIndex = getFilteredIndex();
        int filteredPos = filteredIndex.get(position);
        Log.d("filteredPos:",String.valueOf(filteredIndex.get(position)));
        //String itemId = this.getSnapshots().getSnapshot(filteredPos).getReference().getId(); //
        return filteredPos;
    }
    //documentid를 바로 가져오게 할수도있음


    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {  //뷰홀더를 최초로 만들어내는곳 인플레이터활용
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        Log.d("재고", "인플레이터실행됨");
        return new InventoryViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull InventoryViewHolder holder, int position, @NonNull Item model) {  // 각 아이템에 대한 매칭을 하는것
        //이미지를 받아와서 이미지뷰에 넣어주는 모습 . null일 경우 default이미지로 출력된다
        if (model.getImage() != null && model.getImage()!=defaultImage) {
            Glide.with(holder.itemView)
                    .load(model.getImage())
                    .into(holder.iv_image);
        } else {
            Glide.with(holder.itemView)
                    .load(R.drawable.default_image_icon)
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

        holder.setAdapter(this); // this하면 adapter이 연결되겠지?
    }
}








