package ru.evendate.android.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import ru.evendate.android.BuildConfig;
import ru.evendate.android.R;

public class SettingsActivity extends PreferenceActivity {

    Context context = this;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference dialogPreference = getPreferenceScreen().findPreference("dialog_preference");
        dialogPreference.setOnPreferenceClickListener((Preference preference) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
            builder.setPositiveButton("Ok", (DialogInterface dialog, int id) -> {
                    // User clicked OK button
            });

            builder.setTitle("About program");
            builder.setMessage("Version " + BuildConfig.VERSION_NAME);
            //LayoutInflater factory = LayoutInflater.from(context);
            //final View view = factory.inflate(R.layout.dialog_version, null);
            //builder.setView(view);
            builder.setIcon(R.drawable.evendate_logo);
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        });

    Preference colorPreference = getPreferenceScreen().findPreference("color_preference");
        colorPreference.setOnPreferenceClickListener((Preference preference) -> {
                ColorPickerDialogBuilder
                        .with(context, R.style.AlertDialogCustom)
                        .noSliders()
                        .setTitle("Choose color")
                        .initialColor(getResources().getColor(R.color.accent))
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener((selectedColor) -> {
                                toast("onColorSelected: 0x" + Integer.toHexString(selectedColor));
                        })
                        .setPositiveButton("ok", (DialogInterface dialog, int selectedColor, Integer[] allColors) -> {
                                //changeBackgroundColor(selectedColor);
                        })
                        .setNegativeButton("cancel", (DialogInterface dialog, int which) -> {

                        })
                        .build()
                        .show();
                return true;
        });
    }
    private void toast(String string){
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }
}