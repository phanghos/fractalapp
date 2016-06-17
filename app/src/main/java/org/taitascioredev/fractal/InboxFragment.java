package org.taitascioredev.fractal;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Message;
import net.dean.jraw.paginators.InboxPaginator;

import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.ItemSelectionSupport;
import org.lucasr.twowayview.widget.DividerItemDecoration;
import org.lucasr.twowayview.widget.TwoWayView;
import org.taitascioredev.adapters.CustomSpinnerAdapter;
import org.taitascioredev.adapters.InboxAdapter;

/**
 * Created by roberto on 25/04/15.
 */
public class InboxFragment extends Fragment {


    private InboxPaginator paginator;
    private InboxAdapter adapter;
    private MyApp app;
    private ActionMode actionMode;

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
        context.getSupportActionBar().setTitle("Inbox");
        context.getSupportActionBar().setDisplayShowTitleEnabled(true);
        setHasOptionsMenu(true);

        NavigationView navView = (NavigationView) context.findViewById(R.id.navigation_view);
        navView.getMenu().findItem(R.id.inbox).setChecked(true);

        Toolbar toolbar = (Toolbar) context.findViewById(R.id.toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.scrollToPosition(0);
            }
        });

        mRecyclerView = (TwoWayView) getView().findViewById(R.id.recycler_view);
        final Drawable divider = getResources().getDrawable(R.drawable.divider_list);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));

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
                MyApp app = (MyApp) context.getApplication();
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

        Spinner spinner = (Spinner) context.findViewById(R.id.spinner);
        spinner.setVisibility(View.VISIBLE);
        CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(context, android.R.layout.simple_spinner_dropdown_item, context.getResources().getStringArray(R.array.inbox));
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        refreshWidget = (SwipyRefreshLayout) getView().findViewById(R.id.swipyrefreshlayout);
        refreshWidget.setDirection(SwipyRefreshLayoutDirection.BOTTOM);
        refreshWidget.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection) {
                if (paginator != null) {
                    if (paginator.hasNext())
                        new GetMessagesTask().execute();
                    else {
                        refreshWidget.setRefreshing(false);
                        Toast.makeText(context.getApplicationContext(), "No more messages to load", Toast.LENGTH_SHORT).show();
                    }
                } else
                    refreshWidget.setRefreshing(false);
            }
        });

        wheel = (ProgressWheel) getView().findViewById(R.id.progress_wheel);
        empty = (TextView) getView().findViewById(R.id.tv_empty);

        paginator = app.getInboxPaginator();
        if (savedInstanceState == null && app.getMessages() == null)
            new GetMessagesTask().execute();
        else if (savedInstanceState == null && app.getMessages() != null) {
            spinner.setSelection(app.getInboxSorting());
            adapter = new InboxAdapter(context, app.getMessages());
            mRecyclerView.setAdapter(adapter);
        }
    }

    /*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_messages, menu);
    }
    */

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
            if (adapter == null) {
                ProgressWheel wheel = (ProgressWheel) getView().findViewById(R.id.progress_wheel);
                wheel.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Listing<Message> doInBackground(Void... params) {
            paginator = app.getInboxPaginator();

            if (paginator == null)
                paginator = new InboxPaginator(app.getClient(), "inbox");
            return paginator.next();
        }

        @Override
        protected void onPostExecute(Listing<Message> messages) {
            super.onPostExecute(messages);
            refreshWidget.setRefreshing(false);
            wheel.setVisibility(View.GONE);

            if (messages != null) {
                if (adapter == null) {
                    adapter = new InboxAdapter(context, messages);
                    mAdapter = adapter;
                    mRecyclerView.setAdapter(adapter);
                }
                else {
                    for (Message m : messages) {
                        adapter.add(m);
                        adapter.notifyItemInserted(adapter.getItemCount() - 1);
                    }
                }

                app.setInboxPaginator(paginator);
                app.setMessages(messages);

                if (messages.size() > 0)
                    empty.setVisibility(View.GONE);
                else
                    empty.setVisibility(View.VISIBLE);
            }
        }
    }

    private class SortMessagesTask extends AsyncTask<String, Void, Listing<Message>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wheel.setVisibility(View.VISIBLE);
        }

        @Override
        protected Listing<Message> doInBackground(String... params) {
            paginator = new InboxPaginator(app.getClient(), params[0]);
            return paginator.next(true);
        }

        @Override
        protected void onPostExecute(Listing<Message> messages) {
            super.onPostExecute(messages);
            wheel.setVisibility(View.GONE);

            if (messages != null) {
                adapter = new InboxAdapter(context, messages);
                mAdapter = adapter;
                mRecyclerView.setAdapter(adapter);

                app.setInboxPaginator(paginator);
                app.setMessages(adapter.getList());

                if (messages.size() > 0)
                    empty.setVisibility(View.GONE);
                else
                    empty.setVisibility(View.VISIBLE);

            }
        }
    }
}
