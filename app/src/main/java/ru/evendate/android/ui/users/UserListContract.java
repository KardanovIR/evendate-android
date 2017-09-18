package ru.evendate.android.ui.users;

import ru.evendate.android.models.UserDetail;
import ru.evendate.android.ui.EndlessContract;

/**
 * Created by dmitry on 11.09.17.
 */

interface UserListContract {
    interface UserListView extends EndlessContract.EndlessView<UserListPresenter, UserDetail> {

        int getEventId();

        int getOrgId();

        UserListFragment.TypeFormat getType();
    }
}
