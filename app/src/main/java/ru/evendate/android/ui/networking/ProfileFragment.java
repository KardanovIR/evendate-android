package ru.evendate.android.ui.networking;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.network.ApiFactory;

public class ProfileFragment extends DialogFragment {

    @BindView(R.id.contact_button) Button mContactButton;
    @BindView(R.id.button_line) View mButtonLine;
    @BindView(R.id.toolbar_networking) Toolbar mToolbar;
    NetworkingProfile profile;
    @BindView(R.id.avatar) ImageView mAvatar;
    @BindView(R.id.info) TextView mInfo;
    @BindViews({R.id.info, R.id.info_label}) List<View> mInfoViews;
    @BindView(R.id.looking_for) TextView mLookingFor;
    @BindViews({R.id.looking_for, R.id.looking_for_label}) List<View> mLookingForViews;
    @BindView(R.id.name) TextView mName;

    @BindView(R.id.email) TextView mEmail;
    @BindViews({R.id.email, R.id.email_label}) List<View> mEmailViews;
    @BindView(R.id.vk) TextView mVk;
    @BindViews({R.id.vk, R.id.email_label}) List<View> mVkViews;
    @BindView(R.id.fb) TextView mFb;
    @BindViews({R.id.fb, R.id.fb_label}) List<View> mFbViews;
    @BindView(R.id.twitter) TextView mTwitter;
    @BindViews({R.id.twitter, R.id.twitter_label}) List<View> mTwitterViews;
    @BindView(R.id.linkedin) TextView mLinkedin;
    @BindViews({R.id.linkedin, R.id.linkedin_label}) List<View> mLinkedinViews;
    @BindView(R.id.telegram) TextView mTelegram;
    @BindViews({R.id.telegram, R.id.telegram_label}) List<View> mTelegramViews;
    @BindView(R.id.instagram) TextView mInstagram;
    @BindViews({R.id.instagram, R.id.instagram_label}) List<View> mInstagramViews;
    @BindView(R.id.github) TextView mGithub;

    @BindView(R.id.data_container) ViewGroup mDataContainer;

    Unbinder unbinder;
    int eventId;
    Disposable mDisposable;

    private final ButterKnife.Action<View> VISIBLE =
            (View view, int index) -> view.setVisibility(View.VISIBLE);
    private final ButterKnife.Action<View> GONE =
            (View view, int index) -> view.setVisibility(View.GONE);

    public static ProfileFragment newInstance(NetworkingProfile profile, int eventId) {
        ProfileFragment fragment = new ProfileFragment();
        fragment.profile = profile;
        fragment.eventId = eventId;
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_network_profile, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        mToolbar.setTitle("PROFILE");
        mToolbar.setNavigationIcon(R.drawable.ic_clear_white);
        mToolbar.setNavigationOnClickListener((View v) -> getActivity().onBackPressed());

        setupButton();
        mContactButton.setOnClickListener((View v) -> onContactButtonPressed());

        Picasso.with(getContext()).load(profile.avatarUrl).into(mAvatar);
        mName.setText(profile.lastName + " " + profile.firstName);

        mLookingFor.setText(profile.lookingFor);
        if (profile.lookingFor == null || profile.lookingFor.isEmpty()) {
            ButterKnife.apply(mLookingForViews, GONE);
        }
        mInfo.setText(profile.info);
        if (profile.info == null || profile.info.isEmpty()) {
            ButterKnife.apply(mInfoViews, GONE);
        }
        setupContactData();

        return rootView;
    }

