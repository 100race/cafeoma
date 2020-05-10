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

    private static final String TAG = "Login";                                                      //로그인과 관련된 로그 태그
    ActionCodeSettings mActionCodeSettings;
    private FirebaseAuth mAuth;
    private String email;
    Button resendbtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify_email_send);

        resendbtn = (Button)findViewById(R.id.resend_button);
        resendbtn.setOnClickListener(view -> onClick(view));


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
        // [START auth_send_sign_in_link]
        FirebaseAuth auth = FirebaseAuth.getInstance();


        auth.sendSignInLinkToEmail(email, actionCodeSettings)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "이메일 다시 보냄");
                            Toast.makeText(NotifyEmailSendActivity.this,"이메일 링크를 재전송 했습니다. 이메일을 확인해주세요",Toast.LENGTH_LONG).show();
                        }
                    }
                });
        // [END auth_send_sign_in_link]
    }

    public void buildActionCodeSettings() {
        // [START auth_build_action_code_settings]
        mActionCodeSettings =
                ActionCodeSettings.newBuilder()
                        // URL you want to redirect back to. The domain (www.example.com) for this
                        // URL must be whitelisted in the Firebase Console. -했어
                        .setUrl("https://www.example.com") //이게맞는진몰겠음 - 내생각에는 컴퓨터에서 열릴 코드인듯 설정안했더니 오류나서 다시써봄
                        // This must be true
                        .setHandleCodeInApp(true)
                        .setAndroidPackageName(
                                "com.vespa.baek.cafeoma", //여기서 패키지네임 마지막에 android가 들어가야하는지 모르겠음 다른곳엔 ios써있길래 - 왠지 안들어갈거같아서 지움
                                true, /* installIfNotAvailable */
                                "22"    /* minimumVersion */)
                        .build();
        // [END auth_build_action_code_settings]
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}
