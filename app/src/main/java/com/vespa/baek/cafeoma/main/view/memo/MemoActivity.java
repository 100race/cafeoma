package com.vespa.baek.cafeoma.main.view.memo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.main.data.UserModel;

public class MemoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager gridLayoutManager;
    private FirebaseFirestore db;
    private MemoAdapter adapter;
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
        setContentView(R.layout.activity_memo);

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

        // 한줄에 3개의 컬럼을 추가합니다.
        int numberOfColumns = 3;
        gridLayoutManager = new GridLayoutManager(this,numberOfColumns);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true); //리사이클러뷰 기존성능강화
        recyclerView.setAdapter(adapter);

    }
}
