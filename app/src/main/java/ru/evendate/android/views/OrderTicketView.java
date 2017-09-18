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
    private TextView mTicketTypeView;
    private TextView mTicketPrice;
    private TextView mTicketTotalSum;
    private NumberPicker mNumberPicker;
    private TicketType mTicketType;
    private TextView mTicketComment;
    private Formatter mFormatter;

    private float ticketTotalSum = 0;
    @Nullable private OnTotalSumChangedListener mListener;

    public OrderTicketView(@NonNull Context context) {
        this(context, null);
    }

    public OrderTicketView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);

        LayoutInflater.from(context).inflate(R.layout.item_order_ticket, this, true);
        ViewGroup viewGroup = (ViewGroup)getChildAt(0);
        mTicketTypeView = viewGroup.findViewById(R.id.ticket_type);
        mTicketPrice = viewGroup.findViewById(R.id.ticket_price);
        mTicketTotalSum = viewGroup.findViewById(R.id.ticket_total_sum);
        mNumberPicker = viewGroup.findViewById(R.id.number_picker);
        mTicketComment = viewGroup.findViewById(R.id.ticket_comment);
    }

    public void setFormatter(@NonNull Formatter formatter) {
        mFormatter = formatter;
    }

    public void setTicketType(TicketType ticketType) {
        mTicketType = ticketType;

        mTicketTypeView.setText(mTicketType.getName());
        if (ticketType.getComment() != null) {
            mTicketComment.setText(ticketType.getComment());
            mTicketComment.setVisibility(VISIBLE);
        }
        mNumberPicker.setMin(mTicketType.getMinCountPerUser());
        mNumberPicker.setMax(mTicketType.getMaxCountPerUser());

        if (mFormatter != null) {
            mTicketPrice.setText(mFormatter.formatNumber(mTicketType.getPrice()));
        } else {
            mTicketPrice.setText(String.valueOf(mTicketType.getPrice()));
        }
        mNumberPicker.setValueChangedListener((int value, ActionEnum action) -> {
            setTicketTotalSum(value);
            if (mListener != null) {
                mListener.totalSumChanged();
            }
        });

        setTicketTotalSum(mTicketType.getMinCountPerUser());
    }

    private void setTicketTotalSum(int num) {
        ticketTotalSum = mTicketType.getPrice() * num;
        if (ticketTotalSum == 0 && num == 0) {
            mTicketTotalSum.setVisibility(INVISIBLE);
        } else {
            mTicketTotalSum.setVisibility(VISIBLE);
        }
        if (mFormatter != null) {
            mTicketTotalSum.setText(mFormatter.formatNumber(ticketTotalSum));
        } else {
            mTicketTotalSum.setText(String.valueOf(ticketTotalSum));
        }
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

    public interface Formatter {
        String formatNumber(float cost);
    }
}
