package org.taitascioredev.fractal;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import net.dean.jraw.models.Message;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

/**
 * Created by roberto on 26/04/15.
 */
public class MessageContentFragment extends Fragment {

    AppCompatActivity context;
    App app;

    @BindView(R.id.tv_author) TextView author;
    @BindView(R.id.tv_subject) TextView subject;
    @BindView(R.id.tv_body) TextView body;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_message_content, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = (AppCompatActivity) getActivity();
        app = (App) context.getApplication();
        context.getSupportActionBar().setTitle("Message");
        context.getSupportActionBar().setDisplayShowTitleEnabled(true);

        Spinner sp = (Spinner) context.findViewById(R.id.spinner);
        sp.setVisibility(View.GONE);

        Message msg = (Message) app.getMessage();
        author.setText("From " + msg.getAuthor());
        String subjectStr = msg.getSubject();
        SpannableStringBuilder ssb = new SpannableStringBuilder(subjectStr);
        List<FormattedLink> list = FormattedLink.extract(subjectStr);

        for (final FormattedLink link : list) {
            int start = link.getStart();
            int end = link.getEnd();
            CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));

            MyClickableSpan span = new MyClickableSpan(ContextCompat.getColor(context, R.color.colorPrimary)) {
                @Override
                public void onClick(View widget) {
                    Log.d("debug", link.getText());
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
                    }
                }
            };

            ssb.setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            ssb.setSpan(typefaceSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        subject.setText(ssb, TextView.BufferType.SPANNABLE);
        subject.setMovementMethod(LinkMovementMethod.getInstance());
        Spanned html = Html.fromHtml(msg.getBodyHtml());
        //Spanned html = Html.fromHtml(msg.getBodyHtml());
        //body.setText(Html.fromHtml(html.toString()));
        body.setText(Utils.setUrlSpans(context, html, false));
        body.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.findItem(R.id.item_about);
        if (item != null) item.setVisible(false);
    }
}
