package com.vespa.baek.cafeoma.main.view.shop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.main.data.UserModel;
import com.vespa.baek.cafeoma.main.view.shop.data.Shop;
import com.vespa.baek.cafeoma.main.view.shop.data.ShopModel;


public class ShopActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager gridLayoutManager;
    private FirebaseFirestore db;
    private ShopAdapter adapter;
    private ImageButton btn_add;
    private ImageButton btn_back;

    //test
    private FirebaseAuth mAuth;
    private String userUid;
    private String userEmail;
    private String invenId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        btn_add = findViewById(R.id.btn_add);
        btn_back = findViewById(R.id.btn_back);

        btn_add.setOnClickListener(v -> onClick(v));
        btn_back.setOnClickListener(v -> onClick(v));


        recyclerView = findViewById(R.id.rv_shop);
        db = FirebaseFirestore.getInstance(); // 파이어스토어 연동

        //계정 정보 ( 이메일, uid , invenId)초기화
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userEmail = currentUser.getEmail();
        userUid = currentUser.getUid();

        //메인에서 액티비티 연결 전에 checkInven으로 invneId를 채워놓고 와서 nullException 안걸림. 안하면 null걸린다.
        invenId = UserModel.invenId;
        Log.d("확인",invenId);
        initView();
    }

    public void initView() {

        Query query = db.collection("Inventory").document(invenId).collection("ShopUrl");

        FirestoreRecyclerOptions<Shop> options = new FirestoreRecyclerOptions.Builder<Shop>()
                .setQuery(query, Shop.class)
                .build();

        adapter = new ShopAdapter(options,this);

        // 한줄에 3개의 컬럼을 추가
        int numberOfColumns = 3;
        gridLayoutManager = new GridLayoutManager(this,numberOfColumns);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true); //리사이클러뷰 기존성능강화
        recyclerView.setAdapter(adapter);

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
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_add:
                addShopDialog();
                break;
        }
    }

    public void addShopDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ShopActivity.this);

        // 다이얼로그를 보여주기 위해 edit_box.xml 파일을 사용

        View view = LayoutInflater.from(ShopActivity.this)
                .inflate(R.layout.edit_box, null, false);
        builder.setView(view);
        final Button ButtonSubmit = (Button) view.findViewById(R.id.btn_dialog_submit);
        final EditText et_dialog_shopName = (EditText) view.findViewById(R.id.et_dialog_shopName);
        final EditText et_dialog_shopUrl = (EditText) view.findViewById(R.id.et_dialog_shopUrl);

        ButtonSubmit.setText("추가");

        final AlertDialog dialog = builder.create();
        ButtonSubmit.setOnClickListener(new View.OnClickListener() {

            // 추가 버튼을 클릭하면 현재 UI에 입력되어 있는 내용으로

            public void onClick(View v) {
                String strName = et_dialog_shopName.getText().toString();
                String strURl = et_dialog_shopUrl.getText().toString();
                Shop shop = new Shop(strName, strURl);

                // firebase에 있는 데이터를 변경하고
                new ShopModel().saveShop(shop,db);

                // 어댑터에서 RecyclerView에 반영

                adapter.notifyDataSetChanged();

                dialog.dismiss();
            }
        });

        dialog.show();
    }

}
