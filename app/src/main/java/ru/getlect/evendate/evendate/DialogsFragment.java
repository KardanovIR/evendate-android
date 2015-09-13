package ru.getlect.evendate.evendate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.EntypoModule;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.joanzapata.iconify.fonts.IoniconsModule;
import com.joanzapata.iconify.fonts.MaterialModule;
import com.joanzapata.iconify.fonts.MeteoconsModule;
import com.joanzapata.iconify.fonts.SimpleLineIconsModule;
import com.joanzapata.iconify.fonts.TypiconsModule;
import com.joanzapata.iconify.fonts.WeathericonsModule;
import com.rey.material.app.DatePickerDialog;
import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.SimpleDialog;
import com.rey.material.app.TimePickerDialog;
import com.rey.material.widget.EditText;
import com.rey.material.widget.Switch;

import java.text.SimpleDateFormat;

/**
 * Created by fj on 17.08.2015.
 */
public class DialogsFragment extends Fragment implements View.OnClickListener, Switch.OnCheckedChangeListener {

    LinearLayout ll_location;
    LinearLayout ll_photo;
    LinearLayout notifications_ll;
    LinearLayout ll_start_date;
    LinearLayout ll_end_date;
    LinearLayout ll_description;
    TextView event_start_date;
    TextView event_start_time;
    TextView event_end_date;
    TextView event_end_time;
    TextView tv_notifications;
    TextView tv_description;
    EditText et_desc_input;
    Switch switch_all_day;

    public static DialogsFragment newInstance() {
        DialogsFragment fragment = new DialogsFragment();
        return fragment;
    }

