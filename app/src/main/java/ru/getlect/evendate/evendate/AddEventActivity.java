package ru.getlect.evendate.evendate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.EntypoModule;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.joanzapata.iconify.fonts.IoniconsModule;
import com.joanzapata.iconify.fonts.MaterialModule;
import com.joanzapata.iconify.fonts.MeteoconsModule;
import com.joanzapata.iconify.fonts.SimpleLineIconsModule;
import com.joanzapata.iconify.fonts.TypiconsModule;
import com.joanzapata.iconify.fonts.WeathericonsModule;

/**
 * Created by fj on 14.08.2015.
 */
public class AddEventActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogs_activity);

        DialogsFragment dialogsFragment = new DialogsFragment();

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Новое");

        Iconify
                .with(new FontAwesomeModule())
                .with(new EntypoModule())
                .with(new TypiconsModule())
                .with(new MaterialModule())
                .with(new MeteoconsModule())
                .with(new WeathericonsModule())
                .with(new SimpleLineIconsModule())
                .with(new IoniconsModule());

    }


}
