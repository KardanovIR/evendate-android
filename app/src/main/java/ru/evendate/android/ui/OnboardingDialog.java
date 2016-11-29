package ru.evendate.android.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.models.OrganizationDetail;
import ru.evendate.android.models.OrganizationFull;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseArray;
import ru.evendate.android.network.ServiceImpl;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class OnboardingDialog extends DialogFragment {
    private static final String LOG_TAG = OnboardingDialog.class.getSimpleName();

    private OnboardingAdapter mAdapter;
    private ProgressBar mProgressBar;
    AlertDialog errorDialog;
    OnOrgSelectedListener listener;

    interface OnOrgSelectedListener {
        void onOrgSelected();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View customTitle = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_title, null);
        mProgressBar = (ProgressBar) customTitle.findViewById(R.id.progress_bar);

        mAdapter = (new OnboardingAdapter(getActivity()));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCustomTitle(customTitle)
                .setAdapter(mAdapter, null)
                .setPositiveButton(R.string.dialog_ok,
                        (DialogInterface dialog, int which) -> {
                            onOrgsSelected(mAdapter.getChecked());
                        })
                .setNeutralButton(R.string.dialog_skip, null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener((DialogInterface d) -> {
            Button NeutralButton = ((AlertDialog) d).getButton(DialogInterface.BUTTON_NEUTRAL);
            NeutralButton.setTextColor(Color.parseColor("#FFB1B1B1"));
        });
        loadOrgs();
        displayProgress();
        return dialog;
    }

    public void setOnOrgSelectedListener(OnOrgSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (errorDialog != null) {
            errorDialog.dismiss();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void loadOrgs() {
        ApiService apiService = ApiFactory.getService(getContext());
        Observable<ResponseArray<OrganizationFull>> observable =
                apiService.getOrgRecommendations(EvendateAccountManager.peekToken(getContext()),
                        OrganizationDetail.FIELDS_LIST, 10, 0);

        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> onLoaded(new ArrayList<>(result.getData())),
                        this::onError,
                        this::hideProgress
                );
    }

    public void onLoaded(ArrayList<OrganizationDetail> subList) {
        if(!isAdded())
            return;
        Log.i(LOG_TAG, "loaded");
        mAdapter.setList(new ArrayList<>(subList));
    }

    public void onError(Throwable error) {
        if(!isAdded())
            return;
        Log.e(LOG_TAG, error.getMessage());
        errorDialog = ErrorAlertDialogBuilder.newInstance(getContext(),
                (DialogInterface dialog, int which) -> {
                    loadOrgs();
                    displayProgress();
                    dialog.dismiss();
                });
        errorDialog.show();
    }

    private void displayProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }


    class OnboardingAdapter extends ArrayAdapter<OrganizationDetail> {
        private final Activity context;
        private boolean[] checked;
        private List<OrganizationDetail> mList = new ArrayList<>();

        OnboardingAdapter(Activity context) {
            super(context, R.layout.item_onboarding);
            this.context = context;
        }

        public void setList(List<OrganizationDetail> list) {
            checked = new boolean[list.size()];
            Arrays.fill(checked, false);
            mList = list;
            super.addAll(list);
        }

        public List<OrganizationDetail> getList() {
            return mList;
        }

        public boolean[] getChecked() {
            return checked;
        }

        class ViewHolder {
            ImageView iconView;
            TextView textView;
            CheckBox checkBox;
            final public Target target = new Target() {
                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    iconView.setImageBitmap(null);
                }

                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    if (bitmap == null)
                        return;
                    iconView.setImageBitmap(bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    iconView.setImageDrawable(errorDrawable);
                }
            };
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = context.getLayoutInflater();
                rowView = inflater.inflate(R.layout.item_onboarding, parent, false);
                holder = new ViewHolder();
                holder.textView = (TextView) rowView.findViewById(R.id.label);
                holder.iconView = (ImageView)rowView.findViewById(R.id.icon);
                holder.checkBox = (CheckBox) rowView.findViewById(R.id.checkbox);
                holder.checkBox.setOnCheckedChangeListener(
                        (CompoundButton buttonView, boolean isChecked) ->
                                checked[position] = isChecked
                );
                rowView.setTag(holder);
            } else {
                holder = (ViewHolder) rowView.getTag();
            }

            OrganizationDetail organization = mList.get(position);
            holder.textView.setText(organization.getShortName());
            Picasso.with(getContext())
                    .load(organization.getLogoSmallUrl())
                    .error(R.mipmap.ic_launcher)
                    .into(holder.target);

            return rowView;
        }

    }

    public void onOrgsSelected(boolean[] selectedItems) {
        List<OrganizationDetail> list = mAdapter.getList();
        for (int i = 0; i < list.size(); i++) {
            if (selectedItems[i])
                ServiceImpl.subscribeOrgAndChangeState(getActivity(), list.get(i));
        }
        //todo fix, cause refresh take place before subscribing done
        listener.onOrgSelected();
    }

}