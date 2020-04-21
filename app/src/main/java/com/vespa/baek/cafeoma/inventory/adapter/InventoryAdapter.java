package com.vespa.baek.cafeoma.inventory.adapter;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.inventory.data.Item;
import androidx.annotation.NonNull;

import static com.facebook.FacebookSdk.getApplicationContext;



public class InventoryAdapter extends FirestoreRecyclerAdapter<Item, InventoryViewHolder> {
    private final static String defaultImage = "https://firebasestorage.googleapis.com/v0/b/cafeoma.appspot.com/o/default-image-icon-14.png?alt=media&token=68f74b65-2041-4dd7-b0a7-c6a20c33b8ee";


    public InventoryAdapter(@NonNull FirestoreRecyclerOptions<Item> options) {
        super(options);

    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @NonNull
        @Override
        public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {  //뷰홀더를 최초로 만들어내는곳 인플레이터활용
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            Log.d("재고", "인플레이터실행됨");
            return new InventoryViewHolder(view);
        }

        @Override
        protected void onBindViewHolder(@NonNull InventoryViewHolder holder, int position, @NonNull Item model) {  // 각 아이템에 대한 매칭을 하는것
            //이미지를 받아와서 이미지뷰에 넣어주는 모습 . null일 경우 디폴트 이미지 그대로 출력된다.
            if (model.getImage() != null) {
                Glide.with(holder.itemView)
                        .load(model.getImage())
                        .into(holder.iv_image);
            }else{
                Glide.with(holder.itemView)
                        .load(defaultImage)
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





