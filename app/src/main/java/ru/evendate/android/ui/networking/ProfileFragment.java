package ru.evendate.android.ui.networking;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ru.evendate.android.R;

public class ProfileFragment extends DialogFragment {

    Button mContactButton;
    Toolbar mToolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_network_profile, container, false);
        mToolbar = rootView.findViewById(R.id.toolbar_networking);

        mToolbar.setTitle("PROFILE");
        mToolbar.setNavigationIcon(R.drawable.ic_clear_white);
        mToolbar.setNavigationOnClickListener((View v) -> getActivity().onBackPressed());

        mContactButton = rootView.findViewById(R.id.contact_button);
        setupButton();
        mContactButton.setOnClickListener((View v) -> onContactButtonPressed());
        return rootView;
    }

    void setupButton() {
        //todo
    }

    void onContactButtonPressed() {
        //        if(){
        //            requestApply();
        //        } else {
        //            acceptApply();
        //        }
    }

    void requestApply() {

    }

    void acceptApply() {

    }

}
