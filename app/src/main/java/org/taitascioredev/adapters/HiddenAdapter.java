package org.taitascioredev.adapters;

import android.content.ActivityNotFoundException;
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

import com.squareup.picasso.Picasso;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import org.ocpsoft.prettytime.PrettyTime;
import org.taitascioredev.fractal.CommentsFragment;
import org.taitascioredev.fractal.FormattedLink;
import org.taitascioredev.fractal.App;
import org.taitascioredev.fractal.MyClickableSpan;
import org.taitascioredev.fractal.PostImageActivity;
import org.taitascioredev.fractal.R;
import org.taitascioredev.viewholders.SubmissionVH;
import org.taitascioredev.fractal.SubredditPageFragment;
import org.taitascioredev.fractal.Utils;

import java.util.Iterator;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

/**
 * Created by roberto on 14/11/15.
 */
public class HiddenAdapter extends RecyclerView.Adapter<SubmissionVH> implements PopupMenu.OnMenuItemClickListener {

    private final AppCompatActivity context;
    private final Listing<Contribution> objects;
    private Submission submission;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View v) {
            super(v);
        }
    }

    public HiddenAdapter(AppCompatActivity context, Listing<Contribution> objects) {
        this.context = context;
        this.objects = objects;
    }

    @Override
    public SubmissionVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;

        String pref = Utils.getDisplayPreference(context);
        if (pref.equals("1"))
            v = LayoutInflater.from(context).inflate(R.layout.row_layout_card, parent, false);
        else
            v = LayoutInflater.from(context).inflate(R.layout.row_layout_list, parent, false);
        return new SubmissionVH(v);
    }

    @Override
    public void onBindViewHolder(final SubmissionVH holder, final int position) {
        /*
        holder.popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submission = (Submission) objects.get(position);
                PopupMenu menu = new PopupMenu(context, v);
                menu.setOnMenuItemClickListener(HiddenAdapter.this);
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

        final Submission submission;

        SpannableStringBuilder ssb = null;

        submission = (Submission) objects.get(position);

        PrettyTime pretty = new PrettyTime(new Locale("en"));
        String dateStr = pretty.format(submission.getCreated());
        //String dateStr = pretty.format(submission.getCreatedUtc());
        int i;
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
        holder.date.setText(dateStr);

        String pref = Utils.getDisplayPreference(context);
        if (pref.equals("1")) {
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommentsFragment fragment = new CommentsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("id", submission.getId());
                    fragment.setArguments(bundle);
                    context.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
                }
            });
        }
        else if (pref.equals("2")) {
            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CommentsFragment fragment = new CommentsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("id", submission.getId());
                    fragment.setArguments(bundle);
                    context.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
                }
            });
        }

        // author
        /*
        String authorStr = "by " + submission.getAuthor();
        int start = authorStr.indexOf(submission.getAuthor());
        ssb = new SpannableStringBuilder(authorStr);
        CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));
        ssb.setSpan(typefaceSpan, start, ssb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        author.setText(ssb, TextView.BufferType.SPANNABLE);
        */

        // title
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentsFragment fragment = new CommentsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("id", submission.getId());
                fragment.setArguments(bundle);
                //context.setFragmentChild(fragment, "Comments");
            }
        };
        final String url = submission.getUrl();

        /*
        ViewGroup.MarginLayoutParams params;
        int big = (int) context.getResources().getDimension(R.dimen.bottom_images_margin_big);
        int small = (int) context.getResources().getDimension(R.dimen.bottom_images_margin_small);
        if (Utils.isImageUrl(url)) {
            params = (ViewGroup.MarginLayoutParams) holder.share.getLayoutParams();
            params.bottomMargin = big;
            params = (ViewGroup.MarginLayoutParams) holder.save.getLayoutParams();
            params.bottomMargin = big;
            params = (ViewGroup.MarginLayoutParams) holder.hide.getLayoutParams();
            params.bottomMargin = big;
        }
        else {
            params = (ViewGroup.MarginLayoutParams) holder.share.getLayoutParams();
            params.bottomMargin = small;
            params = (ViewGroup.MarginLayoutParams) holder.save.getLayoutParams();
            params.bottomMargin = small;
            params = (ViewGroup.MarginLayoutParams) holder.hide.getLayoutParams();
            params.bottomMargin = small;
        }
        */

        // domain
        String str = submission.getTitle();
        if (!submission.isSelfPost()) {
            str = submission.getTitle() + " (" + submission.getDomain() + ")";
            ssb = new SpannableStringBuilder(str);
            int index = str.lastIndexOf("(");
            ForegroundColorSpan boldSpan = new ForegroundColorSpan(R.color.colorDomain);
            ssb.setSpan(boldSpan, index, str.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        if (ssb == null) {
            str = submission.getTitle();
            ssb = new SpannableStringBuilder(submission.getTitle());
        }
        for (final FormattedLink link : FormattedLink.extract(str)) {
            int start = link.getStart();
            int end = link.getEnd();
            CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));
            ssb.setSpan(typefaceSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            MyClickableSpan span = new MyClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Log.d("debug", link.getText());
                    switch (link.getType()) {
                        case FormattedLink.TYPE_SUBREDDIT:
                            SubredditPageFragment fragment = new SubredditPageFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("subreddit_url", link.getText().substring(3));
                            fragment.setArguments(bundle);
                            context.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
                            break;
                    }
                }
            };
            ssb.setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        /*
        ssb.append(" " + dateStr);
        RelativeSizeSpan sizeSpan = new RelativeSizeSpan(0.7f);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(R.color.secondary_text);
        ssb.setSpan(sizeSpan, ssb.toString().indexOf(" " + dateStr), ssb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        ssb.setSpan(colorSpan, ssb.toString().indexOf(" " + dateStr), ssb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        */

        holder.title.setText(ssb, TextView.BufferType.SPANNABLE);
        holder.title.setMovementMethod(LinkMovementMethod.getInstance());
        //title.setText(submission.getTitle());

        if (submission.isSelfPost())
            holder.title.setOnClickListener(listener);
        else
            holder.title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("debug", url);
                    context.startActivity(createIntent(submission.getDomain(), url));
                }
            });

        /*
        VoteDirection voteDir;
        if (!submission.getChangedVote())
            voteDir = submission.getVote();
        else
            voteDir = submission.getVote2();
        final int vote = voteDir.getValue();
        */
        int vt = getVote(submission);

        // vote img
        holder.up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App app = (App) context.getApplication();
                int vote = getVote(submission);
                if (app.getClient().getAuthenticationMethod().isUserless())
                    Toast.makeText(context, "You need to be logged in to perform this action", Toast.LENGTH_SHORT).show();
                else {
                    if (vote == VoteDirection.NO_VOTE.getValue() || vote == VoteDirection.DOWNVOTE.getValue())
                        new VoteTask(submission, VoteDirection.UPVOTE, holder.up, holder.down).execute();
                    else
                        new VoteTask(submission, VoteDirection.NO_VOTE, holder.up, holder.down).execute();
                }
            }
        });
        holder.down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App app = (App) context.getApplication();
                int vote = getVote(submission);
                if (app.getClient().getAuthenticationMethod().isUserless())
                    Toast.makeText(context, "You need to be logged in to perform this action", Toast.LENGTH_SHORT).show();
                else {
                    if (vote == VoteDirection.NO_VOTE.getValue() || vote == VoteDirection.UPVOTE.getValue())
                        new VoteTask(submission, VoteDirection.DOWNVOTE, holder.down, holder.up).execute();
                    else
                        new VoteTask(submission, VoteDirection.NO_VOTE, holder.up, holder.down).execute();
                }
            }
        });

        if (vt == VoteDirection.NO_VOTE.getValue()) {
            //Log.d("debug", "NO VOTE");
            holder.up.setImageResource(R.drawable.ic_arrow_upward_grey_24dp);
            holder.down.setImageResource(R.drawable.ic_arrow_downward_grey_24dp);
        }
        else if (vt == VoteDirection.UPVOTE.getValue()) {
            //Log.d("debug", "UP VOTE");
            holder.up.setImageResource(R.drawable.ic_arrow_upward_yellow_24dp);
            holder.down.setImageResource(R.drawable.ic_arrow_downward_grey_24dp);
        }
        else {
            //Log.d("debug", "DOWN VOTE");
            holder.up.setImageResource(R.drawable.ic_arrow_upward_grey_24dp);
            holder.down.setImageResource(R.drawable.ic_arrow_downward_yellow_24dp);
        }

        // author
        String text = "/u/" + submission.getAuthor() + "  -  ";
        ssb = new SpannableStringBuilder(text);
        CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));
        //ssb.setSpan(typefaceSpan, 0, ("/u/" + submission.getAuthor()).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        MyClickableSpan clickSpan = new MyClickableSpan(context.getResources().getColor(R.color.colorDomain)) {
            @Override
            public void onClick(View widget) {
                // SET FRAGMENT
            }
        };
        ssb.setSpan(clickSpan, 0, ("/u/" + submission.getAuthor()).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        holder.author.setText(ssb, TextView.BufferType.SPANNABLE);
        holder.author.setMovementMethod(LinkMovementMethod.getInstance());

        // subreddit
        text = "/r/" + submission.getSubredditName();
        ssb = new SpannableStringBuilder(text);
        typefaceSpan = new CalligraphyTypefaceSpan(TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));
        //ssb.setSpan(typefaceSpan, 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        clickSpan = new MyClickableSpan(context.getResources().getColor(R.color.colorDomain)) {
            @Override
            public void onClick(View widget) {
                SubredditPageFragment fragment = new SubredditPageFragment();
                Bundle bundle = new Bundle();
                bundle.putString("subreddit_url", submission.getSubredditName());
                fragment.setArguments(bundle);
                context.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
            }
        };
        ssb.setSpan(clickSpan, 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        holder.subreddit.setText(ssb, TextView.BufferType.SPANNABLE);
        holder.subreddit.setMovementMethod(LinkMovementMethod.getInstance());

        // body
        if (submission.isSelfPost() && submission.getSelftext().length() > 0) {
            final Spanned spanned = Html.fromHtml(submission.getSelftext());
            //final Spanned spanned = Html.fromHtml(submission.getSelftextHtml());
            holder.body.setText(Utils.setUrlSpans(context, spanned, true));
            holder.body.setMovementMethod(LinkMovementMethod.getInstance());
            holder.body.setVisibility(View.VISIBLE);
            holder.body.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.body.setText(Utils.setUrlSpans(context, spanned, false));
                }
            });
            holder.divider.setVisibility(View.VISIBLE);
        }
        else {
            holder.body.setVisibility(View.GONE);
            holder.divider.setVisibility(View.GONE);
        }

        // thumbail
        //RelativeLayout thumbnailLayout = (RelativeLayout) v.findViewById(R.id.layout_thumbnail);
        if (Utils.isImageUrl(url)) {
            //thumbnailLayout.setVisibility(View.GONE);
            Picasso.with(context).load(Utils.getImageUrl(url, 'l')).placeholder(R.drawable.placeholder).into(holder.thumbnail);
            holder.thumbnail.setVisibility(View.VISIBLE);
            holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*
                    if (submission.getDomain().contains("imgur")) {
                        if (!url.contains("/gallery/") && !url.contains("/a/") && !url.endsWith("gif") && !url.endsWith("gifv")) {
                            String imgUrl = getImageUrl(submission.getDomain(), submission.getUrl());
                            Intent i = new Intent(context, PostImageActivity.class);
                            int index = imgUrl.lastIndexOf(".");
                            StringBuilder builder = new StringBuilder(imgUrl.substring(0, index));
                            builder.append("l.jpg");
                            imgUrl = builder.toString();
                            i.putExtra("url", imgUrl);
                            Log.d("debug", url);
                            Log.d("debug", imgUrl);
                            context.startActivity(i);
                        } else
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    }
                    */
                    Intent i = new Intent(context, PostImageActivity.class);
                    Log.d("debug", url);
                    Log.d("debug", Utils.getImageUrl(url, 'l'));
                    i.putExtra("url", Utils.getImageUrl(url, 'l'));
                    context.startActivity(i);
                }
            });
        }
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
        else {
            holder.thumbnail.setVisibility(View.GONE);
            //thumbnailLayout.setVisibility(View.GONE);
        }

        holder.share.setOnClickListener(new View.OnClickListener() {
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
    public int getItemCount() {
        return objects.size();
    }

    public int getPosition(Submission submission) {
        Iterator<Contribution> iter = objects.iterator();
        for (int i = 0; iter.hasNext(); i++)
            if (((Submission) iter.next()).getId() == submission.getId())
                return i;

        return -1;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            /*
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
                new SaveSubmissionAsync().execute();
                return true;
            case R.id.permalink_submission:
                String baseUrl = "https://www.reddit.com";
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("permalink", baseUrl + submission.getPermalink());
                clipboard.setPrimaryClip(data);
                Toast.makeText(context, "Permalink copied to the clipboard", Toast.LENGTH_SHORT).show();
                return true;
                */
            default:
                return false;
        }
    }

    private String getImageUrl(String domain, String url) {
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
        /*
        VoteDirection voteDir;
        if (!submission.getChangedVote())
            voteDir = submission.getVote();
        else
            voteDir = submission.getVote2();
        return voteDir.getValue();
        */
        return -1;
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
            App app = (App) context.getApplication();
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
                    image1.setImageResource(R.drawable.ic_arrow_upward_grey_24dp);
                    image2.setImageResource(R.drawable.ic_arrow_downward_grey_24dp);
                }
                if (value == VoteDirection.UPVOTE.getValue()) {
                    image1.setImageResource(R.drawable.ic_arrow_upward_yellow_24dp);
                    image2.setImageResource(R.drawable.ic_arrow_downward_grey_24dp);
                }
                else {
                    image1.setImageResource(R.drawable.ic_arrow_downward_yellow_24dp);
                    image2.setImageResource(R.drawable.ic_arrow_upward_grey_24dp);
                }
                final int index = getPosition(submission);
                Log.d("debug", "INDEX: " + index);
                //Submission aux = submission;
                //remove(submission);
                //notifyItemRemoved(index);
                //notifyDataSetChanged();
                //notifyDataSetChanged();
                //notifyItemRemoved(index);

                //submission.setChangedVote(true);
                //submission.setVote(vote);

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
            App app = (App) context.getApplication();
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
}
