package org.taitascioredev.fractal;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
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
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dgreenhalgh.android.simpleitemdecoration.linear.DividerItemDecoration;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.pnikosis.materialishprogress.ProgressWheel;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Message;
import net.dean.jraw.paginators.InboxPaginator;

import org.taitascioredev.adapters.CustomSpinnerAdapter;
import org.taitascioredev.adapters.InboxAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by roberto on 25/04/15.
 */
public class InboxFragment extends Fragment {

    ArrayList<Message> list;
    InboxPaginator paginator;
    AppCompatActivity context;
    App app;
    ActionMode actionMode;

    @BindView(R.id.list) RecyclerView mRecyclerView;
    InboxAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    Toolbar toolbar;
    NavigationView mNavView;
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
        //context.getSupportActionBar().setTitle("Inbox");
        context.getSupportActionBar().setDisplayShowTitleEnabled(false);
        setHasOptionsMenu(true);

        toolbar = (Toolbar) context.findViewById(R.id.toolbar);
        mNavView = (NavigationView) context.findViewById(R.id.navigation_view);
        sp = (Spinner) context.findViewById(R.id.spinner);

        mNavView.getMenu().findItem(R.id.inbox).setChecked(true);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.smoothScrollToPosition(0);
            }
        });

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        final Drawable divider = ContextCompat.getDrawable(getActivity(), R.drawable.divider_list);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));

        /*
        final ItemSelectionSupport selectionSupport = ItemSelectionSupport.addTo(mRecyclerView);

        ItemClickSupport clickSupport = ItemClickSupport.addTo(mRecyclerView);
        clickSupport.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView recyclerView, View view, int position, long l) {
                if (actionMode != null) {
                    if (selectionSupport.getCheckedItemCount() == 0) {
                        selectionSupport.clearChoices();
                        actionMode.finish();
                        actionMode = null;
                        return;
                    }
                    updateTitle(selectionSupport.getCheckedItemCount());
                    return;
                }
                App app = (App) context.getApplication();
                Message msg = app.getMessages().get(position);
                app.setMessage(msg);
                MessageContentFragment fragment = new MessageContentFragment();
                context.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
            }
        });

        clickSupport.setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(RecyclerView recyclerView, View view, int position, long l) {
                if (actionMode != null)
                    return false;
                selectionSupport.setChoiceMode(ItemSelectionSupport.ChoiceMode.MULTIPLE);
                actionMode = context.startSupportActionMode(new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        MenuInflater inflater = mode.getMenuInflater();
                        inflater.inflate(R.menu.cab_menu_messages, menu);
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        return false;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                        actionMode = null;
                        selectionSupport.clearChoices();
                        selectionSupport.setChoiceMode(ItemSelectionSupport.ChoiceMode.NONE);
                    }
                });
                selectionSupport.setItemChecked(position, true);
                updateTitle(selectionSupport.getCheckedItemCount());
                return true;
            }
        });
        */

        sp.setVisibility(View.VISIBLE);
        CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(context, android.R.layout.simple_spinner_dropdown_item, context.getResources().getStringArray(R.array.inbox));
        sp.setAdapter(spinnerAdapter);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean loaded = false;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                app.setInboxSorting(position);
                if (loaded) {
                    switch (position) {
                        case 0:
                            new SortMessagesTask().execute("inbox");
                            break;
                        case 1:
                            new SortMessagesTask().execute("unread");
                            break;
                        case 2:
                            new SortMessagesTask().execute("messages");
                            break;
                        case 3:
                            new SortMessagesTask().execute("sent");
                            break;
                        case 4:
                            new SortMessagesTask().execute("moderator");
                            break;
                        case 5:
                            new SortMessagesTask().execute("moderator/unread");
                            break;
                        case 6:
                            new SortMessagesTask().execute("mentions");
                            break;
                    }
                }
                loaded = true;
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
                        new GetMessagesTask().execute();
                    else {
                        mRefreshLayout.setRefreshing(false);
                        Toast.makeText(context.getApplicationContext(), "No more messages to load", Toast.LENGTH_SHORT).show();
                    }
                } else
                    mRefreshLayout.setRefreshing(false);
            }
        });

        paginator = app.getInboxPaginator();
        if (app.getMessages() == null)
            new GetMessagesTask().execute();
        else {
            sp.setSelection(app.getInboxSorting());
            mAdapter = new InboxAdapter(context, app.getMessages());
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.menu_messages, menu);
        MenuItem item = menu.findItem(R.id.item_about);
        if (item != null) item.setVisible(false);
    }

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_compose:
                //context.setFragment(new ComposePmFragment(), "New message");
                return true;
            default:
                return false;
        }
    }
    */

    @Override
    public void onStop() {
        super.onStop();
        if (actionMode != null) {
            actionMode.finish();
            actionMode = null;
        }
    }

    private String getSorting() { return paginator.getWhere(); }

    private void updateTitle(int n) {
        actionMode.setTitle(n + " selected");
    }

    private class GetMessagesTask extends AsyncTask<Void, Void, Listing<Message>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mAdapter == null) wheel.setVisibility(View.VISIBLE);
        }

        @Override
        protected Listing<Message> doInBackground(Void... params) {
            if (paginator == null) paginator = new InboxPaginator(app.getClient(), "inbox");

            try {
                return paginator.next();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Listing<Message> messages) {
            super.onPostExecute(messages);
            wheel.setVisibility(View.GONE);
            mRefreshLayout.setRefreshing(false);

            if (messages != null) {
                if (mAdapter == null) {
                    Log.d("Adapter", "Null");
                    list = new ArrayList<>();
                    for (Message m : messages) list.add(m);
                    mAdapter = new InboxAdapter(context, messages);
                    mRecyclerView.setAdapter(mAdapter);
                }
                else {
                    Log.d("Adapter", "Not null");
                    for (Message m : messages) mAdapter.add(m);
                }

                app.setInboxPaginator(paginator);
                app.setMessages(messages);

                if (messages.size() > 0) empty.setVisibility(View.GONE);
                else                     empty.setVisibility(View.VISIBLE);
            }
            else
                Utils.showSnackbar(getActivity(), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new GetMessagesTask().execute();
                    }
                });
        }
    }

    private class SortMessagesTask extends AsyncTask<String, Void, Listing<Message>> {

        String sort;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wheel.setVisibility(View.VISIBLE);
        }

        @Override
        protected Listing<Message> doInBackground(String... params) {
            sort = params[0];
            paginator = new InboxPaginator(app.getClient(), sort);

            try {
                return paginator.next(true);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Listing<Message> messages) {
            super.onPostExecute(messages);
            wheel.setVisibility(View.GONE);

            if (messages != null) {
                list = new ArrayList<>();
                for (Message m : messages) list.add(m);
                mAdapter = new InboxAdapter(context, messages);
                mRecyclerView.setAdapter(mAdapter);

                app.setInboxPaginator(paginator);
                app.setMessages(mAdapter.getList());

                if (messages.size() > 0) empty.setVisibility(View.GONE);
                else                     empty.setVisibility(View.VISIBLE);
            }
            else
                Utils.showSnackbar(getActivity(), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new SortMessagesTask().execute(sort);
                    }
                });
        }
    }
}
