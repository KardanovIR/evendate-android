package ru.evendate.android.ui.checkin;

import android.support.v4.view.ViewPager;

import java.util.List;

import ru.evendate.android.models.EventRegistered;
import ru.evendate.android.models.Ticket;
import ru.evendate.android.models.TicketType;
import ru.evendate.android.models.User;
import ru.evendate.android.network.ServiceUtils;
import ru.evendate.android.ui.BaseAuthView;
import ru.evendate.android.ui.BasePresenter;

/**
 * Created by Aedirn on 14.03.17.
 */

public interface CheckInContract {
    interface EventInteractionListener {
        void onEventSelected(EventRegistered event);
    }

    interface TicketPagerTabInitializator {
        void initTabs(ViewPager viewPager);
    }

    interface TicketInteractionListener {
        void onTicketSelected(int eventId, String ticketUuid);
    }

    interface SearchClickListener {
        void searchAction();
    }

    interface ConfirmInteractionListener {
        void onConfirmCheckIn();

        void onConfirmCheckInRevert();

        void onConfirmCheckInCancel();

        void onConfirmCheckInError();
    }

    interface QRReadListener {
        void onQrRead(int eventId, String ticketUuid);

        void onQrReadError();
    }

    interface EventAdminView extends BaseAuthView<EventAdminPresenter> {
        void setLoadingIndicator(boolean active);

        void showList(List<EventRegistered> list, boolean isLast);

        void reshowList(List<EventRegistered> list, boolean isLast);

        void showEmptyState();

        void showError();

        boolean isEmpty();
    }

    interface EventAdminPresenter extends BasePresenter {
        void reload();

        void load(boolean forceLoad, int page);
    }

    interface TicketsAdminView extends BaseAuthView<TicketAdminPresenter> {
        void setLoadingIndicator(boolean active);

        void showList(List<TicketAdmin> list, boolean isLast);

        void reshowList(List<TicketAdmin> list, boolean isLast);

        void showEmptyState();

        void showSearchEmptyState();

        void showError();

        boolean isEmpty();
    }

    interface TicketAdminPresenter extends BasePresenter {

        void reload(int eventId, boolean isCheckOut);

        void load(int eventId, boolean isCheckOut, boolean forceLoad, int page);

        void reload(int eventId, String query);

        void load(int eventId, String query, boolean forceLoad, int page);
    }

    interface TicketConfirmView extends BaseAuthView<TicketConfirmPresenter> {
        void showConfirm();

        void showConfirmRevert();

        void showConfirmError();

        void showTicketLoading(boolean active);

        void showTicketLoadingError();

        void showTicket(TicketAdmin ticket);

        void showConfirmLoading(boolean active);
    }

    interface TicketConfirmPresenter extends BasePresenter {
        void confirm(String ticketUuid, int eventId, boolean checkout);

        void loadTicket(String ticketUuid, int eventId);
    }

    interface TicketAdmin {
        String params = Ticket.TicketParams.get(new String[]{Ticket.TicketParams.USER + ServiceUtils.encloseFields(User.FIELDS_LIST),
                Ticket.TicketParams.TICKET_TYPE});

        String getUuid();

        boolean isCheckout();

        User getUser();

        String getNumber();

        TicketType getTicketType();
    }
}
