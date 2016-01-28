package ru.evendate.android.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import ru.evendate.android.R;
import ru.evendate.android.sync.LocalDataFetcher;
@Deprecated
public class OrganizationCatalogActivity extends AppCompatActivity {
    private android.support.v7.widget.RecyclerView mRecyclerView;
    private OrganizationCategoryAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_catalog);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back_white);

        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);

        mAdapter = new OrganizationCategoryAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        LocalDataFetcher localDataFetcher = new LocalDataFetcher(getContentResolver(), this);
        mAdapter.setCategoryList(localDataFetcher.getOrganizationCategoriesDataFromDB());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
