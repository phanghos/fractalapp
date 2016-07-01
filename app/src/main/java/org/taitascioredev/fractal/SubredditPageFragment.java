package org.taitascioredev.fractal;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;

import org.lucasr.twowayview.widget.DividerItemDecoration;
import org.lucasr.twowayview.widget.TwoWayView;
import org.taitascioredev.adapters.CustomSpinnerAdapter;
import org.taitascioredev.adapters.SubmissionAdapter;

import java.util.List;

/**
 * Created by roberto on 25/04/15.
 */
public class SubredditPageFragment extends Fragment {

    private int sorting;
    private String url;
    private Listing<Submission> list;
    private SubmissionAdapter adapter;
    private MyApp app;

    private SwipyRefreshLayout refreshWidget;
    private TwoWayView mRecyclerView;
    private SubredditPaginator paginator;
    private AppCompatActivity context;
    private ProgressWheel wheel;
    private TextView empty;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = (AppCompatActivity) getActivity();
        app = (MyApp) context.getApplication();
        Bundle bundle = getArguments();
        url = bundle.getString("subreddit_url");
        context.getSupportActionBar().setTitle(url);
        context.getSupportActionBar().setDisplayShowTitleEnabled(true);
        setHasOptionsMenu(true);

        Toolbar toolbar = (Toolbar) context.findViewById(R.id.toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.scrollToPosition(0);
            }
        });

        final Spinner spinner = (Spinner) context.findViewById(R.id.spinner);
        spinner.setTag(0);
        spinner.setVisibility(View.VISIBLE);
        CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(context, android.R.layout.simple_spinner_dropdown_item, context.getResources().getStringArray(R.array.front_page));
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
                        new SortSubmissionsTask().execute(Sorting.HOT);
                        break;
                    case 1:
                        new SortSubmissionsTask().execute(Sorting.CONTROVERSIAL);
                        break;
                    case 2:
                        new SortSubmissionsTask().execute(Sorting.NEW);
                        break;
                    case 3:
                        new SortSubmissionsTask().execute(Sorting.TOP);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mRecyclerView = (TwoWayView) getView().findViewById(R.id.recycler_view);
        Drawable divider = getResources().getDrawable(R.drawable.divider_card);
        String displayStyle = Utils.getDisplayPreference(context);
        if (displayStyle.equals("2"))
            divider = getResources().getDrawable(R.drawable.divider_list);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));

        refreshWidget = (SwipyRefreshLayout) context.findViewById(R.id.swipyrefreshlayout);
        refreshWidget.setDirection(SwipyRefreshLayoutDirection.BOTTOM);
        refreshWidget.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection) {
                if (paginator != null) {
                    if (paginator.hasNext())
                        new GetSubmissionsTask().execute(url);
                    else {
                        refreshWidget.setRefreshing(false);
                        Toast.makeText(context.getApplicationContext(), "No more posts to load", Toast.LENGTH_SHORT).show();
                    }
                } else
                    refreshWidget.setRefreshing(false);
            }
        });

        wheel = (ProgressWheel) getView().findViewById(R.id.progress_wheel);
        empty = (TextView) getView().findViewById(R.id.tv_empty);

        /*
        paginator = app.getSubredditPaginator();
        if (savedInstanceState == null && app.getSubmissionsSubreddit() == null)
            new GetSubmissionsTask().execute(url);
        else if (savedInstanceState == null && app.getSubmissionsSubreddit() != null) {
            spinner.setTag(app.getSubredditPageSorting());
            spinner.setSelection(app.getSubredditPageSorting(), false);
            adapter = new SubmissionAdapter(context, app.getSubmissionsSubreddit());
            //mRecyclerView.setAdapter(new AlphaInAnimationAdapter(adapter));
            mRecyclerView.setAdapter(adapter);
        }
        */

        if (list == null)
            new GetSubmissionsTask().execute(url);
        else {
            spinner.setTag(sorting);
            spinner.setSelection(sorting, false);
            adapter = new SubmissionAdapter(context, list);
            //mRecyclerView.setAdapter(new AlphaInAnimationAdapter(adapter));
            mRecyclerView.setAdapter(adapter);
        }
    }

    /*
    @Override
    public void onDestroy() {
        super.onDestroy();
        app.setSubredditPaginator(null);
        app.setSubmissionsSubreddit(null);
    }
    */

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

    private String getSorting() { return paginator.getSorting().name(); }

    private class GetSubmissionsTask extends AsyncTask<String, Void, Listing<Submission>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (adapter == null)
                wheel.setVisibility(View.VISIBLE);
        }

        @Override
        protected Listing<Submission> doInBackground(String... params) {
            paginator = app.getSubredditPaginator();

            if (paginator == null)
                paginator = new SubredditPaginator(app.getClient(), params[0]);
            return paginator.next();
        }

        @Override
        protected void onPostExecute(Listing<Submission> submissions) {
            super.onPostExecute(submissions);
            refreshWidget.setRefreshing(false);
            wheel.setVisibility(View.GONE);

            if (submissions != null) {
                if (adapter == null) {
                    list = submissions;
                    adapter = new SubmissionAdapter(context, submissions);
                    //mRecyclerView.setAdapter(new AlphaInAnimationAdapter(adapter));
                    mRecyclerView.setAdapter(adapter);
                }
                else {
                    for (Submission s : submissions) {
                        list.add(s);
                        adapter.add(s);
                        adapter.notifyItemInserted(adapter.getItemCount() - 1);
                    }
                }


                //app.setSubredditPaginator(paginator);
                //app.setSubmissionsSubreddit(adapter.getList());
                //app.setSubredditPageSorting(sorting);

                if (submissions.size() == 0)
                    empty.setVisibility(View.VISIBLE);
                else {
                    empty.setVisibility(View.GONE);
                    //context.getSupportActionBar().setTitle(submissions.get(0).getSubredditName());
                }
            }
        }
    }

    private class SortSubmissionsTask extends AsyncTask<Sorting, Void, Listing<Submission>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wheel.setVisibility(View.VISIBLE);
        }

        @Override
        protected Listing<Submission> doInBackground(Sorting... params) {
            paginator = new SubredditPaginator(app.getClient(), url);
            paginator.setSorting(params[0]);
            return paginator.next(true);
        }

        @Override
        protected void onPostExecute(Listing<Submission> submissions) {
            super.onPostExecute(submissions);
            wheel.setVisibility(View.GONE);

            if (submissions != null) {
                list = submissions;
                adapter = new SubmissionAdapter(context, submissions);
                //mRecyclerView.setAdapter(new AlphaInAnimationAdapter(adapter));
                mRecyclerView.setAdapter(adapter);

                //app.setSubredditPaginator(paginator);
                //app.setSubmissionsSubreddit(adapter.getList());
                //app.setSubredditPageSorting(sorting);

                if (submissions.size() == 0)
                    empty.setVisibility(View.VISIBLE);
                else
                    empty.setVisibility(View.GONE);
            }
        }
    }
}
