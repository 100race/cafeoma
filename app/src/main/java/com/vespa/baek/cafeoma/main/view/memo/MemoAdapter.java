package com.vespa.baek.cafeoma.main.view.memo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.vespa.baek.cafeoma.R;
import com.vespa.baek.cafeoma.main.view.memo.data.Memo;

import androidx.annotation.NonNull;


public class MemoAdapter extends FirestoreRecyclerAdapter<Memo, MemoViewHolder> {
    private Context context;

    public MemoAdapter(@NonNull FirestoreRecyclerOptions<Memo> options, Context context) {
        super(options);
        this.context = context;
//        setHasStableIds(true);
    }

    @NonNull
    @Override
    public MemoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {  //뷰홀더를 최초로 만들어내는곳 인플레이터활용
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_memo, parent, false);
        return new MemoViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull MemoViewHolder holder, int position, @NonNull Memo model) {  // 각 아이템에 대한 매칭을 하는것

        holder.tv_title.setText(model.getTitle());
        holder.tv_contents.setText(model.getContents());
        holder.tv_date.setText(model.getDate());

        //누를 시 url로 연결
        holder.ll_memo.setOnClickListener(new View.OnClickListener() {
                                              @Override
                                              public void onClick(View v) {
                                                  Intent intent = new Intent(v.getContext(),MemoViewActivity.class);
                                                  intent.putExtra("ID",getSnapshots().getSnapshot(position).getReference().getId());
                                                  v.getContext().startActivity(intent);
                                              }
                                          }
        );

        holder.setAdapter(this);
    }

}

