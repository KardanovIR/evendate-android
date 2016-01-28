package ru.evendate.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import ru.evendate.android.R;

public class EventDetailActivity extends AppCompatActivity {
    public Uri mUri;
    public boolean isLocal;
    public static final String IS_LOCAL = "is_local";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent intent = getIntent();
        if(intent != null){
            mUri = intent.getData();
            isLocal = intent.getBooleanExtra(IS_LOCAL, false);
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment, new EventDetailFragment());
        fragmentTransaction.commit();

        setContentView(R.layout.activity_detail);

    }
}
