package org.taitascioredev.fractal;

import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;

import org.taitascioredev.adapters.CustomSpinnerAdapter;
import org.taitascioredev.adapters.SubmissionAdapter;

import java.util.ArrayList;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;

/**
 * Created by roberto on 12/05/15.
 */
public class MainFragment extends Fragment {

    int sorting;
    ArrayList<Submission> list;
    SubredditPaginator paginator;
    AppCompatActivity context;
    App app;

    @BindView(R.id.list) RecyclerView mRecyclerView;
    SubmissionAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutMngr;

    Toolbar toolbar;
    NavigationView mNavView;
    Spinner sp;

    @BindView(R.id.swipyrefreshlayout) SwipyRefreshLayout mRefreshLayout;
    @BindView(R.id.progress_wheel) ProgressWheel wheel;
    @BindView(R.id.tv_empty) TextView empty;

    MaterialDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
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

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.smoothScrollToPosition(0);
            }
        });

        mNavView.getMenu().findItem(R.id.home).setChecked(true);

        sp.setTag(0);
        sp.setVisibility(View.VISIBLE);
        CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(
                context, android.R.layout.simple_spinner_dropdown_item,
                context.getResources().getStringArray(R.array.front_page));
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
                        new SortFrontPageTask().execute(Sorting.CONTROVERSIAL);
                        break;
                    case 1:
                        new SortFrontPageTask().execute(Sorting.HOT);
                        break;
                    case 2:
                        new SortFrontPageTask().execute(Sorting.NEW);
                        break;
                    case 3:
                        new SortFrontPageTask().execute(Sorting.TOP);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String displayStyle = Utils.getDisplayPreference(context);
        if (displayStyle.equals("4")) {
            Drawable divider = ContextCompat.getDrawable(getActivity(), R.drawable.divider_list);
            mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));
        }

        /*
        if (context.getResources().getBoolean(R.bool.is_landscape))
            mLayoutMngr = new GridLayoutManager(getActivity(), 2);
        else
            mLayoutMngr = new LinearLayoutManager(getActivity());
            */

        mLayoutMngr = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutMngr);

        /*
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                submission = mAdapter.getItem(position);
                new HideSubmissionTask(position).execute();
            }
        };
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);
        */

        mRefreshLayout.setDirection(SwipyRefreshLayoutDirection.BOTH);
        mRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection) {
                if (swipyRefreshLayoutDirection == SwipyRefreshLayoutDirection.TOP)
                    new GetFrontPageTask(swipyRefreshLayoutDirection).execute(true);
                else if (paginator != null && paginator.hasNext())
                    new GetFrontPageTask().execute(false);
                else {
                    mRefreshLayout.setRefreshing(false);
                    Toast.makeText(context.getApplicationContext(), getString(R.string.msg_no_more_posts), Toast.LENGTH_SHORT).show();
                }
            }
        });

        Bundle b = getArguments();
        if (b != null) {
            String url = b.getString("url", null);
            if (url != null) {
                Uri uri = Uri.parse(url);
                if (url.contains("subreddits") || url.endsWith("/r") || url.endsWith("/r/")) {
                    if (app.getClient().getAuthenticationMethod().isUserless())
                        context.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new SubredditsFragmentUserless())
                                .commit();
                    else
                        context.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new SubredditsFragmentOAuth()).commit();
                } else if (url.contains("/comments/")) {
                    String path = uri.getPath();
                    Log.d("debug", path);
                    String regexp = "/r/\\w+/comments/\\w+/\\w+/?";
                    boolean matches = Pattern.matches(regexp, path);
                    if (matches) {
                        String[] words = path.split("/");
                        b = new Bundle();
                        b.putString("id", words[4]);
                        CommentsFragment fragment = new CommentsFragment();
                        fragment.setArguments(b);
                        Log.d("debug", words[4]);
                        context.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new SubredditsFragmentUserless()).commit();
                    }
                } else if (url.contains("/r/")) {
                    String path = uri.getPath();
                    String regexp = "/r/\\w+/?";
                    boolean matches = Pattern.matches(regexp, path);
                    if (matches) {
                        String[] words = path.split("/");
                        b = new Bundle();
                        b.putString("subreddit_url", words[2]);
                        SubredditPageFragment fragment = new SubredditPageFragment();
                        fragment.setArguments(b);
                        Log.d("debug", words[2]);
                        context.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, fragment).commit();
                    }
                }
            }
        }

        paginator = app.getPaginator();
        /*
        if (savedInstanceState != null) {
            list = (ArrayList<Submission>) savedInstanceState.getSerializable("list");
            mAdapter = new SubmissionAdapter(context, list);
            mRecyclerView.setAdapter(new AlphaInAnimationAdapter(mAdapter));
        }
        */
        if (app.getSubmissions() == null)
            new GetFrontPageTask().execute(true);
        else {
            sp.setTag(app.getFrontPageSorting());
            sp.setSelection(app.getFrontPageSorting(), false);
            mAdapter = new SubmissionAdapter(context, app.getSubmissions());
            mRecyclerView.setAdapter(new AlphaInAnimationAdapter(mAdapter));
            //mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putSerializable("list", list);
    }

    private String getSorting() { return paginator.getSorting().name(); }

    private class GetFrontPageTask extends AsyncTask<Boolean, Void, Listing<Submission>> {

        private SwipyRefreshLayoutDirection direction;

        public GetFrontPageTask() {
            direction = SwipyRefreshLayoutDirection.BOTTOM;
        }

        public GetFrontPageTask(SwipyRefreshLayoutDirection direction) {
            this.direction = direction;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mAdapter == null)
                wheel.setVisibility(View.VISIBLE);
        }

        @Override
        protected Listing<Submission> doInBackground(Boolean... params) {
            paginator = app.getPaginator();

            if (paginator == null) {
                paginator = new SubredditPaginator(app.getClient());
                paginator.setSorting(Sorting.HOT);
            }

            try {
                return paginator.next(params[0]);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Listing<Submission> submissions) {
            super.onPostExecute(submissions);
            mRefreshLayout.setRefreshing(false);
            wheel.setVisibility(View.GONE);

            if (submissions != null) {
                if (mAdapter == null) {
                    Log.d("Adapter", "Null");
                    //list = new ArrayList<>();
                    //for (Submission s : submissions) list.add(s);
                    mAdapter = new SubmissionAdapter(context, submissions.getChildren());
                    mRecyclerView.setAdapter(new AlphaInAnimationAdapter(mAdapter));
                    //mRecyclerView.setAdapter(mAdapter);
                }
                else {
                    Log.d("Adapter", "Not null");
                    if (direction == SwipyRefreshLayoutDirection.TOP) {
                        Log.d("Direction", "Top");
                        for (Submission s : submissions)
                            if (!mAdapter.contains(s))
                                mAdapter.add(0, s);
                    }
                    else {
                        Log.d("Direction", "Bottom");
                        for (Submission s : submissions) mAdapter.add(s);
                    }
                }

                app.setPaginator(paginator);
                app.setSubmissions(mAdapter.getList());
                app.setFrontPageSorting(sorting);

                if (submissions.size() == 0) empty.setVisibility(View.VISIBLE);
                else                         empty.setVisibility(View.GONE);
            }
            else
                Utils.showSnackbar(getActivity(), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new GetFrontPageTask(direction).execute(true);
                    }
                });
        }
    }

    private class SortFrontPageTask extends AsyncTask<Sorting, Void, Listing<Submission>> {

        Sorting sort;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wheel.setVisibility(View.VISIBLE);
        }

        @Override
        protected Listing<Submission> doInBackground(Sorting... params) {
            sort= params[0];
            paginator = new SubredditPaginator(app.getClient());
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
                mAdapter = new SubmissionAdapter(context, submissions.getChildren());
                mRecyclerView.setAdapter(new AlphaInAnimationAdapter(mAdapter));
                //mRecyclerView.setAdapter(mAdapter);

                app.setPaginator(paginator);
                app.setSubmissions(mAdapter.getList());
                app.setFrontPageSorting(sorting);

                if (submissions.size() == 0) empty.setVisibility(View.VISIBLE);
                else                         empty.setVisibility(View.GONE);
            }
            else
                Utils.showSnackbar(getActivity(), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new SortFrontPageTask().execute(sort);
                    }
                });
        }
    }

    /*
    private class HideSubmissionTask extends AsyncTask<Void, Void, Boolean> {

        private int position;

        public HideSubmissionTask(int position) {
            this.position = position;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            App app = (App) context.getApplication();
            AccountManager manager = new AccountManager(app.getClient());
            if (submission.isHidden())
                try {
                    manager.hide(submission, false);
                    //Toast.makeText(context, "Post saved", Toast.LENGTH_SHORT).show();
                    return true;
                } catch (ApiException e) {
                    return false;
                }
            else
                try {
                    manager.hide(submission, true);
                    //Toast.makeText(context, "Post unsaved", Toast.LENGTH_SHORT).show();
                    return true;
                } catch (ApiException e) {
                    return false;
                }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                Toast.makeText(context, "Post hidden/unhidden", Toast.LENGTH_SHORT).show();
                mAdapter.remove(position);
                mAdapter.notifyItemRemoved(position);
            }
            else
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }
    */
}
