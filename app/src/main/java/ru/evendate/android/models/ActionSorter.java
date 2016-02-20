package ru.evendate.android.models;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Dmitry on 20.02.2016.
 * filter action target by action type
 */
public class ActionSorter {
    //TODo arraylist long
    public static HashMap<Long, ArrayList<Long>> sortActions(ArrayList<Action> actionList){
        HashMap<Long, ArrayList<Long>> map = new HashMap<>();
        for (Action action : actionList) {
            ArrayList<Long> list = map.get(action.getTypeId());
            if(list == null){
                list = new ArrayList<>();
                map.put(action.getTypeId(), list);
            }
            list.add(action.getEventId() != null ? action.getEventId() : action.getOrganizationId());
        }
        return map;
    }
    //TODO
    /**
     * filter actions: remove actions with same target and keep only last action
     */
    /*
    public HashMap<Long, ArrayList<Long>> filterActions(HashMap<Long, ArrayList<Long>>){

    }
    */
}
