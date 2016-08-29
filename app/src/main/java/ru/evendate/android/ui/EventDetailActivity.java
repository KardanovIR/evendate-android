package ru.evendate.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.transition.Slide;
import android.view.Gravity;

import ru.evendate.android.R;
import ru.evendate.android.Statistics;

public class EventDetailActivity extends AppCompatActivity {
    public Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        if (intent == null)
            throw new RuntimeException("no intent with uri");
        mUri = intent.getData();
        Statistics.init(this);
        Statistics.sendEventAction(intent, mUri.getLastPathSegment());

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, new EventDetailFragment());
        transaction.commit();


    }
}
