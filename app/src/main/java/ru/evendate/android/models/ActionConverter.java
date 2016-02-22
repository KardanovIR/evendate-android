package ru.evendate.android.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import ru.evendate.android.adapters.AggregateDate;

/**
 * Created by ds_gordeev on 19.02.2016.
 */
public class ActionConverter {
    /**
     * sort by date abd convert actions
     */
    public static ArrayList<AggregateDate<ActionType>> convertActions(ArrayList<Action> actionList){
        HashMap<Long, ArrayList<Action>> map = new HashMap<>();
        for (Action action: actionList) {
            //we want one date for one day
            long date = action.getDate() - (action.getDate() % 86400);
            ArrayList<Action> actionTargetList = map.get(date);
            if(actionTargetList == null){
                actionTargetList = new ArrayList<>();
                map.put(date, actionTargetList);
            }
            actionTargetList.add(action);
        }
        ArrayList<AggregateDate<ActionType>> actionConvertedList = new ArrayList<>();
        for (Long date: map.keySet()) {
            AggregateDate<ActionType> aggregateDate = new AggregateDate<>(date);
            aggregateDate.setList(ActionSorter.processActions(map.get(date)));
            if(aggregateDate.getList().size() != 0)
                actionConvertedList.add(aggregateDate);
        }

        Collections.sort(actionConvertedList, Collections.reverseOrder());
        return actionConvertedList;
    }
}
