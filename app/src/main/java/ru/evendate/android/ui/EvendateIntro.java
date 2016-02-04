package ru.evendate.android.ui;

import android.content.Intent;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

import ru.evendate.android.R;
import ru.evendate.android.authorization.AuthActivity;

/**
 * Created by Dmitry on 14.12.2015.
 */
public class EvendateIntro extends AppIntro {
    @Override
    public void init(Bundle savedInstanceState) {

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest
        addSlide(AppIntroFragment.newInstance(null, getString(R.string.intro_miss),
                R.drawable.intro1, getResources().getColor(R.color.primary)));
        addSlide(AppIntroFragment.newInstance(null, getString(R.string.intro_interests),
                R.drawable.intro2, getResources().getColor(R.color.primary)));
        addSlide(AppIntroFragment.newInstance(null, getString(R.string.intro_share),
                R.drawable.intro3, getResources().getColor(R.color.primary)));
        addSlide(AppIntroFragment.newInstance(null, getString(R.string.intro_manage),
                R.drawable.intro4, getResources().getColor(R.color.primary)));
        addSlide(AppIntroFragment.newInstance(null, getString(R.string.intro_sync),
                R.drawable.intro5, getResources().getColor(R.color.primary)));

        setDoneText(getString(R.string.intro_done));
        setSkipText(getString(R.string.intro_skip));
        setSeparatorColor(getResources().getColor(R.color.primary));
        showStatusBar(true);
        // Hide Skip/Done button
        showSkipButton(true);
        showDoneButton(true);
    }

    @Override
    public void onSkipPressed() {
        onDonePressed();
    }

    @Override
    public void onDonePressed() {
        startActivity(new Intent(this, AuthActivity.class));
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onSlideChanged() {

    }
}
