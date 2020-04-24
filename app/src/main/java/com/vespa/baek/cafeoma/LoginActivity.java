package com.vespa.baek.cafeoma;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.vespa.baek.cafeoma.main.view.MainActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LoginActivity extends AppCompatActivity {

    private CallbackManager mCallbackManager;                                                        //글로벌로 선언 해줘야됨
    private static final String TAG = "Login";                                                      //로그인과 관련된 로그 태그
    private static final int RC_LOGIN = 100;                                                      //ResultCode_LOGIN - google관련코드였음 아니어도 쓸수있을듯
    LoginButton facebook_login;                                                                     //페이스북 로그인버튼
    SignInButton google_login;
    Button email_login;
    EditText emailbox;
    ActionCodeSettings mActionCodeSettings;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        AppEventsLogger.activateApp(this);

        // Google login 버튼추가 -구글은 따로 onClick등록
        google_login = (SignInButton) findViewById(R.id.google_login_button);

        // Email login 버튼추가
        email_login = (Button)findViewById(R.id.email_login_button);
        email_login.setOnClickListener((view)->onClick(view));
        emailbox = (EditText)findViewById(R.id.emailbox);
        emailbox.setOnClickListener(view->onClick(view));

        // [START Facebook login initialize]
        // Facebook login 버튼추가
        facebook_login = (LoginButton) findViewById(R.id.facebook_login_button);
        facebook_login.setReadPermissions("email");
        mCallbackManager = CallbackManager.Factory.create();

        // facebook 로그인매니저에 Callback 등록

        facebook_login.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");

            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);

            }
        });
        // [END Facebook login initialize]


        // [[START 구글로그인 코드 -onCreate 안에 넣어줌]]

        // [START Configure 구글 로그인]
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))                                          //구글링으로 추가해준라인
                .requestEmail()
                .build();

        // [END configure 구글 로그인]

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        google_login.setOnClickListener((view)->onClick(view));

        //[START initialize firebase Auth]
        mAuth = FirebaseAuth.getInstance();
        //[END initialize firebase Auth]


        //[[END 구글로그인 코드]]


    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.google_login_button:
                Log.d(TAG,"구글 로그인시도");
                signIn();
                break;
            case R.id.email_login_button:
                emailLogin();
                break;

        }

    }
    //구글
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_LOGIN);                                           //ResultCode
    }


    //이메일
    public void emailLogin() {
        String email = emailbox.getText().toString();

        if(emailbox.getVisibility() == View.GONE) {
            emailbox.setVisibility(View.VISIBLE);
        }else if(emailbox.getVisibility() == View.VISIBLE && isValidEmail(email)) {
            //로그인 과정
            Log.d(TAG,"이메일 로그인시도");
            buildActionCodeSettings();
            sendSignInLink(email,mActionCodeSettings);

        }else{
            //로그인 실패 출력
            Toast.makeText(this,"이메일이 유효하지 않습니다.",Toast.LENGTH_SHORT).show();
        }
        }

    public void sendSignInLink(String email, ActionCodeSettings actionCodeSettings) {
        // [START auth_send_sign_in_link]
        FirebaseAuth auth = FirebaseAuth.getInstance();
        //이메일을 프레퍼런스에 저장해줌
        SharedPreferences pref = getSharedPreferences("email", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("user-email",email);
        editor.commit();

        auth.sendSignInLinkToEmail(email, actionCodeSettings)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "이메일 보냄");
                            Intent intent = new Intent(getApplicationContext(),NotifyEmailSendActivity.class);
                            startActivity(intent);
                        }
                    }
                });
        // [END auth_send_sign_in_link]
    }

    //이메일 - 동적링크 수신부분
    public void receptDeepLink(){
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }
                        //딥링크 성공시

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });
    }

    //이메일 유효성체크
    public static boolean isValidEmail(String email) {
        boolean err = false;
        String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        if(m.matches()) {
            err = true;
        }
        return err;
    }

    //구글 & 페북조금
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //facebook 액티비티 result를 facebook sdk로 전달해줌
        mCallbackManager.onActivityResult(requestCode, resultCode, data); //facebook로그인과 필요한부분

        //GoogleSignInApi.getSignInIntent result에 따른 설정
        if (requestCode == RC_LOGIN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account); //얘가문제
                Log.w(TAG, "구글 로그인 성공");

            } catch (ApiException e) {
                Log.w(TAG, "구글 로그인 실패", e);

            }
        }


    }

    @Override
    public void onStart() { //일단 시작이 LoginActivity일 때
        super.onStart();
        // 시작시 로그인 한 계정있나 확인 - 페북구글공통 확인 후 로그인 돼있으면 메인으로 넘겨줌
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }


    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "파이어베이스인증" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "파이어베이스 인증 성공");
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "파이어베이스 인증 실패", task.getException());
                        }
                    }
                });
    }

    // [START auth_with_facebook]
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "페이스북 파이어베이스 인증 성공");
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(intent);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "페이스북 파이어베이스 인증 실패", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
    // [END auth_with_facebook]

    //[START auth_with_emaillink]

    public void buildActionCodeSettings() {
        // [START auth_build_action_code_settings]
        mActionCodeSettings =
                ActionCodeSettings.newBuilder()
                        // URL you want to redirect back to. The domain (www.example.com) for this
                        // URL을 반드시 승인된 화이트리스트에 등록해줘야함. 이거랑 도메인 등록해줌 @@해결
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

    //[END auth_with_emaillink


}
