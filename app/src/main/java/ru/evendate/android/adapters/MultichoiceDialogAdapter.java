package ru.evendate.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import ru.evendate.android.R;
import ru.evendate.android.models.EventNotification;

/**
 * Created by mrZizik on 25/05/16.
 */
public class MultichoiceDialogAdapter extends ArrayAdapter<EventNotification> {
        private final Context context;
        private final ArrayList<EventNotification> values;

        public MultichoiceDialogAdapter(Context context, ArrayList<EventNotification> values) {
            super(context, R.layout.item_multichoise, values);
            this.context = context;
            this.values = values;
        }

        static class ViewHolder {
            CheckBox checkBox;
            TextView textView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.item_multichoise, parent, false);
                holder = new ViewHolder();
                holder.checkBox = (CheckBox) rowView.findViewById(R.id.checkBox);
                holder.textView = (TextView) rowView.findViewById(R.id.tv_checkboxtext);
                rowView.setTag(holder);
            } else {
                holder = (ViewHolder) rowView.getTag();
            }
            EventNotification notification = values.get(position);
            if (notification.getEntryId()!=0) {
                holder.checkBox.setVisibility(View.INVISIBLE);
            }
            holder.textView.setText(""+notification.getNotificationTime());


            return rowView;
        }

}
