package org.taitascioredev.fractal;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.pnikosis.materialishprogress.ProgressWheel;

import net.dean.jraw.models.Account;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.UserContributionPaginator;

/**
 * Created by roberto on 13/11/15.
 */
public class UserFragment extends Fragment {

    private App app;

    private AppCompatActivity context;

    private ProgressWheel wheel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_subreddits, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = (AppCompatActivity) getActivity();
        app = (App) context.getApplication();
        Bundle bundle = getArguments();
        String username = bundle.getString("subreddit_url");
        context.getSupportActionBar().setTitle(username);
        context.getSupportActionBar().setDisplayShowTitleEnabled(true);

        Spinner spinner = (Spinner) context.findViewById(R.id.spinner);
        spinner.setVisibility(View.GONE);

        new GetUserCommentsTask().execute(username);
    }

    private class GetUserTask extends AsyncTask<String, Void, Account> {

        @Override
        protected Account doInBackground(String... params) {
            return app.getClient().getUser(params[0]);
        }

        @Override
        protected void onPostExecute(Account account) {
            super.onPostExecute(account);
            if (account != null) {

            }
            else {

            }
        }
    }

    private class GetUserCommentsTask extends AsyncTask<String, Void, Listing<Contribution>> {

        @Override
        protected Listing<Contribution> doInBackground(String... params) {
            UserContributionPaginator paginator = new UserContributionPaginator(app.getClient(), "comments", params[0]);
            return paginator.next();
        }

        @Override
        protected void onPostExecute(Listing<Contribution> contributions) {
            super.onPostExecute(contributions);
            if (contributions != null) {
                Log.d("debug", "SUCCESS");
                Log.d("debug", contributions.size()+"");
                if (contributions.size() > 0) {
                    Contribution c = contributions.get(0);
                    Submission s = (Submission) c;
                    if (s != null)
                        Log.d("debug", s.getPermalink());
                }
            }
            else
                Log.d("debug", "FAILURE");
        }
    }
}
