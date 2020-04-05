package com.vespa.baek.cafeoma.inventory.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.os.Bundle;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.inventory.data.Item;

import java.util.ArrayList;

public class InventoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Item> arrayList;
    //여기가 firestore은 달라져야되는부분인가? 그리고 어레이리스트도 - > 달라짐. firebaseDatabase 아니고 firebaseFirestore로함
    //private FirebaseFirestore db;
    private DocumentReference docRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        recyclerView = findViewById(R.id.inventoryView);
        recyclerView.setHasFixedSize(true); //리사이클러뷰 기존성능강화
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>(); // Item 객체를 담을 어레이 리스트(어댑터쪽으로)

        //db = FirebaseFirestore.getInstance(); // 파이어스토어 연동




    }
}
