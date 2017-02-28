package org.taitascioredev.adapters;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import android.widget.Toast;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Message;

import org.taitascioredev.fractal.App;
import org.taitascioredev.fractal.CustomLinkMovementMethod;
import org.taitascioredev.fractal.FormattedLink;
import org.taitascioredev.fractal.MessageContentFragment;
import org.taitascioredev.fractal.MyClickableSpan;
import org.taitascioredev.fractal.R;
import org.taitascioredev.fractal.SubredditPageFragment;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

/**
 * Created by roberto on 02/11/15.
 */
public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {

    AppCompatActivity context;
    List<Message> objects;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView author;
        TextView subject;

        public ViewHolder(View v) {
            super(v);
            author = (TextView) v.findViewById(R.id.tv_author);
            subject = (TextView) v.findViewById(R.id.tv_subject);
        }
    }

    public InboxAdapter(AppCompatActivity context, List<Message> objects) {
        this.context = context;
        this.objects = objects;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, final int position) {
        View v = LayoutInflater.from(context).inflate(R.layout.message_row_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Message msg = get(position);
        holder.author.setText(msg.getAuthor());
        //holder.subject.setText(msg.getSubject());
        String subjectStr = msg.getSubject();
        SpannableStringBuilder ssb = new SpannableStringBuilder(subjectStr);
        for (final FormattedLink link : FormattedLink.extract(subjectStr)) {
            int start = link.getStart();
            int end = link.getEnd();
            CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));

            MyClickableSpan clickSpan = new MyClickableSpan(ContextCompat.getColor(context, R.color.colorPrimary)) {
                @Override
                public void onClick(View widget) {
                    Log.d("Link", link.getText());

                    switch (link.getType()) {
                        case FormattedLink.TYPE_SUBREDDIT:
                            SubredditPageFragment fragment = new SubredditPageFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("subreddit_url", link.getText().substring(3));
                            fragment.setArguments(bundle);

                            context.getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null).commit();
                            break;
                        default:
                            Toast.makeText(context, "Link not yet supported", Toast.LENGTH_SHORT).show();
                    }
                }
            };
            ssb.setSpan(clickSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            ssb.setSpan(typefaceSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        holder.subject.setText(ssb, TextView.BufferType.SPANNABLE);
        holder.subject.setMovementMethod(LinkMovementMethod.getInstance());
        holder.subject.setOnTouchListener(new CustomLinkMovementMethod());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App app = (App) context.getApplication();
                app.setMessage(msg);
                MessageContentFragment fragment = new MessageContentFragment();

                context.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null).commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    public Message get(int pos) {
        return objects.get(pos);
    }

    public List<Message> getList() {
        return objects;
    }

    public void add(int index, Message msg) {
        objects.add(index, msg);
        notifyItemInserted(index);
    }

    public void add(Message msg) {
        objects.add(msg);
        notifyItemInserted(getItemCount());
    }

    public boolean contains(Object object) { return objects.contains(object); }
}
