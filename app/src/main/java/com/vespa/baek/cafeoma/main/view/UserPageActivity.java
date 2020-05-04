package com.vespa.baek.cafeoma.main.view;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.inventory.view.InventoryActivity;
import com.vespa.baek.cafeoma.main.data.UserModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class  UserPageActivity extends AppCompatActivity {
    private static final String TAG = "UserPageActivity";

    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(2, 4,
            60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    //[View]
    private Button btn_secession;
    private Button btn_delCollection;
    private Button btn_shareCode;
    private ImageButton btn_back;
    private TextView tv_userEmail;
    private AlertDialog alert;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userUid;
    private String userEmail;
    private String inventoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        btn_shareCode = findViewById(R.id.btn_shareCode);
        btn_delCollection = findViewById(R.id.btn_delCollection);
        btn_secession = findViewById(R.id.btn_secession);
        btn_back = findViewById(R.id.btn_back);
        tv_userEmail = findViewById(R.id.tv_userEmail);

        btn_secession.setOnClickListener(v->onClick(v));
        btn_delCollection.setOnClickListener(v->onClick(v));
        btn_shareCode.setOnClickListener(v->onClick(v));
        btn_back.setOnClickListener(v->onClick(v));

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        userEmail = currentUser.getEmail();
        userUid = currentUser.getUid();

        tv_userEmail.setText(userEmail);

        initProgDialog();

    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.btn_shareCode :
                break;
            case R.id.btn_delCollection :
                new UserModel().checkInventory(db, userUid);
                progressDialog.show();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        if (UserModel.hasInventory == false) { // 저장소 없으면
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "삭제할 저장소가 없습니다.", Toast.LENGTH_SHORT).show();
                        }else { // 저장소 있으면
                            alertDelDialog();
                        }
                    }
                }, 2000);

                break;
            case R.id.btn_secession ://회원탈퇴

                break;
            case R.id.btn_back:
                finish();
                break;
        }
    }

    //[로딩 다이얼로그 초기화]
    private void initProgDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("저장소 확인중...");
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);
    }

    //[삭제확인 다이얼로그]
    private void alertDelDialog() { //진짜로 삭제할건지 확인

        AlertDialog.Builder alt_builder = new AlertDialog.Builder(this);
        alt_builder.setTitle("저장소 삭제 확인");
        alt_builder.setMessage("재고 저장소를 삭제하면 저장소와 관련한 모든 데이터가 사라집니다. 그래도 삭제하시겠습니까?")
                .setPositiveButton("계속",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { //삭제
                                deleteAll();
                                alert.dismiss();
                            }
                        })
                .setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { //취소
                                alert.dismiss();
                            }
                        });

        alert = alt_builder.create();
        alert.show();
    }

    void deleteAll() {
        //인벤토리 삭제하고 user에있는 db문서도 삭제하고 storage에있는것도 삭제해야함
        db.collection("User").document(userUid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.get("inventoryid") != null) {// 연결된 db있으면
                                Log.d(TAG, "연결 ivtid 삭제시도 :" + document.getData());
                                inventoryId = document.get("inventoryid").toString();
                                deleteCollection("Inventory/"+inventoryId+"/InventoryItem");
                            } else { // 연결된 db없으면
                                Log.d(TAG, "연결 ivtid 없음");
                            }
                        } else {
                            Log.d(TAG, "사용자 문서 가져오기 실패", task.getException());
                        }
                    }
                });
        //deleteCollection("Inventory/"+inventoryId+"/InventoryItem");
        //deleteCollection("User/"+userUid);
    }

    private void deleteCollection(final String path) {
        deleteCollection(db.collection(path), 50, EXECUTOR);
    }

    public void exampleData() {
        // [START example_data]
        CollectionReference cities = db.collection("cities");

        Map<String, Object> data1 = new HashMap<>();
        data1.put("name", "San Francisco");
        data1.put("state", "CA");
        data1.put("country", "USA");
        data1.put("capital", false);
        data1.put("population", 860000);
        data1.put("regions", Arrays.asList("west_coast", "norcal"));
        cities.document("SF").set(data1);

        Map<String, Object> data2 = new HashMap<>();
        data2.put("name", "Los Angeles");
        data2.put("state", "CA");
        data2.put("country", "USA");
        data2.put("capital", false);
        data2.put("population", 3900000);
        data2.put("regions", Arrays.asList("west_coast", "socal"));
        cities.document("LA").set(data2);

        Map<String, Object> data3 = new HashMap<>();
        data3.put("name", "Washington D.C.");
        data3.put("state", null);
        data3.put("country", "USA");
        data3.put("capital", true);
        data3.put("population", 680000);
        data3.put("regions", Arrays.asList("east_coast"));
        cities.document("DC").set(data3);

        Map<String, Object> data4 = new HashMap<>();
        data4.put("name", "Tokyo");
        data4.put("state", null);
        data4.put("country", "Japan");
        data4.put("capital", true);
        data4.put("population", 9000000);
        data4.put("regions", Arrays.asList("kanto", "honshu"));
        cities.document("TOK").set(data4);

        Map<String, Object> data5 = new HashMap<>();
        data5.put("name", "Beijing");
        data5.put("state", null);
        data5.put("country", "China");
        data5.put("capital", true);
        data5.put("population", 21500000);
        data5.put("regions", Arrays.asList("jingjinji", "hebei"));
        cities.document("BJ").set(data5);
        // [END example_data]
    }

    // [START delete_collection]
    /**
     * Delete all documents in a collection. Uses an Executor to perform work on a background
     * thread. This does *not* automatically discover and delete subcollections.
     */
    private Task<Void> deleteCollection(final CollectionReference collection,
                                        final int batchSize,
                                        Executor executor) {

        // Perform the delete operation on the provided Executor, which allows us to use
        // simpler synchronous logic without blocking the main thread.
        return Tasks.call(executor, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // Get the first batch of documents in the collection
                Query query = collection.orderBy(FieldPath.documentId()).limit(batchSize);

                // Get a list of deleted documents
                List<DocumentSnapshot> deleted = deleteQueryBatch(query);

                // While the deleted documents in the last batch indicate that there
                // may still be more documents in the collection, page down to the
                // next batch and delete again
                while (deleted.size() >= batchSize) {
                    // Move the query cursor to start after the last doc in the batch
                    DocumentSnapshot last = deleted.get(deleted.size() - 1);
                    query = collection.orderBy(FieldPath.documentId())
                            .startAfter(last.getId())
                            .limit(batchSize);

                    deleted = deleteQueryBatch(query);
                }

                return null;
            }
        });

    }

    /**
     * Delete all results from a query in a single WriteBatch. Must be run on a worker thread
     * to avoid blocking/crashing the main thread.
     */
    @WorkerThread
    private List<DocumentSnapshot> deleteQueryBatch(final Query query) throws Exception {
        QuerySnapshot querySnapshot = Tasks.await(query.get());

        WriteBatch batch = query.getFirestore().batch();
        for (QueryDocumentSnapshot snapshot : querySnapshot) {
            batch.delete(snapshot.getReference());
        }
        Tasks.await(batch.commit());

        return querySnapshot.getDocuments();
    }
    // [END delete_collection]


}
