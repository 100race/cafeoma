package com.vespa.baek.cafeoma.main.view.userpage;

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

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.vespa.baek.cafeoma.login.LoginActivity;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.main.data.UserModel;
import com.vespa.baek.cafeoma.main.view.MainActivity;

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
    private final static String defaultImage = "";
    private boolean confirmDel;
    private boolean confirmLogout;

    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(2, 4,
            60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    //[View]
    private Button btn_secession;
    private Button btn_delCollection;
    private Button btn_shareCode;
    private Button btn_logout;
    private ImageButton btn_back;
    private TextView tv_userEmail;
    private AlertDialog alert;
    private ProgressDialog progressDialog;

    //[Auth]
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userUid;
    private String userEmail;
    private String invenId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        btn_shareCode = findViewById(R.id.btn_shareCode);
        btn_delCollection = findViewById(R.id.btn_delCollection);
        btn_secession = findViewById(R.id.btn_secession);
        btn_back = findViewById(R.id.btn_back);
        tv_userEmail = findViewById(R.id.tv_userEmail);
        btn_logout = findViewById(R.id.btn_logout);

        btn_secession.setOnClickListener(v->onClick(v));
        btn_delCollection.setOnClickListener(v->onClick(v));
        btn_shareCode.setOnClickListener(v->onClick(v));
        btn_back.setOnClickListener(v->onClick(v));
        btn_logout.setOnClickListener(v->onClick(v));

        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(firebaseAuth -> mAuth = FirebaseAuth.getInstance());
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
                initProgDialog();
                new UserModel().checkInventory(db, userUid);
                progressDialog.show();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        if (UserModel.hasInventory == false) { // 저장소 없으면
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "공유할 저장소가 없습니다.", Toast.LENGTH_SHORT).show();
                        }else { // 저장소 있으면
                            progressDialog.dismiss();
                            Intent intent = new Intent(getApplicationContext(), ShareInvenIdActivity.class);
                            intent.putExtra("invenId",UserModel.invenId);
                            startActivity(intent);
                        }
                    }
                }, 2000);


                break;
            case R.id.btn_delCollection :
                initProgDialog();
                new UserModel().checkInventory(db, userUid);
                progressDialog.show();

                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        if (UserModel.hasInventory == false) { // 저장소 없으면
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "삭제할 저장소가 없습니다.", Toast.LENGTH_SHORT).show();
                        }else { // 저장소 있으면
                            progressDialog.dismiss(); // dismiss 했다가는 다시 안보이길래
                            delInvenDialog();
                        }
                    }
                }, 2000);

                break;
            case R.id.btn_secession ://회원탈퇴 -> 메인으로 이동
                initProgDialog();
                new UserModel().checkInventory(db, userUid);
                progressDialog.show();
                progressDialog.dismiss();

                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        delUserDialog();

                    }
                }, 2000);



                break;
            case R.id.btn_logout:
                logOut();
                break;
            case R.id.btn_back:
                finish();
                break;
        }
    }

    // [로그아웃]
    public void logOut() {
        AlertDialog.Builder alt_builder = new AlertDialog.Builder(this);
        alt_builder.setTitle("로그아웃 확인");
        alt_builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                //dismiss 되면서 로그아웃
                if(confirmLogout) {
                    FirebaseAuth.getInstance().signOut();
                    if (LoginManager.getInstance() != null) {
                        LoginManager.getInstance().logOut();
                    }
                    FirebaseUser currentUser = mAuth.getCurrentUser(); //로그아웃이 제대로 됐으면.
                    Log.d("LOGOUT", "로그아웃성공");
                    if (currentUser == null) {
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }
            }
        });

        alt_builder.setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("계속",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { //삭제
                                confirmLogout = true;
                                alert.dismiss();
                                Toast.makeText(UserPageActivity.this, "정상적으로 로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        })
                .setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { //취소
                                confirmLogout = false;
                                alert.cancel();
                                progressDialog.dismiss();
                            }
                        });
        confirmLogout = false;
        alert = alt_builder.create();
        alert.show();

    }

    //[로딩 다이얼로그 초기화]
    private void initProgDialog() {
        progressDialog = new ProgressDialog(UserPageActivity.this);
        progressDialog.setMessage("저장소 확인중...");
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);
    }

    //[탈퇴확인 다이얼로그]
    private void delUserDialog() { //진짜로 탈퇴할건지 확인

        AlertDialog.Builder alt_builder = new AlertDialog.Builder(this);
        alt_builder.setTitle("회원 탈퇴 확인");
        alt_builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                //dismiss 되면서 부를 progressDialog. 삭제구현하는부분
                    if(confirmDel) {
                        progressDialog = new ProgressDialog(UserPageActivity.this);
                        progressDialog.setMessage("저장소 삭제중... 어플을 종료하지 마세요");
                        progressDialog.setCancelable(false);
                        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);

                        progressDialog.show();
                        deleteAll(true);
                    }
            }
        });

        alt_builder.setMessage("회원 정보, 재고와 관련한 모든 데이터가 사라집니다. 그래도 탈퇴하시겠습니까?")
                .setPositiveButton("계속",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { //삭제
                                confirmDel = true;
                                alert.dismiss();

                            }
                        })
                .setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { //취소
                                confirmDel = false;
                                alert.cancel();
                                progressDialog.dismiss();
                            }
                        });
        confirmDel = false;
        alert = alt_builder.create();
        alert.show();
    }



    //[삭제확인 다이얼로그]
    private void delInvenDialog() { //진짜로 삭제할건지 확인

        AlertDialog.Builder alt_builder = new AlertDialog.Builder(this);
        alt_builder.setTitle("저장소 삭제 확인");
        alt_builder.setOnDismissListener(new DialogInterface.OnDismissListener() {


            @Override
            public void onDismiss(DialogInterface dialog) {
                //dismiss 되면서 부를 progressDialog
                if(confirmDel) {
                    progressDialog = new ProgressDialog(UserPageActivity.this);
                    progressDialog.setMessage("저장소 삭제중... 어플을 종료하지 마세요");
                    progressDialog.setCancelable(false);
                    progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);

                    progressDialog.show();
                    deleteAll(false);
                    //dismiss 위치를 deleteAll 로 옮김
                }
            }
        });

        alt_builder.setMessage("재고 저장소를 삭제하면 쇼핑몰, 메모, 재고 저장소와 관련한 모든 데이터가 사라집니다. 그래도 삭제하시겠습니까?")
                .setPositiveButton("계속",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { //삭제
                                confirmDel = true; // dismiss와 cancel 이후에 안전장치 하나 더
                                alert.dismiss();
                            }
                        })
                .setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { //취소
                                confirmDel = false;
                                alert.cancel();
                                progressDialog.dismiss();
                            }
                        });

        confirmDel = false;
        alert = alt_builder.create();
        alert.show();
    }

    private void deleteAll(boolean deleteUser) {
        //삭제순서는 storage - 컬렉션 - 문서 - 사용자정보삭제
        db.collection("User").document(userUid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.get("inventoryid") != null) {// 연결된 db있으면
                                Log.d(TAG, "연결 ivtid 삭제시작 :" + document.getData());

                                //사이사이 delay
                                invenId = document.get("inventoryid").toString();
                                // 1.Storage삭제
                                deleteStorage(invenId);
                                Log.d(TAG, "인벤토리 정보 : 스토리지삭제");
                                // 2.Collection삭제 ( InventoryItem, Memo, ShopUrl)
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        deleteCollection("Inventory/" + invenId + "/InventoryItem"); // 컬렉션삭제
                                        deleteCollection("Inventory/" + invenId + "/Memo");
                                        deleteCollection("Inventory/" + invenId + "/ShopURl");
                                        Log.d(TAG, "인벤토리 정보 : 컬렉션삭제");
                                    }
                                }, 2000);
                                // 3.Document 삭제
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        deleteDocument(invenId);
                                        Log.d(TAG, "인벤토리 정보 : 문서삭제");
                                    }
                                }, 2000);


                                if (deleteUser) { // 탈퇴면 유저까지 삭제

                                    handler.postDelayed(new Runnable() {
                                        public void run() {
                                            deleteUser();
                                        }
                                    }, 2000);

                                    handler.postDelayed(new Runnable() {
                                        public void run() {
                                            delUser();
                                            progressDialog.dismiss();

                                        }
                                    }, 2000);
                                } else { // 탈퇴아니면 유저 삭제 x
                                    handler.postDelayed(new Runnable() {
                                        public void run() {
                                            deleteInvenId(); //Invenid 삭제
                                            progressDialog.dismiss();
                                            Toast.makeText(UserPageActivity.this,"저장소가 삭제되었습니다.",Toast.LENGTH_SHORT).show();
                                        }
                                    }, 2000);

                                }

                            } else { // 탈퇴인데 연결된 db없으면
                                Log.d(TAG, "연결 ivtid 없음");

                                deleteUser();

                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        delUser();
                                        progressDialog.dismiss();
                                        Toast.makeText(UserPageActivity.this,"저장소가 삭제되었습니다.",Toast.LENGTH_SHORT).show();
                                    }
                                }, 2000);



                            }
                        } else {
                            Log.d(TAG, "사용자 문서 가져오기 실패", task.getException());
                        }
                    }
                });


    }

    //[storage삭제]
    private void deleteStorage(String invenId) {
        db.collection("Inventory").document(invenId).collection("InventoryItem")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.get("image"));
                                String image = document.get("image").toString();
                                if (image != null && image != defaultImage){//image가 default이미지가 아닐 때 삭제. default이미지면 삭제 x
                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                    storage.getReferenceFromUrl(image).delete();
                                }

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }


    //[컬렉션삭제]
    private void deleteCollection(final String path) {
        //InventoryItem컬렉션(및 하위문서) 삭제
        deleteCollection(db.collection(path), 50, EXECUTOR);

    }

    //[문서삭제]
    private void deleteDocument(String invenId) {
        db.collection("Inventory").document(invenId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    //[invenId삭제]
    private void deleteInvenId() {
        Map<String,Object> updates = new HashMap<>();
        updates.put("inventoryid", FieldValue.delete());
        db.collection("User").document(userUid)
                .update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "사용자 정보 : 인벤토리아이디 삭제");
            }
        });
    }

    //[유저문서 삭제]
    private void deleteUser() {
        db.collection("User").document(userUid)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "사용자 정보 : 유저문서 삭제");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    //[사용자 삭제]
    private void delUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUser.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {  // 이부분 실행이안됐음 로그인한지 오래된 계정이라 test때 오류 조심
                        if (task.isSuccessful()) {
                            Log.d(TAG, "사용자 정보 : 사용자 탈퇴");
                            Toast.makeText(getApplication(),"사용자 탈퇴되었습니다",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
//                           mAuth.signOut();
//                            if (LoginManager.getInstance() != null) {
//                                LoginManager.getInstance().logOut();
//                            }
//                            FirebaseUser currentUser = mAuth.getCurrentUser(); //로그아웃이 제대로 됐으면.
//                            if (currentUser == null) {
//                                Toast.makeText(getApplicationContext(), "탈퇴 완료되었습니다.", Toast.LENGTH_SHORT).show();
//                                Log.d(TAG, "사용자 정보 : 탈퇴 후 로그아웃성공");
//                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                startActivity(intent);
//                            }else{
//                                Toast.makeText(getApplicationContext(), "탈퇴 실패.", Toast.LENGTH_SHORT).show();
//                                Log.d(TAG, "사용자 정보 : 탈퇴 후 로그아웃 실패");
//                            }

                        }
                    }
                })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Log.d(TAG, " 사용자 정보 : 사용자 탈퇴 실패");
                Toast.makeText(UserPageActivity.this,"사용자 탈퇴 실패했습니다",Toast.LENGTH_LONG).show();
            }
        });
    }


    // [컬렉션 삭제]
    /**
     * Delete all documents in a collection. Uses an Executor to perform work on a background
     * thread. This does *not* automatically discover and delete subcollections.
     */
    private Task<Void> deleteCollection(final CollectionReference collection,
                                        final int batchSize,
                                        Executor executor) {

        //InventoryItem컬렉션(및 하위문서) 삭제

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


}
