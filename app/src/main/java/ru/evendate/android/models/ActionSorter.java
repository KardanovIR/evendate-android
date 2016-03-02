package ru.evendate.android.models;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Dmitry on 20.02.2016.
 * filter action target by action type
 */
public class ActionSorter {
    public static ArrayList<ActionType> processActions(ArrayList<Action> actionList){
        return filterActions(sortActions(convertActions(actionList)));
    }

    /**
     * convert actions to action type structure exclude unnecessary types
     */
    public static ArrayList<ActionType> convertActions(ArrayList<Action> actionList){
        HashMap<Long, ActionType> actionTypes = new HashMap<>();
        for (Action action : actionList) {
            if(!isApprovedType(action.getTypeId()))
                continue;
            ActionType type;
            if(!actionTypes.keySet().contains(action.getTypeId())){
                type = ActionType.newInstance(action.getTypeId(), action.getUser());
                actionTypes.put(action.getTypeId(), type);
            }
            else {
                type = actionTypes.get(action.getTypeId());
            }
            type.getTargetList().add(action);
        }
        return new ArrayList<>(actionTypes.values());
    }
    /**
     * filter actions: remove actions with same target and keep only last action
     */
    public static ArrayList<ActionType> filterActions(ArrayList<ActionType> actionTypes){
        HashSet<Uri> set = new HashSet<>();
        ArrayList<ActionType> removingTypes = new ArrayList<>();
        for (ActionType type: actionTypes) {
            ArrayList<ActionTarget> removingTargets = new ArrayList<>();
            for (ActionTarget action: type.getTargetList()) {
                if(set.contains(action.getTargetUri()))
                    removingTargets.add(action);
                else
                    set.add(action.getTargetUri());
            }
            type.getTargetList().removeAll(removingTargets);
            if(type.getTargetList().size() == 0)
                removingTypes.add(type);
        }
        actionTypes.removeAll(removingTypes);
        return actionTypes;
    }

    /**
     * sort actions for one type
     */
    public static ArrayList<ActionType> sortActions(ArrayList<ActionType> actionTypes){
        for (ActionType type: actionTypes)
            Collections.sort(type.getTargetList(), Collections.reverseOrder());
        return actionTypes;
    }
    public static boolean isApprovedType(long type){
        return (type == Action.Type.ACTION_DISLIKE.type() ||
                type == Action.Type.ACTION_LIKE.type() ||
                type == Action.Type.ACTION_SUBSCRIBE.type() ||
                type == Action.Type.ACTION_UNSUBSCRIBE.type());
    }
}
