package com.vespa.baek.cafeoma.inventory.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import java.io.IOException;
import java.util.Objects;

/*
   04.07 시작
   재고관리에 추가/수정하는 액티비티
   우선 데이터는 나중에 고칠거고 추가 수정만 가능하게 먼저 할거임
 */
public class ModifyInventoryActivity extends AppCompatActivity {

    private final String TAG = "permission";
    private final int GET_GALLERY_IMAGE = 200;
    private final int CAMERA_IMAGE = 201;

    //[VIEW]
    private EditText et_name;
    private EditText et_quantity;
    private EditText et_remark;
    private EditText et_shopUrl;
    private ImageView iv_selectImage;
    private Button btn_save;
    private Button btn_cancel;
    private Item item;
    private ItemModel itemModel;
    private AlertDialog alert;


    private FirebaseFirestore db;
    private Intent intent;
    private Uri selectedImageUri; // 갤러리에서 받아온 이미지를 저장 버튼 누를때까지 저장할 로컬장소
    private Uri imageUri;
    private String currentPhotoPath;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageRef;
    private UploadTask uploadTask;
    private String imageUrl;
    boolean hasImage;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_inventory);
        //시작시 수정버튼으로 시작했으면 받아온 데이터 뿌려줌
        //추가 버튼으로 눌려왔으면 빈액티비티로 시작함

        db = FirebaseFirestore.getInstance();
        //item, itemModel 객체 초기화 안하고 써서 오류남;
        item = new Item();
        itemModel = new ItemModel();
        hasImage = false;

        imageUrl = "";

        et_name = findViewById(R.id.et_name);
        et_quantity = findViewById(R.id.et_quantity);
        et_remark= findViewById(R.id.et_remark);
        et_shopUrl = findViewById(R.id.et_shopUrl);
        iv_selectImage = findViewById(R.id.iv_selectImage);
        btn_save = findViewById(R.id.btn_save);
        btn_cancel = findViewById(R.id.btn_cancel);

        btn_save.setOnClickListener(view -> onClick(view));
        btn_cancel.setOnClickListener(view -> onClick(view));
        iv_selectImage.setOnClickListener(view -> onClick(view));

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                //저장을 누른 순간 이미지를 storage에 저장할것임 - > 이미지가 null일경우도 정의
                //[storage에 이미지 저장]
                if(hasImage==true) {
                    firebaseStorage = FirebaseStorage.getInstance();
                    storageRef = firebaseStorage.getReference().child(selectedImageUri.getLastPathSegment());
                    uploadTask = storageRef.putFile(selectedImageUri);

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
                                Log.d("이미지", imageUrl); //이게 더 나중에 실행되는거같으니까 실행을 여기로 옮겨보자
                                //여기로이동                    // 성공했을 경우에 업로드한 것의 다운로드 url을 가져온다
                                item.setImage(imageUrl); //여기도 유효성검사
                                if(String.valueOf(et_name.getText())=="") {
                                    Toast.makeText(ModifyInventoryActivity.this, "재고명은 필수입력 사항입니다.", Toast.LENGTH_SHORT).show();
                                }else {
                                    item.setName(String.valueOf(et_name.getText()));
                                    item.setRemark(String.valueOf((et_remark.getText() == null) ? "" : et_remark.getText()));
                                    item.setQuantity((et_quantity.getText() == null) ? 0 : Long.parseLong(String.valueOf(et_quantity.getText())));
                                    item.setShopUrl(String.valueOf((et_shopUrl.getText() == null) ? "" : et_shopUrl.getText()));

                                    //[firestore에 데이터 저장]
                                    itemModel.saveItem(item, db);
                                    hasImage = false;

                                    intent = new Intent(ModifyInventoryActivity.this, InventoryActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }
                    });
                }else{
                    //null데이터가 안들어가게 유효성검사
                    item.setImage("https://firebasestorage.googleapis.com/v0/b/cafeoma.appspot.com/o/default-image-icon-14.png?alt=media&token=c3b852d3-22f7-4e42-95b8-bc2f30f092e9");
                    if(String.valueOf(et_name.getText())=="") { //null이아니라 ""인가?
                        Toast.makeText(ModifyInventoryActivity.this, "재고명은 필수입력 사항입니다.", Toast.LENGTH_SHORT).show();
                        break; //break넣어봄
                    }else{
                    item.setName(String.valueOf(et_name.getText()));
                    item.setRemark((String.valueOf(et_remark.getText())=="") ? "" : String.valueOf(et_remark.getText()));
                    Log.d(TAG,String.valueOf(et_remark.getText()));
                    item.setQuantity((String.valueOf(et_quantity.getText())=="") ? 0 : Long.parseLong(String.valueOf(et_quantity.getText())));
                    item.setShopUrl(String.valueOf((et_shopUrl.getText()==null)? "" : et_shopUrl.getText()));

                    //[firestore에 데이터 저장]
                    itemModel.saveItem(item, db);

                    intent = new Intent(ModifyInventoryActivity.this, InventoryActivity.class);
                    startActivity(intent);
                    }
                }

                break;
            case R.id.btn_cancel:
                break;
            case R.id.iv_selectImage:
                //권한설정
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "권한 설정 완료");
                        photoDialogRadio();
                        break;
                    } else {
                        Log.d(TAG, "권한 설정 요청");
                        ActivityCompat.requestPermissions(ModifyInventoryActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                }



        }

    }

    //사진촬영/갤러리 선택해 온 결과 받는부분
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK){
            return;
        }
        switch (requestCode){
            case GET_GALLERY_IMAGE : {
                //앨범에서 가져오기
                if(data.getData()!=null){
                    try{
                        selectedImageUri = data.getData();
                        iv_selectImage.setImageURI(selectedImageUri);
                        hasImage = true;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;
            }
            case CAMERA_IMAGE : {
                //카메라 촬영
                try{
                    Log.v("알림", "FROM_CAMERA 처리");
                    galleryAddPic();
                    iv_selectImage.setImageURI(imageUri);
                    selectedImageUri = imageUri;
                    hasImage = true;
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            }
        }

    }




    //사진권한 받은 후 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //사진 촬영 vs 선택 메서드 실행
            photoDialogRadio();
        }
    }

