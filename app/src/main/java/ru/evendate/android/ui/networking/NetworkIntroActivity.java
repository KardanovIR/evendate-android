package ru.evendate.android.ui.networking;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import ru.evendate.android.R;

public class NetworkIntroActivity extends AppCompatActivity {

    FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_intro);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFab = findViewById(R.id.fab);

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().add(R.id.container, new CodeCheckerFragment()).commit();
    }


    void onProfilePostError() {
        Snackbar.make(findViewById(R.id.coordinator), "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    void onProfilePosted() {
        setResult(RESULT_OK);
        finish();
    }

    void openProfileCreator() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.container, new ProfileCreatorFragment()).commit();
    }


    public static class CodeCheckerFragment extends Fragment {
        EditText codeView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.fragment_code_checker, container, false);
            codeView = root.findViewById(R.id.code_edit_text);
            ((NetworkIntroActivity)getActivity()).mFab.setOnClickListener((View view) -> {
                checkCode();
            });
            return root;
        }

        void checkCode() {
            Toast.makeText(getContext(), codeView.getText(), Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() ->
                    ((NetworkIntroActivity)getActivity()).openProfileCreator(), 2000);
        }
    }

    public static class ProfileCreatorFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ((NetworkIntroActivity)getActivity()).mFab.setOnClickListener((View view) -> {
                postProfile();
            });
            return inflater.inflate(R.layout.fragment_profile_creator, container, false);
        }

        void postProfile() {
            new Handler().postDelayed(() ->
                    ((NetworkIntroActivity)getActivity()).onProfilePosted(), 2000);
        }

    }

}
