package com.vespa.baek.cafeoma.inventory.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.vespa.baek.cafeoma.R;;
import com.vespa.baek.cafeoma.inventory.adapter.InventoryAdapter;
import com.vespa.baek.cafeoma.inventory.data.Item;
import com.vespa.baek.cafeoma.inventory.view.presenter.InventoryDetailsLookUp;


public class InventoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private SelectionTracker<Long> selectionTracker; //private로해도되지?
    private FirebaseFirestore db;
    private InventoryAdapter adapter;
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

        //^InventoryAdapter로 옮긴부분^ 04.13
        adapter = new InventoryAdapter(options,);

        //위치 일로 옮겨봄
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true); //리사이클러뷰 기존성능강화
        recyclerView.setAdapter(adapter);
        setupSelectionTracker();
        adapter.setSelectionTracker(selectionTracker);
        Log.d("재고", "어댑터추가확인");



    } //onCreate끝

    //viewholder - > ^InventoryViewHolder^로 옮긴부분 04.13


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

    private void setupSelectionTracker(){
        selectionTracker = new SelectionTracker.Builder<>(
                "selection_id",
                recyclerView,
                new StableIdKeyProvider(recyclerView),
                new InventoryDetailsLookUp(recyclerView),
                StorageStrategy.createLongStorage())
                .withSelectionPredicate(SelectionPredicates.<Long>createSelectAnything()) //제약사항없이 여러개 선택가능하도록 하는것
                .build();
    }
}