//사진촬영할지 갤러리에서 가져올 지 선택
    private void photoDialogRadio() {
        final CharSequence[] PhotoModels = {"사진 촬영", "갤러리에서 가져오기"};
        AlertDialog.Builder alt_builder = new AlertDialog.Builder(this);
        alt_builder.setTitle("사진 가져오기");
        alt_builder.setSingleChoiceItems(PhotoModels, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) { //사진촬영
                    takePhoto();//사진촬영메서드
                    alert.dismiss(); // 끝나고 돌아오면 창꺼주기
                } else if (item == 1) { //갤러리에서 가져오기
                    intent = new Intent(Intent.ACTION_PICK);
                    intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent, GET_GALLERY_IMAGE);
                    alert.dismiss();
                }
            }
        });
        alert = alt_builder.create();
        alert.show();
    }

    //사진 찍기 클릭시 이벤트
    public void takePhoto(){

        // 촬영 후 이미지 가져옴
        String state = Environment.getExternalStorageState();

        if(Environment.MEDIA_MOUNTED.equals(state)){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(intent.resolveActivity(getPackageManager())!=null){

                File photoFile = null;
                try{
                    photoFile = createImageFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
                if(photoFile!=null){
                    Uri providerURI = FileProvider.getUriForFile(this,getPackageName()+ ".provider",photoFile); // + ".provider"추가해봄
                    imageUri = providerURI;
                    intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, providerURI);
                    startActivityForResult(intent, CAMERA_IMAGE);

                }

            }

        }else{

            Log.v("알림", "저장공간에 접근 불가능");

            return;

        }


    }
//찍은 사진을 이미지파일로 만들기
    public File createImageFile() throws IOException{
        String imgFileName = System.currentTimeMillis() + ".jpg";
        File imageFile= null;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "ireh");
        if(!storageDir.exists()){
            //없으면 만들기
            Log.v("알림","storageDir 존재 x " + storageDir.toString());
            storageDir.mkdirs();
        }
        Log.v("알림","storageDir 존재함 " + storageDir.toString());
        imageFile = new File(storageDir,imgFileName);
        currentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;

    }
//찍은 사진 갤러리에 저장
    public void galleryAddPic(){

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
        Toast.makeText(this,"사진이 저장되었습니다",Toast.LENGTH_SHORT).show();

    }


}
