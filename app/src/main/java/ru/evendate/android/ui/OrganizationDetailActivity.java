package ru.evendate.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import ru.evendate.android.EvendateApplication;
import ru.evendate.android.R;

public class OrganizationDetailActivity extends AppCompatActivity {
    public static final String INTENT_TYPE = "type";
    public static final String NOTIFICATION = "notification";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = new Bundle();
        Intent intent = getIntent();
        if (intent != null) {
            Uri organizationUri = intent.getData();
            args.putString(OrganizationDetailFragment.URI, organizationUri.toString());
            Bundle intent_extras = getIntent().getExtras();
            Tracker tracker = EvendateApplication.getTracker();

            HitBuilders.EventBuilder event = new HitBuilders.EventBuilder()
                    .setCategory(getString(R.string.stat_category_organization))
                    .setLabel(organizationUri.getLastPathSegment());
            if (intent_extras != null && intent_extras.containsKey(INTENT_TYPE) &&
                    intent.getStringExtra(INTENT_TYPE).equals(NOTIFICATION)) {
                event.setAction(getString(R.string.stat_action_notification));
            } else {
                event.setAction(getString(R.string.stat_action_view));
            }
            tracker.send(event.build());
        }

        Fragment fragment = new OrganizationDetailFragment();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment, fragment);
        fragmentTransaction.commit();

        setContentView(R.layout.activity_organization);
    }

}
