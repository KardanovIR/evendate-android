package ru.getlect.evendate.evendate;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class OrganizationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = new Bundle();
        Intent intent = getIntent();
        if(intent != null){
            Uri organizationUri = intent.getData();
            args.putString(OrganizationActivityFragment.URI, organizationUri.toString());
        }

        Fragment fragment = new OrganizationActivityFragment();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment, fragment);
        fragmentTransaction.commit();

        setContentView(R.layout.activity_organization);
    }

}
