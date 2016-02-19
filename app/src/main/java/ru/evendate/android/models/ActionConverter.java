package ru.evendate.android.models;

import java.util.ArrayList;
import java.util.HashMap;

import ru.evendate.android.adapters.AggregateDate;

/**
 * Created by ds_gordeev on 19.02.2016.
 */
public class ActionConverter {
    public ArrayList<AggregateDate<Action>> convertActions(ArrayList<Action> actionList){
        HashMap<Long, ArrayList<Action>> map = new HashMap<>();
        for (Action action: actionList) {
            ArrayList<Action> actionMapList = map.get(action.getDate());
            if(actionMapList == null){
                actionMapList = new ArrayList<>();
                map.put(action.getDate(), actionMapList);
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
