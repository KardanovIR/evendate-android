package ru.evendate.android.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import ru.evendate.android.adapters.AggregateDate;

/**
 * Created by ds_gordeev on 19.02.2016.
 */
public class ActionConverter {
    /**
     * sort by date actions
     */
    public static ArrayList<AggregateDate<ActionType>> convertActions(ArrayList<Action> actionList) {
        HashMap<Date, ArrayList<Action>> map = new HashMap<>();
        for (Action action : actionList) {
            //we want one date for one day
            Date date = new Date(action.getDate().getTime() - (action.getDate().getTime() % (86400 * 1000)));
            ArrayList<Action> actionTargetList = map.get(date);
            if (actionTargetList == null) {
                actionTargetList = new ArrayList<>();
                map.put(date, actionTargetList);
            }
            actionTargetList.add(action);
        }
        ArrayList<AggregateDate<ActionType>> actionConvertedList = new ArrayList<>();
        for (Date date : map.keySet()) {
            AggregateDate<ActionType> aggregateDate = new AggregateDate<>(date);
            aggregateDate.setList(ActionSorter.processActions(map.get(date)));
            if (aggregateDate.getList().size() != 0)
                actionConvertedList.add(aggregateDate);
        }

        Collections.sort(actionConvertedList, Collections.reverseOrder());
        return actionConvertedList;
    }
}
