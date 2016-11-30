package ru.evendate.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import ru.evendate.android.R;

public class OrganizationDetailActivity extends AppCompatActivity {
    Fragment fragment = new OrganizationDetailFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = new Bundle();
        Intent intent = getIntent();
        if (intent == null)
            throw new RuntimeException("no intent with uri");
        Uri organizationUri = intent.getData();
        args.putString(OrganizationDetailFragment.URI_KEY, organizationUri.toString());

        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment, fragment);
        fragmentTransaction.commit();

        setContentView(R.layout.activity_organization);
    }

    //http://stackoverflow.com/questions/13418436/android-4-2-back-stack-behaviour-with-nested-fragments/14030872#14030872
    @Override
    public void onBackPressed() {
        if (fragment != null && fragment.getChildFragmentManager().getBackStackEntryCount() > 0) {
            fragment.getChildFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
