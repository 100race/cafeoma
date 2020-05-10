package com.vespa.baek.cafeoma.main.view.memo.data;

import android.os.Handler;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vespa.baek.cafeoma.main.data.UserModel;
import com.vespa.baek.cafeoma.main.view.memo.MemoAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

public class MemoModel {
    private static final String TAG = "MemoModel";

    private MemoAdapter adpater;

    private static String invenId;

    //로그인 된 사용자에 있는 인벤토리 정보를 가져와서 연결해줌
    public void setInvenId(){
        this.invenId = UserModel.invenId;
    }

    //수정
    public void updateMemo(Memo memo, FirebaseFirestore db, String documentId){
        Map<String, Object> map = new HashMap<>();
        setInvenId();
        DocumentReference docref = db.collection("Inventory").document(invenId).collection("Memo").document(documentId);

        map.put("title", memo.getTitle());
        map.put("contents", memo.getContents());

        //핸들러를 써서 실행에 딜레이를 줌
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                //임시경로
                db.collection("Inventory").document(invenId).collection("Memo")
                        .document(documentId).update(map);
            }
        }, 2000);
    }

    //삭제
    public void deleteMemo(MemoAdapter adapter, int position) {
        this.adpater = adapter;
        DocumentSnapshot snapshot = adapter.getSnapshots().getSnapshot(position);
        snapshot.getReference().delete();
    }

    //저장 - editText에 있는 정보와 현재 시간 정보 저장
    public void saveMemo(Memo memo, FirebaseFirestore db) {
        setInvenId();
        Map<String, Object> map = new HashMap<>();
        map.put("title", memo.getTitle());
        map.put("contents", memo.getContents());

        // 현재시간을 msec 으로 구한다.
        long now = System.currentTimeMillis();
        // 현재시간을 date 변수에 저장한다.
        Date date = new Date(now);
        // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        // nowDate 변수에 값을 저장한다.
        String formatDate = sdfNow.format(date);

        map.put("date",formatDate);

        // Add a new document with a generated ID
        db.collection("Inventory").document(invenId).collection("Memo")
                .add(map)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });



    }
}
