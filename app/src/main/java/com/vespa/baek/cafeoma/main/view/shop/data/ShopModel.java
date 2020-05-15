package com.vespa.baek.cafeoma.main.view.shop.data;

import android.os.Handler;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.vespa.baek.cafeoma.main.data.UserModel;
import com.vespa.baek.cafeoma.main.view.shop.ShopAdapter;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

public class ShopModel {
    private static final String TAG = "ShopModel";

    private ShopAdapter adpater;

    private static String invenId;

    //로그인 된 사용자에 있는 인벤토리 정보를 가져와서 연결해줌
    public void setInvenId(){
        this.invenId = UserModel.invenId;
    }

    //[ShopActivity]

    //[수정]
    public void updateShop(Shop shop, FirebaseFirestore db, String documentId){
        Map<String, Object> map = new HashMap<>();
        setInvenId();
        DocumentReference docref = db.collection("Inventory").document(invenId).collection("ShopUrl").document(documentId);

        map.put("shopName", shop.getShopName());
        map.put("shopUrl", shop.getShopUrl());

        // 딜레이를 줌
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                db.collection("Inventory").document(invenId).collection("ShopUrl")
                        .document(documentId).update(map);
            }
        }, 2000);
    }

    //[삭제] - storage 와 firestore 둘 다 삭제
    public void deleteShop(ShopAdapter adapter, int position) {
        this.adpater = adapter;
        DocumentSnapshot snapshot = adapter.getSnapshots().getSnapshot(position);
        snapshot.getReference().delete();
    }

    //[저장]
    public void saveShop(Shop shop, FirebaseFirestore db) {
        setInvenId();
        Map<String, Object> map = new HashMap<>();
        map.put("shopName", shop.getShopName());
        map.put("shopUrl", shop.getShopUrl());

        db.collection("Inventory").document(invenId).collection("ShopUrl")
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
