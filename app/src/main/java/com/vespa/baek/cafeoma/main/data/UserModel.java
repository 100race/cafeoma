package com.vespa.baek.cafeoma.main.data;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

import static com.facebook.FacebookSdk.getApplicationContext;

public class UserModel {
    private static final String TAG = "UserModel";
    public static boolean hasInventory = false;
    public static String invenId;

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

//    [사용자 DB 확인]
    public boolean checkInventory(FirebaseFirestore db, String uId) {

        db.collection("User").document(uId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.get("inventoryid") != null) {// 연결된 db있으면
                                Log.d(TAG, "연결 ivtid :" + document.getData());
                                invenId = document.get("inventoryid").toString();
                                setInvenId(invenId);
                                hasInventory = true;
                            } else { // 연결된 db없으면
                                Log.d(TAG, "연결 ivtid 없음");
                                hasInventory = false;
                            }
                        } else {
                            Log.d(TAG, "사용자 문서 가져오기 실패", task.getException());
                        }
                    }
                });
        return hasInventory;

    }

        //[DB생성]
        public void createInventory(FirebaseFirestore db, String uId){

            Map<String, Object> userInven = new HashMap<>();

            db.collection("Inventory") //내용은 없는 InventoryDB생성
                    .add(userInven).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    if (task.isSuccessful()) {
                        DocumentReference document = task.getResult();
                        invenId = document.getId(); //여기서도 한번 static에 정보저장해줌(?) 이게 일단 차이점임
                        connectInventory(db,uId,invenId); // 인벤토리 연결
                        //setInvenId(invenId); //static에 정보저장
                        Log.d("확인", "성공적으로 인벤토리 추가 invenId :" + invenId);
                    }}
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "인벤토리 추가 실패", e);
                }
            });
        }



    //[DB 연결해주기] -> 따로 만든 이유는 기존 DBID를 입력했을때도 이걸로이용해야되기때문
    public void connectInventory(FirebaseFirestore db, String uId, String invenId) {

        db.collection("User").document(uId) //필드추가하려면 set 말고 update해야됨
                .update("inventoryid",invenId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        setInvenId(invenId); //static에 정보저장
                        Log.d(TAG, "성공적으로 사용자 inven 연결"
                        );
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "사용자 inven 연결 실패", e);
            }
        });
    }


    public void setInvenId(String invenId){ this.invenId = invenId; }

    public String getInvenId() {
        return invenId;
    }
}
