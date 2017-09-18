package ru.evendate.android.ui.users;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;

import io.reactivex.Observable;
import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.User;
import ru.evendate.android.models.UserDetail;
import ru.evendate.android.ui.AbstractAdapter;
import ru.evendate.android.ui.EndlessListFragment;
import ru.evendate.android.ui.userdetail.UserProfileActivity;

public class UserListFragment extends EndlessListFragment<UserListPresenter, UserDetail, UsersAdapter.UserHolder>
        implements UserListContract.UserListView, UsersAdapter.UsersInteractionListener {
    private String LOG_TAG = UserListFragment.class.getSimpleName();
    public static final String TYPE = "type";
    public static final String EVENT_ID = "event_id";
    public static final String ORGANIZATION_ID = "organization_id";
    private int type = 0;
    private int organizationId;
    private int eventId;

    public enum TypeFormat {
        EVENT(0),
        ORGANIZATION(1),
        FRIENDS(2);

        final int type;

        TypeFormat(int nativeInt) {
            this.type = nativeInt;
        }

        static public TypeFormat getType(int pType) {
            for (TypeFormat type : TypeFormat.values()) {
                if (type.type() == pType) {
                    return type;
                }
            }
            throw new RuntimeException("unknown type");
        }

        public int type() {
            return type;
        }
    }

    public static UserListFragment newInstance(int type, int id) {
        UserListFragment userListFragment = new UserListFragment();
        userListFragment.type = type;
        if (type == TypeFormat.EVENT.type) {
            userListFragment.eventId = id;
        } else {
            userListFragment.organizationId = id;
        }
        return userListFragment;
    }

    public static UserListFragment newFriendsInstance(int type) {
        UserListFragment userListFragment = new UserListFragment();
        userListFragment.type = type;
        return userListFragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(TYPE, type);
        outState.putInt(EVENT_ID, eventId);
        outState.putInt(ORGANIZATION_ID, organizationId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            type = savedInstanceState.getInt(TYPE);
            eventId = savedInstanceState.getInt(EVENT_ID);
            organizationId = savedInstanceState.getInt(ORGANIZATION_ID);
        }
    }

    @Override
    protected AbstractAdapter<UserDetail, UsersAdapter.UserHolder> getAdapter() {
        return new UsersAdapter(getActivity(), this);
    }

    public void onReload() {
        onRefresh();
    }

    @Override
    protected String getEmptyHeader() {
        return getString(R.string.list_users_empty_text);
    }

    @Override
    protected String getEmptyDescription() {
        return null;
    }

    @Override
    //todo
    public Observable<String> requestAuth() {
        return null;
    }

    @Override
    public int getEventId() {
        return eventId;
    }

    @Override
    public int getOrgId() {
        return organizationId;
    }

    @Override
    public TypeFormat getType() {
        return TypeFormat.getType(type);
    }

    @Override
    public void openUser(User user) {
        Intent intent = new Intent(getContext(), UserProfileActivity.class);
        intent.setData(EvendateContract.UserEntry.getContentUri(user.getEntryId()));
        if (Build.VERSION.SDK_INT >= 21) {
            getContext().startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
        } else
            getContext().startActivity(intent);
    }
}
