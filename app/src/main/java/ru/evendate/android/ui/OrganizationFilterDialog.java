package ru.evendate.android.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;

import ru.evendate.android.R;
import ru.evendate.android.sync.models.OrganizationType;

/**
 * Created by ds_gordeev on 05.02.2016.
 * contain list of categories that user choose
 */
public class OrganizationFilterDialog extends DialogFragment {
    private ArrayList<OrganizationType> mItemsList;
    private boolean[] mSelectedItems;
    private OnCategorySelectListener mCategorySelectListener;


    public static OrganizationFilterDialog newInstance(ArrayList<OrganizationType> itemsList,
                                                       boolean[] selectedItems) {
        OrganizationFilterDialog fragment = new OrganizationFilterDialog();
        fragment.mItemsList = itemsList;
        fragment.mSelectedItems = selectedItems;
        return fragment;
    }

    public void setCategorySelectListener(OnCategorySelectListener listener) {
        mCategorySelectListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogCustom);
        builder.setTitle(R.string.dialog_check_organizations)
                .setMultiChoiceItems(
                        typeToStrings(),
                        mSelectedItems,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                mSelectedItems[which] = isChecked;
                            }
                        }
                )
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (mCategorySelectListener != null) {
                            mCategorySelectListener.onCategorySelected(mSelectedItems);
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        return builder.create();
    }
    interface OnCategorySelectListener{
        void onCategorySelected(boolean[] selectedItems);
    }
    private String[] typeToStrings(){
        String[] stringArray = new String[mItemsList.size()];
        for (int i = 0; i < mItemsList.size(); i++){
            stringArray[i] = mItemsList.get(i).getName();
        }
        return stringArray;
    }
}