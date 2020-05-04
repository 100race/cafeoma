package com.vespa.baek.cafeoma.main.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vespa.baek.cafeoma.LoginActivity;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.inventory.view.InventoryActivity;
import com.vespa.baek.cafeoma.main.data.UserModel;

import static com.facebook.FacebookSdk.getApplicationContext;

public class InputInvenIdActivity extends AppCompatActivity {
    private static final String TAG = "InputInvenIdActivity";
    //[View]
    private EditText et_inventoryId;
    private Button btn_confirm;
    private ImageButton btn_back;
    private TextView tv_inventoryId;
    //[Auth]
    private FirebaseFirestore db;
    private String userUid;
    private String userEmail;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_inven_id);

        et_inventoryId = findViewById(R.id.et_inventoryId); //이거하나마난데
        tv_inventoryId = findViewById(R.id.tv_inventoryId);
        btn_confirm = findViewById(R.id.btn_confirm);
        btn_back = findViewById(R.id.btn_back);

        btn_confirm.setOnClickListener(v->onClick(v));
        btn_back.setOnClickListener(v->onClick(v));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userEmail = currentUser.getEmail();
        userUid = currentUser.getUid();


    }

    public void onClick(View v){
        switch (v.getId()){

            case R.id.btn_confirm:
                String invenId = et_inventoryId.getText().toString();
                if(invenId.length() > 0) {
                    db.collection("Inventory").document(invenId)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document != null && document.exists()) {
                                            //검색해봐서 있으면
                                            Log.d(TAG, "존재하는 재고 저장소:" + document.getId());
                                            new UserModel().connectInventory(db, userUid, invenId);
                                            Toast.makeText(getApplicationContext(), "재고 저장소에 연결했습니다.", Toast.LENGTH_SHORT).show();

                                            //인벤토리가 서버에 추가 된 후에 인텐트가 실행되도록 딜레이주기
                                            Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                public void run() {
                                                    Intent intent = new Intent(getApplicationContext(), InventoryActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }, 2000);
                                        } else {
                                            //없으면 저장소를 찾을 수 없습니다.
                                            Log.d(TAG, "존재하지 않는 재고 저장소");
                                            Toast.makeText(getApplicationContext(), "재고 저장소가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Log.d(TAG, "저장소 문서 가져오기 실패 ", task.getException());
                                    }
                                }
                            });
                }else{ //입력안했으면
                    Toast.makeText(getApplicationContext(), "저장소 코드를 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btn_back:
                finish();
                break;
        }

    }
}
