package ru.evendate.android.ui.networking;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import ru.evendate.android.R;
import ru.evendate.android.ui.AbstractAdapter;
import ru.evendate.android.ui.EndlessListFragment;

/**
 * Created by dmitry on 29.11.2017.
 */
public class ProfileListFragment extends EndlessListFragment<NetworkActivity.ProfileListPresenter,
        NetworkingProfile, ProfileListFragment.ProfileRecyclerViewAdapter.ProfileViewHolder> {

    NetworkContract.OnProfileInteractionListener mListener;

    public static ProfileListFragment newInstance() {
        return new ProfileListFragment();
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
    protected AbstractAdapter<NetworkingProfile, ProfileRecyclerViewAdapter.ProfileViewHolder> getAdapter() {
        if (mAdapter != null) {
            return mAdapter;
        } else {
            return new ProfileRecyclerViewAdapter(getContext(), mListener);
        }
    }

    @Override
    protected String getEmptyHeader() {
        return "Нет контактов";
    }

    @Override
    protected String getEmptyDescription() {
        return "Пока здесь никого нет";
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void appendProfile(NetworkingProfile profile) {
        mAdapter.append(profile);
        setLoadingIndicator(false);
    }

    class ProfileRecyclerViewAdapter extends AbstractAdapter<NetworkingProfile,
            ProfileRecyclerViewAdapter.ProfileViewHolder> {

        private final ButterKnife.Action<View> VISIBLE =
                (View view, int index) -> view.setVisibility(View.VISIBLE);
        private final ButterKnife.Action<View> GONE =
                (View view, int index) -> view.setVisibility(View.GONE);

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
            ButterKnife.apply(holder.mApplyViews, GONE);
            ButterKnife.apply(holder.mInfoViews, VISIBLE);
            ButterKnife.apply(holder.mLookingViews, VISIBLE);
        }

        @Override
        public void onBindViewHolder(final ProfileViewHolder holder, int position) {
            NetworkingProfile profile = getItem(position);
            holder.mName.setText(profile.lastName + " " + profile.firstName);
            holder.mCompany.setText(profile.companyName);
            holder.mUseful.setText(profile.info);
            holder.mFind.setText(profile.lookingFor);
            holder.mNetworkingProfile = profile;
            Picasso.with(mContext).load(profile.avatarUrl).into(holder.mAvatar);
            holder.holderView.setOnClickListener((View v) -> mListener.openProfile(holder.mNetworkingProfile));

            if (profile.lookingFor == null) {
                ButterKnife.apply(holder.mLookingViews, GONE);
            }
            if (profile.info == null) {
                ButterKnife.apply(holder.mInfoViews, GONE);
            }
            if (profile.request != null && profile.request.accept_status == null) {
                ButterKnife.apply(holder.mApplyViews, VISIBLE);
                holder.mMessage.setText(profile.request.getMessage());
                holder.mApplyButton.setOnClickListener((View view) -> {
                    mListener.applyRequest(profile).subscribe(hiddenProfile -> {
                        if (profile == hiddenProfile) {
                            mAdapter.remove(profile);
                        }
                    });
                });
                holder.mHideButton.setOnClickListener((View view) -> {
                    mListener.hideRequest(profile).subscribe(hiddenProfile -> {
                        if (profile == hiddenProfile) {
                            mAdapter.remove(profile);
                        }
                    });
                });
            }
        }

        class ProfileViewHolder extends RecyclerView.ViewHolder {
            View holderView;
            @BindView(R.id.name) TextView mName;
            @BindView(R.id.company) TextView mCompany;
            @BindView(R.id.useful) TextView mUseful;
            @BindView(R.id.looking_for) TextView mFind;
            @BindView(R.id.avatar) ImageView mAvatar;
            @BindView(R.id.message) TextView mMessage;
            @BindView(R.id.hide_button) Button mHideButton;
            @BindView(R.id.apply_button) Button mApplyButton;
            @BindViews({R.id.line, R.id.message, R.id.hide_button, R.id.apply_button})
            List<View> mApplyViews;
            @BindViews({R.id.looking_for_label, R.id.looking_for})
            List<View> mLookingViews;
            @BindViews({R.id.useful_label, R.id.useful})
            List<View> mInfoViews;

            @Nullable NetworkingProfile mNetworkingProfile;

            ProfileViewHolder(View view) {
                super(view);
                holderView = itemView;
                ButterKnife.bind(this, view);
            }
        }
    }
}
