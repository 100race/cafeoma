package com.vespa.baek.cafeoma.main.data;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.UploadTask;


import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

import static com.facebook.FacebookSdk.getApplicationContext;

public class UserModel {
    private static final String TAG = "UserModel";
    //boolean hasInventory;
    private String invenId;

    //[사용자 정보 확인]
    public void checkUser(FirebaseFirestore db, String uId, String email) {

        db.collection("User").document(uId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {// 기존 사용자라면
                                Log.d(TAG, "기존 사용자 로그인 :" + document.getData());
                                Toast.makeText(getApplicationContext(), "반갑습니다!", Toast.LENGTH_SHORT).show();
                            } else { // 처음 로그인이라면
                                Log.d(TAG, "새로 사용자 추가");
                                saveUser(db, uId, email);
                                Toast.makeText(getApplicationContext(), "환영합니다!", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Log.d(TAG, "사용자 문서 가져오기 실패 ", task.getException());
                        }
                    }
                });

    }

    //[사용자 정보 저장]
    public void saveUser(FirebaseFirestore db, String uId, String email){

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);

        db.collection("User").document(uId)
                .set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "성공적으로 사용자 추가");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "사용자 추가 실패", e);
            }
        });
    }

//    [사용자 DB 확인] 내부클래스에서 외부의 변수를 제어할수가없어서, 내부에 구현하기엔 너무 복잡해져서 그냥 직접사용
//    public boolean checkInventory(FirebaseFirestore db, String uId) {
//
//        db.collection("User").document(uId)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if (task.isSuccessful()) {
//                            DocumentSnapshot document = task.getResult();
//                            if (document.get("inventoryid") != null) {// 연결된 db있으면 -> 여긴문제없음 연결된거 있다고 뜸 제대로
//                                Log.d(TAG, "연결 ivtid :" + document.getData());
//                                hasInventory = true;
//                                //설마 이 true 데이터가 또여기서만 유지돼서, 여기서 다 끝내줘야되는건가?왠지그런거같음
//                            } else { // 연결된 db없으면
//                                Log.d(TAG, "연결 ivtid 없음");
//                                hasInventory = false;
//                            }
//                        } else {
//                            Log.d(TAG, "사용자 문서 가져오기 실패", task.getException());
//                        }
//                    }
//                });
//
//        return hasInventory;
//    }

        //[DB생성]
        public void createInventory(FirebaseFirestore db, String uId){

            Map<String, Object> userInven = new HashMap<>();

            db.collection("Inventory") //내용은 없는 InventoryDB생성
                    .add(userInven).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    if (task.isSuccessful()) {
                        DocumentReference document = task.getResult();
                        invenId = document.getId();
                        //db.collection("Inventory").document(invenId).collection("InventoryItem"); //하위컬렉션 만들어지나?
                        connectInventory(db,uId,invenId); // 인벤토리 연결
                        Log.d(TAG, "성공적으로 인벤토리 추가 invenId :" + invenId);
                    }}
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "인벤토리 추가 실패", e);
                }
            });
        }

    //[Inven 연결해주기] -> 따로 만든 이유는 기존 DBID를 입력했을때도 이걸로이용해야되기때문
    public void connectInventory(FirebaseFirestore db, String uId, String invenId) {

        db.collection("User").document(uId) //필드추가하려면 set 말고 update해야됨
                .update("inventoryid",invenId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "성공적으로 사용자 inven 연결");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "사용자 inven 연결 실패", e);
            }
        });
    }
}
