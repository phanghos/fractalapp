package org.taitascioredev.fractal;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.pnikosis.materialishprogress.ProgressWheel;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;

import org.lucasr.twowayview.widget.DividerItemDecoration;
import org.lucasr.twowayview.widget.TwoWayView;
import org.taitascioredev.adapters.CustomSpinnerAdapter;
import org.taitascioredev.adapters.SubmissionAdapter;

import java.util.regex.Pattern;

/**
 * Created by roberto on 12/05/15.
 */
public class MainFragment extends Fragment {

    private int sorting;

    private SubredditPaginator paginator;
    private SubmissionAdapter adapter;
    private Submission submission;
    private MyApp app;

    private SwipyRefreshLayout refreshWidget;
    private TwoWayView mRecyclerView;
    private AppCompatActivity context;
    private ProgressWheel wheel;
    private TextView empty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = (AppCompatActivity) getActivity();
        app = (MyApp) context.getApplication();
        //context.getSupportActionBar().setTitle("Fractal");
        context.getSupportActionBar().setDisplayShowTitleEnabled(false);

        setHasOptionsMenu(true);

        Toolbar toolbar = (Toolbar) context.findViewById(R.id.toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.scrollToPosition(0);
            }
        });

        NavigationView navView = (NavigationView) context.findViewById(R.id.navigation_view);
        navView.getMenu().findItem(R.id.home).setChecked(true);

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

        mRecyclerView = (TwoWayView) getView().findViewById(R.id.recycler_view);
        String displayStyle = Utils.getDisplayPreference(context);
        if (displayStyle.equals("2")) {
            Drawable divider = getResources().getDrawable(R.drawable.divider_list);
            mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));
        }

        /*
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                submission = adapter.getItem(position);
                new HideSubmissionTask(position).execute();
            }
        };
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);
        */

        refreshWidget = (SwipyRefreshLayout) getView().findViewById(R.id.swipyrefreshlayout);
        refreshWidget.setDirection(SwipyRefreshLayoutDirection.BOTH);
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

        Bundle b = getArguments();
        if (b != null) {
            String url = b.getString("url", null);
            if (url != null) {
                Uri uri = Uri.parse(url);
                if (url.contains("subreddits") || url.endsWith("/r") || url.endsWith("/r/")) {
                    if (app.getClient().getAuthenticationMethod().isUserless())
                        context.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SubredditsFragmentUserless()).commit();
                    else
                        context.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SubredditsFragmentOAuth()).commit();
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
                        context.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SubredditsFragmentUserless()).commit();
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
                        context.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                    }
                }
            }
        }

        wheel = (ProgressWheel) getView().findViewById(R.id.progress_wheel);
        empty = (TextView) getView().findViewById(R.id.tv_empty);

        /*
        final MaterialSection user = context.getSectionByTitle("User");
        if (user != null)
            user.setOnClickListener(new MaterialSectionListener() {
                @Override
                public void onClick(MaterialSection materialSection) {
                    new MaterialDialog.Builder(context)
                            .title("Search user")
                            .input("User", "", false, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                                    SearchUserFragment fragment = new SearchUserFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("user", charSequence.toString());
                                    fragment.setArguments(bundle);
                                    context.setFragmentChild(fragment, "'" + user + "'");
                                }
                            })
                            .positiveText("search")
                            .negativeText("cancel")
                            .show();
                }
            });
        */

        paginator = app.getPaginator();
        if (savedInstanceState == null && app.getSubmissions() == null)
            new GetFrontPageTask().execute(true);
        else if (savedInstanceState == null && app.getSubmissions() != null) {
            spinner.setTag(app.getFrontPageSorting());
            spinner.setSelection(app.getFrontPageSorting(), false);
            adapter = new SubmissionAdapter(context, app.getSubmissions());
            //mRecyclerView.setAdapter(new AlphaInAnimationAdapter(adapter));
            mRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("debug", "onResume MainFragment");
        /*
        if (Utils.refresh) {
            Utils.refresh = false;
            app.setPaginator(null);
            app.setSubmissions(null);
            adapter = null;
            new GetFrontPageTask().execute(true);
        }
        */
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("debug", "onStop MainFragment");
        Utils.update = false;
        //app.setSubredditStream(null);
        //app.setSubreddits(null);
        //app.setUserSubredditsPaginator(null);
        //app.setUserSubreddits(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*
        if (requestCode == Constants.LOGIN_REQUEST) {
            if (resultCode == context.RESULT_OK) {
                app.setPaginator(null);
                app.setSubmissions(null);
                adapter = null;
                MaterialSection subreddits = context.getSectionByTitle("Subreddits");
                if (subreddits != null) {
                    subreddits.setOnClickListener(new MaterialSectionListener() {
                        @Override
                        public void onClick(MaterialSection materialSection) {
                            SubredditsFragmentOAuth fragment = new SubredditsFragmentOAuth();
                            context.setFragment(fragment, "Subreddits");
                        }
                    });
                }
                context.addAccountSection(context.newSection("Logout", new MaterialSectionListener() {
                    @Override
                    public void onClick(MaterialSection materialSection) {
                        Utils.logout(context);
                    }
                }));
                if (context.getSectionByTitle("Inbox") == null) {
                    MaterialSection messages = context.newSection("Inbox", R.drawable.msg, new MessagesFragment());
                    context.addSection(messages);
                }
                new GetFrontPageTask().execute(true);
            }
            else {

            }
        }
        */
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*
            case R.id.random:
                MyApp app = (MyApp) context.getApplication();
                app.setSubredditPaginator(null);
                app.setSubmissionsSubreddit(null);
                SubredditPageFragment fragment = new SubredditPageFragment();
                Bundle bundle = new Bundle();
                bundle.putString("subreddit_url", "random");
                fragment.setArguments(bundle);
                context.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
                return true;
                */
            default:
                return false;
        }
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
            if (adapter == null) {
                wheel.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Listing<Submission> doInBackground(Boolean... params) {
            paginator = app.getPaginator();

            Log.d("debug", "forceNetwork: " + params[0] + "");
            if (paginator == null) {
                paginator = new SubredditPaginator(app.getClient());
                paginator.setSorting(Sorting.HOT);
            }
            return paginator.next(params[0]);
        }

        @Override
        protected void onPostExecute(Listing<Submission> submissions) {
            super.onPostExecute(submissions);
            refreshWidget.setRefreshing(false);
            wheel.setVisibility(View.GONE);
            if (submissions != null) {
                if (adapter == null) {
                    adapter = new SubmissionAdapter(context, submissions);
                    //mRecyclerView.setAdapter(new AlphaInAnimationAdapter(adapter));
                    mRecyclerView.setAdapter(adapter);
                }
                else {
                    if (direction == SwipyRefreshLayoutDirection.TOP) {
                        int count = 0;
                        for (Submission s : submissions)
                            if (!adapter.contains(s)) {
                                adapter.add(count++, s);
                                adapter.notifyItemInserted(0);
                            }
                    }
                    else {
                        for (Submission s : submissions) {
                            adapter.add(s);
                            adapter.notifyItemInserted(adapter.getItemCount() - 1);
                        }
                    }
                }
                app.setPaginator(paginator);
                app.setSubmissions(adapter.getList());
                /*
                switch (sorting) {
                    case 0:
                        app.setFrontPageSorting(Sorting.HOT.ordinal());
                        break;
                    case 1:
                        app.setFrontPageSorting(Sorting.CONTROVERSIAL.ordinal());
                        break;
                    case 2:
                        app.setFrontPageSorting(Sorting.NEW.ordinal());
                        break;
                    case 3:
                        app.setFrontPageSorting(Sorting.TOP.ordinal());
                        break;
                }
                Log.d("debug", paginator.getSorting().name());
                */
                app.setFrontPageSorting(sorting);

                if (submissions.size() == 0)
                    empty.setVisibility(View.VISIBLE);
                else
                    empty.setVisibility(View.GONE);
            }
        }
    }

    private class SortFrontPageTask extends AsyncTask<Sorting, Void, Listing<Submission>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wheel.setVisibility(View.VISIBLE);
        }

        @Override
        protected Listing<Submission> doInBackground(Sorting... params) {
            paginator = new SubredditPaginator(app.getClient());
            paginator.setSorting(params[0]);
            return paginator.next(true);
        }

        @Override
        protected void onPostExecute(Listing<Submission> submissions) {
            super.onPostExecute(submissions);
            wheel.setVisibility(View.GONE);
            if (submissions != null) {
                adapter = new SubmissionAdapter(context, submissions);
                //mRecyclerView.setAdapter(new AlphaInAnimationAdapter(adapter));
                mRecyclerView.setAdapter(adapter);

                app.setPaginator(paginator);
                app.setSubmissions(adapter.getList());
                /*
                switch (sorting) {
                    case 0:
                        app.setFrontPageSorting(Sorting.HOT.ordinal());
                        break;
                    case 1:
                        app.setFrontPageSorting(Sorting.CONTROVERSIAL.ordinal());
                        break;
                    case 2:
                        app.setFrontPageSorting(Sorting.NEW.ordinal());
                        break;
                    case 3:
                        app.setFrontPageSorting(Sorting.TOP.ordinal());
                        break;
                }
                Log.d("debug", paginator.getSorting().name());
                */
                app.setFrontPageSorting(sorting);

                if (submissions.size() == 0)
                    empty.setVisibility(View.VISIBLE);
                else
                    empty.setVisibility(View.GONE);
            }
        }
    }

    private class HideSubmissionTask extends AsyncTask<Void, Void, Boolean> {

        private int position;

        public HideSubmissionTask(int position) {
            this.position = position;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            MyApp app = (MyApp) context.getApplication();
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
                adapter.remove(position);
                adapter.notifyItemRemoved(position);
            }
            else
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }
}
