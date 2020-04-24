package com.vespa.baek.cafeoma.inventory.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.vespa.baek.cafeoma.R;;
import com.vespa.baek.cafeoma.inventory.adapter.InventoryAdapter;
import com.vespa.baek.cafeoma.inventory.adapter.InventoryViewHolder;
import com.vespa.baek.cafeoma.inventory.data.Item;

import java.util.Locale;


public class InventoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseFirestore db;
    private InventoryAdapter adapter;
    private Button btn_search;
    private Button btn_delete;
    private Button btn_add;
    private EditText et_search;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_inventory);


        btn_add = findViewById(R.id.btn_add);
        btn_search = findViewById(R.id.btn_search);
        btn_delete = findViewById(R.id.btn_delete);
        et_search = findViewById(R.id.et_search);

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



        //^InventoryAdapter로 옮긴부분^ 04.13
        adapter = new InventoryAdapter(options,this);

        //위치 일로 옮겨봄
        layoutManager = new newLinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true); //리사이클러뷰 기존성능강화
        recyclerView.setAdapter(adapter);
        et_search.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {

                String text = et_search.getText().toString()

                        .toLowerCase(Locale.getDefault());
                adapter.getFilter().filter(text);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
                // TODO Auto-generated method stub
            }
        });
    }

    //onCreate끝


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
           //     String searchText = String.valueOf(et_search.getText()); 일단 아직 쓸일이없음
                //    searchItem(searchText);
                break;
            case R.id.btn_delete:
                break;

        }

    }

//    public void searchItem(String searchText){
//         adapter.getFilter().filter(searchText);
//        CollectionReference collectionref = db.collection("Inventory").document("jG9OZBK4zUH7mgWAeh7q").collection("InventoryItem");
//        //Query query = collectionref.whereEqualTo("name", searchText);
//        Query query = collectionref.orderBy("name").startAt(searchText).endAt(searchText+"\uf8ff");
//
//        FirestoreRecyclerOptions<Item> options = new FirestoreRecyclerOptions.Builder<Item>()
//                .setQuery(query, Item.class)
//                .build();
//
//        adapter = new InventoryAdapter(options,this);
//
//        recyclerView.setAdapter(adapter); onCreate 아닌곳에서 setAdapter하려고하면 안되나봄..?
//        adapter.notifyDataSetChanged();
//        //뿌려줬다가도 뒤로버튼을 누르면 다시 원래 어댑터, 원래 옵션 쿼리로 작동하도록 설정해야됨
//        }

    private static class newLinearLayoutManager extends LinearLayoutManager {
        /**
         * Disable predictive animations. There is a bug in RecyclerView which causes views that
         * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
         * adapter size has decreased since the ViewHolder was recycled.
         */
        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }

        public newLinearLayoutManager(Context context) {
            super(context);
        }

        public newLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public newLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }
    }
    }



