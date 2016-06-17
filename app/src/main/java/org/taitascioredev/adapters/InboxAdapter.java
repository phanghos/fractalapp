package org.taitascioredev.adapters;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Message;

import org.taitascioredev.fractal.FormattedLink;
import org.taitascioredev.fractal.MyClickableSpan;
import org.taitascioredev.fractal.R;
import org.taitascioredev.fractal.SubredditPageFragment;

import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

/**
 * Created by roberto on 02/11/15.
 */
public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {

    private final AppCompatActivity context;
    private final Listing<Message> objects;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView author;
        TextView subject;

        public ViewHolder(View v) {
            super(v);
            author = (TextView) v.findViewById(R.id.tv_author);
            subject = (TextView) v.findViewById(R.id.text_subject);
        }
    }

    public InboxAdapter(AppCompatActivity context, Listing<Message> objects) {
        this.context = context;
        this.objects = objects;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, final int position) {
        View v = LayoutInflater.from(context).inflate(R.layout.message_row_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message msg = objects.get(position);
        holder.author.setText(msg.getAuthor());
        //holder.subject.setText(msg.getSubject());
        String subjectStr = msg.getSubject();
        SpannableStringBuilder ssb = new SpannableStringBuilder(subjectStr);
        for (final FormattedLink link : FormattedLink.extract(subjectStr)) {
            int start = link.getStart();
            int end = link.getEnd();
            CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));
            ssb.setSpan(typefaceSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            MyClickableSpan clickSpan = new MyClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Log.d("debug", link.getText());
                    switch (link.getType()) {
                        case FormattedLink.TYPE_SUBREDDIT:
                            context.onBackPressed(); // hack
                            SubredditPageFragment fragment = new SubredditPageFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("subreddit_url", link.getText().substring(3));
                            fragment.setArguments(bundle);
                            context.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
                            break;
                    }
                }
            };
            ssb.setSpan(clickSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        holder.subject.setText(ssb, TextView.BufferType.SPANNABLE);
        holder.subject.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    public Listing<Message> getList() {
        return objects;
    }

    public void add(Message msg) { objects.add(msg); }

    public void add(int index, Message msg) { objects.add(index, msg); }

    public boolean contains(Object object) { return objects.contains(object); }
}
