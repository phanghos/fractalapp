package org.taitascioredev.fractal;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.dgreenhalgh.android.simpleitemdecoration.linear.DividerItemDecoration;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.pnikosis.materialishprogress.ProgressWheel;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;

import org.taitascioredev.adapters.CustomSpinnerAdapter;
import org.taitascioredev.adapters.SubmissionAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;

/**
 * Created by roberto on 25/04/15.
 */
public class SubredditPageFragment extends Fragment {

    int sorting;
    String url;
    ArrayList<Submission> list;
    SubredditPaginator paginator;
    AppCompatActivity context;
    App app;

    @BindView(R.id.list) RecyclerView mRecyclerView;
    SubmissionAdapter mAdapter;
    LinearLayoutManager mLayoutMngr;

    Toolbar toolbar;
    Spinner sp;

    @BindView(R.id.swipyrefreshlayout) SwipyRefreshLayout mRefreshLayout;
    @BindView(R.id.progress_wheel) ProgressWheel wheel;
    @BindView(R.id.tv_empty) TextView empty;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = (AppCompatActivity) getActivity();
        app = (App) context.getApplication();
        Bundle bundle = getArguments();
        url = bundle.getString("subreddit_url");
        context.getSupportActionBar().setTitle(url);
        context.getSupportActionBar().setDisplayShowTitleEnabled(true);

        toolbar = (Toolbar) context.findViewById(R.id.toolbar);
        sp = (Spinner) context.findViewById(R.id.spinner);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.smoothScrollToPosition(0);
            }
        });

        sp.setTag(0);
        sp.setVisibility(View.VISIBLE);
        CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(context, android.R.layout.simple_spinner_dropdown_item, context.getResources().getStringArray(R.array.front_page));
        sp.setAdapter(spinnerAdapter);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sorting = position;
                int tag = (int) sp.getTag();

                if (position == tag) return;

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

        mLayoutMngr = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutMngr);
        Drawable divider = ContextCompat.getDrawable(getActivity(), R.drawable.divider_card);

        String displayStyle = Utils.getDisplayPreference(context);
        if (displayStyle.equals("4")) divider = getResources().getDrawable(R.drawable.divider_list);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));

        mRefreshLayout.setDirection(SwipyRefreshLayoutDirection.BOTTOM);
        mRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection) {
                if (paginator != null) {
                    if (paginator.hasNext())
                        new GetSubmissionsTask().execute(url);
                    else {
                        mRefreshLayout.setRefreshing(false);
                        Toast.makeText(context.getApplicationContext(), "No more posts to load", Toast.LENGTH_SHORT).show();
                    }
                } else
                    mRefreshLayout.setRefreshing(false);
            }
        });

        paginator = app.getSubredditPaginator();
        if (savedInstanceState != null) {
            list = (ArrayList<Submission>) savedInstanceState.getSerializable("list");
            sorting = savedInstanceState.getInt("sorting");
            //sp.setTag(app.getSubredditPageSorting());
            //sp.setSelection(app.getSubredditPageSorting(), false);
            //mRecyclerView.setAdapter(mAdapter);
        }

        if (list != null) {
            sp.setTag(sorting);
            sp.setSelection(sorting, false);
            mAdapter = new SubmissionAdapter(context, list);
            mRecyclerView.setAdapter(new AlphaInAnimationAdapter(mAdapter));
            mAdapter = new SubmissionAdapter(context, list);
            mRecyclerView.setAdapter(new AlphaInAnimationAdapter(mAdapter));
        }
        else
            new GetSubmissionsTask().execute(url);
        /*
        else if (app.getSubmissionsSubreddit() == null)
            new GetSubmissionsTask().execute(url);
        else {
            sp.setTag(sorting);
            sp.setSelection(sorting, false);
            mAdapter = new SubmissionAdapter(context, app.getSubmissionsSubreddit());
            mRecyclerView.setAdapter(new AlphaInAnimationAdapter(mAdapter));
            //mRecyclerView.setAdapter(mAdapter);
        }
        */
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("list", list);
        outState.putInt("sorting", sorting);
    }

    private String getSorting() { return paginator.getSorting().name(); }

    private class GetSubmissionsTask extends AsyncTask<String, Void, Listing<Submission>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mAdapter == null) wheel.setVisibility(View.VISIBLE);
        }

        @Override
        protected Listing<Submission> doInBackground(String... params) {
            if (paginator == null) paginator = new SubredditPaginator(app.getClient(), params[0]);

            try {
                return paginator.next();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Listing<Submission> submissions) {
            super.onPostExecute(submissions);
            wheel.setVisibility(View.GONE);
            mRefreshLayout.setRefreshing(false);

            if (submissions != null) {
                if (mAdapter == null) {
                    Log.d("Adapter", "Null");
                    list = new ArrayList<>();
                    for (Submission s : submissions) list.add(s);
                    mAdapter = new SubmissionAdapter(context, list);
                    mRecyclerView.setAdapter(new AlphaInAnimationAdapter(mAdapter));
                    //mRecyclerView.setAdapter(mAdapter);
                }
                else {
                    Log.d("Adapter", "Not null");
                    for (Submission s : submissions) mAdapter.add(s);
                }

                app.setSubredditPaginator(paginator);
                //app.setSubmissionsSubreddit(mAdapter.getList());
                //app.setSubredditPageSorting(sorting);

                if (submissions.size() == 0) empty.setVisibility(View.VISIBLE);
                else                         empty.setVisibility(View.GONE);
            }
            else
                Utils.showSnackbar(getActivity(), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new GetSubmissionsTask().execute();
                    }
                });
        }
    }

    private class SortSubmissionsTask extends AsyncTask<Sorting, Void, Listing<Submission>> {

        Sorting sort;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wheel.setVisibility(View.VISIBLE);
        }

        @Override
        protected Listing<Submission> doInBackground(Sorting... params) {
            sort = params[0];
            paginator = new SubredditPaginator(app.getClient(), url);
            paginator.setSorting(sort);

            try {
                return paginator.next(true);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Listing<Submission> submissions) {
            super.onPostExecute(submissions);
            wheel.setVisibility(View.GONE);

            if (submissions != null) {
                list = new ArrayList<>();
                for (Submission s : submissions) list.add(s);
                mAdapter = new SubmissionAdapter(context, list);
                mRecyclerView.setAdapter(new AlphaInAnimationAdapter(mAdapter));
                //mRecyclerView.setAdapter(mAdapter);

                app.setSubredditPaginator(paginator);
                //app.setSubmissionsSubreddit(mAdapter.getList());
                //app.setSubredditPageSorting(sorting);

                if (submissions.size() == 0) empty.setVisibility(View.VISIBLE);
                else                         empty.setVisibility(View.GONE);
            }
            else
                Utils.showSnackbar(getActivity(), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new SortSubmissionsTask().execute(sort);
                    }
                });
        }
    }
}
