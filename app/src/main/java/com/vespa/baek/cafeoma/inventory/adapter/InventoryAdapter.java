package com.vespa.baek.cafeoma.inventory.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.inventory.data.Item;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> implements InventoryAdapterContract.View,InventoryAdapterContract.Model{

    private Context context;
    private ArrayList<Item> arrayList;

    public InventoryAdapter(Context context, ArrayList<Item> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //뷰홀더를 최초로 만들어내는부분 인플레이터활용
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        InventoryViewHolder holder = new InventoryViewHolder(view);
        return holder;
}

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) { //각 아이템에대한 매칭을하는것
        //이미지를 받아와서 이미지뷰에 넣어주는 모습 - ******null일경우 디폴트를 넣어주는곳이 여긴가??
        Glide.with(holder.itemView)
                .load(arrayList.get(position).getImage())
                .into(holder.iv_image);
        holder.tv_name.setText(arrayList.get(position).getName());
        holder.tv_remark.setText(arrayList.get(position).getRemark());
        holder.tv_quantity.setText(arrayList.get(position).getQuantity());
        //여기서 버튼을 누를 시 url로 연결해주는 리스너를 붙여주기
        //holder.btn_order.setOnClickListener();


    }

    @Override
    public int getItemCount() {
        //삼항연산자 사용
        return (arrayList != null ? arrayList.size() : 0);
    }

    @Override
    public void notifyAdapter() {

    }

    @Override
    public void addItems(ArrayList<Item> items) {

    }

    @Override
    public void clearItems() {

    }

    public class InventoryViewHolder extends RecyclerView.ViewHolder{ // 여기에 레이아웃으로 나와야되는애들 써줌 ex.사진,이름,수량,주문버튼
        ImageView iv_image;
        TextView tv_name;
        TextView tv_remark;
        TextView tv_quantity;
        Button btn_order;


        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            this.iv_image = itemView.findViewById(R.id.iv_image);
            this.tv_name = itemView.findViewById(R.id.tv_name);
            this.tv_remark = itemView.findViewById(R.id.tv_remark);
            this.tv_quantity = itemView.findViewById(R.id.tv_quantity);
            this.btn_order = itemView.findViewById(R.id.btn_order);

        }
    }
}
