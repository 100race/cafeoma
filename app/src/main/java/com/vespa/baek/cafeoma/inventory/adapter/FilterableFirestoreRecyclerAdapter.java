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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @param <T>  model class, for parsing {@link DocumentSnapshot}s.
 * @param <VH> {@link RecyclerView.ViewHolder} class.
 */
public abstract class FilterableFirestoreRecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>
        implements ChangeEventListener, LifecycleObserver, Filterable {

    private static final String TAG = "FirestoreRecycler";

    private final ObservableSnapshotArray<T> mSnapshots;
    private final List<T> list, backupList;
    private CustomFilter mCustomFilter;
    private boolean isFiltarable;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     */
    public FilterableFirestoreRecyclerAdapter(@NonNull FirestoreRecyclerOptions<T> options, boolean isFiltarable) {
        mSnapshots = options.getSnapshots();

        list = new ArrayList<>();
        backupList = new ArrayList<>();
        if (options.getOwner() != null) {
            options.getOwner().getLifecycle().addObserver(this);
        }
        this.isFiltarable = isFiltarable;
    }

    /**
     * Start listening for database changes and populate the adapter.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void startListening() {
        if (list.size() > 0) list.clear(); // 3.추가한거
        if (backupList.size() > 0) backupList.clear(); //추가보류 - 근데 같이 추가해야 말이될거같음
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
        //itemsList.getSnapshots().removeChangeEventListener(this); 고친사람이 원래 윗줄을 이렇게 씀
        list.clear(); // 이건 새로 넣어준 부분 44
        backupList.clear(); // 이건 내가 새로 넣어준부분. 백업리스트도 같이 초기화해야될거같았어
        notifyDataSetChanged();
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
    @NonNull
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
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onChildChanged(@NonNull ChangeEventType type,
                               @NonNull DocumentSnapshot snapshot,
                               int newIndex,
                               int oldIndex) {
        T model = mSnapshots.get(type!=ChangeEventType.REMOVED ? newIndex : oldIndex);// 1. 바꿨던거 -> 여기서 indexoutofBoundsException남
        onChildUpdate(model, type, snapshot, newIndex, oldIndex);
        Log.d("찾자","onChildUpdate호출"+model);
    }

    protected void onChildUpdate(T model, ChangeEventType type,
                                 DocumentSnapshot snapshot,
                                 int newIndex,
                                 int oldIndex) {
        switch (type) {
            case ADDED: //이게 너무많이실행됨
                addItem(snapshot.getId(), model);
                notifyItemInserted(newIndex);
                Log.d("찾자","added"+model);
                break;
            case CHANGED:
                addItem(snapshot.getId(), model, newIndex);
                notifyItemChanged(newIndex);
                Log.d("찾자","changed"+model);
                break;
            case REMOVED: // 바꾼거 2 oldIndex로
                removeItem(oldIndex);
                notifyItemRemoved(oldIndex);
                Log.d("찾자","removed"+model);
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

    private void removeItem(int oldIndex) {
        list.remove(oldIndex);
        if (isFiltarable)
            backupList.remove(oldIndex);
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

    public class CustomFilter extends Filter { //여기서 검색된 item을 걸러주는거같은데 여기서 adapter position같은 연결기능이없어서 수정 삭제가 이상하게되나?

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            final FilterResults results = new FilterResults();
            if (constraint.length() == 0) { // -> editText에서 글씨 다 지웠을 때 / 여기다 이놈이다 여기가 데이터를 다 지웠을때임 글자를. 그때 백업리스트를 불러오는데 dubplicate되어있는거야 -> add가 너무많이 됨. - 원인이 list는 초기화 잘 시켜주고데이터
                        //잘 지워주는데 backuplist는 같이 안지워줘서였던거같음 이제 테스트중인데 꽤 잘되는느낌
                results.values = backupList;
                results.count = backupList.size();
            } else {   //editText에 글자 있을 때
                List<T> filteredList = new ArrayList<>();
                final String filterPattern = constraint.toString().toLowerCase().trim();
                for (T t : backupList) {
                    if (filterCondition(t, filterPattern)) {
                        filteredList.add(t);
                    }
                }
                results.values = filteredList;
                results.count = filteredList.size();
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