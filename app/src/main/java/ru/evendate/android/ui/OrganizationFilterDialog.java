package ru.evendate.android.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;

import ru.evendate.android.R;
import ru.evendate.android.models.OrganizationCategory;

/**
 * Created by ds_gordeev on 05.02.2016.
 * contain list of categories that user choose
 */
public class OrganizationFilterDialog extends DialogFragment {
    private ArrayList<OrganizationCategory> mItemsList;
    private boolean[] mSelectedItems;
    private OnCategorySelectListener mCategorySelectListener;


    public static OrganizationFilterDialog newInstance(ArrayList<OrganizationCategory> itemsList,
                                                       boolean[] selectedItems) {
        OrganizationFilterDialog fragment = new OrganizationFilterDialog();
        fragment.mItemsList = itemsList;
        fragment.mSelectedItems = selectedItems;
        return fragment;
    }

    public void setCategorySelectListener(OnCategorySelectListener listener) {
        mCategorySelectListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogCustom);
        builder.setTitle(R.string.dialog_check_organizations)
                .setMultiChoiceItems(
                        typeToStrings(),
                        mSelectedItems,
                        (DialogInterface dialog, int which, boolean isChecked) -> {
                            mSelectedItems[which] = isChecked;
                        }
                )
                .setPositiveButton(R.string.dialog_ok, (DialogInterface dialog, int id) -> {
                    if (mCategorySelectListener != null) {
                        mCategorySelectListener.onCategorySelected(mSelectedItems);
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, (DialogInterface dialog, int id) -> {
                });

        return builder.create();
    }

    interface OnCategorySelectListener {
        void onCategorySelected(boolean[] selectedItems);
    }

    private String[] typeToStrings() {
        String[] stringArray = new String[mItemsList.size()];
        for (int i = 0; i < mItemsList.size(); i++) {
            stringArray[i] = mItemsList.get(i).getName();
        }
        return stringArray;
    }
}