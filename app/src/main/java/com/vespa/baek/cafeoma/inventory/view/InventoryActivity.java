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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.vespa.baek.cafeoma.R;;
import com.vespa.baek.cafeoma.inventory.adapter.InventoryAdapter;
import com.vespa.baek.cafeoma.inventory.adapter.InventoryViewHolder;
import com.vespa.baek.cafeoma.inventory.data.Item;
import com.vespa.baek.cafeoma.main.data.UserModel;

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

    //test
    private FirebaseAuth mAuth;
    private String userUid;
    private String userEmail;
    private String inventoryId;


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

        //test
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userEmail = currentUser.getEmail();
        userUid = currentUser.getUid();

        Log.d("확인",UserModel.invenId);
        Log.d("확인","추가되기전에 인벤토리액티비티가 실행되나");


//        db.collection("User").document(userUid)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if (task.isSuccessful()) {
//                           DocumentSnapshot document = task.getResult();
//                            if (document.get("inventoryid") != null) {// db있으면
//
 //                               //설마 이 true 데이터가 또여기서만 유지돼서, 여기서 다 끝내줘야되는건가?왠지그런거같음
//                            } else { // db없으면
//
//                            }
 //                       } else { // 실패
 //                       }
 //                   }
 //               });



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



