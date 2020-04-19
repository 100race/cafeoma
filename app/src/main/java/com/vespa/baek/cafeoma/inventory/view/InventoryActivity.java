package com.vespa.baek.cafeoma.inventory.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.vespa.baek.cafeoma.R;;
import com.vespa.baek.cafeoma.inventory.adapter.InventoryAdapter;
import com.vespa.baek.cafeoma.inventory.adapter.InventoryViewHolder;
import com.vespa.baek.cafeoma.inventory.data.Item;
import com.vespa.baek.cafeoma.inventory.data.ItemModel;
import com.vespa.baek.cafeoma.inventory.view.presenter.InventoryDetailsLookUp;

import java.util.ArrayList;
import java.util.HashMap;


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
    private ArrayList<Item> items; //필요없을듯


    private HashMap<Long,Boolean> selectedItems;
    //private ActionModeCallback actionModeCallback;


    //액션모드 종료를 위해
    ActionMode actionMode = null;

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

        items = new ItemModel().getItemList(db,"jG9OZBK4zUH7mgWAeh7q"); //데이터베이스에서 가져올 아이템초기화

        //Query 쿼리 사용하는 법을 잘 몰라서 이렇게된듯 ㅎ 일단 하위컬렉션 접근하려면 이렇게 해야되나? path를 한번에 쓰면 안되고? 데이터를 이름순으로 정렬해서 뿌려줌
        Query query = db.collection("Inventory").document("jG9OZBK4zUH7mgWAeh7q").collection("InventoryItem").orderBy("name", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Item> options = new FirestoreRecyclerOptions.Builder<Item>()
                .setQuery(query, Item.class)
                .build();

        //^InventoryAdapter로 옮긴부분^ 04.13
        adapter = new InventoryAdapter(options,this,items);

        //위치 일로 옮겨봄
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true); //리사이클러뷰 기존성능강화
        recyclerView.setAdapter(adapter);
        setupSelectionTracker();
        adapter.setSelectionTracker(selectionTracker);
        Log.d("재고", "어댑터추가확인");

        //액션모드 콜백메서드
        //actionModeCallback = new ActionModeCallback();



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
            case R.id.btn_delete: // 여기서 버튼누르면 selectedItem 삭제
                this.selectedItems = adapter.getSelectedItems();
                deleteItem(selectedItems,adapter);
                //버튼 누를 시 selection모드 비활성화하기. 끄기.
                selectionTracker.clearSelection();
//                if (actionMode != null) {
//                    actionMode.finish();
//                }
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


    //액션모드 내부메서드로 콜백메서드 구현
//    private class ActionModeCallback implements ActionMode.Callback {
//        @Override
//        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
////            Tools.setSystemBarColor(MultiSelect.this, R.color.colorDarkBlue2);
//            Toolbar tb = (Toolbar) findViewById(R.id.toolBar) ;
//            setSupportActionBar(tb) ;
//            mode.getMenuInflater().inflate(R.menu.contexual_menu, menu);
//
//            actionMode = mode;
//            return true;
//        }
//
//        @Override
//        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//            return false;
//        }
//
//        @Override
//        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
////            int id = item.getItemId();
////            if (id == R.id.action_delete) {
////                deleteInboxes();
////                mode.finish();
////                return true;
////            }
//            return false;
//        }
//
//        @Override
//        public void onDestroyActionMode(ActionMode mode) {
//            adapter.clearSelections();
//            actionMode = null;
//            //Tools.setSystemBarColor(MultiSelect.this, R.color.colorPrimary);
//        }
//    }

    @Override
    public void onBackPressed() { //super이 처음으로 안나와도되나?
        if(actionMode!=null){
            actionMode.finish();
        }else{
        super.onBackPressed();
        }

    }

    public void deleteItem(HashMap<Long,Boolean> selectedItems, InventoryAdapter adapter) { // 여길 수정해야할듯 position이아니라 selectedItems가 순서랑 stablekey를 갖도록. 그 아이디로 position을 갖고와서 getSnapshot
        for(long key : selectedItems.keySet()){
            //stableid로 adpater의 position을 얻어오는 방법?
            int position = new StableIdKeyProvider(recyclerView).getPosition(key);
           //키 프로바이더를 가져와서 .getPosition(key) 하면 position나옴
            adapter.getSnapshots().getSnapshot(position).getReference().delete(); //와 개기네 근데 이게맞나?
        }

    }
}

