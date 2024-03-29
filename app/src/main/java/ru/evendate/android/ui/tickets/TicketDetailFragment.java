package ru.evendate.android.ui.tickets;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ru.evendate.android.R;
import ru.evendate.android.models.Event;
import ru.evendate.android.models.EventRegistered;
import ru.evendate.android.models.Ticket;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ServiceUtils;
import ru.evendate.android.ui.utils.DateFormatter;
import ru.evendate.android.ui.utils.EventFormatter;
import ru.evendate.android.ui.utils.TicketFormatter;

public class TicketDetailFragment extends Fragment {
    private static final String EVENT_KEY = "event";
    private static final String TICKET_KEY = "ticket";

    @BindView(R.id.ticket_number) TextView mTicketNumber;
    @BindView(R.id.ticket_status) TextView mTicketStatus;
    @BindView(R.id.qr_code) ImageView mQrCode;
    @BindView(R.id.ticket_type) TextView mTicketType;
    @BindView(R.id.event_title) TextView mEventTitle;
    @BindView(R.id.datetime) TextView mDatetime;
    @BindView(R.id.place) TextView mPlace;
    @BindView(R.id.order_datetime) TextView mOrderDatetime;
    @BindView(R.id.check_out_image) ImageView checkOutImage;

    private EventRegistered mEvent;
    private Ticket mTicket;

    private OnTicketInteractionListener mListener;
    private Unbinder unbinder;

    public static TicketDetailFragment newInstance(EventRegistered event, Ticket ticket) {
        TicketDetailFragment fragment = new TicketDetailFragment();
        fragment.mEvent = event;
        fragment.mTicket = ticket;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mEvent = Parcels.unwrap(savedInstanceState.getParcelable(EVENT_KEY));
            mTicket = Parcels.unwrap(savedInstanceState.getParcelable(TICKET_KEY));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EVENT_KEY, Parcels.wrap(mEvent));
        outState.putParcelable(TICKET_KEY, Parcels.wrap(mTicket));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ticket_detail, container, false);
        unbinder = ButterKnife.bind(this, view);

        mEventTitle.setText(mEvent.getTitle());
        String QrLink = ServiceUtils.constructQrLink(ApiFactory.getHostName(getContext()),
                mEvent.getEntryId(), mTicket.getUuid());
        Picasso.with(getActivity())
                .load(QrLink)
                .error(R.drawable.default_background)
                .into(mQrCode);
        mTicketStatus.setText(mTicket.getOrder().getStatusName());
        mPlace.setText(mEvent.getLocation());
        mDatetime.setText(EventFormatter.formatDate(EventFormatter.getNearestDateTime((Event) mEvent)));
        mTicketType.setText(mTicket.getTicketType().getName());
        Date orderDate = mTicket.getOrder().getPayedAt() != null ?
                mTicket.getOrder().getPayedAt() : mTicket.getCreatedAt();
        mOrderDatetime.setText(getString(R.string.ticket_date_label) + " " + DateFormatter.formatOrderDateTime(orderDate));

        mTicketNumber.setText(TicketFormatter.formatNumber(getContext(), mTicket.getNumber()));
        if (mTicket.isCheckout()) {
            checkOutImage.setVisibility(View.VISIBLE);
            mQrCode.setAlpha(0.2f);
        }

        return view;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.event_title)
    public void onEventTitleClicked(View view) {
        if (mListener != null) {
            mListener.onEventClicked(mEvent);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTicketInteractionListener) {
            mListener = (OnTicketInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnTicketInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    interface OnTicketInteractionListener {
        void onEventClicked(EventRegistered Event);
    }
}
