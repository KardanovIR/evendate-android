package ru.evendate.android.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ru.evendate.android.R;
import ru.evendate.android.models.ActionType;
import ru.evendate.android.ui.OrganizationCatalogAdapter;

/**
 * Created by ds_gordeev on 17.02.2016.
 */
public class ActionTypesAdapter extends RecyclerView.Adapter<ActionTypesAdapter.ActionHolder> {
    protected Context mContext;
    private ArrayList<ActionType> mList;

    public ActionTypesAdapter(Context context){
        this.mContext = context;
    }
    public void setList(ArrayList<ActionType> list){
        mList = list;
        notifyDataSetChanged();
    }
    public ArrayList<ActionType> getList(){
        return mList;
    }
    @Override
    public int getItemCount() {
        if(mList == null)
            return 0;
        return mList.size();
    }

    @Override
    public ActionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ActionHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_action, parent, false));
    }
    @Override
    public void onBindViewHolder(ActionHolder holder, int position) {
        if(getList() == null)
            return;
        ActionType type = getList().get(position);
        holder.mActionTextView.setText(type.getTypeName());
        holder.mActionTargetsAdapter = new ActionTargetsAdapter(mContext);
        holder.recyclerView.setLayoutManager(
                new OrganizationCatalogAdapter.CatalogLinearLayoutManager(mContext,
                        LinearLayoutManager.VERTICAL, false));
        holder.recyclerView.setAdapter(holder.mActionTargetsAdapter);
        holder.mActionTargetsAdapter.setList(type.getTargetList());
        holder.recyclerView.setNestedScrollingEnabled(false);
    }
    public class ActionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View holderView;
        public TextView mActionTextView;
        public RecyclerView recyclerView;
        public ActionTargetsAdapter mActionTargetsAdapter;

        public ActionHolder(View itemView){
            super(itemView);
            holderView = itemView;
            recyclerView = (RecyclerView)itemView.findViewById(R.id.recycler_view);
            mActionTextView = (TextView)itemView.findViewById(R.id.action);
            holderView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //if(v == holderView){
            //    Intent intent = new Intent(mContext, UserProfileActivity.class);
            //    intent.setData(EvendateContract.UserEntry.CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build());
            //    mContext.startActivity(intent);
            //}
        }

    }
}
