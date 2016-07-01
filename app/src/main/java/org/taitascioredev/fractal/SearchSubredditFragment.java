package org.taitascioredev.fractal;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.pnikosis.materialishprogress.ProgressWheel;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.SubredditSearchPaginator;

import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.widget.DividerItemDecoration;
import org.lucasr.twowayview.widget.TwoWayView;
import org.taitascioredev.adapters.SubredditAdapter;

/**
 * Created by roberto on 28/04/15.
 */
public class SearchSubredditFragment extends Fragment {

    private SubredditSearchPaginator paginator;
    private SubredditAdapter adapter;
    private MyApp app;

    private TwoWayView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private SwipyRefreshLayout refreshWidget;
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

        Toolbar toolbar = (Toolbar) context.findViewById(R.id.toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.scrollToPosition(0);
            }
        });

        mRecyclerView = (TwoWayView) getView().findViewById(R.id.recycler_view);
        //mLayoutManager = new LinearLayoutManager(context);
        //mRecyclerView.setLayoutManager(mLayoutManager);
        final Drawable divider = getResources().getDrawable(R.drawable.divider_list);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));
        ItemClickSupport clickSupport = ItemClickSupport.addTo(mRecyclerView);

        clickSupport.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView recyclerView, View view, int position, long l) {
                MyApp app = (MyApp) context.getApplication();
                app.setSubredditPaginator(null);
                app.setSubmissionsSubreddit(null);
                Subreddit subreddit = adapter.getItem(position);
                SubredditPageFragment fragment = new SubredditPageFragment();
                Bundle bundle = new Bundle();
                bundle.putString("subreddit_url", subreddit.getDisplayName());
                fragment.setArguments(bundle);
                context.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
            }
        });

        Spinner spinner = (Spinner) context.findViewById(R.id.spinner);
        spinner.setVisibility(View.GONE);

        Bundle bundle = getArguments();
        final String query = bundle.getString("query");

        context.getSupportActionBar().setTitle("'" + query + "'");
        context.getSupportActionBar().setDisplayShowTitleEnabled(true);

        refreshWidget = (SwipyRefreshLayout) context.findViewById(R.id.swipyrefreshlayout);
        refreshWidget.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection) {
                if (paginator != null) {
                    if (paginator.hasNext())
                        new SearchSubredditsTask().execute(query);
                    else {
                        refreshWidget.setRefreshing(false);
                        Toast.makeText(context.getApplicationContext(), "No more results for this query", Toast.LENGTH_SHORT).show();
                    }
                } else
                    refreshWidget.setRefreshing(false);
            }
        });

        wheel = (ProgressWheel) getView().findViewById(R.id.progress_wheel);
        empty = (TextView) getView().findViewById(R.id.tv_empty);

        if (adapter == null && paginator == null)
            new SearchSubredditsTask().execute(query);
        else {
            mAdapter = adapter;
            mRecyclerView.setAdapter(adapter);
            //mRecyclerView.setAdapter(new ScaleInAnimationAdapter(adapter));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        app.setSubredditPaginator(null);
        app.setSubmissionsSubreddit(null);
    }

    private class SearchSubredditsTask extends AsyncTask<String, Void, Listing<Subreddit>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (adapter == null) {
                wheel.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Listing<Subreddit> doInBackground(String... params) {
            if (paginator == null)
                paginator = new SubredditSearchPaginator(app.getClient(), params[0]);
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
                    mAdapter = adapter;
                    mRecyclerView.setAdapter(adapter);
                }
                else {
                    for (Subreddit s : subreddits) {
                        adapter.add(s);
                        adapter.notifyItemInserted(adapter.getItemCount() - 1);
                    }
                }

                if (subreddits.size() == 0)
                    empty.setVisibility(View.VISIBLE);
                else
                    empty.setVisibility(View.GONE);
            }
        }
    }
}
