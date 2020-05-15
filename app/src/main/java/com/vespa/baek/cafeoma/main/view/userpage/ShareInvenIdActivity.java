package com.vespa.baek.cafeoma.main.view.userpage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.main.data.UserModel;

public class ShareInvenIdActivity extends AppCompatActivity {

    //[View]
    private ImageButton btn_back;
    private TextView tv_invenId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_inven_id);

        btn_back = findViewById(R.id.btn_back);
        tv_invenId = findViewById(R.id.tv_invenId);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        tv_invenId.setText(intent.getExtras().getString("invenId"));

    }
}
