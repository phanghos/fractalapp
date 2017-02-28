package org.taitascioredev.adapters;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.Paginator;

import org.taitascioredev.fractal.App;
import org.taitascioredev.fractal.R;
import org.taitascioredev.fractal.SubredditPageFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by roberto on 26/05/15.
 */
public class SubredditAdapter extends RecyclerView.Adapter<SubredditAdapter.ViewHolder> {

    AppCompatActivity context;
    List<Subreddit> objects;

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_subreddit_name) TextView name;
        @BindView(R.id.tv_subreddit_url) TextView url;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Subreddit subreddit = get(getAdapterPosition());
                    App app = (App) context.getApplication();

                    app.setSubredditPaginator(null);
                    app.setSubmissionsSubreddit(null);

                    SubredditPageFragment fragment = new SubredditPageFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("subreddit_url", subreddit.getDisplayName());
                    fragment.setArguments(bundle);

                    context.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null).commit();
                }
            });
        }
    }

    public SubredditAdapter(AppCompatActivity context, List<Subreddit> objects) {
        this.context = context;
        this.objects = objects;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, final int position) {
        View v = LayoutInflater.from(context).inflate(R.layout.subreddit_row_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Subreddit subreddit = get(position);
        holder.name.setText(subreddit.getTitle());
        holder.url.setText(subreddit.getDisplayName());
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    public Subreddit get(int pos) {
        return objects.get(pos);
    }

    public List<Subreddit> getList() {
        return objects;
    }

    public void add(int pos, Subreddit subreddit) {
        objects.add(pos, subreddit);
        notifyItemInserted(pos);
    }

    public void add(Subreddit subreddit) {
        objects.add(subreddit);
        notifyItemInserted(getItemCount());
    }

    public boolean contains(Object object) { return objects.contains(object); }
}
