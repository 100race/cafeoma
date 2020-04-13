package com.vespa.baek.cafeoma.inventory.adapter;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vespa.baek.cafeoma.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

public class InventoryViewHolder extends RecyclerView.ViewHolder { // 여기에 레이아웃으로 나와야되는애들 써줌 ex.사진,이름,수량,주문버튼
    protected LinearLayout ll_back;
    protected ImageView iv_image;
    protected TextView tv_name;
    protected TextView tv_remark;
    protected TextView tv_quantity;
    protected Button btn_order;


    public InventoryViewHolder(@NonNull View itemView) {
        super(itemView);
        this.ll_back = itemView.findViewById(R.id.ll_back);
        this.iv_image = itemView.findViewById(R.id.iv_image);
        this.tv_name = itemView.findViewById(R.id.tv_name);
        this.tv_remark = itemView.findViewById(R.id.tv_remark);
        this.tv_quantity = itemView.findViewById(R.id.tv_quantity);
        this.btn_order = itemView.findViewById(R.id.btn_order);

    }


    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        if(selectionTracker!=null && selectionTracker.isSelected((long) getAdapterPosition())){
            //checkBox.setImageResource(android.R.drawable.checkbox_on_background); 선택되면 바꿀부분 -> 오 진짜 바꿔짐 04.13. 04:29 여기에서 걍 정보를 넣어서 삭제처리나중에 해도될듯?
            tv_name.setText("임시테스트중클릭되었다"); // 화면 뒤로갔다 다시오니 어차피 리사이클러뷰가 파이어스토어에서 불러오는거기때문에 다시 원래이름으로돌아옴 그리고 다시클릭하면 클릭해제됨
            ll_back.setActivated(true);
        }else{
            //checkBox.setImageResource(android.R.drawable.checkbox_off_background);
            ll_back.setActivated(false);
        }
    }


    public ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
        //추상클래스인 ItemDetails에는 getPosition과 getSelectionKey라는 메소드를 구현하게끔 되어있는데, adapter의 position과 item id를 리턴.
        return new ItemDetailsLookup.ItemDetails<Long>() {
            @Override
            public int getPosition() {
                return getAdapterPosition(); // 뷰홀더의 메서드
            }

            @Nullable
            @Override
            public Long getSelectionKey() {
                return getItemId(); // 뷰홀더의 메서드
            }
        };
    }

}