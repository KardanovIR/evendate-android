package ru.evendate.android.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.travijuu.numberpicker.library.Enums.ActionEnum;
import com.travijuu.numberpicker.library.NumberPicker;

import ru.evendate.android.R;
import ru.evendate.android.models.TicketOrder;
import ru.evendate.android.models.TicketType;

/**
 * Created by dmitry on 23.08.17.
 */

public class OrderTicketView extends FrameLayout {
    TextView mTicketTypeView;
    TextView mTicketPrice;
    TextView mTicketTotalSum;
    NumberPicker mNumberPicker;
    TicketType mTicketType;

    float ticketTotalSum = 0;
    @Nullable OnTotalSumChangedListener mListener;

    public OrderTicketView(@NonNull Context context) {
        this(context, null);
    }

    public OrderTicketView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);

        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.item_order_ticket, this, true);
        ViewGroup viewGroup = (ViewGroup)getChildAt(0);
        mTicketTypeView = viewGroup.findViewById(R.id.ticket_type);
        mTicketPrice = viewGroup.findViewById(R.id.ticket_price);
        mTicketTotalSum = viewGroup.findViewById(R.id.ticket_total_sum);
        mNumberPicker = viewGroup.findViewById(R.id.number_picker);
    }

    public void setTicketType(TicketType ticketType) {
        mTicketType = ticketType;

        mTicketTypeView.setText(mTicketType.getName());
        //todo format
        mTicketPrice.setText(mTicketType.getPrice() + "");
        mNumberPicker.setMin(mTicketType.getMinCountPerUser());
        mNumberPicker.setMax(mTicketType.getMaxCountPerUser());

        mNumberPicker.setValueChangedListener((int value, ActionEnum action) -> {
            if (value == 0) {
                mTicketTotalSum.setVisibility(INVISIBLE);
            } else {
                mTicketTotalSum.setVisibility(VISIBLE);
            }
            ticketTotalSum = mTicketType.getPrice() * value;
            mTicketTotalSum.setText(ticketTotalSum + "");
            if (mListener != null) {
                mListener.totalSumChanged();
            }
        });

        ticketTotalSum = mTicketType.getPrice() * mTicketType.getMinCountPerUser();
        mTicketTotalSum.setText(ticketTotalSum + "");
    }

    public float getTicketTotalSum() {
        return ticketTotalSum;
    }

    public TicketOrder getTicket() {
        return new TicketOrder(mTicketType.getUuid(), mNumberPicker.getValue());
    }

    public int getNumber() {
        return mNumberPicker.getValue();
    }

    public void setOnTicketTotalSumChangedListener(@Nullable OnTotalSumChangedListener listener) {
        mListener = listener;
    }

    public interface OnTotalSumChangedListener {
        void totalSumChanged();
    }
}
