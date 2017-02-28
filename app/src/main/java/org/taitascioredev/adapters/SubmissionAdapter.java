package org.taitascioredev.adapters;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.OEmbed;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import org.ocpsoft.prettytime.PrettyTime;
import org.taitascioredev.fractal.App;
import org.taitascioredev.fractal.CommentsFragment;
import org.taitascioredev.fractal.Constants;
import org.taitascioredev.fractal.CustomLinkMovementMethod;
import org.taitascioredev.fractal.Data;
import org.taitascioredev.fractal.FormattedLink;
import org.taitascioredev.fractal.GalleryActivity;
import org.taitascioredev.fractal.ImgurImage;
import org.taitascioredev.fractal.MyClickableSpan;
import org.taitascioredev.fractal.PostImageActivity;
import org.taitascioredev.fractal.PostVideoActivity;
import org.taitascioredev.fractal.R;
import org.taitascioredev.fractal.SubredditPageFragment;
import org.taitascioredev.fractal.Utils;
import org.taitascioredev.viewholders.CommentVH;
import org.taitascioredev.viewholders.SubmissionVH;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

/**
 * Created by roberto on 26/05/15.
 */
public class SubmissionAdapter extends RecyclerView.Adapter<SubmissionVH> {

    private final AppCompatActivity context;
    private final List<Submission> objects;
    private final Map<String, Data> galleryData;
    private final String pref;
    private Submission submission;

    public SubmissionAdapter(AppCompatActivity context, List<Submission> objects) {
        this.context     = context;
        this.objects     = objects;
        this.galleryData = new HashMap<String, Data>();
        this.pref        = Utils.getDisplayPreference(context);
    }

    @Override
    public SubmissionVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;

        if      (pref.equals("1")) v = LayoutInflater.from(context).inflate(R.layout.row_layout_card, parent, false);
        else if (pref.equals("2")) v = LayoutInflater.from(context).inflate(R.layout.row_layout_card_full_width, parent, false);
        else if (pref.equals("3")) v = LayoutInflater.from(context).inflate(R.layout.row_layout_card_small, parent, false);
        else                       v = LayoutInflater.from(context).inflate(R.layout.row_layout_list, parent, false);

