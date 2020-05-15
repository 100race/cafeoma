package com.vespa.baek.cafeoma.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.vespa.baek.cafeoma.R;


public class NotifyEmailSendActivity extends AppCompatActivity {
    private static final String TAG = "Login";
    ActionCodeSettings mActionCodeSettings;
    private FirebaseAuth mAuth;
    private String email;
    private Button btn_resend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify_email_send);

        btn_resend = findViewById(R.id.resend_button);
        btn_resend.setOnClickListener(v -> onClick(v));

    }

    public void onClick(View v){
        //액션코드세팅빌드
        buildActionCodeSettings();

        //프리퍼런스에 저장했던 이메일 넣어주기
        SharedPreferences pref = getSharedPreferences("email", Activity.MODE_PRIVATE);
        email = pref.getString("user-email", "");

        //이메일 다시보내주기
        sendSignInLink(email,mActionCodeSettings);

    }

    public void sendSignInLink(String email, ActionCodeSettings actionCodeSettings) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendSignInLinkToEmail(email, actionCodeSettings)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "이메일 재전송");
                            Toast.makeText(NotifyEmailSendActivity.this,"이메일 링크를 재전송 했습니다. 이메일을 확인해주세요",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void buildActionCodeSettings() {
        mActionCodeSettings =
                ActionCodeSettings.newBuilder()
                        // 리다이렉트 되길 원하는 도메인을 Url에 등록
                        // URL을 반드시 승인된 화이트리스트에 등록해줘야함. 이거랑 도메인 등록해줌 @@해결
                        .setUrl("https://www.example.com") // 컴퓨터에서 열릴 코드인듯 설정안했더니 오류나서 다시써봄
                        // This must be true
                        .setHandleCodeInApp(true)
                        .setAndroidPackageName(
                                "com.vespa.baek.cafeoma",
                                true, // 설치안되어있으면 설치페이지로 이동시켜주는곳
                                "22"  ) //minVersion
                        .build();
    }

}
