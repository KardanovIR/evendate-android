package ru.evendate.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import ru.evendate.android.R;

public class EventDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle args = new Bundle();
        Intent intent = getIntent();
        if (intent == null)
            throw new RuntimeException("no intent with uri");
        Uri eventUri = intent.getData();
        args.putString(OrganizationDetailFragment.URI_KEY, eventUri.toString());

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = new EventDetailFragment();
        fragment.setArguments(args);
        transaction.replace(R.id.fragment, fragment);
        transaction.commit();
    }
}
