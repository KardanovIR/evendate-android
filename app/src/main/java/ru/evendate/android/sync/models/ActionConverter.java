package ru.evendate.android.sync.models;

import java.util.ArrayList;
import java.util.HashMap;

import ru.evendate.android.adapters.AgregateDate;

/**
 * Created by ds_gordeev on 19.02.2016.
 */
public class ActionConverter {
    public ArrayList<AgregateDate<Action>> convertActions(ArrayList<Action> actionList){
        HashMap<Long, ArrayList<Action>> map = new HashMap<>();
        for (Action action: actionList) {
            ArrayList<Action> actionMapList = map.get(action.getDate());
            if(actionMapList == null){
                actionMapList = new ArrayList<>();
                map.put(action.getDate(), actionMapList);
            }
            actionMapList.add(action);
        }
        ArrayList<AgregateDate<Action>> actionConvertedList = new ArrayList<>();
        for (Long date: map.keySet()) {
            AgregateDate<Action> agregateDate = new AgregateDate<>(date);
            agregateDate.setList(map.get(date));
            actionConvertedList.add(agregateDate);
        }
        return actionConvertedList;
    }
}
