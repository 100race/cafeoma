package com.vespa.baek.cafeoma.main.view.memo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.main.data.UserModel;
import com.vespa.baek.cafeoma.main.view.memo.data.Memo;
import com.vespa.baek.cafeoma.main.view.memo.data.MemoModel;

import java.util.Map;

public class MemoViewActivity extends AppCompatActivity {
    private final String TAG = "MemoViewActivity";
    //[VIEW]
    private TextView tv_title;
    private TextView tv_contents;
    private TextView tv_date;
    private Button btn_toEdit;
    private Button btn_cancel;
    private Memo memo;
    private MemoModel memoModel;

    private FirebaseFirestore db;
    private Intent intent;
    private String documentId;
    private static String invenId;

    private boolean isModify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_view);

        tv_title = findViewById(R.id.tv_title);
        tv_contents = findViewById(R.id.tv_contents);
        tv_date = findViewById(R.id.tv_date);
        btn_toEdit = findViewById(R.id.btn_toEdit);
        btn_cancel = findViewById(R.id.btn_cancel);

        db = FirebaseFirestore.getInstance(); // 초기화 까먹지마라


        memo = new Memo();
        memoModel = new MemoModel();

        intent = getIntent();


        setInvenId();
        Log.d("MemoViewActivity",invenId); // 어 여기 뜸. 문제는 invenId가 아니었다??

        documentId = intent.getExtras().getString("ID");

        DocumentReference docref = db.collection("Inventory").document(invenId).collection("Memo").document(documentId); //collection
        docref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                Map<String, Object> map = document.getData();
                                tv_title.setText(String.valueOf(map.get("title")));
                                tv_contents.setText(String.valueOf(map.get("contents")));
                                tv_date.setText(String.valueOf(map.get("date")));
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });


        btn_toEdit.setOnClickListener(v->onClick(v));
        btn_cancel.setOnClickListener(v->onClick(v));

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_toEdit:
                intent = new Intent(MemoViewActivity.this,ModifyMemoActivity.class);
                intent.putExtra("ID",documentId);
                startActivity(intent);
                finish();
                break;

            case R.id.btn_cancel:
                isModify = false;
                finish();
                break;

        }

    }

    public void setInvenId(){
        this.invenId = UserModel.invenId;
    }


}
