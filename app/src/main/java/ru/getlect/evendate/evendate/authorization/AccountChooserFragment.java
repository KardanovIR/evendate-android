package ru.getlect.evendate.evendate.authorization;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.EntypoModule;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.joanzapata.iconify.fonts.IoniconsModule;
import com.joanzapata.iconify.fonts.MaterialModule;
import com.joanzapata.iconify.fonts.MeteoconsModule;
import com.joanzapata.iconify.fonts.SimpleLineIconsModule;
import com.joanzapata.iconify.fonts.TypiconsModule;
import com.joanzapata.iconify.fonts.WeathericonsModule;
import com.joanzapata.iconify.widget.IconTextView;

import ru.getlect.evendate.evendate.R;

/**
 * Created by Dmitry on 29.10.2015.
 */
public class AccountChooserFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_account_chooser, container, false);


        Iconify
                .with(new FontAwesomeModule())
                .with(new EntypoModule())
                .with(new TypiconsModule())
                .with(new MaterialModule())
                .with(new MeteoconsModule())
                .with(new WeathericonsModule())
                .with(new SimpleLineIconsModule())
                .with(new IoniconsModule());


        IconTextView itv_vk = (IconTextView)rootView.findViewById(R.id.icon_text_vk);
        IconTextView itv_fb = (IconTextView)rootView.findViewById(R.id.icon_text_fb);
        IconTextView itv_gPlus = (IconTextView)rootView.findViewById(R.id.icon_text_google);

        itv_vk.setText("{entypo-vk}");
        itv_fb.setText("{entypo-facebook}");
        itv_gPlus.setText("{entypo-google}");

        Button SingInVkButton = (Button)rootView.findViewById(R.id.sing_in_vk_button);
        Button SingInFbButton = (Button)rootView.findViewById(R.id.sing_in_fb_button);
        Button SingInGoogleButton = (Button)rootView.findViewById(R.id.sing_in_google_button);

        SingInVkButton.setOnClickListener((AuthActivity)getContext());
        SingInFbButton.setOnClickListener((AuthActivity)getContext());
        SingInGoogleButton.setOnClickListener((AuthActivity)getContext());
        return rootView;
    }


}
