package org.taitascioredev.adapters;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.OEmbed;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import org.ocpsoft.prettytime.PrettyTime;
import org.taitascioredev.fractal.CommentsFragment;
import org.taitascioredev.fractal.FormattedLink;
import org.taitascioredev.fractal.MyApp;
import org.taitascioredev.fractal.MyClickableSpan;
import org.taitascioredev.fractal.PostImageActivity;
import org.taitascioredev.fractal.R;
import org.taitascioredev.viewholders.SubmissionViewHolder;
import org.taitascioredev.fractal.SubredditPageFragment;
import org.taitascioredev.fractal.Utils;

import java.util.Iterator;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

/**
 * Created by roberto on 26/05/15.
 */
public class SubmissionAdapter extends RecyclerView.Adapter<SubmissionViewHolder> implements PopupMenu.OnMenuItemClickListener {

    private final AppCompatActivity context;
    private final Listing<Submission> objects;
    private Submission submission;

    public SubmissionAdapter(AppCompatActivity context, Listing<Submission> objects) {
        this.context = context;
        this.objects = objects;
    }

    @Override
    public SubmissionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;

        String pref = Utils.getDisplayPreference(context);
        if (pref.equals("1"))
            v = LayoutInflater.from(context).inflate(R.layout.row_layout_card, parent, false);
        else
            v = LayoutInflater.from(context).inflate(R.layout.row_layout_list, parent, false);
        return new SubmissionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final SubmissionViewHolder vh, final int position) {
        final Submission submission = objects.get(position);
        String pref = Utils.getDisplayPreference(context);

        //if (pref.equals("1"))
            //setMargins(position, vh);

        setDate(submission, vh);
        setTitle(submission, vh);
        setAuthor(submission, vh);
        setSubreddit(submission, vh);
        setBody(submission, vh);
        setThumbnail(submission, vh);
        setGif(submission.getUrl(), vh);
        vh.score.setText(submission.getScore()+"");
        vh.comments.setText(submission.getCommentCount() + " comments");
        //vh.votes.setText(submission.getScore() + "");
        //vh.comments.setText(submission.getCommentCount() + " comments");

        /*
        vh.popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(context, v);
                menu.setOnMenuItemClickListener(SubmissionAdapter.this);
                MenuInflater inflater = menu.getMenuInflater();
                inflater.inflate(R.menu.popup, menu.getMenu());
                menu.show();

                MenuItem item = menu.getMenu().findItem(R.id.comments);
                item.setTitle("Comments (" + submission.getCommentCount() + ")");

                item = menu.getMenu().findItem(R.id.save);
                if (submission.isSaved())
                    item.setTitle("Unsave");
                else
                    item.setTitle("Save");
            }
        });
        */

        final View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommentsFragment fragment = new CommentsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("id", submission.getId());
                fragment.setArguments(bundle);
                context.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null).commit();
            }
        };

        if (pref.equals("1"))
            vh.card.setOnClickListener(listener);
        else if (pref.equals("2"))
            vh.layout.setOnClickListener(listener);

        /*
        VoteDirection voteDir;
        if (!submission.getChangedVote())
            voteDir = submission.getVote();
        else
            voteDir = submission.getVote2();
        final int vote = voteDir.getValue();
        */
        int vt = getVote(submission);

        /*
        // vote img
        vh.up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApp app = (MyApp) context.getApplication();
                int vote = getVote(submission);
                if (app.getClient().getAuthenticationMethod().isUserless())
                    Toast.makeText(context, "You need to be logged in to perform this action", Toast.LENGTH_SHORT).show();
                else {
                    if (vote == VoteDirection.NO_VOTE.getValue() || vote == VoteDirection.DOWNVOTE.getValue())
                        new VoteTask(submission, VoteDirection.UPVOTE, vh.up, vh.down).execute();
                    else
                        new VoteTask(submission, VoteDirection.NO_VOTE, vh.up, vh.down).execute();
                }
            }
        });
        vh.down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApp app = (MyApp) context.getApplication();
                int vote = getVote(submission);
                if (app.getClient().getAuthenticationMethod().isUserless())
                    Toast.makeText(context, "You need to be logged in to perform this action", Toast.LENGTH_SHORT).show();
                else {
                    if (vote == VoteDirection.NO_VOTE.getValue() || vote == VoteDirection.UPVOTE.getValue())
                        new VoteTask(submission, VoteDirection.DOWNVOTE, vh.down, vh.up).execute();
                    else
                        new VoteTask(submission, VoteDirection.NO_VOTE, vh.up, vh.down).execute();
                }
            }
        });
        */

        /*
        if (vt == VoteDirection.NO_VOTE.getValue()) {
            //Log.d("VOTE", "NO VOTE");
            vh.up.setImageResource(R.drawable.up_24dp);
            vh.down.setImageResource(R.drawable.down_24dp);
        }
        else if (vt == VoteDirection.UPVOTE.getValue()) {
            //Log.d("VOTE", "UP VOTE");
            vh.up.setImageResource(R.drawable.up2);
            vh.down.setImageResource(R.drawable.down_24dp);
        }
        else {
            //Log.d("VOTE", "DOWN VOTE");
            vh.up.setImageResource(R.drawable.up_24dp);
            vh.down.setImageResource(R.drawable.down2);
        }
        */

        //Log.d("debug", submission.getDomain() + ": " + url);

        /*
        else if (!submission.isSelfPost()) {
            thumbnail.setVisibility(View.GONE);
            TextView urlTv = (TextView) v.findViewById(R.id.text_url);
            urlTv.setText(url);
            ImageView urlImg = (ImageView) v.findViewById(R.id.image_minithumbnail);
            if (submission.getThumbnail() != null && !submission.getThumbnail().equals("null")) {
                Picasso.with(context).load(submission.getThumbnail()).placeholder(R.drawable.placeholder).into(urlImg);
            }
            else {
                urlImg.setImageResource(R.drawable.ic_question);
            }
            thumbnailLayout.setVisibility(View.VISIBLE);
        }
        */
        /*
        else if (url.contains(".jpg") || url.contains(".png")) {
            holder.divider.setVisibility(View.VISIBLE);
            holder.thumbnailSmall.setVisibility(View.VISIBLE);
            holder.url.setVisibility(View.VISIBLE);
            Picasso.with(context).load(url).into(holder.thumbnailSmall);
            holder.url.setText(url);
        }
        */

        vh.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.putExtra(Intent.EXTRA_TEXT, submission.getTitle() + " " + submission.getUrl());
                i.setType("text/plain");
                context.startActivity(i);
            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.putExtra(Intent.EXTRA_TEXT, submission.getTitle() + " " + submission.getUrl());
                i.setType("text/plain");
                context.startActivity(i);
                return true;
            case R.id.comments:
                CommentsFragment fragment = new CommentsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("id", submission.getId());
                fragment.setArguments(bundle);
                context.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
                return true;
            case R.id.save:
                new SaveSubmissionTask().execute();
                return true;
            case R.id.permalink_submission:
                String baseUrl = "https://www.reddit.com";
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("permalink", baseUrl + submission.getPermalink());
                clipboard.setPrimaryClip(data);
                Toast.makeText(context, "Permalink copied to the clipboard", Toast.LENGTH_SHORT).show();
                return true;
            /*
            case R.id.gold:

                return true;
                */
            default:
                return false;
        }
    }

    @Override
    public int getItemCount() { return objects.size(); }

    public Submission getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) { return Long.parseLong(getItem(position).getId()); }

    public void add(int index, Submission submission) {
        objects.add(index, submission);
        notifyItemInserted(index);
    }

    public void add(Submission submission) {
        objects.add(submission);
        notifyItemInserted(getItemCount() - 1);
    }

    public boolean contains(Object object) { return objects.contains(object); }

    public Submission remove(int index) { return objects.remove(index); }

    public boolean remove(Submission submission) { return objects.remove(submission); }

    public Listing<Submission> getList() { return objects; }

    public int getPosition(Submission submission) {
        Iterator<Submission> iter = objects.iterator();
        for (int i = 0; iter.hasNext(); i++)
            if (iter.next().getId() == submission.getId())
                return i;

        return -1;
    }

    private void setMargins(int position, SubmissionViewHolder vh) {
        if (position == 0) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) vh.card.getLayoutParams();
            params.topMargin = 8;
            vh.card.setLayoutParams(params);
        }
        else {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) vh.card.getLayoutParams();
            params.topMargin = 0;
            vh.card.setLayoutParams(params);
        }
    }

    private void setDate(Submission submission, SubmissionViewHolder vh) {
        int i;
        PrettyTime pretty = new PrettyTime(new Locale("en"));
        String dateStr = pretty.format(submission.getCreatedUtc());

        if (dateStr.contains("hour")) {
            i = dateStr.indexOf("hour");
            dateStr = dateStr.substring(0, i - 1) + "h ago";
        }
        else if (dateStr.contains("minute")) {
            i = dateStr.indexOf("minute");
            dateStr = dateStr.substring(0, i - 1) + "m ago";
        }
        else if (dateStr.contains("second")) {
            i = dateStr.indexOf("second");
            dateStr = dateStr.substring(0, i - 1) + "s ago";
        }
        else if (dateStr.contains("moment")) {
            i = dateStr.indexOf("moment");
            dateStr = "seconds ago";
        }

        vh.date.setText(dateStr);
    }

    private void setTitle(final Submission submission, final SubmissionViewHolder vh) {
        int start;
        int end;
        String str = submission.getTitle();
        SpannableStringBuilder ssb = null;
        CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(
                TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));

        if (!submission.isSelfPost()) {
            str = submission.getTitle() + " (" + submission.getDomain() + ")";
            ssb = new SpannableStringBuilder(str);
            int index = str.lastIndexOf("(");
            ForegroundColorSpan boldSpan = new ForegroundColorSpan(
                    context.getResources().getColor(R.color.colorDomain));
            ssb.setSpan(boldSpan, index, str.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        if (ssb == null) {
            str = submission.getTitle();
            ssb = new SpannableStringBuilder(submission.getTitle());
        }

        for (final FormattedLink link : FormattedLink.extract(str)) {
            start = link.getStart();
            end = link.getEnd();

            MyClickableSpan span = new MyClickableSpan(context.getResources().getColor(R.color.colorPrimary)) {
                @Override
                public void onClick(View widget) {
                    Log.d("onClick", link.getText());
                    switch (link.getType()) {
                        case FormattedLink.TYPE_SUBREDDIT:
                            MyApp app = (MyApp) context.getApplication();
                            app.setSubredditPaginator(null);
                            app.setSubmissionsSubreddit(null);

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

            ForegroundColorSpan colorSpan = new ForegroundColorSpan(
                    context.getResources().getColor(R.color.colorPrimary));

            ssb.setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            ssb.setSpan(typefaceSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            ssb.setSpan(colorSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        MyClickableSpan clickableSpan = new MyClickableSpan() {
            @Override
            public void onClick(View widget) {
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CommentsFragment fragment = new CommentsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("id", submission.getId());
                        fragment.setArguments(bundle);
                    }
                };

                if (submission.isSelfPost())
                    vh.title.setOnClickListener(listener);
                else
                    vh.title.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String url = submission.getUrl();
                            Log.d("URL", url);
                            context.startActivity(createIntent(submission.getDomain(), url));
                        }
                    });
            }
        };
        ssb.setSpan(clickableSpan, 0, ssb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        vh.title.setText(ssb, TextView.BufferType.SPANNABLE);
        vh.title.setMovementMethod(LinkMovementMethod.getInstance());

        /*
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentsFragment fragment = new CommentsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("id", submission.getId());
                fragment.setArguments(bundle);
            }
        };

        if (submission.isSelfPost())
            vh.title.setOnClickListener(listener);
        else
            vh.title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = submission.getUrl();
                    Log.d("URL", url);
                    context.startActivity(createIntent(submission.getDomain(), url));
                }
            });
            */
    }

    private void setAuthor(Submission submission, SubmissionViewHolder vh) {
        String text = "/u/" + submission.getAuthor() + "  -  ";
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(
                TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));
        MyClickableSpan clickSpan = new MyClickableSpan(context.getResources().getColor(R.color.colorDomain)) {
            @Override
            public void onClick(View widget) {
                // SET FRAGMENT
            }
        };
        ssb.setSpan(clickSpan, 0, ("/u/" + submission.getAuthor()).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        vh.author.setText(ssb, TextView.BufferType.SPANNABLE);
        vh.author.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setSubreddit(final Submission submission, SubmissionViewHolder vh) {
        String text = "/r/" + submission.getSubredditName();
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(
                TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));

        MyClickableSpan clickSpan = new MyClickableSpan(context.getResources().getColor(R.color.colorDomain)) {
            @Override
            public void onClick(View widget) {
                SubredditPageFragment fragment = new SubredditPageFragment();
                Bundle bundle = new Bundle();
                bundle.putString("subreddit_url", submission.getSubredditName());
                fragment.setArguments(bundle);

                context.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null).commit();
            }
        };
        ssb.setSpan(clickSpan, 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        vh.subreddit.setText(ssb, TextView.BufferType.SPANNABLE);
        vh.subreddit.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setBody(Submission submission, final SubmissionViewHolder vh) {
        if (submission.isSelfPost() && submission.getSelftext().length() > 0) {
            final Spanned spanned = Html.fromHtml(submission.getSelftextHtml());
            vh.body.setText(Utils.setUrlSpans(context, spanned, true));
            vh.body.setMovementMethod(LinkMovementMethod.getInstance());
            vh.body.setVisibility(View.VISIBLE);
            vh.body.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vh.body.setText(Utils.setUrlSpans(context, spanned, false));
                }
            });
            vh.divider.setVisibility(View.VISIBLE);
        }
        else {
            vh.body.setVisibility(View.GONE);
            vh.divider.setVisibility(View.GONE);
        }
    }

    private void setThumbnail(final Submission submission, final SubmissionViewHolder vh) {
        String url = submission.getUrl();
        OEmbed embed = submission.getOEmbedMedia();
        String domain = submission.getDomain();
        Uri uri;

        if (embed != null)
            url = embed.getThumbnail().getUrl().toExternalForm();

        if (domain.contains("imgur") && !url.contains("gallery") && !url.contains(".gifv")) {
            final String img = url;
            Log.d("IMAGE URL", submission.getTitle() + ":   " + url + " - " + Utils.getImageUrl(url, 'l'));
            uri = Uri.parse(Utils.getImageUrl(url, 'l'));
            vh.thumbnail.setImageURI(uri);
            vh.thumbnail.setVisibility(View.VISIBLE);
            vh.lySmallThumb.setVisibility(View.GONE);
            vh.divider.setVisibility(View.GONE);
            vh.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("IMG", img);
                    Log.d("getImageUrl", Utils.getImageUrl(img, 'l'));
                    Intent i = new Intent(context, PostImageActivity.class);
                    i.putExtra("url", Utils.getImageUrl(img, 'l'));
                    context.startActivity(i);
                }
            });
        }
        else if (url.contains("gallery")) {
            vh.thumbnail.setVisibility(View.GONE);
            vh.lySmallThumb.setVisibility(View.GONE);
            vh.divider.setVisibility(View.GONE);
            vh.type.setText("GALLERY");
            vh.type.setVisibility(View.VISIBLE);
        }
        else if (url.contains(".gifv")) {
            vh.thumbnail.setVisibility(View.GONE);
            vh.lySmallThumb.setVisibility(View.GONE);
            vh.divider.setVisibility(View.GONE);
            vh.type.setText("GIF");
            vh.type.setVisibility(View.VISIBLE);
        }
        else if (embed != null) {
            Log.d("URL", url);
            vh.thumbnail.setVisibility(View.GONE);
            vh.divider.setVisibility(View.VISIBLE);
            vh.lySmallThumb.setVisibility(View.VISIBLE);
            uri = Uri.parse(url);
            vh.thumbnailSmall.setImageURI(uri);
            vh.url.setText(submission.getUrl());

            vh.lySmallThumb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View.OnClickListener listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CommentsFragment fragment = new CommentsFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("id", submission.getId());
                            fragment.setArguments(bundle);
                        }
                    };

                    if (submission.isSelfPost())
                        vh.lySmallThumb.setOnClickListener(listener);
                    else
                        vh.lySmallThumb.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String url = submission.getUrl();
                                Log.d("URL", url);
                                context.startActivity(createIntent(submission.getDomain(), url));
                            }
                        });
                }
            });
        }
        else {
            vh.thumbnail.setVisibility(View.GONE);
            vh.lySmallThumb.setVisibility(View.GONE);
            if (!submission.isSelfPost())
                vh.divider.setVisibility(View.GONE);
        }
    }

    private void setGif(String url, SubmissionViewHolder vh) {
        if (url.contains(".gif") || url.contains(".gifv")) {
            vh.type.setText("GIF");
            vh.type.setVisibility(View.VISIBLE);
        }
        else
            vh.type.setVisibility(View.GONE);
    }

    private String getImageUrl(String domain, String url) {
        Log.d("IMG URL", url);
        if (url.endsWith(".jpg") || url.endsWith(".png"))
            return url;
        return url + ".jpg";
    }

    private Intent createIntent(String domain, String url) {
        int j;
        Intent i;
        if (domain.contains("youtube.com")) {
            try {
                int index = url.indexOf("?v=");
                for (j = index + 3; j < url.length(); j++) {
                    char c = url.charAt(j);
                    if (!Character.isLetterOrDigit(c) && c != '_' && c != '-')
                        break;
                }
                String videoId = url.substring(index + 3, j);
                i = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
            } catch (ActivityNotFoundException e) {
                i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            }
        }
        else if (domain.contains("youtu.be")) {
            try {
                int index = url.indexOf("youtu.be/");
                for (j = index + "youtu.be/".length(); j < url.length(); j++) {
                    char c = url.charAt(j);
                    if (!Character.isLetterOrDigit(c) && c != '_' && c != '-')
                        break;
                }
                String videoId = url.substring(index + "youtu.be/".length(), j);
                i = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
            } catch (ActivityNotFoundException e) {
                i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            }
        }
        else {
            //i = new Intent(context, PostLinkActivity.class);
            //i.putExtra("url", url);
            i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        }
        return i;
    }

    private int getVote(Submission submission) {
        VoteDirection voteDir;
        if (!submission.getChangedVote())
            voteDir = submission.getVote();
        else
            voteDir = submission.getVote2();
        return voteDir.getValue();
    }

    private class VoteTask extends AsyncTask<Void, Void, Boolean> {

        private Submission submission;
        private VoteDirection vote;
        private ImageView image1;
        private ImageView image2;

        public VoteTask(Submission submission, VoteDirection vote, ImageView image1, ImageView image2) {
            this.submission = submission;
            this.vote = vote;
            this.image1 = image1;
            this.image2 = image2;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            MyApp app = (MyApp) context.getApplication();
            AccountManager manager = new AccountManager(app.getClient());
            try {
                manager.vote(submission, vote);
                return true;
            } catch (ApiException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                int value = vote.getValue();
                Log.d("debug", "VOTED: " + value);
                if (value == VoteDirection.NO_VOTE.getValue()) {
                    image1.setImageResource(R.drawable.up_24dp);
                    image2.setImageResource(R.drawable.down_24dp);
                }
                if (value == VoteDirection.UPVOTE.getValue()) {
                    image1.setImageResource(R.drawable.up2);
                    image2.setImageResource(R.drawable.down_24dp);
                }
                else {
                    image1.setImageResource(R.drawable.down2);
                    image2.setImageResource(R.drawable.up_24dp);
                }
                final int index = getPosition(submission);
                Log.d("debug", "INDEX: " + index);
                //Submission aux = submission;
                //remove(submission);
                //notifyItemRemoved(index);
                //notifyDataSetChanged();
                //notifyDataSetChanged();
                //notifyItemRemoved(index);
                submission.setChangedVote(true);
                submission.setVote(vote);
                //add(index, aux);
                notifyDataSetChanged();
                //notifyItemInserted(index);
                //updateAdapter(index);
                Toast.makeText(context, "Voted on post successfully", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(context, "Something went wrong. Try again later", Toast.LENGTH_SHORT).show();
        }
    }

    private class SaveSubmissionTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            MyApp app = (MyApp) context.getApplication();
            AccountManager manager = new AccountManager(app.getClient());
            if (submission.isSaved())
                try {
                    manager.unsave(submission);
                    //Toast.makeText(context, "Post saved", Toast.LENGTH_SHORT).show();
                    return true;
                } catch (ApiException e) {
                    return false;
                }
            else
                try {
                    manager.save(submission);
                    //Toast.makeText(context, "Post unsaved", Toast.LENGTH_SHORT).show();
                    return true;
                } catch (ApiException e) {
                    return false;
                }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result)
                Toast.makeText(context, "Post saved/unsaved", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private class GiveGoldTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            MyApp app = (MyApp) context.getApplication();
            AccountManager manager = new AccountManager(app.getClient());
            try {
                manager.giveGold(submission);
                return true;
            } catch (ApiException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {

            }
            else {

            }
        }
    }
}
