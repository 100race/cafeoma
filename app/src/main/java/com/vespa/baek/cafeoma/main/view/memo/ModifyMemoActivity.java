package com.vespa.baek.cafeoma.main.view.memo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
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

public class ModifyMemoActivity extends AppCompatActivity {
    private final String TAG = "ModifyMemoActivity";
    //[VIEW]
    private EditText et_title;
    private EditText et_contents;
    private Button btn_save;
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
        setContentView(R.layout.activity_modify_memo);

        //추가 버튼으로 눌려왔으면 빈액티비티로 시작함
        isModify = false;

        db = FirebaseFirestore.getInstance();

        memo = new Memo();
        memoModel = new MemoModel();

        intent = getIntent();
        setInvenId();

        et_title = findViewById(R.id.et_title);
        et_contents = findViewById(R.id.et_contents);
        btn_save = findViewById(R.id.btn_save);
        btn_cancel = findViewById(R.id.btn_cancel);

        //시작시 수정버튼으로 시작했으면(intent로 getExtra했는데 null이아니라 받아온게있으먄, 왜냐면 intent는 둘다 전해줌) 받아온 데이터 뿌려주는 초기화
        if (intent.getExtras() != null || isModify == true) {
            //수정버튼으로 받아온 어댑터에존재하는 문서의 아이디
            isModify = true;
            documentId = intent.getExtras().getString("ID");
            //어댑터의 문서의 내용을 item에 저장한후
            DocumentReference docref = db.collection("Inventory").document(invenId).collection("Memo").document(documentId);
            docref.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData()); //제대로 원래이미지 뜸
                                    Map<String, Object> map = document.getData();
                                    et_title.setText(String.valueOf(map.get("title")));
                                    et_contents.setText(String.valueOf(map.get("contents")));
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
        }

        btn_save.setOnClickListener(v -> onClick(v));
        btn_cancel.setOnClickListener(v -> onClick(v));

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                if (TextUtils.isEmpty(et_title.getText().toString())||TextUtils.isEmpty(et_contents.getText().toString())) {
                    Toast.makeText(this, "내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                    break;
                }
                onAddModify();
                isModify = false;
                finish();
                break;

            case R.id.btn_cancel:
                isModify = false;
                finish();
                break;
        }
    }

    //[Memo에 데이터 넣기]
    public void onSetMemo() {
        memo.setTitle(String.valueOf(et_title.getText()));
        memo.setContents(String.valueOf(et_contents.getText()));
    }


    //[수정/저장]
    public void onAddModify() {
        onSetMemo();

        if (isModify == true) { //"수정" 버튼
            Log.d(TAG, "수정시 실행됨");
            memoModel.updateMemo(memo,db,documentId);
            //어댑터에서 RecyclerView에 반영하도록
            //adapter.notifyItemChanged(getAdapterPosition());
        } else { //"추가" 버튼
            Log.d(TAG, "추가시 실행됨");
            memoModel.saveMemo(memo, db);
        }
    }

    //[invenId가져오기]
    public void setInvenId(){
        this.invenId = UserModel.invenId;
    }
}





//    Intent intent = new Intent(context, ModifyMemoActivity.class);
//        intent.putExtra("ID",adapter.getSnapshots().getSnapshot(getAdapterPosition()).getReference().getId());
//                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//
//                db= FirebaseFirestore.getInstance();
//                documentId = adapter.getSnapshots().getSnapshot(getAdapterPosition()).getReference().getId();
//
//
//                String title = String.valueOf(adapter.getSnapshots().getSnapshot(getAdapterPosition()).get("shopName"));
//                String contents = String.valueOf(adapter.getSnapshots().getSnapshot(getAdapterPosition()).get("shopUrl"));
//                Memo memo = new Memo(title,contents);
//
//
//
//
//                String strName = et_dialog_shopName.getText().toString();
//                String strURl = et_dialog_shopUrl.getText().toString();
//                Shop shop = new Shop(strName, strURl);
//
//                // 8. firebase에 있는 데이터를 변경하고
//                new ShopModel().updateShop(shop,db,documentId);
//
//
//                // 9. 어댑터에서 RecyclerView에 반영하도록
//
//                adapter.notifyItemChanged(getAdapterPosition());