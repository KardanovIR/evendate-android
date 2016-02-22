package ru.evendate.android.models;

import android.content.Context;

import java.util.ArrayList;

import ru.evendate.android.R;

/**
 * Created by Dmitry on 22.02.2016.
 */
public class ActionType{
    private long type;
    private UserModel mUser;
    private ArrayList<ActionTarget> mTargetList;

    public static ActionType newInstance(long type, UserModel user){
        ActionType actionType = new ActionType();
        actionType.type = type;
        actionType.mUser = user;
        actionType.mTargetList = new ArrayList<>();
        return actionType;
    }

    public long getType() {
        return type;
    }

    public String getTypeName(Context context) {
        if(type == Action.Type.ACTION_DISLIKE.type())
            return context.getString(R.string.action_dislike);
        if(type == Action.Type.ACTION_LIKE.type())
            return context.getString(R.string.action_like);
        if(type == Action.Type.ACTION_SUBSCRIBE.type())
            return context.getString(R.string.action_subscribe);
        if(type == Action.Type.ACTION_UNSUBSCRIBE.type())
            return context.getString(R.string.action_unsubscribe);
        return null;
    }

    public UserModel getUser() {
        return mUser;
    }

    public ArrayList<ActionTarget> getTargetList() {
        return mTargetList;
    }
}
