package ru.evendate.android.ui;

import java.util.List;

/**
 * Created by dmitry on 12.09.17.
 */

public interface EndlessContract {
    interface EndlessView<T extends BasePresenter, D> extends BaseAuthView<T> {
        void setLoadingIndicator(boolean active);

        void showList(List<D> list, boolean isLast);

        void reshowList(List<D> list, boolean isLast);

        void showEmptyState();

        void showError();

        boolean isEmpty();
    }

    interface EndlessPresenter extends BasePresenter {
        void reload();

        void load(boolean forceLoad, int page);
    }

}