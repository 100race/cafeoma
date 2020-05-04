package com.vespa.baek.cafeoma.inventory.data;

import android.os.Handler;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.vespa.baek.cafeoma.inventory.adapter.InventoryAdapter;
import com.vespa.baek.cafeoma.main.data.UserModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

public class ItemModel {
    private static final String TAG = "ItemModel";
    private final static String defaultImage = "";

    private InventoryAdapter adpater;

    private static String invenId;

    //로그인 된 사용자에 있는 인벤토리 정보를 가져와서 연결해줌
    public void setInvenId(){
        this.invenId = UserModel.invenId;
    }

    //[InventoryActivity]

    //수정
    public void updateItem(Item item,FirebaseFirestore db, String documentId,boolean isChangedImg,boolean isDefaultImg){
        Map<String, Object> map = new HashMap<>();
        setInvenId();
        if((isChangedImg == false && isDefaultImg) || (isChangedImg && isDefaultImg ==false)){ // 기존 이미지 storage 삭제 -> 이미지를 1. 다른 사진이나 2. 디폴트이미지로 바꾸고 싶을때
            DocumentReference docref = db.collection("Inventory").document(invenId).collection("InventoryItem").document(documentId);
            docref.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                    Map<String, Object> m = document.getData();
                                    String image = String.valueOf(m.get("image"));
                                    if (image!=null && image!= defaultImage) { // 기존에 갖고있던 이미지가 default이미지 아닐때 삭제
                                        FirebaseStorage storage = FirebaseStorage.getInstance();
                                        storage.getReferenceFromUrl(image).delete();
                                        map.put("image", item.getImage());
                                    }
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });

        }//이미지 안바꿨으면 map에 image관련을 아예 map에 전달할 때 안넣고 update는 나머지만 하도록
        map.put("name", item.getName());
        map.put("remark", item.getRemark());
        map.put("quantity", item.getQuantity());
        map.put("shopUrl",item.getShopUrl());

        //핸들러를 써서 실행에 딜레이를 살짝 줌 -> 안쓰면
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                //임시경로
                Log.d(TAG,"설마이게먼저실행됨?");
                db.collection("Inventory").document(invenId).collection("InventoryItem")
                        .document(documentId).update(map);
            }
        }, 2000);
    }

    //삭제 - storage 와 firestore 둘 다 삭제
    public void deleteItem(InventoryAdapter adapter, int position) {
        this.adpater = adapter;
        DocumentSnapshot snapshot = adapter.getSnapshots().getSnapshot(position);
        String image = String.valueOf(snapshot.get("image"));
        //URL로 받은 이미지를 firestore에서 삭제 후 필드도 같이 삭제
        //image가 default이미지가 아닐 때 삭제. default이미지면 삭제 x
        if (image != null && image != defaultImage){
            FirebaseStorage storage = FirebaseStorage.getInstance();
            storage.getReferenceFromUrl(image).delete();
         }

        snapshot.getReference().delete();
        //adapter.getSnapshots().getSnapshot(position).getReference().delete();

    }




    //[ModifyInventoryActivity]

    //저장버튼 누르면 실행될것. editText에 있는 정보를 다 담아와 db에 push해준다
    public void saveItem(Item item, FirebaseFirestore db) {
        setInvenId();
        Map<String, Object> map = new HashMap<>();
        map.put("image", item.getImage());
        map.put("name", item.getName());
        map.put("remark", item.getRemark());
        map.put("quantity", item.getQuantity());
        map.put("shopUrl",item.getShopUrl());

        // Add a new document with a generated ID
        //임시 컬렉션주소 db.collection("Inventory").document("jG9OZBK4zUH7mgWAeh7q").collection("InventoryItem")
        db.collection("Inventory").document(invenId).collection("InventoryItem")
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