    void setupContactData() {
        boolean isDataExist = false;
        mEmail.setText(profile.email);
        if (profile.email == null || profile.email.isEmpty()) {
            ButterKnife.apply(mEmailViews, GONE);
        } else {
            isDataExist = true;
        }
        mVk.setText(profile.vkUrl);
        if (profile.vkUrl == null || profile.vkUrl.isEmpty()) {
            ButterKnife.apply(mVkViews, GONE);
        } else {
            isDataExist = true;
        }
        mFb.setText(profile.facebookUrl);
        if (profile.facebookUrl == null || profile.facebookUrl.isEmpty()) {
            ButterKnife.apply(mFbViews, GONE);
        } else {
            isDataExist = true;
        }
        mTwitter.setText(profile.twitter_url);
        if (profile.twitter_url == null || profile.twitter_url.isEmpty()) {
            ButterKnife.apply(mTwitterViews, GONE);
        } else {
            isDataExist = true;
        }
        mLinkedin.setText(profile.linkedinUrl);
        if (profile.linkedinUrl == null || profile.linkedinUrl.isEmpty()) {
            ButterKnife.apply(mLinkedinViews, GONE);
        } else {
            isDataExist = true;
        }
        mTelegram.setText(profile.telegramUrl);
        if (profile.telegramUrl == null || profile.telegramUrl.isEmpty()) {
            ButterKnife.apply(mTelegramViews, GONE);
        } else {
            isDataExist = true;
        }
        mInstagram.setText(profile.instagramUrl);
        if (profile.instagramUrl == null || profile.instagramUrl.isEmpty()) {
            ButterKnife.apply(mInstagramViews, GONE);
        } else {
            isDataExist = true;
        }
        mGithub.setText(profile.githubUrl);
        if (profile.githubUrl == null || profile.githubUrl.isEmpty()) {
            ButterKnife.apply(mGithub, GONE);
        } else {
            isDataExist = true;
        }
        if (!isDataExist) {
            mDataContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    boolean checkRequesting() {
        return profile.request == null && profile.outgoingRequestUuid == null;
    }

    boolean checkAccepting() {
        return profile.request != null && profile.request.accept_status == null;
    }

    void setupButton() {
        if (checkRequesting()) {
            mContactButton.setText("Запросить контактные данные");
        } else if (checkAccepting()) {
            mContactButton.setText("Принять заявку");
        } else {
            mContactButton.setVisibility(View.GONE);
            mButtonLine.setVisibility(View.GONE);
        }
    }

    void onContactButtonPressed() {
        if (checkRequesting()) {
            requestApply();
        } else if (checkAccepting()) {
            acceptApply();
        }
    }

    void requestApply() {
        if (mDisposable != null && !mDisposable.isDisposed())
            return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        EditText editText = new EditText(getContext());
        editText.setHint("Введите сообщение");
        int px = (int)(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        editText.setPadding(px, px, px, px);
        builder.setView(editText);
        builder.setTitle("Заявка участнику");
        builder.setPositiveButton("Отправить", (DialogInterface dialog, int which) -> {
            String message = editText.getText().toString();
            String token = EvendateAccountManager.peekToken(getContext());

            mDisposable = ApiFactory.getService(getContext()).postNetworkingRequest(token, eventId, profile.userId, message).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        if (response.isOk()) {
                            Toast.makeText(getContext(), "Заявка отправлена", Toast.LENGTH_SHORT).show();
                            mContactButton.setEnabled(false);
                        } else {
                            Toast.makeText(getContext(), "Произошла ошибка", Toast.LENGTH_SHORT).show();
                        }
                    }, throwable -> Toast.makeText(getContext(), "Произошла ошибка", Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton("Отменить", (DialogInterface dialog, int which) -> dismiss());
        builder.create().show();
    }

    void acceptApply() {
        if (mDisposable != null && !mDisposable.isDisposed())
            return;
        String token = EvendateAccountManager.peekToken(getContext());
        mDisposable = ApiFactory.getService(getContext()).acceptNetworkingRequest(token, eventId, profile.requestUuid, false).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            if (result.isOk()) {
                                mContactButton.setEnabled(false);
                                Toast.makeText(getContext(), "Заявка отправлена", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Произошла ошибка", Toast.LENGTH_SHORT).show();
                            }
                        }, throwable -> Toast.makeText(getContext(), "Произошла ошибка", Toast.LENGTH_SHORT).show()
                );
    }

}
