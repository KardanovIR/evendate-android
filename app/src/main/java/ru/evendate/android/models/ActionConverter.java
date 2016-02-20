package ru.evendate.android.models;

import java.util.ArrayList;
import java.util.HashMap;

import ru.evendate.android.adapters.AggregateDate;

/**
 * Created by ds_gordeev on 19.02.2016.
 */
public class ActionConverter {
    public static ArrayList<AggregateDate<Action>> convertActions(ArrayList<Action> actionList){
        HashMap<Long, ArrayList<Action>> map = new HashMap<>();
        for (Action action: actionList) {
            //we want one date for one day
            long date = action.getDate() - (action.getDate() % 86400);
            ArrayList<Action> actionMapList = map.get(date);
            if(actionMapList == null){
                actionMapList = new ArrayList<>();
                map.put(date, actionMapList);
            }
            actionMapList.add(action);
        }
        ArrayList<AggregateDate<Action>> actionConvertedList = new ArrayList<>();
        for (Long date: map.keySet()) {
            AggregateDate<Action> aggregateDate = new AggregateDate<>(date);
            aggregateDate.setList(map.get(date));
            actionConvertedList.add(aggregateDate);
        }
        return actionConvertedList;
    }
}
