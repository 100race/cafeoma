package com.vespa.baek.cafeoma.inventory.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.inventory.data.ItemModel;
import com.vespa.baek.cafeoma.inventory.view.ModifyInventoryActivity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import static com.facebook.FacebookSdk.getApplicationContext;

public class InventoryViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener { // 여기에 레이아웃으로 나와야되는애들 써줌 ex.사진,이름,수량,주문버튼
    protected LinearLayout ll_back;
    protected ImageView iv_image;
    protected TextView tv_name;
    protected TextView tv_remark;
    protected TextView tv_quantity;
    protected Button btn_order;
    protected InventoryAdapter adapter;
    protected Context context;


    public InventoryViewHolder(@NonNull View itemView) { //오버로딩
        super(itemView);
        this.ll_back = itemView.findViewById(R.id.ll_back);
        this.iv_image = itemView.findViewById(R.id.iv_image);
        this.tv_name = itemView.findViewById(R.id.tv_name);
        this.tv_remark = itemView.findViewById(R.id.tv_remark);
        this.tv_quantity = itemView.findViewById(R.id.tv_quantity);
        this.btn_order = itemView.findViewById(R.id.btn_order);

        itemView.setOnCreateContextMenuListener(this);

    }

    public void setAdapter(InventoryAdapter adapter) { this.adapter = adapter; }

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
                case 1001: //수정
                    //여기서 가져온 데이터를 intent로 넘겨줌 position넘기고 데이터는 직접통신하는게 빠를듯
                    context = getApplicationContext();
                    Intent intent = new Intent(context, ModifyInventoryActivity.class);
                    //여기서 getAdapterPosition으로 현재 뷰에 띄워져있는 위치 기반으로 데이터가 넘어가는듯
                   // adapter.getSnapshots //->ObservableSnapshotArray 이게 return 된다고하거든?
                    //문제는 이 getAdapterPosition이 현재 뷰에 뿌려져있는걸 전달하는게 아니라서 애초에 ID고뭐고 받을수가없는거같음
                    //오류 : 그냥 클릭했을 때 getFilteredPos에서 null접근해서 오류남 -> getFilteredPos 에서 처리해줘서 nullPointerException 안일어남
                   intent.putExtra("ID",adapter.getSnapshots().getSnapshot(adapter.getFilteredPos(getAdapterPosition())).getReference().getId());
                    Log.d("ViewHolderIndex", String.valueOf(adapter.getFilteredPos(getAdapterPosition())));
                    //여기서 getAdapterPosition 이 원래 포지션으로 들어감
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    break;

                case 1002: //삭제
                    //얘는 같이 filteredPos로 수정안해줬는데 왜 잘됨?? 이 삭제메서드는 filterable 어댑터에 구현이 돼있거든 list.remove가? 거기서 자꾸 오류남
                    //여기서 수정처럼 snapshot을 같이 찍어봄. ItemModel에서말고
                    new ItemModel().deleteItem(adapter,adapter.getFilteredPos(getAdapterPosition()));
                    adapter.notifyItemRemoved(adapter.getFilteredPos(getAdapterPosition()));
                    break;
            }
            return true;
        }
    };
}