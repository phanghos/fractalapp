package org.taitascioredev.fractal;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dgreenhalgh.android.simpleitemdecoration.linear.DividerItemDecoration;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.pnikosis.materialishprogress.ProgressWheel;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.SubredditSearchPaginator;

import org.taitascioredev.adapters.SubredditAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by roberto on 28/04/15.
 */
public class SearchSubredditFragment extends Fragment {

    String query;
    ArrayList<Subreddit> list;
    SubredditSearchPaginator paginator;
    AppCompatActivity context;
    App app;

    @BindView(R.id.list) RecyclerView mRecyclerView;
    SubredditAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    Toolbar toolbar;
    Spinner sp;

    @BindView(R.id.swipyrefreshlayout) SwipyRefreshLayout mRefreshLayout;
    @BindView(R.id.progress_wheel) ProgressWheel wheel;
    @BindView(R.id.tv_empty) TextView empty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_subreddits, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = (AppCompatActivity) getActivity();
        app = (App) context.getApplication();

        Bundle extras = getArguments();
        query = extras.getString("query");

        context.getSupportActionBar().setTitle("'" + query + "'");
        context.getSupportActionBar().setDisplayShowTitleEnabled(true);

        toolbar = (Toolbar) context.findViewById(R.id.toolbar);
        sp = (Spinner) context.findViewById(R.id.spinner);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.smoothScrollToPosition(0);
            }
        });

        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        final Drawable divider = ContextCompat.getDrawable(getActivity(), R.drawable.divider_list);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));

        sp.setVisibility(View.GONE);

        mRefreshLayout.setDirection(SwipyRefreshLayoutDirection.BOTTOM);
        mRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection) {
                if (paginator != null) {
                    if (paginator.hasNext())
                        new SearchSubredditsAsync().execute(query);
                    else {
                        mRefreshLayout.setRefreshing(false);
                        Toast.makeText(context.getApplicationContext(), "No more results for this query", Toast.LENGTH_SHORT).show();
                    }
                } else
                    mRefreshLayout.setRefreshing(false);
            }
        });

        if (savedInstanceState != null)
            list = (ArrayList<Subreddit>) savedInstanceState.getSerializable("list");

        paginator = App.searchPaginator;
        if (list == null)
            new SearchSubredditsAsync().execute(query);
        else {
            mAdapter = new SubredditAdapter(context, list);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("list", list);
    }

    @Override
    public void onStop() {
        super.onStop();
        //app.setSubredditPaginator(null);
        //app.setSubmissionsSubreddit(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem itemAbout = menu.findItem(R.id.item_about);
        if (itemAbout != null) itemAbout.setVisible(false);
    }

    private class SearchSubredditsAsync extends AsyncTask<String, Void, Listing<Subreddit>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mAdapter == null) wheel.setVisibility(View.VISIBLE);
        }

        @Override
        protected Listing<Subreddit> doInBackground(String... params) {
            if (paginator == null) paginator = new SubredditSearchPaginator(app.getClient(), params[0]);

            try {
                return paginator.next();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Listing<Subreddit> subreddits) {
            super.onPostExecute(subreddits);
            wheel.setVisibility(View.GONE);
            mRefreshLayout.setRefreshing(false);

            if (subreddits != null) {
                if (mAdapter == null) {
                    Log.d("Adapter", "Null");
                    list = new ArrayList<>();
                    for (Subreddit s : subreddits) list.add(s);
                    mAdapter = new SubredditAdapter(context, list);
                    mRecyclerView.setAdapter(mAdapter);
                }
                else {
                    Log.d("Adapter", "Not null");
                    for (Subreddit s : subreddits) mAdapter.add(s);
                }

                if (subreddits.size() == 0) {
                    empty.setVisibility(View.VISIBLE);
                    mRefreshLayout.setVisibility(View.GONE);
                    mRefreshLayout.setSize(0);
                }
                else
                    empty.setVisibility(View.GONE);

                App.searchPaginator = paginator;
            }
            else
                Utils.showSnackbar(getActivity(), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new SearchSubredditsAsync().execute(query);
                    }
                });
        }
    }
}
