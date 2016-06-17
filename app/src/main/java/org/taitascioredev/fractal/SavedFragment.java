package org.taitascioredev.fractal;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.pnikosis.materialishprogress.ProgressWheel;

import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.UserContributionPaginator;

import org.lucasr.twowayview.widget.DividerItemDecoration;
import org.lucasr.twowayview.widget.TwoWayView;
import org.taitascioredev.adapters.SavedAdapter;

/**
 * Created by roberto on 14/11/15.
 */
public class SavedFragment extends Fragment {

    private UserContributionPaginator paginator;
    private SavedAdapter adapter;
    private MyApp app;
    private Submission submission;

    private TwoWayView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private SwipyRefreshLayout refreshWidget;
    private AppCompatActivity context;
    private ProgressWheel wheel;
    private TextView empty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        context = (AppCompatActivity) getActivity();
        app = (MyApp) context.getApplication();
        context.getSupportActionBar().setTitle("Saved");
        context.getSupportActionBar().setDisplayShowTitleEnabled(true);

        Toolbar toolbar = (Toolbar) context.findViewById(R.id.toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.scrollToPosition(0);
            }
        });

        NavigationView navView = (NavigationView) context.findViewById(R.id.navigation_view);
        navView.getMenu().findItem(R.id.hidden).setChecked(true);

        Spinner spinner = (Spinner) context.findViewById(R.id.spinner);
        spinner.setVisibility(View.GONE);

        mRecyclerView = (TwoWayView) getView().findViewById(R.id.recycler_view);
        //mLayoutManager = new LinearLayoutManager(context);
        //mRecyclerView.setLayoutManager(mLayoutManager);
        Drawable divider = getResources().getDrawable(R.drawable.divider_card);
        String displayStyle = Utils.getDisplayPreference(context);
        if (displayStyle.equals("2"))
            divider = getResources().getDrawable(R.drawable.divider_list);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));

        refreshWidget = (SwipyRefreshLayout) getView().findViewById(R.id.swipyrefreshlayout);
        //refreshWidget.setDirection(SwipyRefreshLayoutDirection.BOTH);
        /*
        refreshWidget.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection) {
                if (swipyRefreshLayoutDirection == SwipyRefreshLayoutDirection.TOP)
                    new GetFrontPageTask(swipyRefreshLayoutDirection).execute(true);
                else if (paginator != null && paginator.hasNext())
                    new GetFrontPageTask().execute(false);
                else {
                    refreshWidget.setRefreshing(false);
                    Toast.makeText(context.getApplicationContext(), "No more posts to load", Toast.LENGTH_SHORT).show();
                }
            }
        });
        */

        wheel = (ProgressWheel) getView().findViewById(R.id.progress_wheel);
        empty = (TextView) getView().findViewById(R.id.tv_empty);

        /*
        paginator = app.getHiddenPaginator();
        if (savedInstanceState == null && app.getSubmissions() == null)
            new GetHiddenTask().execute();
        if (savedInstanceState == null && app.getSubmissions() != null) {
            adapter = new SubmissionAdapter(context, app.getSubmissions());
            mAdapter = adapter;
            //mRecyclerView.setAdapter(new AlphaInAnimationAdapter(adapter));
            mRecyclerView.setAdapter(adapter);
        }
        */

        if (savedInstanceState == null && adapter == null)
            new GetSavedTask().execute();
        if (savedInstanceState == null && adapter != null) {
            //adapter = new SubmissionAdapter(context, app.getSubmissions());
            //mAdapter = adapter;
            //mRecyclerView.setAdapter(new AlphaInAnimationAdapter(adapter));
            mRecyclerView.setAdapter(adapter);
        }

        //if (Utils.username != null)
        //new GetSavedTask().execute();
    }

    private class GetSavedTask extends AsyncTask<Void, Void, Listing<Contribution>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (adapter == null)
                wheel.setVisibility(View.VISIBLE);
        }

        @Override
        protected Listing<Contribution> doInBackground(Void... params) {
            paginator = app.getHiddenPaginator();

            if (paginator == null)
                paginator = new UserContributionPaginator(app.getClient(), "saved", Utils.username);
            return paginator.next();
        }

        @Override
        protected void onPostExecute(Listing<Contribution> contributions) {
            super.onPostExecute(contributions);
            refreshWidget.setRefreshing(false);
            wheel.setVisibility(View.GONE);

            if (contributions != null) {
                /*
                for (Contribution c : contributions)
                    if (!(c instanceof Submission))
                        contributions.remove(c);
                        */
                
                if (adapter == null) {
                    adapter = new SavedAdapter(context, contributions);
                    mAdapter = adapter;
                    mRecyclerView.setAdapter(adapter);

                    if (contributions.size() == 0) {
                        empty.setVisibility(View.VISIBLE);
                        //Toast.makeText(context, "Nothing to show", Toast.LENGTH_SHORT).show();
                        //context.onBackPressed();
                    }
                    else
                        empty.setVisibility(View.GONE);
                }
            }
        }
    }
}
