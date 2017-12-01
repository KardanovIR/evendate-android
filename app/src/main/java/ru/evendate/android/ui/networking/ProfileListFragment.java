package ru.evendate.android.ui.networking;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import ru.evendate.android.R;
import ru.evendate.android.ui.AbstractAdapter;
import ru.evendate.android.ui.EndlessListFragment;

/**
 * Created by dmitry on 29.11.2017.
 */
public class ProfileListFragment extends EndlessListFragment<NetworkActivity.ProfileListPresenter,
        Profile, ProfileListFragment.ProfileRecyclerViewAdapter.ProfileViewHolder> {

    boolean isApplication;
    NetworkContract.OnProfileInteractionListener mListener;

    public static ProfileListFragment newInstance() {
        return new ProfileListFragment();
    }

    public static ProfileListFragment newInstance(boolean isApplication) {

        ProfileListFragment fragment = new ProfileListFragment();
        fragment.isApplication = isApplication;
        return fragment;
    }

    @Override
    public Observable<String> requestAuth() {
        //todo
        return null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        final Fragment parent = getParentFragment();
        if (parent != null) {
            mListener = (NetworkContract.OnProfileInteractionListener)parent;
        } else {
            mListener = (NetworkContract.OnProfileInteractionListener)context;
        }
    }

    @Override
    protected AbstractAdapter<Profile, ProfileRecyclerViewAdapter.ProfileViewHolder> getAdapter() {
        if (mAdapter != null) {
            return mAdapter;
        } else {
            return new ProfileRecyclerViewAdapter(getContext(), mListener);
        }
    }

    @Override
    protected String getEmptyHeader() {
        return "no contacts";
    }

    @Override
    protected String getEmptyDescription() {
        return "no";
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    class ProfileRecyclerViewAdapter extends AbstractAdapter<Profile,
            ProfileRecyclerViewAdapter.ProfileViewHolder> {

        private final NetworkContract.OnProfileInteractionListener mListener;
        private Context mContext;

        ProfileRecyclerViewAdapter(@NonNull Context context,
                                   @NonNull NetworkContract.OnProfileInteractionListener listener) {
            mListener = listener;
            mContext = context;
        }

        @Override
        public ProfileRecyclerViewAdapter.ProfileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_network_profile, parent, false);
            return new ProfileViewHolder(view);
        }

        @Override
        public void onViewRecycled(ProfileViewHolder holder) {
            super.onViewRecycled(holder);
            holder.mAvatar.setImageDrawable(null);
        }

        @Override
        public void onBindViewHolder(final ProfileViewHolder holder, int position) {
            Profile profile = getItem(position);
            //            holder.mName.setText();
            //            holder.mCompany.setText();
            //            holder.mUseful.setText();
            //            holder.mFind.setText();
            //            holder.mProfile = profile;
            //            Picasso.with(mContext).load().into(holder.mAvatar);
            holder.holderView.setOnClickListener((View v) -> mListener.openProfile(holder.mProfile));
        }

        class ProfileViewHolder extends RecyclerView.ViewHolder {
            View holderView;
            @BindView(R.id.name) TextView mName;
            @BindView(R.id.company) TextView mCompany;
            @BindView(R.id.useful) TextView mUseful;
            @BindView(R.id.find) TextView mFind;
            @BindView(R.id.avatar) ImageView mAvatar;

            @Nullable Profile mProfile;

            ProfileViewHolder(View view) {
                super(view);
                holderView = itemView;
                ButterKnife.bind(this, view);
            }
        }
    }
}
