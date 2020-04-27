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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.inventory.data.Item;
import com.vespa.baek.cafeoma.inventory.data.ItemModel;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/*
   04.07 시작
   재고관리에 추가/수정하는 액티비티
   우선 데이터는 나중에 고칠거고 추가 수정만 가능하게 먼저 할거임
 */
public class ModifyInventoryActivity extends AppCompatActivity {

    private final String TAG = "permission";
    private final static String defaultImage = "";
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
    private Uri selectedImageUri; // 갤러리에서 받아온 이미지를 저장 버튼 누를때까지 저장할 로컬장소 -> onCreate때 초기화시킬것임
    private Uri imageUri;
    private String currentPhotoPath;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageRef;
    private UploadTask uploadTask;
    private String imageUrl;

    private boolean isModify;
    private boolean hasImage;
    private boolean isDefault;
    private String documentId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_inventory);

        //추가 버튼으로 눌려왔으면 빈액티비티로 시작함 - 이미지 없는상태
        isModify = false;
        hasImage = false;
        isDefault = false;

        db = FirebaseFirestore.getInstance();

        //item, itemModel 객체 초기화 안하고 써서 오류남;

        item = new Item();
        itemModel = new ItemModel();

        Intent intent = getIntent();

        imageUrl = "";
        et_name = findViewById(R.id.et_name);
        et_quantity = findViewById(R.id.et_quantity);
        et_remark= findViewById(R.id.et_remark);
        et_shopUrl = findViewById(R.id.et_shopUrl);
        iv_selectImage = findViewById(R.id.iv_selectImage);
        btn_save = findViewById(R.id.btn_save);
        btn_cancel = findViewById(R.id.btn_cancel);

        //시작시 수정버튼으로 시작했으면(intent로 getExtra했는데 null이아니라 받아온게있으먄, 왜냐면 intent는 둘다 전해줌) 받아온 데이터 뿌려주는 초기화
        if (intent.getExtras() != null || isModify == true) {
            //수정버튼으로 받아온 어댑터에존재하는 문서의 아이디
            isModify = true;
            documentId = intent.getExtras().getString("ID");
            //임시경로임
            //어댑터의 문서의 내용을 item에 저장한후
            DocumentReference docref = db.collection("Inventory").document("jG9OZBK4zUH7mgWAeh7q").collection("InventoryItem").document(documentId);
            docref.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData()); //제대로 원래이미지 뜸
                                    Map<String, Object> map = document.getData();

                                    if ((map.get("image") != null) && (map.get("image") != defaultImage)) { //원래 넣었던 이미지가 있었다면
                                        Glide.with(getApplicationContext())
                                                .load(String.valueOf(map.get("image")))
                                                .into(iv_selectImage);
                                    } else { //원래 넣었던 이미지가 없었다면 갤러리이미지 출력
                                        Glide.with(getApplicationContext())
                                                .load(android.R.drawable.ic_menu_gallery)
                                                .into(iv_selectImage);
                                    }
                                    et_name.setText(String.valueOf(map.get("name")));
                                    et_quantity.setText(String.valueOf(map.get("quantity")));
                                    et_remark.setText(String.valueOf(map.get("remark")));
                                    et_shopUrl.setText(String.valueOf(map.get("shopUrl")));


                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
        }

        btn_save.setOnClickListener(view -> onClick(view));
        btn_cancel.setOnClickListener(view -> onClick(view));
        iv_selectImage.setOnClickListener(view -> onClick(view));

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                if(TextUtils.isEmpty(et_name.getText().toString())){
                    Toast.makeText(this,"재고 이름은 필수입력사항입니다.",Toast.LENGTH_SHORT).show();
                    break;
                }
                onAddModify();
                isModify = false;
                finish();
                break;

            case R.id.btn_cancel:
                isModify = false;
                finish();
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

    //[데이터 유효성 검사 후 item에 데이터넣어주기]
    //name은 필수입력사항이라 넘어가면안되고 나머지는 0이나 ""로 데이터 넣어주기 (null로는 전달않기)
    public void onSetItem() {
        item.setName(String.valueOf(et_name.getText()));
        item.setRemark(String.valueOf(et_remark.getText()));
        if(et_quantity.getText().length() <= 0) {
            item.setQuantity(0);
        }else {
            item.setQuantity(Long.parseLong(String.valueOf(et_quantity.getText())));
        }
        item.setShopUrl(String.valueOf(et_shopUrl.getText()));

    }


    //[storage에 이미지 저장]
    //selectedImageUri가 null이아니고(이미지를 바꿨고) 수정으로 실행된 것일 때 -> 사진 올리고 기존 데이터에 수정
    //selectedImageUri가 null이고(이미지를 안바꿨고) 수정으로 실행된 것일 때 -> 기존 데이터->ID로 접근하면될듯? 에 수정
    //selectedImageUri가 null이아니고(이미지 추가했고) 추가로 실행된 것일 때 ->이대로
    //selectedImageUri가 null이고 (이미지를 추가안했고) 추가로 실행된 것일 때 -> null데이터 들어갈 시 기본이미지로 설정(깃허브)

    //이미지추가 - 수정 / 추가
    //이미지 추가 x - 수정 / 추가

    public void onAddModify() {  // 기본 데이터 null확인도 해야됨
        if (hasImage == true) { // 이미지를 추가했을 때 - 1이랑 4 합칠거임
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
                        // 성공했을 경우에 업로드한 것의 다운로드 url을 가져온다
                        Uri downloadUri = task.getResult();
                        imageUrl = downloadUri.toString();

                        item.setImage(imageUrl);

                        onSetItem();

                        //[firestore에 데이터 저장] - 수정/추가 구분
                        if (isModify == true) { //"수정" 버튼
                            Log.d(TAG, "경우 1 실행됨");
                            itemModel.updateItem(item, db, documentId, true, false);
                        } else { //"추가" 버튼
                            Log.d(TAG, "경우 4 실행됨");
                            itemModel.saveItem(item, db);
                        }

                    }
                }
            });
        } else { //이미지 추가 안했을때 - 2 3 5 합침

            onSetItem();

            //[firestore에 데이터 저장]
            if (isModify == false) { //이미지 추가 안하고 "추가"버튼
                Log.d(TAG, "경우 5 실행됨");
                item.setImage(defaultImage);
                itemModel.saveItem(item, db);
            } else{
                if (isDefault == false) { // 이미지 추가는 안했는데 - 기존에 쓰던 이미지로 하고싶을 때 "수정"버튼
                    Log.d(TAG, "경우 2 실행됨");
                    itemModel.updateItem(item, db, documentId, false, false);
                } else { // 이미지 없었어도 기존에 이미지 있었어도 디폴트 이미지로 바꾸고싶을때 "수정"버튼
                    Log.d(TAG, "경우 3 실행됨");
                    item.setImage(defaultImage);
                    itemModel.updateItem(item, db, documentId, false, true);
                }
            }

        }
    }


    //사진촬영할지 갤러리에서 가져올 지 기본이미지를 할지 선택
    private void photoDialogRadio() {
        final CharSequence[] PhotoModels = {"사진 촬영", "갤러리에서 가져오기","기본이미지"};
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
                } else if (item == 2) { // 기본이미지로 설정
                    iv_selectImage.setImageResource(android.R.drawable.ic_menu_gallery);
                    hasImage = false;
                    isDefault = true;
                    alert.dismiss();
                }
            }
        });
        alert = alt_builder.create();
        alert.show();
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
