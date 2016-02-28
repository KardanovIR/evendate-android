package ru.evendate.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import ru.evendate.android.EvendateApplication;
import ru.evendate.android.R;

public class EventDetailActivity extends AppCompatActivity {
    public Uri mUri;
    public static final String INTENT_TYPE = "type";
    public static final String NOTIFICATION = "notification";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent intent = getIntent();
        if(intent != null){
            mUri = intent.getData();
            Bundle intent_extras = getIntent().getExtras();
            if (intent_extras != null && intent_extras.containsKey(INTENT_TYPE) &&
                    intent.getStringExtra(INTENT_TYPE).equals(NOTIFICATION)) {
                Tracker tracker = EvendateApplication.getTracker();
                HitBuilders.EventBuilder event = new HitBuilders.EventBuilder()
                        .setCategory(getString(R.string.stat_category_event))
                        .setAction(getString(R.string.stat_action_notification))
                        .setLabel(mUri.getLastPathSegment());
                tracker.send(event.build());
            }
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment, new EventDetailFragment());
        fragmentTransaction.commit();

        setContentView(R.layout.activity_detail);

    }
}
