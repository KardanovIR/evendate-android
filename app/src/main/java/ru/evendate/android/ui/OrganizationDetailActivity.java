package ru.evendate.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import ru.evendate.android.R;

public class OrganizationDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = new Bundle();
        Intent intent = getIntent();
        if (intent == null)
            throw new RuntimeException("no intent with uri");
        Uri organizationUri = intent.getData();
        args.putString(OrganizationDetailFragment.URI, organizationUri.toString());

        Fragment fragment = new OrganizationDetailFragment();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment, fragment);
        fragmentTransaction.commit();

        setContentView(R.layout.activity_organization);
    }
}
