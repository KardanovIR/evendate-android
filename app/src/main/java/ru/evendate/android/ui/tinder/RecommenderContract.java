package ru.evendate.android.ui.tinder;

import java.util.List;

import ru.evendate.android.models.Event;
import ru.evendate.android.ui.BaseAuthView;
import ru.evendate.android.ui.BasePresenter;

/**
 * Created by Aedirn on 31.03.17.
 */

class RecommenderContract {
    static final int PAGE_LENGTH = 4;

    interface View extends BaseAuthView<Presenter> {
        void setLoadingIndicator(boolean active);

        void showRecommends(List<Event> events, boolean isLast);

        void reshowRecommends(List<Event> events, boolean isLast);

        void showEmptyState();

        void showError();

        boolean isEmpty();
    }

    interface Presenter extends BasePresenter {
        void loadRecommends(boolean forceLoad, int page);

        void faveEvent(Event event);

        void unfaveEvent(Event event);

        void hideEvent(Event event);

        void unhideEvent(Event event);
    }
}
