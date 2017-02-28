package org.taitascioredev.fractal;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.taitascioredev.adapters.GalleryAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by saleventa7 on 7/7/2016.
 */
public class GalleryActivity extends AppCompatActivity {

    ImgurImage[] images;

    @BindView(R.id.list) RecyclerView mRecyclerView;
    GalleryAdapter mAdapter;
    GridLayoutManager mLayoutMngr;

    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);
        setToolbar();

        images = (ImgurImage[]) getIntent().getSerializableExtra("images");

        mLayoutMngr = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutMngr);

        if (savedInstanceState != null)
            images = (ImgurImage[]) savedInstanceState.getSerializable("images");

        mAdapter = new GalleryAdapter(this, images);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("images", images);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            super.onBackPressed();

        return super.onOptionsItemSelected(item);
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Gallery");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
