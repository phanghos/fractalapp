package org.taitascioredev.fractal;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.pnikosis.materialishprogress.ProgressWheel;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.SubredditStream;

import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.widget.DividerItemDecoration;
import org.lucasr.twowayview.widget.TwoWayView;
import org.taitascioredev.adapters.CustomSpinnerAdapter;
import org.taitascioredev.adapters.SubredditAdapter;

/**
 * Created by roberto on 13/05/15.
 */
public class SubredditsFragmentUserless extends Fragment {

    private int sorting;

    private SubredditAdapter adapter;
    private Parcelable state;
    private MyApp app;

    private SwipyRefreshLayout refreshWidget;
    private RecyclerView mRecyclerView;
    private SubredditStream paginator;
    private AppCompatActivity context;
    private ProgressWheel wheel;
    private TextView empty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_subreddits, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = (AppCompatActivity) getActivity();
        app = (MyApp) context.getApplication();
        //context.getSupportActionBar().setTitle("Subreddits");
        context.getSupportActionBar().setDisplayShowTitleEnabled(false);
        setHasOptionsMenu(true);

        NavigationView navView = (NavigationView) context.findViewById(R.id.navigation_view);
        navView.getMenu().findItem(R.id.subreeddits).setChecked(true);

        Toolbar toolbar = (Toolbar) context.findViewById(R.id.toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.smoothScrollToPosition(0);
            }
        });

        mRecyclerView = (TwoWayView) getView().findViewById(R.id.recycler_view);
        final Drawable divider = getResources().getDrawable(R.drawable.divider_list);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));

        final Spinner spinner = (Spinner) context.findViewById(R.id.spinner);
        spinner.setTag(0);
        spinner.setVisibility(View.VISIBLE);
        CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(context, android.R.layout.simple_spinner_dropdown_item, context.getResources().getStringArray(R.array.subreddits_userless));
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sorting = position;
                int tag = (int) spinner.getTag();

                if (position == tag)
                    return;

                switch (position) {
                    case 0:
                        new SortSubredditsTask().execute("popular");
                        break;
                    case 1:
                        new SortSubredditsTask().execute("new");
                        break;
                    case 2:
                        new SortSubredditsTask().execute("employee");
                        break;
                    case 3:
                        new SortSubredditsTask().execute("gold");
                        break;
                    case 4:
                        new SortSubredditsTask().execute("default");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        refreshWidget = (SwipyRefreshLayout) context.findViewById(R.id.swipyrefreshlayout);
        refreshWidget.setDirection(SwipyRefreshLayoutDirection.BOTTOM);
        refreshWidget.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection) {
                if (paginator != null) {
                    if (paginator.hasNext())
                        new GetSubredditsTask().execute();
                    else {
                        refreshWidget.setRefreshing(false);
                        Toast.makeText(context.getApplicationContext(), "No more subreddits to load", Toast.LENGTH_SHORT).show();
                    }
                } else
                    refreshWidget.setRefreshing(false);
            }
        });

        wheel = (ProgressWheel) getView().findViewById(R.id.progress_wheel);
        empty = (TextView) getView().findViewById(R.id.tv_empty);

        paginator = app.getSubredditStream();
        if (savedInstanceState == null && app.getSubreddits() == null)
            new GetSubredditsTask().execute();
        else if (savedInstanceState == null && app.getSubreddits() != null) {
            spinner.setTag(app.getSubredditSorting());
            spinner.setSelection(app.getSubredditSorting(), false);
            adapter = new SubredditAdapter(context, app.getSubreddits());
            mRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        final SearchManager searchManager = (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(context.getComponentName()));
        searchView.setQueryHint("Subreddit name");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (s.length() == 0 || s.equals("")) {
                    Toast.makeText(context, "Field cannot be blank", Toast.LENGTH_SHORT).show();
                    return true;
                }
                SearchSubredditFragment fragment = new SearchSubredditFragment();
                Bundle bundle = new Bundle();
                bundle.putString("query", s);
                fragment.setArguments(bundle);
                menu.findItem(R.id.search).collapseActionView();
                context.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    public String getSorting() { return paginator.getSorting().name(); }

    private class GetSubredditsTask extends AsyncTask<Void, Void, Listing<Subreddit>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (adapter == null)
                wheel.setVisibility(View.VISIBLE);
        }

        @Override
        protected Listing<Subreddit> doInBackground(Void... params) {
            paginator = app.getSubredditStream();

            if (paginator == null)
                paginator = new SubredditStream(app.getClient(), "popular");
            return paginator.next();
        }

        @Override
        protected void onPostExecute(Listing<Subreddit> subreddits) {
            super.onPostExecute(subreddits);
            refreshWidget.setRefreshing(false);
            wheel.setVisibility(View.GONE);

            if (subreddits != null) {
                if (adapter == null) {
                    adapter = new SubredditAdapter(context, subreddits);
                    mRecyclerView.setAdapter(adapter);
                }
                else {
                    for (Subreddit s : subreddits) {
                        adapter.add(s);
                        adapter.notifyItemInserted(adapter.getItemCount() - 1);
                    }
                }
                app.setSubredditStream(paginator);
                app.setSubreddits(adapter.getList());
                app.setSubredditSorting(sorting);

                if (subreddits.size() == 0)
                    empty.setVisibility(View.VISIBLE);
                else
                    empty.setVisibility(View.GONE);
            }
        }
    }

    private class SortSubredditsTask extends AsyncTask<String, Void, Listing<Subreddit>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wheel.setVisibility(View.VISIBLE);
        }

        @Override
        protected Listing<Subreddit> doInBackground(String... params) {
            paginator = new SubredditStream(app.getClient(), params[0]);
            return paginator.next(true);
        }

        @Override
        protected void onPostExecute(Listing<Subreddit> subreddits) {
            super.onPostExecute(subreddits);
            wheel.setVisibility(View.GONE);

            if (subreddits != null) {
                adapter = new SubredditAdapter(context, subreddits);
                mRecyclerView.setAdapter(adapter);

                app.setSubredditStream(paginator);
                app.setSubreddits(adapter.getList());
                app.setSubredditSorting(sorting);

                if (subreddits.size() == 0)
                    empty.setVisibility(View.VISIBLE);
                else
                    empty.setVisibility(View.GONE);
            }
        }
    }
}
