package com.vespa.baek.cafeoma.inventory.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.inventory.data.Item;
import com.vespa.baek.cafeoma.inventory.data.ItemModel;

import java.io.File;
import java.util.Objects;

/*
   04.07 시작
   재고관리에 추가/수정하는 액티비티
   우선 데이터는 나중에 고칠거고 추가 수정만 가능하게 먼저 할거임
 */
public class ModifyInventoryActivity extends AppCompatActivity {

    private EditText et_name;
    private EditText et_quantity;
    private EditText et_remark;
    private EditText et_shopUrl;
    private ImageView iv_selectImage;
    private Button btn_save;
    private Button btn_cancel;
    private Item item;
    private ItemModel itemModel;
    private FirebaseFirestore db;
    private Intent intent;
    private final int GET_GALLERY_IMAGE = 200;
    private Uri selectedImageUri; // 갤러리에서 받아온 이미지를 저장 버튼 누를때까지 저장할 로컬장소
    private FirebaseStorage firebaseStorage;
    private StorageReference storageRef;
    private UploadTask uploadTask;
    private String imageUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_inventory);
        //시작시 받아온 데이터 있으면 뿌려줌
        //추가 버튼으로 눌려왔으면 빈액티비티로 시작함

        db = FirebaseFirestore.getInstance();
        //Item 초기화 안함* ItemModel도 - 초기화안하고 써서 오류난듯
        item = new Item();
        itemModel = new ItemModel();

        imageUrl = "디폴트값으로초기화";

        et_name = findViewById(R.id.et_name);
        et_quantity = findViewById(R.id.et_quantity);
        et_remark= findViewById(R.id.et_remark);
        et_shopUrl = findViewById(R.id.et_shopUrl);
        iv_selectImage = findViewById(R.id.iv_selectImage);
        btn_save = findViewById(R.id.btn_save);
        btn_cancel = findViewById(R.id.btn_cancel);


        //Task<Uri> downloadUri = taskSnapshot.getStorage().getDownloadUrl();

        btn_save.setOnClickListener(view -> onClick(view));
        btn_cancel.setOnClickListener(view -> onClick(view));
        iv_selectImage.setOnClickListener(view -> onClick(view));

    }

    public void onClick(View v) { //여기서 이미지 저장되는 무언가에 문제가있는것같음
        switch (v.getId()) {
            case R.id.btn_save:
                //저장을 누른 순간 이미지를 storage에 저장할것임
                //[storage에 이미지 저장]
                firebaseStorage = FirebaseStorage.getInstance();
                storageRef = firebaseStorage.getReference().child(selectedImageUri.getLastPathSegment());
                uploadTask = storageRef.putFile(selectedImageUri);
                //[구글링으로찾은방법 얘도 돌아가긴함]
//                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                            @Override
//                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                //pd.dismiss();
//                                Toast.makeText(ModifyInventoryActivity.this, "성공적으로 업데이트 되었습니다.", Toast.LENGTH_SHORT).show();
//                                //Task<Uri> downloadUri = taskSnapshot.getStorage().getDownloadUrl();
//                                Task<Uri> downloadUri = storageRef.getDownloadUrl();
//                                downloadUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                    @Override
//                                    public void onSuccess(Uri uri) {
//                                        imageUrl = uri.toString();
//                                        item.setImage(imageUrl);
//                                        Log.d("이미지",imageUrl);
//                                    }
//                                });
//
//
//                                }
//
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Toast.makeText(ModifyInventoryActivity.this,"이미지를 저장하지 못했습니다.",Toast.LENGTH_SHORT).show();
//                                Log.d("이미지","실패");
//                            }
//                        });
                //[문서에나온방법 - 얘도 올려는 짐]
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return storageRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            imageUrl = downloadUri.toString();
                            Log.d("이미지",imageUrl); //이게 더 나중에 실행되는거같으니까 실행을 여기로 옮겨보자
        //여기로이동                    // 성공했을 경우에 업로드한 것의 다운로드 url을 가져온다
                            item.setImage(imageUrl);
                            item.setName(String.valueOf(et_name.getText()));
                            item.setRemark(String.valueOf(et_remark.getText()));
                            item.setQuantity(Long.parseLong(String.valueOf(et_quantity.getText())));
                            item.setShopUrl(String.valueOf(et_shopUrl.getText()));

                            //오류발생
                            //[firestore에 데이터 저장]
                            itemModel.saveItem(item,db);

                        }
                    }
                });






        //String imageUrl = storageRef.child(selectedImageUri.getLastPathSegment()).getDownloadUrl().toString();
                Log.d("이미지2",imageUrl); //여기는 디폴트값으로초기화라고뜸


//      위치이동          // 성공했을 경우에 업로드한 것의 다운로드 url을 가져온다
//                item.setImage(imageUrl);
//                item.setName(String.valueOf(et_name.getText()));
//                item.setRemark(String.valueOf(et_remark.getText()));
//                item.setQuantity(Long.parseLong(String.valueOf(et_quantity.getText())));
//                item.setShopUrl(String.valueOf(et_shopUrl.getText()));
//
//                //오류발생
//                //[firestore에 데이터 저장]
//                itemModel.saveItem(item,db);

                intent = new Intent(ModifyInventoryActivity.this, InventoryActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_cancel:
                break;
            case R.id.iv_selectImage:
                //사진선택해오는부분
                intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);
                break;
        }

    }
    //사진선택해오는걸 받는부분
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            selectedImageUri = data.getData();
            iv_selectImage.setImageURI(selectedImageUri);
        }
    }
}
