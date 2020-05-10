package com.vespa.baek.cafeoma.main.view.shop;

import android.app.AlertDialog;
import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.main.view.shop.data.Shop;
import com.vespa.baek.cafeoma.main.view.shop.data.ShopModel;


import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;

import androidx.recyclerview.widget.RecyclerView;


public class ShopViewHolder  extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener { // 여기에 레이아웃으로 나와야되는애들 써줌 ex.사진,이름,수량,주문버튼
    protected LinearLayout ll_shop;
    protected ImageView iv_shop;
    protected TextView tv_shopName;
    protected ShopAdapter adapter;

    protected Context context;
    protected String documentId;
    protected FirebaseFirestore db;


    public ShopViewHolder(@NonNull View itemView) { //오버로딩
        super(itemView);
        this.ll_shop = itemView.findViewById(R.id.ll_shop);
        this.iv_shop = itemView.findViewById(R.id.iv_shop);
        this.tv_shopName = itemView.findViewById(R.id.tv_shopName);

        context = itemView.getContext();

        itemView.setOnCreateContextMenuListener(this);

    }


    public void setAdapter(ShopAdapter adapter) { this.adapter = adapter; }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuItem modify = menu.add(Menu.NONE,1001,1,"수정");
        MenuItem delete = menu.add(Menu.NONE, 1002, 2, "삭제");
        modify.setOnMenuItemClickListener(onClickMenu);
        delete.setOnMenuItemClickListener(onClickMenu);
    }

    private final MenuItem.OnMenuItemClickListener onClickMenu = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {

            switch (item.getItemId()) {
                case 1001: //수정 - 다이얼로그를 띄워서 바로 수정할 수 있게
                    //context = getApplicationContext(); dialog만들 때 getApplicationContext쓰면 에러

                    db= FirebaseFirestore.getInstance();
                    documentId = adapter.getSnapshots().getSnapshot(getAdapterPosition()).getReference().getId();

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    // 다이얼로그를 보여주기 위해 edit_box.xml 파일을 사용합니다.

                    View view = LayoutInflater.from(context)
                            .inflate(R.layout.edit_box, null, false);
                    builder.setView(view);
                    final Button ButtonSubmit = (Button) view.findViewById(R.id.btn_dialog_submit);
                    final EditText et_dialog_shopName = (EditText) view.findViewById(R.id.et_dialog_shopName);
                    final EditText et_dialog_shopUrl = (EditText) view.findViewById(R.id.et_dialog_shopUrl);

                    // 6. 해당 줄에 입력되어 있던 데이터를 불러와서 다이얼로그에 보여줍니다.

                    et_dialog_shopName.setText(String.valueOf(adapter.getSnapshots().getSnapshot(getAdapterPosition()).get("shopName")));
                    et_dialog_shopUrl.setText(String.valueOf(adapter.getSnapshots().getSnapshot(getAdapterPosition()).get("shopUrl")));


                    final AlertDialog dialog = builder.create();
                    ButtonSubmit.setOnClickListener(new View.OnClickListener() {

                        // 7. 수정 버튼을 클릭하면 현재 UI에 입력되어 있는 내용으로

                        public void onClick(View v) {
                            String strName = et_dialog_shopName.getText().toString();
                            String strURl = et_dialog_shopUrl.getText().toString();
                            Shop shop = new Shop(strName, strURl);

                            // 8. firebase에 있는 데이터를 변경하고
                            new ShopModel().updateShop(shop,db,documentId);


                            // 9. 어댑터에서 RecyclerView에 반영하도록 합니다.

                            adapter.notifyItemChanged(getAdapterPosition());

                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                    break;

                case 1002: //삭제

                    new ShopModel().deleteShop(adapter,getAdapterPosition());
                    adapter.notifyItemRemoved(getAdapterPosition());
                    adapter.notifyDataSetChanged();
                    break;
            }
            return true;
        }
    };
}

