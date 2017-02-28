package org.taitascioredev.fractal;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dgreenhalgh.android.simpleitemdecoration.linear.DividerItemDecoration;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.pnikosis.materialishprogress.ProgressWheel;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import org.taitascioredev.adapters.CustomSpinnerAdapter;
import org.taitascioredev.adapters.SubredditAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by roberto on 17/05/15.
 */
public class SubredditsFragmentOAuth extends Fragment{

    int sorting;
    UserSubredditsPaginator paginator;
    AppCompatActivity context;
    App app;

    @BindView(R.id.list) RecyclerView mRecyclerView;
    SubredditAdapter adapter;
    LinearLayoutManager mLayoutMngr;

    Toolbar toolbar;
    NavigationView mNavView;
    Spinner sp;

    @BindView(R.id.swipyrefreshlayout) SwipyRefreshLayout mRefreshLayout;
    @BindView(R.id.progress_wheel) ProgressWheel wheel;
    @BindView(R.id.tv_empty) TextView empty;

    MaterialDialog progressDialog;

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
        context.getSupportActionBar().setDisplayShowTitleEnabled(false);
        setHasOptionsMenu(true);

        toolbar = (Toolbar) context.findViewById(R.id.toolbar);
        mNavView = (NavigationView) context.findViewById(R.id.navigation_view);
        sp = (Spinner) context.findViewById(R.id.spinner);

        mNavView.getMenu().findItem(R.id.subreeddits).setChecked(true);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.smoothScrollToPosition(0);
            }
        });

        mLayoutMngr = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutMngr);
        final Drawable divider = ContextCompat.getDrawable(getActivity(), R.drawable.divider_list);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));

        sp.setTag(0);
        sp.setVisibility(View.VISIBLE);
        CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(context, android.R.layout.simple_spinner_dropdown_item, context.getResources().getStringArray(R.array.subreddits_oauth));
        sp.setAdapter(spinnerAdapter);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sorting = position;
                int tag = (int) sp.getTag();

                if (position == tag)
                    return;

                switch (position) {
                    case 0:
                        new SortSubredditsTask().execute("subscriber");
                        break;
                    case 1:
                        new SortSubredditsTask().execute("contributor");
                        break;
                    case 2:
                        new SortSubredditsTask().execute("moderator");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mRefreshLayout.setDirection(SwipyRefreshLayoutDirection.BOTTOM);
        mRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection) {
                if (paginator != null) {
                    if (paginator.hasNext())
                        new GetSubredditsTask().execute();
                    else {
                        mRefreshLayout.setRefreshing(false);
                        Toast.makeText(context.getApplicationContext(), "No more subreddits to load", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mRefreshLayout.setRefreshing(false);
                }
            }
        });

        paginator = app.getUserSubredditsPaginator();
        if (app.getUserSubreddits() == null)
            new GetSubredditsTask().execute();
        else {
            sp.setTag(app.getSubredditSorting());
            sp.setSelection(app.getSubredditSorting(), false);
            adapter = new SubredditAdapter(context, app.getUserSubreddits());
            mRecyclerView.setAdapter(adapter);
        }
    }

    public String getSorting() { return paginator.getWhere(); }

    private class GetSubredditsTask extends AsyncTask<Void, Void, Listing<Subreddit>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (adapter == null) wheel.setVisibility(View.VISIBLE);
        }

        @Override
        protected Listing<Subreddit> doInBackground(Void... params) {
            paginator = app.getUserSubredditsPaginator();
            if (paginator == null) paginator = new UserSubredditsPaginator(app.getClient(), "subscriber");

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
                if (adapter == null) {
                    Log.d("Adapter", "Null");
                    adapter = new SubredditAdapter(context, subreddits);
                    mRecyclerView.setAdapter(adapter);
                }
                else {
                    Log.d("Adapter", "Not null");
                    for (Subreddit s : subreddits) adapter.add(s);
                }

                app.setUserSubredditsPaginator(paginator);
                app.setUserSubreddits(adapter.getList());
                app.setSubredditSorting(sorting);


                if (subreddits.size() == 0) empty.setVisibility(View.VISIBLE);
                else                        empty.setVisibility(View.GONE);
            }
            else
                Utils.showSnackbar(getActivity(), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new GetSubredditsTask().execute();
                    }
                });
        }
    }

    private class SortSubredditsTask extends AsyncTask<String, Void, Listing<Subreddit>> {

        String sort;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wheel.setVisibility(View.VISIBLE);
        }

        @Override
        protected Listing<Subreddit> doInBackground(String... params) {
            sort = params[0];
            paginator = new UserSubredditsPaginator(app.getClient(), sort);

            try {
                return paginator.next(true);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Listing<Subreddit> subreddits) {
            super.onPostExecute(subreddits);
            wheel.setVisibility(View.GONE);

            if (subreddits != null) {
                adapter = new SubredditAdapter(context, subreddits);
                mRecyclerView.setAdapter(adapter);

                app.setUserSubredditsPaginator(paginator);
                app.setUserSubreddits(adapter.getList());
                app.setSubredditSorting(sorting);

                if (subreddits.size() == 0) empty.setVisibility(View.VISIBLE);
                else                        empty.setVisibility(View.GONE);
            }
            else
                Utils.showSnackbar(getActivity(), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new SortSubredditsTask().execute(sort);
                    }
                });
        }
    }
}
