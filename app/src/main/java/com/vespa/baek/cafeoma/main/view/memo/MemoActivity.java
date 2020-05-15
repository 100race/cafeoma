package com.vespa.baek.cafeoma.main.view.memo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.main.data.UserModel;
import com.vespa.baek.cafeoma.main.view.memo.data.Memo;

public class MemoActivity extends AppCompatActivity {

    //[View]
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseFirestore db;
    private MemoAdapter adapter;
    private ImageButton btn_add;
    private ImageButton btn_back;

    //[Auth]
    private FirebaseAuth mAuth;
    private String userUid;
    private String userEmail;
    public  String invenId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo);

        btn_add = findViewById(R.id.btn_add);
        btn_back = findViewById(R.id.btn_back);

        btn_add.setOnClickListener(v -> onClick(v));
        btn_back.setOnClickListener(v -> onClick(v));


        recyclerView = findViewById(R.id.rv_memo); // 리사이클러뷰
        db = FirebaseFirestore.getInstance();

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

        Query query = db.collection("Inventory").document(invenId).collection("Memo").orderBy("date", Query.Direction.DESCENDING); //날짜순

        FirestoreRecyclerOptions<Memo> options = new FirestoreRecyclerOptions.Builder<Memo>()
                .setQuery(query, Memo.class)
                .build();

        adapter = new MemoAdapter(options,this);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
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
                Intent intent = new Intent(MemoActivity.this,ModifyMemoActivity.class);
                startActivity(intent);
                break;
        }
    }

}