        return new SubmissionVH(v);
    }

    @Override
    public void onBindViewHolder(final SubmissionVH vh, final int position) {
        final Submission submission = objects.get(position);

        setDate(submission, vh);
        setTitle(submission, vh);
        setAuthor(submission, vh);
        setSubreddit(submission, vh);
        setBody(submission, vh);
        setThumbnail(submission, vh);
        setVote(submission, vh);
        vh.score.setText(submission.getScore()+"");
        vh.comments.setText(submission.getCommentCount() + " comments");

        vh.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("SUBMISSION ID", submission.getId()+"");

                CommentsFragment fragment = new CommentsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("id", submission.getId());
                fragment.setArguments(bundle);

                context.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null).commit();
            }
        });

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
                sharePost(submission);
            }
        });
    }

    @Override
    public int getItemCount() { return objects.size(); }

    public Submission getItem(int pos) {
        return objects.get(pos);
    }

    @Override
    public long getItemId(int pos) { return Long.parseLong(getItem(pos).getId()); }

    public void add(int index, Submission submission) {
        objects.add(index, submission);
        notifyItemInserted(index);
    }

    public void add(Submission submission) {
        objects.add(submission);
        notifyItemInserted(getItemCount());
    }

    public void set(int pos, Submission submission) {
        objects.set(pos, submission);
        notifyItemChanged(pos);
    }

    public boolean contains(Object object) { return objects.contains(object); }

    public Submission remove(int pos) { return objects.remove(pos); }

    public boolean remove(Submission s) { return objects.remove(s); }

    public List<Submission> getList() { return objects; }

    public int getPosition(Submission submission) {
        int i = 0;
        for (Submission s : objects) {
            if (s.getId().equals(submission.getId()))
                return i;
            i++;
        }

        /*
        Iterator<Submission> iter = objects.iterator();
        for (int i = 0; iter.hasNext(); i++)
            if (iter.next().getId() == submission.getId())
                return i;
                */

        return -1;
    }

    private void setMargins(int position, SubmissionVH vh) {
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

    private void setDate(Submission submission, SubmissionVH vh) {
        int i;
        PrettyTime pretty = new PrettyTime(new Locale("en"));
        String dateStr = pretty.format(submission.getCreated());
        //String dateStr = pretty.format(submission.getCreatedUtc());

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

    private void setTitle(final Submission submission, final SubmissionVH vh) {
        int start;
        int end;
        String str = submission.getTitle();
        SpannableStringBuilder ssb = null;

        if (!submission.isSelfPost()) {
            str = Utils.toHtml(submission.getTitle()) + " (" + submission.getDomain() + ")";
            ssb = new SpannableStringBuilder(str);
            int index = str.lastIndexOf("(");
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(
                    ContextCompat.getColor(context, R.color.colorDomain));
            ssb.setSpan(colorSpan, index, str.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        if (ssb == null) {
            str = submission.getTitle();
            ssb = new SpannableStringBuilder(submission.getTitle());
        }

        for (final FormattedLink link : FormattedLink.extract(str)) {
            start = link.getStart();
            end = link.getEnd();

            MyClickableSpan span = new MyClickableSpan(ContextCompat.getColor(context, R.color.colorPrimary)) {
                @Override
                public void onClick(View widget) {
                    Log.d("onClick", link.getText());
                    switch (link.getType()) {
                        case FormattedLink.TYPE_SUBREDDIT:
                            App app = (App) context.getApplication();
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
                        default:
                            Toast.makeText(context, "Link not yet supported", Toast.LENGTH_SHORT).show();
                    }
                }
            };

            CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(
                    TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));

            ForegroundColorSpan colorSpan = new ForegroundColorSpan(
                    ContextCompat.getColor(context, R.color.colorPrimary));

            ssb.setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            ssb.setSpan(typefaceSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            ssb.setSpan(colorSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        MyClickableSpan clickableSpan = new MyClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (submission.isSelfPost()) {
                    CommentsFragment fragment = new CommentsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("id", submission.getId());
                    fragment.setArguments(bundle);

                    context.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null).commit();
                }
                else {
                    String url = submission.getUrl();
                    Log.d("URL", url);
                    Utils.launchIntent(context, submission.getDomain(), url);
                }
            }
        };
        ssb.setSpan(clickableSpan, 0, ssb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        vh.title.setText(ssb, TextView.BufferType.SPANNABLE);
        vh.title.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("title", submission.getTitle());
                clipboard.setPrimaryClip(data);

                Toast.makeText(context, "Comment copied to the clipboard", Toast.LENGTH_SHORT).show();

                return true;
            }
        });
        //vh.title.setMovementMethod(LinkMovementMethod.getInstance());
        vh.title.setOnTouchListener(new CustomLinkMovementMethod());
    }

    private void setAuthor(Submission submission, SubmissionVH vh) {
        String text = "/u/" + submission.getAuthor() + "  -  ";
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(
                TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));
        MyClickableSpan clickSpan = new MyClickableSpan(ContextCompat.getColor(context, R.color.colorDomain)) {
            @Override
            public void onClick(View widget) {
                Toast.makeText(context, "Link not yet supported", Toast.LENGTH_SHORT).show();
            }
        };
        ssb.setSpan(clickSpan, 0, ("/u/" + submission.getAuthor()).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        vh.author.setText(ssb, TextView.BufferType.SPANNABLE);
        vh.author.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setSubreddit(final Submission submission, SubmissionVH vh) {
        String text = "/r/" + submission.getSubredditName();
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(
                TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));

        MyClickableSpan clickSpan = new MyClickableSpan(ContextCompat.getColor(context, R.color.colorDomain)) {
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

    private void setBody(final Submission submission, final SubmissionVH vh) {
        if (submission.isSelfPost() && submission.getSelftext().length() > 0) {
            final Spanned spanned = Html.fromHtml(submission.getSelftextHtml());
            /*
            SpannableStringBuilder ssb = (SpannableStringBuilder) Utils.setUrlSpans(context, spanned, true);
            MyClickableSpan clickableSpan = new MyClickableSpan() {
                @Override
                public void onClick(View widget) {
                    if (submission.isSelfPost()) {
                        CommentsFragment fragment = new CommentsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("id", submission.getId());
                        fragment.setArguments(bundle);

                        context.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null).commit();
                    }
                    else {
                        String url = submission.getUrl();
                        Log.d("URL", url);
                        Utils.launchIntent(context, submission.getDomain(), url);
                    }
                }
            };
            ssb.setSpan(clickableSpan, 0, ssb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            vh.body.setText(ssb);
            */

            vh.body.setText(Utils.setUrlSpans(context, spanned, true));
            vh.body.setVisibility(View.VISIBLE);
            vh.body.setMovementMethod(LinkMovementMethod.getInstance());
            vh.body.setOnTouchListener(new CustomLinkMovementMethod());
            vh.divider.setVisibility(View.VISIBLE);
            vh.body.setOnClickListener(new View.OnClickListener() {
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
            });
        }
        else {
            vh.body.setVisibility(View.GONE);
            vh.divider.setVisibility(View.GONE);
        }
    }

    private void setThumbnail(final Submission submission, final SubmissionVH vh) {
        String url = submission.getUrl();
        OEmbed embed = submission.getOEmbedMedia();
        final String domain = submission.getDomain();
        final Uri uri;

        if (embed != null) {
            Log.d("EMBED URL", url);
            url = embed.getThumbnail().getUrl().toExternalForm();
        }

        if (domain.contains("imgur") && !url.contains("gallery") && !url.contains(".gif") && !url.contains("/a/")) {
            final String img = url;
            Log.d("IMAGE URL", url + ":   " + Utils.getImageUrl(url, 'l') + " -> " + Utils.getImageUrl(context, url));
            //uri = Uri.parse(Utils.getImageUrl(url, 'l'));
            //uri = Uri.parse(Utils.getImageUrl(context, url));
            uri = Uri.parse(Utils.getImageUrl(url, 'l'));
            vh.thumbnail.setImageURI(uri);
            vh.thumbnail.setVisibility(View.VISIBLE);
            vh.thumbnail.setImageAlpha(255);
            vh.lySmallThumb.setVisibility(View.GONE);
            vh.divider.setVisibility(View.GONE);
            vh.btnPlay.setVisibility(View.GONE);

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
        else if (domain.contains("imgur") && (url.contains("gallery") || url.contains("/a/"))) {
            Log.d("GALLERY URL", url);
            vh.thumbnail.setVisibility(View.VISIBLE);
            Uri nullUri = null;
            vh.thumbnail.setImageURI(nullUri);
            //vh.thumbnail.setImageAlpha(Constants.ALPHA);
            vh.lySmallThumb.setVisibility(View.GONE);
            vh.divider.setVisibility(View.GONE);
            vh.btnPlay.setImageResource(R.drawable.ic_gallery_48dp);
            //vh.btnPlay.setVisibility(View.VISIBLE);

            uri = Uri.parse(url);
            List<String> segments = uri.getPathSegments();
            Log.d("CONTAINS KEY", galleryData.containsKey(segments.get(segments.size() - 1))+"");
            if (!galleryData.containsKey(segments.get(segments.size() - 1))) {
                vh.thumbnail.setOnClickListener(null);
                new GetGalleryDataAsync(vh).execute(segments.get(segments.size() - 1));
            }
            else {
                final Data data = galleryData.get(segments.get(segments.size() - 1));

                if (data.is_album && data.images != null && data.images.length > 0) {
                    Log.d("Image count", data.images.length+"");
                    String coverUrl = Utils.getImageUrl(data.images[0].link, 'l');
                    Log.d("COVER URL", coverUrl);
                    Uri coverUri = Uri.parse(coverUrl);
                    vh.thumbnail.setImageURI(coverUri);
                    vh.thumbnail.setImageAlpha(Constants.ALPHA);
                    vh.btnPlay.setVisibility(View.VISIBLE);

                    vh.thumbnail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(context, GalleryActivity.class);
                            i.putExtra("images", data.images);
                            context.startActivity(i);
                        }
                    });
                }
                else {
                    String coverUrl = Utils.getImageUrl(data.link, 'l');
                    Log.d("COVER URL", coverUrl);
                    Uri coverUri = Uri.parse(coverUrl);
                    vh.thumbnail.setImageURI(coverUri);
                    vh.thumbnail.setOnClickListener(null);
                    vh.thumbnail.setImageAlpha(255);
                    vh.btnPlay.setVisibility(View.GONE);
                }
            }
        }
        else if (url.contains(".gif") && !url.contains(".gifv")) {
            final String img = url;
            Log.d("GIF URL", url);
            uri = Uri.parse(url);

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(uri)
                    .setAutoPlayAnimations(true)
                    .build();

            vh.thumbnail.setController(controller);
            vh.thumbnail.setVisibility(View.VISIBLE);
            vh.thumbnail.setImageAlpha(255);
            vh.thumbnail.setOnClickListener(null);
            vh.lySmallThumb.setVisibility(View.GONE);
            vh.divider.setVisibility(View.GONE);
            vh.btnPlay.setVisibility(View.GONE);

            /*
            vh.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("IMG", img);
                    Log.d("getImageUrl", Utils.getImageUrl(img, 'l'));
                    Intent i = new Intent(context, PostImageActivity.class);
                    i.putExtra("url", img);
                    //i.putExtra("url", Utils.getImageUrl(img, 'l'));
                    context.startActivity(i);
                }
            });
            */
        }
        else if (url.contains(".gifv")) {
            /*
            vh.thumbnail.setVisibility(View.GONE);
            vh.lySmallThumb.setVisibility(View.GONE);
            vh.divider.setVisibility(View.GONE);
            */

            final String videoUrl = url;
            final String img = Utils.getImageUrl(url.replaceAll(".gifv", ""), 'l');

            Log.d("THUMBNAIL URL", img + " (" + submission.getTitle() + ")");
            Log.d("GIFV URL", url);
            uri = Uri.parse(img);

            vh.thumbnail.setImageURI(uri);
            vh.thumbnail.setVisibility(View.VISIBLE);
            vh.thumbnail.setImageAlpha(Constants.ALPHA);
            vh.lySmallThumb.setVisibility(View.GONE);
            vh.divider.setVisibility(View.GONE);
            vh.btnPlay.setImageResource(R.drawable.ic_play_circle_outline_48dp);
            vh.btnPlay.setVisibility(View.VISIBLE);

            vh.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String video = videoUrl.replace("gifv", "mp4");
                    Intent i;

                    try {
                        i = new Intent();
                        i.setDataAndType(Uri.parse(video), "video/*");
                        context.startActivity(i);
                    } catch (ActivityNotFoundException e) {
                        i = new Intent(context, PostVideoActivity.class);
                        i.putExtra("url", video);
                    }
                }
            });
        }
        else if (embed != null) {
            Log.d("EMBED PROVIDER", embed.getProviderName()+"");
            Log.d("EMBED THUMBNAIL", url);
            vh.thumbnail.setVisibility(View.VISIBLE);
            vh.thumbnail.setImageAlpha(255);
            //vh.thumbnail.setVisibility(View.GONE);
            //vh.divider.setVisibility(View.VISIBLE);
            //vh.lySmallThumb.setVisibility(View.VISIBLE);
            uri = Uri.parse(url);
            vh.thumbnail.setImageURI(uri);
            //vh.thumbnailSmall.setImageURI(uri);
            vh.url.setText(submission.getUrl());
            vh.btnPlay.setVisibility(View.GONE);

            final String embedUrl = submission.getUrl();
            vh.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utils.launchIntent(context, domain, embedUrl);
                }
            });

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
                                Utils.launchIntent(context, submission.getDomain(), url);
                            }
                        });
                }
            });
        }
        else if (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png")) {
            final String img = url;
            Log.d("IMAGE URL", img);
            uri = Uri.parse(img);
            vh.thumbnail.setImageURI(uri);
            vh.thumbnail.setVisibility(View.VISIBLE);
            vh.thumbnail.setImageAlpha(255);
            vh.btnPlay.setVisibility(View.GONE);
            vh.lySmallThumb.setVisibility(View.GONE);
            vh.divider.setVisibility(View.GONE);

            vh.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("IMG", img);
                    Intent i = new Intent(context, PostImageActivity.class);
                    i.putExtra("url", img);
                    context.startActivity(i);
                }
            });
        }
        else {
            vh.thumbnail.setVisibility(View.GONE);
            vh.lySmallThumb.setVisibility(View.GONE);
            vh.btnPlay.setVisibility(View.GONE);

            if (!submission.isSelfPost())
                vh.divider.setVisibility(View.GONE);
        }
    }

    private String getImageUrl(String domain, String url) {
        Log.d("IMG URL", url);
        if (url.endsWith(".jpg") || url.endsWith(".png"))
            return url;
        return url + ".jpg";
    }

    private void sharePost(Submission submission) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_TEXT, submission.getTitle() + " " + submission.getUrl());
        i.setType("text/plain");
        context.startActivity(i);
    }

    private int getVote(Submission submission) {
        return submission.getVote().getValue();
    }

    private void setVote(final Submission submission, final SubmissionVH vh) {
        final int vote = getVote(submission);

        if (vote == VoteDirection.UPVOTE.getValue()) {
            vh.up.setImageResource(R.drawable.ic_arrow_upward_yellow_24dp);
            vh.down.setImageResource(R.drawable.ic_arrow_downward_grey_24dp);
        }
        else if (vote == VoteDirection.DOWNVOTE.getValue()) {
            vh.down.setImageResource(R.drawable.ic_arrow_downward_yellow_24dp);
            vh.up.setImageResource(R.drawable.ic_arrow_upward_grey_24dp);
        }
        else {
            vh.up.setImageResource(R.drawable.ic_arrow_upward_grey_24dp);
            vh.down.setImageResource(R.drawable.ic_arrow_downward_grey_24dp);
        }

        vh.up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App app = (App) context.getApplication();

                if (app.getClient().getAuthenticationMethod().isUserless())
                    Toast.makeText(context, "You need to be logged in to perform this action", Toast.LENGTH_SHORT).show();

                else {
                    if (vote == VoteDirection.NO_VOTE.getValue() || vote == VoteDirection.DOWNVOTE.getValue())
                        new VoteAsync(submission, vh).execute(VoteDirection.UPVOTE);
                    else
                        new VoteAsync(submission, vh).execute(VoteDirection.NO_VOTE);
                }
            }
        });
        vh.down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App app = (App) context.getApplication();

                if (app.getClient().getAuthenticationMethod().isUserless())
                    Toast.makeText(context, "You need to be logged in to perform this action", Toast.LENGTH_SHORT).show();

                else {
                    if (vote == VoteDirection.NO_VOTE.getValue() || vote == VoteDirection.UPVOTE.getValue())
                        new VoteAsync(submission, vh).execute(VoteDirection.DOWNVOTE);
                    else
                        new VoteAsync(submission, vh).execute(VoteDirection.NO_VOTE);
                }
            }
        });
    }

    class VoteAsync extends AsyncTask<VoteDirection, Void, Submission> {

        Submission submission;
        SubmissionVH vh;
        VoteDirection vote;

        public VoteAsync(Submission submission, SubmissionVH vh) {
            this.submission = submission;
            this.vh = vh;
        }

        @Override
        protected Submission doInBackground(VoteDirection... params) {
            vote = params[0];
            App app = (App) context.getApplication();
            AccountManager manager = new AccountManager(app.getClient());

            try {
                manager.vote(submission, params[0]);
                return app.getClient().getSubmission(submission.getId());
            } catch (ApiException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Submission submission) {
            super.onPostExecute(submission);

            if (submission != null) {
                int value = vote.getValue();
                Log.d("debug", "VOTED: " + value);

                if (value == VoteDirection.UPVOTE.getValue()) {
                    vh.up.setImageResource(R.drawable.ic_arrow_upward_yellow_24dp);
                    vh.down.setImageResource(R.drawable.ic_arrow_downward_grey_24dp);
                }
                else if (value == VoteDirection.DOWNVOTE.getValue()) {
                    vh.down.setImageResource(R.drawable.ic_arrow_downward_yellow_24dp);
                    vh.up.setImageResource(R.drawable.ic_arrow_upward_grey_24dp);
                }
                else {
                    vh.up.setImageResource(R.drawable.ic_arrow_upward_grey_24dp);
                    vh.down.setImageResource(R.drawable.ic_arrow_downward_grey_24dp);
                }

                set(vh.getAdapterPosition(), submission);

                Toast.makeText(context, "Voted on post successfully", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(context, "Something went wrong. Try again", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    class SaveSubmissionAsync extends AsyncTask<Void, Void, Boolean> {

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
    */

    /*
    class GiveGoldAsync extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            App app = (App) context.getApplication();
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
    */

    class GetGalleryDataAsync extends AsyncTask<String, Void, Data> {

        String id;
        RecyclerView.ViewHolder vh;

        public GetGalleryDataAsync(RecyclerView.ViewHolder vh) {
            this.vh  = vh;
        }

        @Override
        protected Data doInBackground(String... strings) {
            id = strings[0];
            return Utils.getGalleryData(id);
        }

        @Override
        protected void onPostExecute(Data data) {
            super.onPostExecute(data);
            if (data != null) {
                Log.d("Debug", "data NOT NULL");
                galleryData.put(id, data);

                if (data.images != null) {
                    ImgurImage[] images = data.images;
                    String[] list = new String[images.length];
                    for (int i = 0; i < list.length; i++) list[0] = images[i].link;
                    Uri uri = Uri.parse(images[0].link);

                    if (vh instanceof SubmissionVH)
                        ((SubmissionVH) vh).thumbnail.setImageURI(uri);
                    else if (vh instanceof CommentVH)
                        ((CommentVH) vh).thumbnail.setImageURI(uri);

                    notifyItemChanged(vh.getAdapterPosition());
                }
            }
            else {

            }
        }
    }
}
