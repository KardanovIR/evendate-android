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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.EvendateApplication;
import ru.evendate.android.R;
import ru.evendate.android.models.OrganizationFull;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.Response;
import ru.evendate.android.network.ResponseArray;
import ru.evendate.android.views.IconView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class OnboardingDialog extends DialogFragment {
    private static final String LOG_TAG = OnboardingDialog.class.getSimpleName();

    private OnboardingAdapter mAdapter;
    private ProgressBar mProgressBar;
    AlertDialog errorDialog;

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
                        OrganizationFull.FIELDS_LIST, 10, 0);

        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> onLoaded(result.getData()),
                        this::onError,
                        this::hideProgress
                );
    }

    public void onLoaded(ArrayList<OrganizationFull> subList) {
        if(!isAdded())
            return;
        Log.i(LOG_TAG, "loaded");
        mAdapter.setList(subList);
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


    public class OnboardingAdapter extends ArrayAdapter<OrganizationFull> {
        private final Activity context;
        private boolean[] checked;
        private List<OrganizationFull> mList = new ArrayList<>();

        public OnboardingAdapter(Activity context) {
            super(context, R.layout.item_onboarding);
            this.context = context;
        }

        public void setList(List<OrganizationFull> list) {
            checked = new boolean[list.size()];
            Arrays.fill(checked, false);
            mList = list;
            super.addAll(list);
        }

        public List<OrganizationFull> getList() {
            return mList;
        }

        public boolean[] getChecked() {
            return checked;
        }

        class ViewHolder {
            public IconView iconView;
            public TextView textView;
            public CheckBox checkBox;
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

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = context.getLayoutInflater();
                rowView = inflater.inflate(R.layout.item_onboarding, parent, false);
                holder = new ViewHolder();
                holder.textView = (TextView) rowView.findViewById(R.id.label);
                holder.iconView = (IconView) rowView.findViewById(R.id.icon);
                holder.checkBox = (CheckBox) rowView.findViewById(R.id.checkbox);
                holder.checkBox.setOnCheckedChangeListener(
                        (CompoundButton buttonView, boolean isChecked) ->
                                checked[position] = isChecked
                );
                rowView.setTag(holder);
            } else {
                holder = (ViewHolder) rowView.getTag();
            }

            OrganizationFull organization = mList.get(position);
            holder.textView.setText(organization.getShortName());
            Picasso.with(getContext())
                    .load(organization.getLogoSmallUrl())
                    .error(R.mipmap.ic_launcher)
                    .into(holder.target);

            return rowView;
        }

    }

    public void onOrgsSelected(boolean[] selectedItems) {
        List<OrganizationFull> list = mAdapter.getList();
        for (int i = 0; i < list.size(); i++) {
            if (selectedItems[i])
                subscribe(list.get(i));
        }
    }

    /**
     * handle subscription button
     * start subscribe/unsubscribe loader to carry it to server
     * push subscribe/unsubscribe stat to analytics
     */
    //todo DRY
    public void subscribe(OrganizationFull organization) {
        ApiService apiService = ApiFactory.getService(getActivity());
        Observable<Response> subOrganizationObservable;

        if (organization.isSubscribed()) {
            subOrganizationObservable = apiService.orgDeleteSubscription(organization.getEntryId(),
                    EvendateAccountManager.peekToken(getActivity()));
        } else {
            subOrganizationObservable = apiService.orgPostSubscription(organization.getEntryId(),
                    EvendateAccountManager.peekToken(getActivity()));
        }
        subOrganizationObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.isOk())
                        Log.i(LOG_TAG, "subscription applied");
                    else
                        Log.e(LOG_TAG, "Error with response with organization sub");
                }, error -> {
                    Log.e(LOG_TAG, error.getMessage());
                });

        organization.subscribe();

        Tracker tracker = EvendateApplication.getTracker();
        HitBuilders.EventBuilder event = new HitBuilders.EventBuilder()
                .setCategory(getActivity().getString(R.string.stat_category_organization))
                .setLabel((Long.toString(organization.getEntryId())));
        if (organization.isSubscribed()) {
            event.setAction(getActivity().getString(R.string.stat_action_subscribe));
        } else {
            event.setAction(getActivity().getString(R.string.stat_action_unsubscribe));
        }
        tracker.send(event.build());
    }
}