package com.vespa.baek.cafeoma.main.view.memo;

import android.content.Context;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.main.view.memo.data.Memo;
import com.vespa.baek.cafeoma.main.view.memo.data.MemoModel;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



public class MemoViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener { // 여기에 레이아웃으로 나와야되는애들 써줌 ex.사진,이름,수량,주문버튼
    protected LinearLayout ll_memo;
    protected TextView tv_title;
    protected TextView tv_contents;
    protected TextView tv_date;
    protected MemoAdapter adapter;

    protected Context context;
    protected String documentId;
    protected FirebaseFirestore db;


    public MemoViewHolder(@NonNull View itemView) { //오버로딩
        super(itemView);
        this.ll_memo = itemView.findViewById(R.id.ll_memo);
        this.tv_title = itemView.findViewById(R.id.tv_title);
        this.tv_contents = itemView.findViewById(R.id.tv_contents);
        this.tv_date = itemView.findViewById(R.id.tv_date);

        context = itemView.getContext();

        itemView.setOnCreateContextMenuListener(this);

    }

    public void setAdapter(MemoAdapter adapter) { this.adapter = adapter; }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuItem delete = menu.add(Menu.NONE, 1001, 1, "삭제");
        delete.setOnMenuItemClickListener(onClickMenu);
    }

    private final MenuItem.OnMenuItemClickListener onClickMenu = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {

            switch (item.getItemId()) {
                case 1001: //삭제

                    new MemoModel().deleteMemo(adapter,getAdapterPosition());
                    adapter.notifyItemRemoved(getAdapterPosition());
                    adapter.notifyDataSetChanged();
                    break;
            }
            return true;
        }
    };
}
