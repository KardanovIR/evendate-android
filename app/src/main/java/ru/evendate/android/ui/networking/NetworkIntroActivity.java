package ru.evendate.android.ui.networking;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.network.ApiFactory;

public class NetworkIntroActivity extends AppCompatActivity {

    FloatingActionButton mFab;
    public final static String PROFILE_KEY = "profile_key";
    public final static String EVENT_ID_KEY = "event_id_key";
    public final static String SKIP_CODE_KEY = "skip_code_key";
    @Nullable NetworkingProfile profile;
    boolean skipCode = false;
    int eventId;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_intro);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getIntent() != null) {
            profile = Parcels.unwrap(getIntent().getParcelableExtra(PROFILE_KEY));
            skipCode = getIntent().getBooleanExtra(SKIP_CODE_KEY, false);
        }
        eventId = getIntent().getIntExtra(EVENT_ID_KEY, 0);

        mFab = findViewById(R.id.fab);

        FragmentManager manager = getSupportFragmentManager();
        if (!skipCode) {
            mToolbar.setTitle("Вход в комнату участников");
            manager.beginTransaction().add(R.id.container, CodeCheckerFragment.newInstance(eventId)).commit();
        } else {
            openProfileCreator(profile);
        }
    }


    void onProfilePostError() {
        Snackbar.make(findViewById(R.id.coordinator), "Ошибка при создании профиля. Попробуйте повторить", Snackbar.LENGTH_LONG).show();
    }

    void onProfilePosted() {
        setResult(RESULT_OK);
        finish();
    }

    void openProfileCreator(NetworkingProfile profile) {
        mToolbar.setTitle("Карточка участника");
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.container, ProfileCreatorFragment.newInstance(profile, eventId)).commit();
    }


    public static class CodeCheckerFragment extends Fragment {
        EditText codeView;
        boolean loading = false;
        int eventId;

        public static CodeCheckerFragment newInstance(int eventId) {
            CodeCheckerFragment fragment = new CodeCheckerFragment();
            fragment.eventId = eventId;
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.fragment_code_checker, container, false);
            codeView = root.findViewById(R.id.code_edit_text);

            ((NetworkIntroActivity)getActivity()).mFab.setOnClickListener((View view) -> {
                if (loading || codeView.getText().toString().isEmpty())
                    return;
                loading = true;
                checkCode(codeView.getText().toString());
            });
            return root;
        }

        void checkCode(String code) {
            String token = EvendateAccountManager.peekToken(getContext());
            ApiFactory.getService(getContext()).getMyNetworkingProfile(token, eventId, code).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            result -> {
                                loading = false;
                                if (result.isOk()) {
                                    onLoaded(result.getData());
                                } else {
                                    onCheckFail();
                                }
                            }
                    );
        }

        private void onLoaded(NetworkingProfile profile) {
            if (!profile.isSignedUp()) {
                ((NetworkIntroActivity)getActivity()).openProfileCreator(profile);
            } else {
                getActivity().setResult(RESULT_OK);
                getActivity().finish();
            }
        }

        private void onCheckFail() {
            // todo string
            Toast.makeText(getContext(), "Введен неверный код", Toast.LENGTH_SHORT).show();
        }
    }


    public static class ProfileCreatorFragment extends Fragment {
        Unbinder unbinder;
        @BindView(R.id.avatar) CircleImageView mAvatar;
        @BindView(R.id.first_name) TextInputEditText mFirstName;
        @BindView(R.id.first_name_layout) TextInputLayout mFirstNameLayout;
        @BindView(R.id.last_name) TextInputEditText mLastName;
        @BindView(R.id.last_name_layout) TextInputLayout mLastNameLayout;
        @BindView(R.id.company) TextInputEditText mCompany;
        @BindView(R.id.info) TextInputEditText mInfo;
        @BindView(R.id.looking_for) TextInputEditText mLookingFor;
        @BindView(R.id.email) TextInputEditText mEmail;
        @BindView(R.id.vk_url) TextInputEditText mVkUrl;
        @BindView(R.id.fb_url) TextInputEditText mFbUrl;
        @BindView(R.id.twitter_url) TextInputEditText mTwitterUrl;
        @BindView(R.id.linkedin_url) TextInputEditText mLinkedInUrl;
        @BindView(R.id.telegram_url) TextInputEditText mTelegramUrl;
        @BindView(R.id.instagram_url) TextInputEditText mInstagramUrl;
        @BindView(R.id.github_url) TextInputEditText mGithubUrl;

        NetworkingProfile profile;
        int eventId;

        public static ProfileCreatorFragment newInstance(NetworkingProfile profile, int eventId) {
            ProfileCreatorFragment fragment = new ProfileCreatorFragment();
            fragment.profile = profile;
            fragment.eventId = eventId;
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ((NetworkIntroActivity)getActivity()).mFab.setOnClickListener((View view) -> {
                postProfile();
            });

            View root = inflater.inflate(R.layout.fragment_profile_creator, container, false);

            unbinder = ButterKnife.bind(this, root);
            Picasso.with(getContext()).load(profile.avatarUrl).into(mAvatar);
            mFirstName.setText(profile.firstName);
            mLastName.setText(profile.lastName);
            return root;
        }

        void postProfile() {
            if (!checkAndAlertFields()) {
                prepareProfile();
                String token = EvendateAccountManager.peekToken(getContext());
                ApiFactory.getService(getContext()).postMyNetworkingProfile(token, eventId,
                        profile.firstName, profile.lastName, profile.companyName, profile.info,
                        profile.lookingFor, profile.email, profile.vkUrl, profile.facebookUrl, profile.twitter_url,
                        profile.linkedinUrl, profile.telegramUrl, profile.instagramUrl, profile.githubUrl).subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    if (result.isOk()) {
                                        ((NetworkIntroActivity)getActivity()).onProfilePosted();
                                    } else {
                                        ((NetworkIntroActivity)getActivity()).onProfilePostError();
                                    }
                                }, throwable -> ((NetworkIntroActivity)getActivity()).onProfilePostError()
                        );
            }
        }

        void prepareProfile() {
            profile.firstName = mFirstName.getEditableText().toString();
            profile.lastName = mLastName.getEditableText().toString();
            profile.companyName = mCompany.getEditableText().toString();
            profile.info = mInfo.getEditableText().toString();
            profile.lookingFor = mLookingFor.getEditableText().toString();
            profile.email = mEmail.getEditableText().toString();
            profile.vkUrl = mVkUrl.getEditableText().toString();
            profile.facebookUrl = mFbUrl.getEditableText().toString();
            profile.twitter_url = mTwitterUrl.getEditableText().toString();
            profile.linkedinUrl = mLinkedInUrl.getEditableText().toString();
            profile.telegramUrl = mTelegramUrl.getEditableText().toString();
            profile.instagramUrl = mInstagramUrl.getEditableText().toString();
            profile.githubUrl = mGithubUrl.getEditableText().toString();
        }

        boolean checkAndAlertFields() {
            boolean neededFields = false;
            if (mFirstName.getEditableText().toString().isEmpty()) {
                mFirstNameLayout.setError("Имя — обязательное поле");
                neededFields = true;
            }
            if (mLastName.getEditableText().toString().isEmpty()) {
                mLastNameLayout.setError("Фамилия — обязательное поле");
                neededFields = true;
            }
            return neededFields;
        }


        @Override
        public void onDestroyView() {
            super.onDestroyView();
            unbinder.unbind();
        }
    }

}
