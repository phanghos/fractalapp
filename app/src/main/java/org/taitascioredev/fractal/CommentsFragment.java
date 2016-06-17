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

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.pnikosis.materialishprogress.ProgressWheel;

import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;

import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.widget.DividerItemDecoration;
import org.lucasr.twowayview.widget.TwoWayView;
import org.taitascioredev.adapters.CommentAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roberto on 19/04/15.
 */
public class CommentsFragment extends Fragment {

    private List<CommentNode> list;
    private List<CommentInfo> infoList;

    private CommentAdapter adapter;
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
        context.getSupportActionBar().setTitle("Comments");
        context.getSupportActionBar().setDisplayShowTitleEnabled(true);

        String id = getArguments().getString("id", null);

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
        final Drawable divider = getResources().getDrawable(R.drawable.divider_comments);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));
        ItemClickSupport clickSupport = ItemClickSupport.addTo(mRecyclerView);
        clickSupport.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView recyclerView, View view, int i, long l) {
            }
        });

        Spinner spinner = (Spinner) context.findViewById(R.id.spinner);
        spinner.setVisibility(View.GONE);

        refreshWidget = (SwipyRefreshLayout) context.findViewById(R.id.swipyrefreshlayout);
        refreshWidget.setDirection(SwipyRefreshLayoutDirection.BOTTOM);
        refreshWidget.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection) {

            }
        });

        wheel = (ProgressWheel) context.findViewById(R.id.progress_wheel);
        empty = (TextView) context.findViewById(R.id.tv_empty);

        new GetCommentsTask().execute(id);
    }

    private void addCommentInfo(CommentNode node, int indentation) {
        CommentInfo info = new CommentInfo();
        info.id = node.getComment().getId();
        info.indentation = indentation;
        infoList.add(info);
        for (CommentNode child : node.getChildren())
            addCommentInfo(child, indentation + 1);
    }

    private class GetCommentsTask extends AsyncTask<String, Void, Submission> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (adapter == null)
                wheel.setVisibility(View.VISIBLE);
        }

        @Override
        protected Submission doInBackground(String... params) {
            return app.getClient().getSubmission(params[0]);
        }

        @Override
        protected void onPostExecute(Submission submission) {
            super.onPostExecute(submission);
            refreshWidget.setRefreshing(false);
            wheel.setVisibility(View.GONE);

            if (submission != null) {
                list = new ArrayList<>();
                infoList = new ArrayList<>();
                CommentNode root = submission.getComments();

                Iterable<CommentNode> iter = root.walkTree();
                for (CommentNode node : iter)
                    list.add(node);

                for (CommentNode child : root.getChildren())
                    addCommentInfo(child, 0);

                adapter = new CommentAdapter(context, list, infoList);
                adapter.setBaseUrl("https://www.reddit.com" + submission.getPermalink());
                //adapter.setRecyclerView(mRecyclerView);
                mAdapter = adapter;
                mRecyclerView.setAdapter(adapter);

                if (submission.getCommentCount() == 0) {
                    empty.setText("There are no comments on this post");
                    empty.setVisibility(View.VISIBLE);
                }
                else {
                    //Log.d("debug", submission.getComments().getImmediateSize() + " - " + submission.getComments().getTotalSize());
                    empty.setVisibility(View.GONE);
                }
            }
        }
    }
}
