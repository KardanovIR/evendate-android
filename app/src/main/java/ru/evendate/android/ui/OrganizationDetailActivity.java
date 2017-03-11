package ru.evendate.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import ru.evendate.android.R;

public class OrganizationDetailActivity extends BaseActivity {
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
}
