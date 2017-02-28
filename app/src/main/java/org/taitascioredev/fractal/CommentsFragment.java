package org.taitascioredev.fractal;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.Spinner;
import android.widget.TextView;

import com.dgreenhalgh.android.simpleitemdecoration.linear.DividerItemDecoration;
import com.google.common.collect.FluentIterable;
import com.pnikosis.materialishprogress.ProgressWheel;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;

import org.taitascioredev.adapters.CommentAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by roberto on 19/04/15.
 */
public class CommentsFragment extends Fragment {

    boolean delete = true;
    String id;
    int pos;
    ArrayList<Comment> list;
    ArrayList<CommentInfo> listInfo;
    Submission sub;

    AppCompatActivity context;
    App app;

    @BindView(R.id.list) RecyclerView mRecyclerView;
    CommentAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutMngr;

    Toolbar toolbar;
    Spinner sp;

    @BindView(R.id.progress_wheel) ProgressWheel wheel;
    @BindView(R.id.tv_empty) TextView empty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_comments, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = (AppCompatActivity) getActivity();
        app = (App) context.getApplication();
        context.getSupportActionBar().setTitle("Comments");
        context.getSupportActionBar().setDisplayShowTitleEnabled(true);
        setHasOptionsMenu(true);

        Bundle extras = getArguments();
        if (extras != null) {
            id  = getArguments().getString("id", null);
            pos = getArguments().getInt("pos");
        }

        toolbar = (Toolbar) context.findViewById(R.id.toolbar);
        sp = (Spinner) context.findViewById(R.id.spinner);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.scrollToPosition(0);
            }
        });

        mLayoutMngr = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutMngr);
        final Drawable divider = getResources().getDrawable(R.drawable.divider_comments);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));

        sp.setVisibility(View.GONE);

        //list = Utils.list;
        //listInfo = Utils.listInfo;
        //sub = Utils.sub;

        if (savedInstanceState != null) {
            list = (ArrayList<Comment>) savedInstanceState.getSerializable("list");
            listInfo = (ArrayList<CommentInfo>) savedInstanceState.getSerializable("list_info");
            sub = (Submission) savedInstanceState.getSerializable("submission");
        }

        //if (list == null || listInfo == null || sub == null)
        if (list == null)
            new GetCommentsAsync().execute(id);
        else {
            mAdapter = new CommentAdapter(context, list, listInfo, sub);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("onSaveInstanceState", "Entro");
        outState.putSerializable("list", list);
        outState.putSerializable("list_info", listInfo);
        outState.putSerializable("submission", sub);
        //delete = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy", "Entro");
        /*
        if (delete) {
            Utils.list = null;
            Utils.listInfo = null;
            Utils.sub = null;
        }
        */
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem itemAbout  = menu.findItem(R.id.item_about);
        MenuItem itemSearch = menu.findItem(R.id.search);

        if (itemAbout != null)  itemAbout.setVisible(false);
        if (itemSearch != null) itemSearch.setVisible(false);
    }

    private void addCommentInfo(CommentNode node, int indentation) {
        CommentInfo info = new CommentInfo();
        info.id = node.getComment().getId();
        info.indentation = indentation;
        listInfo.add(info);
        for (CommentNode child : node.getChildren()) addCommentInfo(child, indentation + 1);
    }

    private class GetCommentsAsync extends AsyncTask<String, Void, Submission> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wheel.setVisibility(View.VISIBLE);
        }

        @Override
        protected Submission doInBackground(String... params) {
            try {
                return app.getClient().getSubmission(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Submission submission) {
            super.onPostExecute(submission);
            wheel.setVisibility(View.GONE);

            if (submission != null) {
                list = new ArrayList<>();
                listInfo = new ArrayList<>();
                sub = submission;
                CommentNode root = submission.getComments();

                FluentIterable<CommentNode> iter = root.walkTree();
                for (CommentNode node : iter) list.add(node.getComment());
                for (CommentNode child : root.getChildren()) addCommentInfo(child, 0);

                mAdapter = new CommentAdapter(context, list, listInfo, submission);
                mAdapter.setBaseUrl("https://www.reddit.com" + submission.getPermalink());
                mAdapter.setPosition(pos);
                mRecyclerView.setAdapter(mAdapter);

                //Utils.list = list;
                //Utils.listInfo = listInfo;
                //Utils.sub = sub;

                /*
                if (submission.getCommentCount() == 0) {
                    empty.setText("No comments on this post");
                    empty.setVisibility(View.VISIBLE);
                }
                else
                    empty.setVisibility(View.GONE);
                    */
            }
            else
                Utils.showSnackbar(getActivity(), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new GetCommentsAsync().execute(id);
                    }
                });
        }
    }
}