    private AddEventActivity mActivity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.add_event, container, false);

        Iconify
                .with(new FontAwesomeModule())
                .with(new EntypoModule())
                .with(new TypiconsModule())
                .with(new MaterialModule())
                .with(new MeteoconsModule())
                .with(new WeathericonsModule())
                .with(new SimpleLineIconsModule())
                .with(new IoniconsModule());



        ll_location = (LinearLayout) v.findViewById(R.id.ll_location);
        ll_location.setOnClickListener(this);

        ll_photo = (LinearLayout)v.findViewById(R.id.ll_photo);
        ll_photo.setOnClickListener(this);

        notifications_ll = (LinearLayout)v.findViewById(R.id.notifications_ll);
        notifications_ll.setOnClickListener(this);

        event_start_date = (TextView)v.findViewById(R.id.event_start_date);
        event_start_time = (TextView)v.findViewById(R.id.event_start_time);
        event_end_date = (TextView)v.findViewById(R.id.event_end_date);
        event_end_time = (TextView)v.findViewById(R.id.event_end_time);

        et_desc_input = (EditText)v.findViewById(R.id.et_desc_input);

        event_start_date.setOnClickListener(this);
        event_start_time.setOnClickListener(this);
        event_end_date.setOnClickListener(this);
        event_end_time.setOnClickListener(this);

        ll_start_date = (LinearLayout)v.findViewById(R.id.ll_start_date);
        ll_start_date.setOnClickListener(this);

        ll_end_date = (LinearLayout)v.findViewById(R.id.ll_end_date);
        ll_end_date.setOnClickListener(this);

        ll_description = (LinearLayout)v.findViewById(R.id.ll_description);
        ll_description.setOnClickListener(this);

        tv_notifications = (TextView)v.findViewById(R.id.tv_notifications);
        tv_description = (TextView)v.findViewById(R.id.tv_description);

        switch_all_day = (Switch)v.findViewById(R.id.switch_all_day);
        switch_all_day.setOnCheckedChangeListener(this);


        mActivity = (AddEventActivity)getActivity();



        return v;
    }




    @Override
    public void onClick(View v){
        Dialog.Builder builder = null;

        switch (v.getId()) {

            case R.id.event_start_date:
                builder = new DatePickerDialog.Builder(R.style.Evendate_DatePicker) {
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        DatePickerDialog dialog = (DatePickerDialog) fragment.getDialog();
                        String date = dialog.getFormattedDate(SimpleDateFormat.getDateInstance());
                        event_start_date.setText(date);
                        super.onPositiveActionClicked(fragment);
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        super.onNegativeActionClicked(fragment);
                    }
                };

                builder.positiveAction("Ок")
                        .negativeAction("Отмена");

                break;

            case R.id.event_start_time:
                builder = new TimePickerDialog.Builder(R.style.Evendate_TimePicker, 24, 00) {
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        TimePickerDialog dialog = (TimePickerDialog) fragment.getDialog();
                        String time = dialog.getFormattedTime(SimpleDateFormat.getTimeInstance());
                        event_start_time.setText(time);
                        super.onPositiveActionClicked(fragment);
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(mActivity, "Cancelled", Toast.LENGTH_SHORT).show();
                        super.onNegativeActionClicked(fragment);
                    }
                } ;

                builder.positiveAction("Ок")
                        .negativeAction("Отмена");

                break;

            case R.id.event_end_date:
                builder = new DatePickerDialog.Builder(R.style.Evendate_DatePicker) {
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        DatePickerDialog dialog = (DatePickerDialog) fragment.getDialog();
                        String date = dialog.getFormattedDate(SimpleDateFormat.getDateInstance());
                        event_end_date.setText(date);
                        super.onPositiveActionClicked(fragment);
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        super.onNegativeActionClicked(fragment);
                    }
                };

                builder.positiveAction("Ок")
                        .negativeAction("Отмена");

                break;

            case R.id.event_end_time:
                builder = new TimePickerDialog.Builder(R.style.Evendate_TimePicker, 24, 00) {
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        TimePickerDialog dialog = (TimePickerDialog) fragment.getDialog();
                        String time = dialog.getFormattedTime(SimpleDateFormat.getTimeInstance());
                        event_end_time.setText(time);
                        super.onPositiveActionClicked(fragment);
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(mActivity, "Cancelled", Toast.LENGTH_SHORT).show();
                        super.onNegativeActionClicked(fragment);
                    }
                } ;

                builder.positiveAction("Ок")
                        .negativeAction("Отмена");

                break;

            case R.id.notifications_ll:
                builder = new SimpleDialog.Builder(R.style.SimpleDialogLight) {
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        CharSequence[] values =  getSelectedValues();

                        super.onPositiveActionClicked(fragment);
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        super.onNegativeActionClicked(fragment);
                    }
                };

                ((SimpleDialog.Builder)builder).multiChoiceItems(
                        new String[]{"За 2 часа", "За день", "За три дня", "За неделю"} )
                        .title("Выберите напоминания")
                       // .contentView(R.layout.layout_event_notifications)
                        .positiveAction("Ок")
                        .negativeAction("Отмена");

                break;

            case R.id.ll_description:

                builder = new SimpleDialog.Builder(R.style.SimpleDialogLight) {
                    @Override
                    protected void onBuildDone(Dialog dialog) {
                        dialog.layoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    }

                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        com.rey.material.widget.EditText et_description = (com.rey.material.widget.EditText)fragment.getDialog().findViewById(R.id.et_description);
                        String description = et_description.getText().toString();
                        tv_description.setTextColor(getResources().getColor(R.color.text_color_black));
                        tv_description.setText(description);
                        super.onPositiveActionClicked(fragment);
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        super.onNegativeActionClicked(fragment);
                    }
                };

                builder.title("Описание мероприятия")
                        .positiveAction("Ок")
                        .negativeAction("Отмена")
                        .contentView(R.layout.layout_event_description);



                break;

            case R.id.ll_location:
                Intent intent_map_fragment = new Intent(getActivity(), MapsActivity.class);
                startActivity(intent_map_fragment);
                break;

            case R.id.ll_photo:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), 1);
                break;




        }

        DialogFragment fragment = DialogFragment.newInstance(builder);
        fragment.show(getFragmentManager(),null);



    }


    @Override
    public void onCheckedChanged(Switch aSwitch, boolean b) {
        if(b==true){
            switch_all_day.applyStyle(R.style.Evendate_Switch_On);
            event_start_time.setVisibility(View.GONE);
            event_end_time.setVisibility(View.GONE);
        }
        if(b==false) {
            switch_all_day.applyStyle(R.style.Evendate_Switch_Off);
            event_start_time.setVisibility(View.VISIBLE);
            event_end_time.setVisibility(View.VISIBLE);
        }
    }
}
