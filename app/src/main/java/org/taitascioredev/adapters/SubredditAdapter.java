package org.taitascioredev.adapters;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;

import org.taitascioredev.fractal.R;

/**
 * Created by roberto on 26/05/15.
 */
public class SubredditAdapter extends RecyclerView.Adapter<SubredditAdapter.ViewHolder> {

    private final AppCompatActivity context;
    private final Listing<Subreddit> objects;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView url;

        public ViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.text_subreddit_name);
            url = (TextView) v.findViewById(R.id.text_subreddit_url);
        }
    }

    public SubredditAdapter(AppCompatActivity context, Listing<Subreddit> objects) {
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
        Subreddit subreddit = objects.get(position);
        holder.name.setText(subreddit.getTitle());
        holder.url.setText(subreddit.getDisplayName());
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    public Listing<Subreddit> getList() {
        return objects;
    }

    public void add(Subreddit subreddit) { objects.add(subreddit); }

    public void add(int index, Subreddit subreddit) { objects.add(index, subreddit); }

    public boolean contains(Object object) { return objects.contains(object); }

    public Subreddit getItem(int position) { return objects.get(position); }
}
