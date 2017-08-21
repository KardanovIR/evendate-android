package ru.evendate.android.ui.checkin;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ru.evendate.android.R;
import ru.evendate.android.ui.utils.TicketFormatter;
import ru.evendate.android.ui.utils.UserFormatter;
import ru.evendate.android.views.LoadStateView;

public class CheckInConfirmDialogFragment extends BottomSheetDialogFragment implements CheckInContract.TicketConfirmView {

    private Unbinder unbinder;

    static final ButterKnife.Action<View> VISIBLE =
            (View view, int index) -> view.setVisibility(View.VISIBLE);
    static final ButterKnife.Action<View> INVISIBLE =
            (View view, int index) -> view.setVisibility(View.INVISIBLE);
    private static final String KEY_TICKET_UUID = "key_ticket";
    private static final String KEY_EVENT_ID = "key_event_id";
    @BindView(R.id.ticket_type) TextView mTicketType;
    @BindView(R.id.cancel) Button mCancel;
    @BindView(R.id.confirm) Button mConfirm;
    @BindView(R.id.confirm_progress) ProgressBar mConfirmProgress;
    @BindView(R.id.avatar) ImageView mAvatar;
    @BindView(R.id.ticket_number) TextView mTicketNumber;
    @BindView(R.id.user_name) TextView UserName;
    @BindView(R.id.revert_check_out) Button mTicketCheckOut;
    @BindView(R.id.load_state) LoadStateView mLoadState;
    @BindViews({R.id.ticket_type, R.id.avatar,
            R.id.ticket_number, R.id.user_name, R.id.icon_ticket})
    List<View> mTicketViews;
    CheckInContract.TicketConfirmPresenter mPresenter;
    private CheckInContract.ConfirmInteractionListener mListener;

    public static CheckInConfirmDialogFragment newInstance(int eventId, String ticketUuid) {
        final CheckInConfirmDialogFragment fragment = new CheckInConfirmDialogFragment();
        final Bundle args = new Bundle();
        args.putString(KEY_TICKET_UUID, ticketUuid);
        args.putInt(KEY_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setPresenter(CheckInContract.TicketConfirmPresenter presenter) {
        mPresenter = presenter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check_in_confirm_dialog, container, false);
        unbinder = ButterKnife.bind(this, view);
        mLoadState.setOnReloadListener(() ->
                mPresenter.loadTicket(getArguments().getString(KEY_TICKET_UUID),
                        getArguments().getInt(KEY_EVENT_ID)));
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (CheckInContract.ConfirmInteractionListener)context;
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.loadTicket(getArguments().getString(KEY_TICKET_UUID),
                getArguments().getInt(KEY_EVENT_ID));
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void showTicketLoading(boolean active) {
        if (active) {
            ButterKnife.apply(mTicketViews, INVISIBLE);
            mTicketCheckOut.setVisibility(View.INVISIBLE);
            mLoadState.setVisibility(View.VISIBLE);
            mLoadState.showProgress();
            mCancel.setVisibility(View.GONE);
            mConfirm.setVisibility(View.GONE);
            mTicketCheckOut.setVisibility(View.GONE);
        } else {
            ButterKnife.apply(mTicketViews, VISIBLE);
            mLoadState.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void showTicketLoadingError() {
        ButterKnife.apply(mTicketViews, INVISIBLE);
        mLoadState.showErrorHint();
    }

    @Override
    public void showTicket(CheckInContract.TicketAdmin ticket) {
        mTicketType.setText(ticket.getTicketType().getName());
        Picasso.with(getContext()).load(ticket.getUser().getAvatarUrl()).into(mAvatar);
        mTicketNumber.setText(TicketFormatter.formatNumber(getContext(), ticket.getNumber()));
        UserName.setText(UserFormatter.formatUserName(ticket.getUser()));
        if (ticket.isCheckout()) {
            mCancel.setVisibility(View.GONE);
            mConfirm.setVisibility(View.GONE);
            mTicketCheckOut.setVisibility(View.VISIBLE);
        } else {
            mCancel.setVisibility(View.VISIBLE);
            mConfirm.setVisibility(View.VISIBLE);
            mTicketCheckOut.setVisibility(View.GONE);
        }
    }

    @Override
    public void showConfirmLoading(boolean active) {
        if (active) {
            mConfirm.setClickable(false);
            mConfirmProgress.setVisibility(View.VISIBLE);
        } else {
            mConfirm.setClickable(true);
            mConfirmProgress.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void showConfirmError() {
        if (!isAdded())
            return;
        mConfirm.setClickable(true);
        mListener.onConfirmCheckInError();
        mConfirmProgress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showConfirm() {
        mListener.onConfirmCheckIn();
        dismiss();
    }

    @Override
    public void showConfirmRevert() {
        mListener.onConfirmCheckInRevert();
        dismiss();
    }

    @OnClick({R.id.cancel, R.id.confirm, R.id.revert_check_out})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                dismiss();
                mListener.onConfirmCheckInCancel();
                break;
            case R.id.confirm:
                mPresenter.confirm(getArguments().getString(KEY_TICKET_UUID),
                        getArguments().getInt(KEY_EVENT_ID), true);
                break;
            case R.id.revert_check_out:
                mPresenter.confirm(getArguments().getString(KEY_TICKET_UUID),
                        getArguments().getInt(KEY_EVENT_ID), false);
                break;
        }
    }
}
