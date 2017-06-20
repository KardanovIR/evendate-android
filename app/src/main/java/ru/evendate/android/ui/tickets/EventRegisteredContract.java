package ru.evendate.android.ui.tickets;

import java.util.List;

import ru.evendate.android.models.EventRegistered;
import ru.evendate.android.ui.BasePresenter;
import ru.evendate.android.ui.BaseView;

/**
 * Created by Aedirn on 14.03.17.
 */

interface EventRegisteredContract {

    interface View extends BaseView<EventRegisteredContract.Presenter> {
        void setLoadingIndicator(boolean active);

        void showEvents(List<EventRegistered> list, boolean isLast);

        void reshowEvents(List<EventRegistered> list, boolean isLast);

        void showEmptyState();

        void showError();

        boolean isEmpty();
    }

    interface Presenter extends BasePresenter {
        void loadEvents(boolean forceLoad, int page);
    }

    interface OnEventInteractionListener {
        void onEventClick(EventRegistered item);

        void onEventLongClick(EventRegistered item);
    }
}
