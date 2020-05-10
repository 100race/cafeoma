package com.vespa.baek.cafeoma.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.login.LoginActivity;
import com.vespa.baek.cafeoma.main.view.MainActivity;

public class VerifyEmailLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email_login);
        verifySignInLink();
    }

    //링크를 수신하고 이메일 링크 인증을 위한 링크인지 확인하고 로그인을 완료한다.
    public void verifySignInLink() {
        // [START auth_verify_sign_in_link]
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        String emailLink = intent.getData().toString();

        // Confirm the link is a sign-in with email link.
        if (auth.isSignInWithEmailLink(emailLink)) {
            // Retrieve this from wherever you stored it
            //저장된 프리퍼런스 이메일 데이터를 가져온다
            SharedPreferences pref = getSharedPreferences("email", Activity.MODE_PRIVATE);
            String email = pref.getString("user-email", "");


            // The client SDK will parse the code from the link for you.
            auth.signInWithEmailLink(email, emailLink)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d("LOGIN", "이메일 링크로 로그인 성공");
                                AuthResult result = task.getResult();
                                // 사용자 추가 됨 알아서
                                // You can check if the user is new or existing:
                                // result.getAdditionalUserInfo().isNewUser()
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                            } else {
                                Log.e("LOGIN", "이메일 링크로 로그인 실패", task.getException());
                                Toast.makeText(getApplicationContext(), "만료된 링크입니다", Toast.LENGTH_SHORT);
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            }
                        }
                    });
        }
        // [END auth_verify_sign_in_link]
    }
}
