package com.vespa.baek.cafeoma.inventory.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.vespa.baek.cafeoma.R;;
import com.vespa.baek.cafeoma.inventory.data.Item;


public class InventoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter adapter;
    private Button btn_search;
    private Button btn_delete;
    private Button btn_add;
    private TextView tv_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        btn_add = findViewById(R.id.btn_add);
        btn_search = findViewById(R.id.btn_search);
        btn_delete = findViewById(R.id.btn_delete);
        tv_search = findViewById(R.id.tv_search);

        btn_add.setOnClickListener(view -> onClick(view));
        btn_search.setOnClickListener(view -> onClick(view));
        btn_delete.setOnClickListener(view -> onClick(view));


        recyclerView = findViewById(R.id.inventoryView);
        db = FirebaseFirestore.getInstance(); // 파이어스토어 연동

        //Query 쿼리 사용하는 법을 잘 몰라서 이렇게된듯 ㅎ 일단 하위컬렉션 접근하려면 이렇게 해야되나? path를 한번에 쓰면 안되고? 데이터를 이름순으로 정렬해서 뿌려줌
        Query query = db.collection("Inventory").document("jG9OZBK4zUH7mgWAeh7q").collection("InventoryItem").orderBy("name", Query.Direction.ASCENDING);
        //RecyclerOptions 요기 옵션을 넣기 때문에 파이어스토어 어댑터를 따로 써야함

        FirestoreRecyclerOptions<Item> options = new FirestoreRecyclerOptions.Builder<Item>()
                .setQuery(query, Item.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Item, InventoryViewHolder>(options) {
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
                if(model.getImage()!=null) {
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
                                                                startActivity(intent);
                                                            } else {
                                                                Toast.makeText(InventoryActivity.this, "주문 사이트를 등록해주세요", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    }
                );
            }
        };
        //위치 일로 옮겨봄
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true); //리사이클러뷰 기존성능강화
        recyclerView.setAdapter(adapter);
        Log.d("재고", "어댑터추가확인");

    } //onCreate끝

    //viewholder

    public class InventoryViewHolder extends RecyclerView.ViewHolder { // 여기에 레이아웃으로 나와야되는애들 써줌 ex.사진,이름,수량,주문버튼
        private ImageView iv_image;
        private TextView tv_name;
        private TextView tv_remark;
        private TextView tv_quantity;
        private Button btn_order;


        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            this.iv_image = itemView.findViewById(R.id.iv_image);
            this.tv_name = itemView.findViewById(R.id.tv_name);
            this.tv_remark = itemView.findViewById(R.id.tv_remark);
            this.tv_quantity = itemView.findViewById(R.id.tv_quantity);
            this.btn_order = itemView.findViewById(R.id.btn_order);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening(); // 어댑터가 stopListening(?)할수잇게게
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.btn_add:
                Intent intent = new Intent(InventoryActivity.this, ModifyInventoryActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_search:
                break;
            case R.id.btn_delete:
                break;

        }

    }
}


//테스트용
//
//        DocumentReference docRef = db.collection("Inventory").document("jG9OZBK4zUH7mgWAeh7q").collection("InventoryItem").document("79fDM5YNF08WdtCGveWd");
//        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        Log.d("재고", "DocumentSnapshot data: " + document.getData());
//                    } else {
//                        Log.d("재고", "No such document");
//                    }
//                } else {
//                    Log.d("재고", "get failed with ", task.getException());
//                }
//            }
//        });

//