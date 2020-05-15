package com.vespa.baek.cafeoma.inventory.adapter;



import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.widget.Filter;
import android.widget.Filterable;


import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.firestore.ChangeEventListener;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @param <T>  model class, for parsing {@link DocumentSnapshot}s.
 * @param <VH> {@link RecyclerView.ViewHolder} class.
 * 파이어베이스 리사이클러 어댑터를 필터적용할 수 있게 상속해서 구현한 클래스
 */
public abstract class FilterableFirestoreRecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>
        implements ChangeEventListener, LifecycleObserver, Filterable {

    private static final String TAG = "FilterableFirestoreRecycler";

    private final ObservableSnapshotArray<T> mSnapshots;
    private final ArrayList<T> list, backupList;
    private ArrayList<Integer> filteredIndex; // new 필된 인덱스를 저장할곳
    private CustomFilter mCustomFilter;
    private boolean isFiltarable;
    private boolean isFiltered;


    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     */
    public FilterableFirestoreRecyclerAdapter(@NonNull FirestoreRecyclerOptions<T> options, boolean isFiltarable) {
        mSnapshots = options.getSnapshots(); //여기서 아예 데이터가 options 넣어준거랑 snapshot연결돼서 이 position이랑 늘 연결됨 즉 position을 따로 저장할 곳이 필요하다

        list = new ArrayList<>();
        backupList = new ArrayList<>();
        filteredIndex = new ArrayList<>(); //
        if (options.getOwner() != null) {
            options.getOwner().getLifecycle().addObserver(this);
        }
        this.isFiltarable = isFiltarable;
        this.isFiltered = false;
    }

    /**
     * Start listening for database changes and populate the adapter.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void startListening() {
        if (list.size() > 0) list.clear(); // 3.추가한거
        if (backupList.size() > 0) backupList.clear(); //
        if (filteredIndex.size() > 0) filteredIndex.clear(); //
        if (!mSnapshots.isListening(this)) {
            mSnapshots.addChangeEventListener(this);
        }
    }

    /**
     * Stop listening for database changes and clear all items in the adapter.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void stopListening() {
        mSnapshots.removeChangeEventListener(this);
//        list.clear(); // 4. 추가한거
//        backupList.clear(); //
//        filteredIndex.clear();
        notifyDataSetChanged();
    }

    @Override //여기서 구현해줘야 stableID를 쓸 수 있다. 새로 구현 - 필터된리스트의 getItemId할거다. 지우면 안되는 부분
    public long getItemId(int position) {
        return list.get(position).hashCode();
    }

    // 받아온 아이템 리스트를 딴데서도 이용할 수 있게
    public ArrayList<T> getList() {
        return list;
    }

    public ArrayList<T> getBackupList() {
        return backupList;
    }

    //필터링 된 뷰의 index를 저장해놓는 배열
    public ArrayList<Integer> getFilteredIndex() {
        //만약 필터링이 안된상태면 -nullPointerException 발생 그리고 필터링 됐다가 지운 상태/수정 액티비티 다녀온 상태에서 IndexOutofBoundsException
        if(filteredIndex == null || filteredIndex.isEmpty()){ //필터링을 한번도 사용하지않았거나 필터했다가 지웠을 때
            filteredIndex = new ArrayList<>();
            for (int i = 0; i < backupList.size(); i++) {
                //백업리스트랑 index일치시켜준다
                filteredIndex.add(i);
            }
        }
        return filteredIndex;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void cleanup(LifecycleOwner source) {
        source.getLifecycle().removeObserver(this);
    }

    /**
     * Returns the backing {@link ObservableSnapshotArray} used to populate this adapter.
     *
     * @return the backing snapshot array
     */
    @NonNull //여기서 한번 snapshot을 새로 return 할 수 있나 current data로?
    //snapshot이란건, 처음 어댑터를 등록 할 때 넣었던 option으로 얻는 그건가봄.
    //방법 1. 기존 다른 어댑터들 하는것처럼 position(또는 stableId 또는 ID)데이터를 따로 구현한다
    //방법 2. stable ID를 이용해본다
    public ObservableSnapshotArray<T> getSnapshots() {
        return mSnapshots;
    }

    /**
     * Gets the item at the specified position from the backing snapshot array.
     *
     * @see ObservableSnapshotArray#get(int)
     */
    @NonNull
    public T getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getItemCount() { return list.size(); } // 여기서 이 사이즈를 +1을 하던지 아니면 backupList.size로 바꿔볼까 -> 둘다 crash 일단 건드리면 안되는듯 여기는

    @Override
    public void onChildChanged(@NonNull ChangeEventType type,  //이게 데이터 바뀌고 나서 나오는거라 snapshot이 size가 자꾸 작은거같음.
                               @NonNull DocumentSnapshot snapshot,
                               int newIndex,
                               int oldIndex) {
        Log.d("한번new",String.valueOf(newIndex)+" old:"+String.valueOf(oldIndex));
       // 1. 바꿨던거 -> 여기서 indexoutofBoundsException남
      //  T model = (type != ChangeEventType.REMOVED ?  mSnapshots.get(newIndex):null);
        T model = mSnapshots.isEmpty()? null: mSnapshots.get(type!=ChangeEventType.REMOVED ? newIndex : 0) ; //삼항연산자 중첩 -> 해결

        /**        text oldIndex 는 그냥 넘겨주는거 어차피 Removed때는 snapshots 쓸 일이없음 아무데이터나 넘겨주기
        얘가 oldIndex -1 이면 0인덱스에서 오류, oldIndex면 마지막 인덱스에서 오류
        어차피 데이터를 지우려면 하나는 있어야하니까...index대신 0을 넘겨줌
        근데 마지막삭제일때는 어떡해? -> 역시나 마지막 삭제시 오류가 남 일단 remove 일 때 model데이터만 null안되게 넘어가면 문제없는듯
         **/

        onChildUpdate(model, type, snapshot, newIndex, oldIndex);
    }

    protected void onChildUpdate(T model, ChangeEventType type,
                                 DocumentSnapshot snapshot,
                                 int newIndex,
                                 int oldIndex) {
        switch (type) {
            case ADDED:
                addItem(snapshot.getId(), model);
                notifyItemInserted(newIndex);
                break;
            case CHANGED:
                addItem(snapshot.getId(), model, newIndex);
                notifyItemChanged(newIndex);
                break;
            case REMOVED:
                removeItem(oldIndex);
                notifyItemRemoved(oldIndex);
                break;
            case MOVED:
                moveItem(snapshot.getId(), model, newIndex, oldIndex);
                notifyItemMoved(oldIndex, newIndex);
                break;
            default:
                throw new IllegalStateException("Incomplete case statement");
        }
    }

    private void moveItem(String key, T t, int newIndex, int oldIndex) {
        list.remove(oldIndex);
        list.add(newIndex, t);
        if (isFiltarable) {
            backupList.remove(oldIndex);
            backupList.add(newIndex, t);
        }
    }

    private void removeItem(int oldIndex) {  //오류발생부분 -> newIndex로 하면 기본삭제할때 오류
//        list.remove(oldIndex); //생각해보면 백업리스트랑 리스트랑 같은인덱스에 들어간 데이터가 다를텐데. 올드인덱스는 오히려 괜찮은데 list에 들어가는 인덱스는 위치기반이라 새로운위치랑 좀 안맞을거란말임?
//        if (isFiltarable)
//            backupList.remove(oldIndex);
        Log.d("remove", "oldindex:" + oldIndex);

        //백업리스트가 안지워져서 이것저것하다보면 length가 10이되어있음 그 말은 여기서도 뭔가 같이 지워줘야되는데 필터안하고지우면 백업리스트는 좀 이상해짐 -> 아냐근데 어차피 필터쓴다는건 첨에알려주기때문에.
        if (isFiltarable) { //생각해보면 백업리스트랑 리스트랑 같은인덱스에 들어간 데이터가 다를텐데. 올드인덱스는 오히려 괜찮은데 list에 들어가는 인덱스는 위치기반이라 새로운위치랑 좀 안맞을거란말임?
            //list.remove(getFilteredIndex().get(oldIndex)); //또 같은 오류남 Index 1 size 1
            if(isFiltered){
                /**근데 이렇게 해주면 list데이터가 결과적으로는 이상해지는거아닌가... 근데 어차피 filter될때마다 clear되고있긴한데
                그렇게 어차피 필터안돼있을때는 기존 oldIndex가 삭제돼서 이건 화면처리상으로만 문제있을듯
                새로 뿌려지는 index가 필요함 근데 new 는 맨날 -1만 반환하니
                리스트는 새로뿌려진거 기준으로 2갠데 3번째 인덱스 삭제하려니 오류
                여기선 getFilteredList의 반대로 oldIndex를 새로뿌려진뷰의 인덱스로 바꿔줘야함**/
                list.remove(filteredIndex.indexOf(oldIndex)); //시도! ->잘삭제됨!!


                backupList.remove(oldIndex);
            }
            else{
                list.remove(oldIndex);
                backupList.remove(oldIndex);
            }

    }
    }

    private void addItem(String key, T t, int newIndex) {
        list.remove(newIndex);
        list.add(newIndex, t);
        if (isFiltarable) {
            backupList.remove(newIndex);
            backupList.add(newIndex, t);
        }
    }

    private void addItem(String id, T t) {
        list.add(t);
        if (isFiltarable)
            backupList.add(t);
    }

    @Override
    public void onDataChanged() {
    }

    @Override
    public void onError(@NonNull FirebaseFirestoreException e) {
        Log.w(TAG, "onError", e);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        onBindViewHolder(holder, position, getItem(position));
    }

    /**
     * @param model the model object containing the data that should be used to populate the view.
     * @see #onBindViewHolder(RecyclerView.ViewHolder, int)
     */
    protected abstract void onBindViewHolder(@NonNull VH holder, int position, @NonNull T model);

    /**
     * filter condition for Filter
     *
     * @param model         model T
     * @param filterPattern filter pattern with Lower Case
     */
    protected boolean filterCondition(T model, String filterPattern) {
        return true;
    }

    @Override
    public Filter getFilter() {
        if (mCustomFilter == null) {
            mCustomFilter = new CustomFilter();
        }

        return mCustomFilter;
    }

    public class CustomFilter extends Filter {
        //여기서 검색된 item을 걸러주는곳. 여기서 adapter position같은 연결기능이없어서 수정 삭제가 이상하게되나?

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            final FilterResults results = new FilterResults();
            if (constraint.length() == 0) { // -> editText에서 글씨 다 지웠을 때 / 여기다 이놈이다 여기가 데이터를 다 지웠을때임 글자를. 그때 백업리스트를 불러오는데 dubplicate되어있는거야
                // 원인이 list는 초기화 잘 시켜주고데이터
                //잘 지워주는데 backuplist는 같이 안지워줘서였던거같음
                //필터했다가 지웠을 때 다시
                filteredIndex = new ArrayList<>();
                for (int i = 0; i < backupList.size(); i++) {
                    //백업리스트랑 index일치
                    filteredIndex.add(i);
                }
                results.values = backupList;
                results.count = backupList.size();
                isFiltered = false; //
            } else {   //editText에 글자 있을 때
                List<T> filteredList = new ArrayList<>();
                filteredIndex = new ArrayList<>(); //추가
                int index = 0; //for-each문에서 인덱스를 사용할수있게 선언한
                final String filterPattern = constraint.toString().toLowerCase().trim();
                for (T t : backupList) { //backupList에서 꺼내서 하나씩 검사하니까. backupList는 snapshot이랑 데이터 동일할거고
                    //넣어줄 때 스냅샷position중 필터링 된 뷰의 position이 저장됨
                    if (filterCondition(t, filterPattern)) {
                        filteredList.add(t);
                        filteredIndex.add(index); // 이부분
                        Log.d("BaekupListindex :",String.valueOf(index));
                    }
                    index++;
                }
                results.values = filteredList;
                results.count = filteredList.size();
                isFiltered = true;//
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            list.addAll((Collection<? extends T>) results.values);
            notifyDataSetChanged();
        }
    }
}