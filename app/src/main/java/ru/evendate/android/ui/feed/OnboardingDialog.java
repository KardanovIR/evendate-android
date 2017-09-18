package ru.evendate.android.ui.feed;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.models.OrganizationDetail;
import ru.evendate.android.network.ServiceImpl;
import ru.evendate.android.views.LoadStateView;

public class OnboardingDialog extends DialogFragment implements LoadStateView.OnReloadListener {
    private static final String LOG_TAG = OnboardingDialog.class.getSimpleName();

    private OnboardingAdapter mAdapter;
    @BindView(R.id.load_state) LoadStateView mLoadStateView;
    private OnOrgSelectedListener listener;
    private Unbinder unbinder;

    interface OnOrgSelectedListener {
        void onOrgSelected();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View customTitle = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_onboarding_title, null);

        mAdapter = (new OnboardingAdapter(getActivity()));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCustomTitle(customTitle)
                .setAdapter(mAdapter, null)
                .setPositiveButton(R.string.dialog_ok,
                        (DialogInterface dialog, int which) -> onOrgsSelected(mAdapter.getChecked()))
                .setNeutralButton(R.string.dialog_skip, null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener((DialogInterface d) -> {
            Button NeutralButton = ((AlertDialog)d).getButton(DialogInterface.BUTTON_NEUTRAL);
            NeutralButton.setTextColor(Color.parseColor("#FFB1B1B1"));
        });
        unbinder = ButterKnife.bind(this, customTitle);
        mLoadStateView.setOnReloadListener(this);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLoadStateView.showProgress();
        loadOrgs();
    }

    public void setOnOrgSelectedListener(OnOrgSelectedListener listener) {
        this.listener = listener;
    }

    private void loadOrgs() {
        String token = EvendateAccountManager.peekToken(getContext());
        if (token == null) {
            dismiss();
            return;
        }
        new DataRepository(getContext())
                .getOrgRecommendations(token, 0, 10)
                .subscribe(
                        result -> onLoaded(new ArrayList<>(result.getData())),
                        this::onError,
                        mLoadStateView::hideProgress
                );
    }

    private void onLoaded(ArrayList<OrganizationDetail> subList) {
        if (!isAdded())
            return;
        Log.i(LOG_TAG, "loaded");
        mAdapter.setList(new ArrayList<>(subList));
    }

    @Override
    public void onReload() {
        loadOrgs();
    }

    private void onError(Throwable error) {
        if (!isAdded())
            return;
        Log.e("" + LOG_TAG, error.getMessage());
        mLoadStateView.showErrorHint();
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

        boolean[] getChecked() {
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
                holder.textView = rowView.findViewById(R.id.label);
                holder.iconView = rowView.findViewById(R.id.icon);
                holder.checkBox = rowView.findViewById(R.id.checkbox);
                holder.checkBox.setOnCheckedChangeListener(
                        (CompoundButton buttonView, boolean isChecked) ->
                                checked[position] = isChecked
                );
                rowView.setTag(holder);
            } else {
                holder = (ViewHolder)rowView.getTag();
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

    private void onOrgsSelected(boolean[] selectedItems) {
        List<OrganizationDetail> list = mAdapter.getList();
        for (int i = 0; i < list.size(); i++) {
            if (selectedItems[i])
                ServiceImpl.subscribeOrgAndChangeState(getActivity(), list.get(i));
        }
        //todo fix, cause reload take place before subscribing done
        listener.onOrgSelected();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}