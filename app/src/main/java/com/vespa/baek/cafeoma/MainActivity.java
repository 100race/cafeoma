package com.vespa.baek.cafeoma;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import android.content.pm.Signature;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    private FirebaseAuth mAuth;
    private Button logOut_btn;
    //뒤로가기버튼관련변수
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

        logOut_btn = (Button)findViewById(R.id.logout);
        logOut_btn.setOnClickListener(view->onClick(view));
    }

    //시작시 구글 로그인 확인하는 메서드
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
        }
        //여기서 currenUser이 null로오면 로그인액티비티로 넘어가게?
        //기존 로그인되어있던 사용자로 초기화하는 코드 필요


    }
    //로그인 된 상태에서 뒤로가기 막기
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
            finish();
            toast.cancel();
        }
    }

    // [로그아웃]
    public void logOut(){
        FirebaseAuth.getInstance().signOut();
        FirebaseUser currentUser = mAuth.getCurrentUser(); //로그아웃이 제대로 됐으면.
        if (currentUser == null) {
            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
        }
    }

    public void onClick(View v){
        switch(v.getId()) {
            case R.id.logout:
                Log.d("LOGOUT", "구글 로그인시도");
                logOut();
                break;
        }
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



