package org.taitascioredev.adapters;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.OEmbed;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import org.ocpsoft.prettytime.PrettyTime;
import org.taitascioredev.fractal.App;
import org.taitascioredev.fractal.CommentInfo;
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
import org.taitascioredev.viewholders.EmptyVH;
import org.taitascioredev.viewholders.SubmissionVH;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

/**
 * Created by roberto on 30/10/15.
 */
public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements PopupMenu.OnMenuItemClickListener {

    private static final int TYPE_EMPTY = 0;
    private static final int TYPE_SUBMISSION = 1;
    private static final int TYPE_COMMENT = 2;
    private static final String BASE_URL = "https://www.reddit.com";

    String baseUrl;

    AppCompatActivity context;
    List<Comment> objects;
    List<CommentInfo> infoList;
    Map<String, Data> galleryData;
    Submission submission;
    Comment comment;
    String pref;
    int pos;

    RecyclerView recyclerView;

    OnSubmissionVoteListener mListener;

    public interface OnSubmissionVoteListener {

        void onVoted(int pos, Submission s);
    }

    public CommentAdapter(AppCompatActivity context, List<Comment> objects, List<CommentInfo> infoList, Submission submission) {
        this.context     = context;
        this.objects     = objects;
        this.infoList    = infoList;
        this.submission  = submission;
        this.galleryData = new HashMap<String, Data>();
        this.pref        = Utils.getDisplayPreference(context);
    }

    public void setPosition(int pos) {
        this.pos = pos;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;

        switch (viewType) {
            case TYPE_EMPTY:
                v = LayoutInflater.from(context).inflate(R.layout.empty, parent, false);
                return new EmptyVH(v);
            case TYPE_COMMENT:
                v = LayoutInflater.from(context).inflate(R.layout.comment_row_layout, parent, false);
                return new CommentVH(v);
            case TYPE_SUBMISSION:
                v = LayoutInflater.from(context).inflate(R.layout.row_layout_list, parent, false);
                return new SubmissionVH(v);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder vh, final int position) {
        switch (vh.getItemViewType()) {
            case TYPE_EMPTY:
                ((EmptyVH) vh).empty.setText("No comments on this post");
                break;
            case TYPE_COMMENT:
                //CommentNode node = get(position - 1);
                final Comment comment = get(position - 1);
                final CommentVH cVH = (CommentVH) vh;

                cVH.author.setText(comment.getAuthor());

                final Spanned spanned = Html.fromHtml(comment.getBodyHtml());
                cVH.body.setText(Utils.setUrlSpans(context, cVH, spanned, false, true, CommentAdapter.this));
                cVH.body.setMovementMethod(LinkMovementMethod.getInstance());
                cVH.body.setOnTouchListener(new CustomLinkMovementMethod());

                cVH.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //ActionsBottomSheet bottomSheet = new ActionsBottomSheet();
                        //bottomSheet.show(context.getSupportFragmentManager(), "actions");
                        showActionsDialog(comment);
                    }
                });

                cVH.body.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //ActionsBottomSheet bottomSheet = new ActionsBottomSheet();
                        //bottomSheet.show(context.getSupportFragmentManager(), "actions");
                        showActionsDialog(comment);
                    }
                });

                cVH.body.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        copy(comment);
                        return true;
                    }
                });

                int indentation = getIndentation(comment.getId());
                if (indentation > 0) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) cVH.line.getLayoutParams();
                    int pixels = Utils.dpToPixels(context, indentation - 1) * 5;
                    int color = Color.parseColor(Constants.INDENTATION_COLORS[(indentation - 1) % Constants.INDENTATION_COLORS.length]);
                    cVH.line.setBackgroundColor(color);
                    cVH.line.setVisibility(View.VISIBLE);
                    params = (ViewGroup.MarginLayoutParams) cVH.layout.getLayoutParams();
                    params.leftMargin = pixels;
                    cVH.layout.setLayoutParams(params);

                }
                else {
                    cVH.line.setVisibility(View.GONE);
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) cVH.layout.getLayoutParams();
                    params.leftMargin = 0;
                    cVH.layout.setLayoutParams(params);
                }

                break;
            case TYPE_SUBMISSION:
                SubmissionVH sVH = (SubmissionVH) vh;

                setDate(submission, sVH);
                setTitle(submission, sVH);
                setAuthor(submission, sVH);
                setSubreddit(submission, sVH);
                setBody(submission, sVH);
                setThumbnail(submission, sVH);
                setVote(submission, sVH);
                sVH.score.setText(submission.getScore()+"");
                sVH.comments.setText(submission.getCommentCount() + " comments");

                sVH.share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sharePost(submission);
                    }
                });

                break;
        }
    }

    @Override
    public int getItemCount() {
        int size = objects.size();
        return size > 0 ? size + 1 : 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (objects.size() > 0 && position > 0) return TYPE_COMMENT;
        else if (position == 0) return TYPE_SUBMISSION;
        return TYPE_EMPTY;
    }

    public Comment get(int pos) {
        return objects.get(pos);
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
        notifyItemChanged(0);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reply:

                return true;
            case R.id.permalink_comment:
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("permalink", baseUrl + comment.getId());
                clipboard.setPrimaryClip(data);
                Toast.makeText(context, "Permalink copied to the clipboard", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.parent:

                return true;
            default:
                return false;
        }
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    private int getIndentation(String id) {
        for (CommentInfo comment : infoList)
            if (comment.id.equals(id))
                return comment.indentation;
        return -1;
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
        CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(
                TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));

        if (!submission.isSelfPost()) {
            str = submission.getTitle() + " (" + submission.getDomain() + ")";
            ssb = new SpannableStringBuilder(str);
            int index = str.lastIndexOf("(");
            ForegroundColorSpan boldSpan = new ForegroundColorSpan(
                    ContextCompat.getColor(context, R.color.colorDomain));
            ssb.setSpan(boldSpan, index, str.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
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
        vh.title.setOnTouchListener(new CustomLinkMovementMethod());

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
                    context.startActivity(launchIntent(submission.getDomain(), url));
                }
            });
            */
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

    private void setBody(Submission submission, final SubmissionVH vh) {
        if (submission.isSelfPost() && submission.getSelftext().length() > 0) {
            final Spanned spanned = Html.fromHtml(submission.getSelftextHtml());
            vh.body.setText(Utils.setUrlSpans(context, spanned, false));
            vh.body.setVisibility(View.VISIBLE);
            vh.body.setMovementMethod(LinkMovementMethod.getInstance());
            vh.body.setOnTouchListener(new CustomLinkMovementMethod());
            vh.divider.setVisibility(View.VISIBLE);
        }
        else {
            vh.body.setVisibility(View.GONE);
            vh.divider.setVisibility(View.GONE);
        }
    }

    private void setThumbnail(final Submission submission, final SubmissionVH vh) {
        String url = submission.getUrl();
        OEmbed embed = submission.getOEmbedMedia();
        String domain = submission.getDomain();
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
                    Utils.launchIntent(context, embedUrl);
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
                                launchIntent(submission.getDomain(), url);
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

    private void launchIntent(String domain, String url) {
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
                context.startActivity(i);
            } catch (ActivityNotFoundException e) {
                i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(i);
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
                context.startActivity(i);
            } catch (ActivityNotFoundException e) {
                i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(i);
            }
        }
        else {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
            builder.setCloseButtonIcon(BitmapFactory.decodeResource(
                    context.getResources(), R.drawable.ic_action_back));
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(context, Uri.parse(url));
        }
    }

    private void showActionsDialog(final Comment comment) {
        final MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(context);

        /*
        adapter.add(new MaterialSimpleListItem.Builder(context)
                .content("Down vote")
                .icon(R.drawable.ic_arrow_downward_grey_24dp)
                .backgroundColor(Color.WHITE)
                .build());
        adapter.add(new MaterialSimpleListItem.Builder(context)
                .content("Up vote")
                .icon(R.drawable.ic_arrow_upward_grey_24dp)
                .backgroundColor(Color.WHITE)
                .build());
                */
        adapter.add(new MaterialSimpleListItem.Builder(context)
                .content("Share")
                .icon(R.drawable.ic_share_grey_24dp)
                .backgroundColor(Color.WHITE)
                .build());
        adapter.add(new MaterialSimpleListItem.Builder(context)
                .content("Copy text")
                .icon(R.drawable.ic_content_copy_grey_24dp)
                .backgroundColor(Color.WHITE)
                .build());

        new MaterialDialog.Builder(context)
                .theme(Theme.LIGHT)
                .title("Choose action")
                .adapter(adapter, new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        switch (which) {
                            case 0:
                                shareComment(comment);
                                break;
                            case 1:
                                copy(comment);
                                break;
                        }

                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void sharePost(Submission submission) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_TEXT, submission.getTitle() + " " + submission.getUrl());
        i.setType("text/plain");
        context.startActivity(i);
    }

    private void shareComment(Comment comment) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_TEXT, comment.getBody() + " " + getCommentUrl(comment));
        i.setType("text/plain");
        context.startActivity(i);
    }

    private String getCommentUrl(Comment comment) {
        return BASE_URL + submission.getPermalink() + comment.getId();
    }

    private void copy(Comment comment) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData data = ClipData.newPlainText("comment", comment.getBody());
        clipboard.setPrimaryClip(data);

        Toast.makeText(context, "Comment copied to the clipboard", Toast.LENGTH_SHORT).show();
    }

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

                setSubmission(submission);
                try {
                    mListener = (OnSubmissionVoteListener) context;
                    mListener.onVoted(pos, submission);
                } catch (ClassCastException e) {

                }

                Toast.makeText(context, "Voted on post successfully", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(context, "Something went wrong. Try again", Toast.LENGTH_SHORT).show();
        }
    }
}