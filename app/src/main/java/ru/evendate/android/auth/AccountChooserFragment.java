package ru.evendate.android.auth;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ru.evendate.android.R;

/**
 * Created by Dmitry on 29.10.2015.
 */
public class AccountChooserFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_account_chooser, container, false);

        Button SingInVkButton = (Button)rootView.findViewById(R.id.sing_in_vk_button);
        Button SingInFbButton = (Button)rootView.findViewById(R.id.sing_in_fb_button);
        Button SingInGoogleButton = (Button)rootView.findViewById(R.id.sing_in_google_button);

        SingInVkButton.setOnClickListener((AuthActivity)getContext());
        SingInFbButton.setOnClickListener((AuthActivity)getContext());
        SingInGoogleButton.setOnClickListener((AuthActivity)getContext());
        return rootView;
    }


}
