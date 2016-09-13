package ru.evendate.android.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;

import ru.evendate.android.R;

//todo loading orgs
public class OnboardingDialog extends DialogFragment {
    private OnOrgsSelectListener mOrgsSelectListener;
    private boolean[] checked;

    public void setOrgsSelectListener(OnOrgsSelectListener listener) {
        mOrgsSelectListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View view = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_title, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCustomTitle(view)
                .setAdapter(new MyArrayAdapter(getActivity(), new String[]{"институт", "digital",
                        "digital", "digital", "digital", "digital", "digital", "digital"}), null)
                .setPositiveButton("Ok",
                        (DialogInterface dialog, int which) -> {
                            if (mOrgsSelectListener != null)
                                mOrgsSelectListener.onOrgsSelected(checked);
                        })
                .setNeutralButton("neutral", null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener((DialogInterface d) -> {
            Button NeutralButton = ((AlertDialog) d).getButton(DialogInterface.BUTTON_NEUTRAL);
            NeutralButton.setTextColor(Color.parseColor("#FFB1B1B1"));
        });
        return dialog;
    }

    public class MyArrayAdapter extends ArrayAdapter<String> {
        private final Activity context;
        private final String[] names;

        public MyArrayAdapter(Activity context, String[] names) {
            super(context, R.layout.item_onboarding, names);
            this.context = context;
            this.names = names;
            checked = new boolean[names.length];
            Arrays.fill(checked, false);
        }

        class ViewHolder {
            public ImageView imageView;
            public TextView textView;
            public CheckBox checkBox;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = context.getLayoutInflater();
                rowView = inflater.inflate(R.layout.item_onboarding, parent, false);
                holder = new ViewHolder();
                holder.textView = (TextView) rowView.findViewById(R.id.label);
                holder.imageView = (ImageView) rowView.findViewById(R.id.icon);
                holder.checkBox = (CheckBox) rowView.findViewById(R.id.checkbox);
                holder.checkBox.setOnCheckedChangeListener(
                        (CompoundButton buttonView, boolean isChecked) -> {
                            checked[position] = isChecked;
                        }
                );
                rowView.setTag(holder);
            } else {
                holder = (ViewHolder) rowView.getTag();
            }

            holder.textView.setText(names[position]);

            return rowView;
        }
    }

    interface OnOrgsSelectListener {
        void onOrgsSelected(boolean[] selectedItems);
    }
}