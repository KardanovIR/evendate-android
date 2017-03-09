package ru.evendate.android.ui.ticketsdetail;

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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.evendate.android.R;
import ru.evendate.android.models.DateUtils;
import ru.evendate.android.models.EventFormatter;
import ru.evendate.android.models.EventRegistered;
import ru.evendate.android.models.Ticket;
import ru.evendate.android.ui.DateFormatter;

public class TicketDetailFragment extends Fragment {
    private static final String EVENT_KEY = "event";
    private static final String TICKET_KEY = "ticket";

    @Bind(R.id.ticket_number) TextView mTicketNumber;
    @Bind(R.id.ticket_status) TextView mTicketStatus;
    @Bind(R.id.qr_code) ImageView mQrCode;
    @Bind(R.id.ticket_type) TextView mTicketType;
    @Bind(R.id.event_title) TextView mEventTitle;
    @Bind(R.id.datetime) TextView mDatetime;
    @Bind(R.id.place) TextView mPlace;
    @Bind(R.id.order_datetime) TextView mOrderDatetime;
    @Bind(R.id.check_out_image) ImageView checkOutImage;

    private EventRegistered mEvent;
    private Ticket mTicket;

    private OnTicketInteractionListener mListener;

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
        ButterKnife.bind(this, view);

        mEventTitle.setText(mEvent.getTitle());
        String QrLink = "https://evendate.ru/api/v1/events/" + mEvent.getEntryId() + "/tickets/" +
                mTicket.getUuid() + "/qr?format=png&size=5";
        Picasso.with(getActivity())
                .load(QrLink)
                .error(R.drawable.default_background)
                .into(mQrCode);
        mTicketStatus.setText(mTicket.getOrder().getStatusName());
        mPlace.setText(mEvent.getLocation());
        mDatetime.setText(EventFormatter.formatDate(mEvent.getNearestDate()));
        mTicketType.setText(mTicket.getTicketType().getName());
        long payedAt = mTicket.getOrder().getPayedAt();
        mOrderDatetime.setText(getString(R.string.ticket_date_label) + " " +
                DateFormatter.formatOrderDateTime(DateUtils.date(payedAt)));

        //todo ticket number
        mTicketNumber.setText(getString(R.string.ticket_number_label) + "0000");
        if (mTicket.isCheckedOut()) {
            checkOutImage.setVisibility(View.VISIBLE);
            mQrCode.setAlpha(0.2f);
        }

        return view;
    }

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
        ButterKnife.unbind(this);
    }


    interface OnTicketInteractionListener {
        void onEventClicked(EventRegistered Event);
    }
}
