package com.vespa.baek.cafeoma.main.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import android.content.pm.Signature;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.vespa.baek.cafeoma.login.LoginActivity;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.inventory.view.InventoryActivity;
import com.vespa.baek.cafeoma.main.data.UserModel;
import com.vespa.baek.cafeoma.main.view.memo.MemoActivity;
import com.vespa.baek.cafeoma.main.view.shop.ShopActivity;
import com.vespa.baek.cafeoma.main.view.userpage.UserPageActivity;

import java.security.MessageDigest;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    //[View]
    private Button btn_toShop;
    private Button btn_toMemo;
    private Button btn_toInventory;
    private Button btn_toUserPage;
    private AlertDialog alert;
    private ProgressDialog progressDialog;

    //[Auth]
    private Context mContext;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserModel um;
    private String userUid;
    private String userEmail;


    //뒤로가기변수
    // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로가기 버튼을 누를때 표시
    private Toast toast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();

        getHashKey(mContext);

        //initialize auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btn_toInventory = findViewById(R.id.btn_toInventory);
        btn_toUserPage = findViewById(R.id.btn_toUserPage);
        btn_toMemo = findViewById(R.id.btn_toMemo);
        btn_toShop = findViewById(R.id.btn_toShop);

        btn_toInventory.setOnClickListener(v -> onClick(v));
        btn_toUserPage.setOnClickListener(v -> onClick(v));
        btn_toMemo.setOnClickListener(v -> onClick(v));
        btn_toShop.setOnClickListener(v -> onClick(v));

        //[사용자 추가] 기존 사용자인지 확인 필요. 기존사용자면 추가 x)
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) { //로그인 상태 확인 후 로그인 안돼있으면 login으로 넘겨줌
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }else {

            userEmail = currentUser.getEmail();
            userUid = currentUser.getUid();
            Log.d("확인", userEmail);

            um = new UserModel();
            um.checkUser(db, userUid, userEmail);
        }

        initProgDialog();

    }


    //[뒤로가기 두번누르면 종료]
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지났으면 Toast Show
        // 2000 milliseconds = 2 seconds

        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
        // 현재 표시된 Toast 취소
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            ActivityCompat.finishAffinity(MainActivity.this);
            System.exit(0);
            toast.cancel();
        }
    }


    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_toInventory:
                progressDialog.show();
                db.collection("User").document(userUid)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.get("inventoryid") != null) {// 연결된 db있으면
                                    Log.d(TAG, "연결 ivtid :" + document.getData());
                                    um.checkInventory(db,userUid);
                                    //인벤토리가 서버에 추가 된 후에 인텐트가 실행되도록 딜레이주기
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        public void run() {
                                            Intent intent = new Intent(getApplicationContext(), InventoryActivity.class);
                                            startActivity(intent);
                                            progressDialog.dismiss();
                                        }
                                    }, 2000);

                                } else { // 연결된 db없으면
                                    Log.d(TAG, "연결 ivtid 없음");
                                    progressDialog.dismiss();
                                    inventoryDialog();
                                }
                            } else {
                                Log.d(TAG, "사용자 문서 가져오기 실패", task.getException());
                            }
                        }
                    });
                break;
            case R.id.btn_toUserPage:
                Intent intent = new Intent(getApplicationContext(), UserPageActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_toShop:
                progressDialog.show();
                um.checkInventory(db, userUid);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        Boolean hasInven = UserModel.hasInventory;
                        if (hasInven) {
                            progressDialog.dismiss();
                            Intent intent = new Intent(getApplicationContext(), ShopActivity.class);
                            startActivity(intent);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"재고 저장소를 먼저 생성해주세요.",Toast.LENGTH_SHORT).show();

                        }
                    }
                }, 2000);
                break;
            case R.id.btn_toMemo:
                progressDialog.show();
                um.checkInventory(db, userUid);

                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        Boolean hasInven = UserModel.hasInventory;
                        if (hasInven) {
                            progressDialog.dismiss();
                            Intent intent = new Intent(getApplicationContext(), MemoActivity.class);
                            startActivity(intent);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"재고 저장소를 먼저 생성해주세요.",Toast.LENGTH_SHORT).show();
                        }
                    }
                }, 2000);
                break;


        }
    }

    //[저장소확인 다이얼로그]
    private void initProgDialog() {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("저장소 확인중...");
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);
    }


    //[재고저장소 선택 다이얼로그]
    private void inventoryDialog() { //생성할지 기존DBID 입력할지 dialog 띄워줌

        AlertDialog.Builder alt_builder = new AlertDialog.Builder(this);
        alt_builder.setTitle("재고저장소 선택");
        alt_builder.setMessage("재고저장소를 새로 만드시겠습니까?")
                .setPositiveButton("새로 생성",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { //createDB 하고 InventoryActivity연결
                                alert.dismiss();
                                um.createInventory(db,userUid);
                                //인벤토리가 서버에 추가 된 후에 인텐트가 실행되도록 딜레이주기
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        Intent intent = new Intent(getApplicationContext(), InventoryActivity.class);
                                        startActivity(intent);
                                    }
                                }, 2000);



                            }
                        })
                .setNegativeButton("저장소 참여 코드 입력",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {  //inputDbidActivity로 인텐트 연결
                                Intent intent = new Intent(getApplicationContext(), InputInvenIdActivity.class);
                                startActivity(intent);
                                alert.dismiss();
                            }
                        });

        alert = alt_builder.create();
        alert.show();
    }


    // [[프로젝트의 해시키를 반환]]
    @Nullable

    public static String getHashKey(Context context) {
        final String TAG = "KeyHash";
        String keyHash = null;
        try {
            PackageInfo info =
                    context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                keyHash = new String(Base64.encode(md.digest(), 0));
                Log.d(TAG, keyHash);
            }

        } catch (Exception e) {

            Log.e("name not found", e.toString());

        }

        if (keyHash != null) {
            return keyHash;
        } else {
            return null;
        }

    }



}



