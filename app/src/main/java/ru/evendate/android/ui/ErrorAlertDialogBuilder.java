package ru.evendate.android.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import ru.evendate.android.R;

/**
 * Created by ds_gordeev on 05.02.2016.
 */
public class ErrorAlertDialogBuilder{
    public static AlertDialog newInstance(Context context, DialogInterface.OnClickListener onClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogCustom);
        builder.setTitle(context.getString(R.string.loading_error));
        builder.setMessage(context.getString(R.string.loading_error_description));

        builder.setPositiveButton("Retry", onClickListener);
        return builder.create();
    }
}
