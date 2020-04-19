package com.vespa.baek.cafeoma.inventory.data;

import android.util.Log;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vespa.baek.cafeoma.inventory.adapter.InventoryAdapter;
import com.vespa.baek.cafeoma.inventory.adapter.InventoryViewHolder;
import com.vespa.baek.cafeoma.inventory.view.InventoryActivity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public class ItemModel {


    public ItemModel() {
    }

    private static final String TAG = "ItemModel";
    //Map params = new Map<>; 정보를 담아놓을 그릇필요


    //로그인 된 사용자에 있는 데이터베이스 정보를 가져와서 adapter에 연결해주도록함
    public void getUserInventory(){

    }

    //[InventoryActivity]
    //검색 - 입력받은 재고명, 수량에 맞는 정보를 db에서 찾아 불러온다
    public void searchItem() {

    }

    //수정 - 롱클릭 한 뷰에서 position을 받아내고, 그 뷰의 text에서 getText등으로 이름?을 받던지 정보를 받아 DB에서 검색해서 그 정보를 퍼블릭 로컬정보에 저장.
    //한마디로 롱클릭한 뷰의 정보를 저장해놓는다

    //수정 - 저장한 정보를 수정 액티비티로 넘겨줌 또는 수정액티비티에서 이 정보에 접근하도록 설정. 수정액티비티에서 저장, 취소, 뒤로가기 누르면 로컬 정보 삭제하기

    //어댑터로 전달 된 아이템 리스트를 저장 ->  onCreate랑 아이템 변경될때마다 새로 받아오면됨( onChildChange?나 norifydatasetchanges오버라이드 해서 넣기)
    public ArrayList<Item> getItemList(FirebaseFirestore db,String InventoryID){ // 컬렉션아이디로 나중엔 받아오기 수정해야될부분 DocumentReference docRef로 받아올까 아니면 db로 받을까
        ArrayList<Item> items= new ArrayList<>();

        db.collection("Inventory").document(InventoryID).collection("InventoryItem")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Item item = document.toObject(Item.class);
                        items.add(item);
                        Log.d(TAG, document.getId() + " => " + document.get("name"));
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        return items;
    }

    //삭제 - 나중에
//    public void deleteItem(HashMap<Long,Boolean> selectedItems, InventoryAdapter adapter) { // 여길 수정해야할듯 position이아니라 selectedItems가 순서랑 stablekey를 갖도록. 그 아이디로 position을 갖고와서 getSnapshot
//            List<Long> itemKeys = new ArrayList<>(selectedItems.size());
//            for (int i = 0; i < selectedItems.size(); i++) {
//                itemKeys.add(selectedItems.keyAt(i));
//            }
//        for(long key : items){
//
//            adapter.getSnapshots().getSnapshot(position).getReference().delete(); //와 개기네 근데 이게맞나?
//        }
//
//    }



    //[ModifyInventoryActivity]

    //저장버튼 누르면 실행될것. editText에 있는 정보를 다 담아와 db에 push해준다
    public void saveItem(Item item, FirebaseFirestore db) {
        Map<String, Object> map = new HashMap<>();
        map.put("image", item.getImage());
        map.put("name", item.getName());
        map.put("remark", item.getRemark());
        map.put("quantity", item.getQuantity());
        map.put("shopUrl",item.getShopUrl());

        // Add a new document with a generated ID
        //임시 컬렉션주소 db.collection("Inventory").document("jG9OZBK4zUH7mgWAeh7q").collection("InventoryItem")
        db.collection("Inventory").document("jG9OZBK4zUH7mgWAeh7q").collection("InventoryItem")
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





    //    query에 넣어 (firestore?)adapter에 넣어준다. -> adapter은 실시간으로 변해주기때문에 걍 데이터만 바꿔도 될듯? 리스너가 알아서 존재하니까
//    public void pushItam() {
//
//    }

    //query에 넣어 원래 선택했던 재고정보를 찾아 거기에 정보를 update함.
    public void updateItem() {

    }


}
