package com.vespa.baek.cafeoma.main.view.shop;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.vespa.baek.cafeoma.R;

import com.vespa.baek.cafeoma.main.view.shop.data.Shop;

import java.util.ArrayList;

import androidx.annotation.NonNull;

import static com.facebook.FacebookSdk.getApplicationContext;

public class ShopAdapter extends FirestoreRecyclerAdapter<Shop, ShopViewHolder> {
    private final static String defaultImage = "";
    private Context context;



    public ShopAdapter(@NonNull FirestoreRecyclerOptions<Shop> options, Context context) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {  //뷰홀더를 최초로 만들어내는곳 인플레이터활용
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_shop, parent, false);
        Log.d("재고", "인플레이터실행됨");
        return new ShopViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ShopViewHolder holder, int position, @NonNull Shop model) {  // 각 아이템에 대한 매칭을 하는것

        holder.tv_shopName.setText(model.getShopName()); //현재 이 model은 Shop 타입임

        //shop크기의 그걸 누를 시 url로 연결
        holder.ll_shop.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if ( model.getShopUrl() != null && URLUtil.isValidUrl(model.getShopUrl())) {
                                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(model.getShopUrl()));
                                                        v.getContext().startActivity(intent);
                                                    } else {
                                                        Toast.makeText(getApplicationContext(), "유효한 사이트를 등록해주세요", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
        );

        holder.setAdapter(this); //
    }

}
