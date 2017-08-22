package com.github.dkharrat.nexusdialog.controllers;

import android.content.Context;
import android.support.annotation.IdRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.github.dkharrat.nexusdialog.R;
import com.github.dkharrat.nexusdialog.validations.InputValidator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by dmitry on 19.06.17.
 */

public class RadioButtonController extends CheckBoxController {

    public RadioButtonController(Context ctx, String name, String labelText, Set<InputValidator> validators, List<String> items, boolean useItemsAsValues) {
        super(ctx, name, labelText, validators, items, useItemsAsValues);
    }

    public RadioButtonController(Context ctx, String name, String labelText, Set<InputValidator> validators, List<String> items, List<?> values) {
        super(ctx, name, labelText, validators, items, values);
    }

    public RadioButtonController(Context ctx, String name, String labelText, boolean isRequired, List<String> items, boolean useItemsAsValues) {
        super(ctx, name, labelText, isRequired, items, useItemsAsValues);
    }

    public RadioButtonController(Context ctx, String name, String labelText, boolean isRequired, List<String> items, List<?> values) {
        super(ctx, name, labelText, isRequired, items, values);
    }

    @Override
    protected View createFieldView() {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup radioGroupContainer = (ViewGroup)inflater.inflate(R.layout.form_checkbox_container, null);

        RadioGroup radioGroup = new RadioGroup(getContext());
        radioGroupContainer.addView(radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if (i == -1)
                    return;
                int position = i - CHECKBOX_ID;
                Object value = areValuesDefined() ? values.get(position) : position;
                Set<Object> modelValues = new HashSet<>(retrieveModelValues());
                modelValues.clear();
                modelValues.add(value);
                getModel().setValue(getName(), modelValues);
            }
        });

        RadioButton radioButton;
        int nbItem = items.size();
        for (int index = 0; index < nbItem; index++) {
            radioButton = new RadioButton(getContext());
            radioButton.setText(items.get(index));
            radioButton.setId(CHECKBOX_ID + index);

            radioGroup.addView(radioButton);
            refresh(radioButton, index);
        }
        radioGroup.clearCheck();
        return radioGroupContainer;
    }

    public void refresh(RadioButton radioButton, int index) {
        Set<Object> modelValues = retrieveModelValues();
        radioButton.setChecked(
                modelValues.contains(
                        areValuesDefined() ? values.get(index) : index
                )
        );
    }

    @Override
    public void refresh() {
        ViewGroup layout = getContainer();

        RadioButton radioButton;
        int nbItem = items.size();
        for (int index = 0; index < nbItem; index++) {
            radioButton = (RadioButton)layout.findViewById(CHECKBOX_ID + index);
            refresh(radioButton, index);
        }
    }

}
