package org.taitascioredev.viewholders;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.taitascioredev.fractal.R;

/**
 * Created by roberto on 14/11/15.
 */
public class SubmissionViewHolder extends RecyclerView.ViewHolder {

    public CardView card;
    public LinearLayout layout;
    public LinearLayout layoutActions;
    public RelativeLayout lySmallThumb;
    public TextView title;
    public TextView gif;
    public TextView votes;
    public ImageView up;
    public ImageView down;
    public TextView author;
    public TextView subreddit;
    public TextView date;
    public View divider;
    public SimpleDraweeView thumbnailSmall;
    public TextView url;
    public TextView body;
    public TextView score;
    public TextView comments;
    //public ImageView thumbnail;
    public SimpleDraweeView thumbnail;

    public ImageView share;
    public ImageView save;
    public ImageView hide;

    public SubmissionViewHolder(View v) {
        super(v);

        card = (CardView) v.findViewById(R.id.card_view);
        layout = (LinearLayout) v.findViewById(R.id.layout_title);
        layoutActions = (LinearLayout) v.findViewById(R.id.layout_actions);
        lySmallThumb = (RelativeLayout) v.findViewById(R.id.layout_small_thumbnail);
        title = (TextView) v.findViewById(R.id.tv_title);
        gif = (TextView) v.findViewById(R.id.tv_gif);
        votes = (TextView) v.findViewById(R.id.text_votes);
        up = (ImageView) v.findViewById(R.id.image_up);
        down = (ImageView) v.findViewById(R.id.image_down);
        author = (TextView) v.findViewById(R.id.tv_author);
        subreddit = (TextView) v.findViewById(R.id.tv_subreddit);
        date = (TextView) v.findViewById(R.id.tv_date);
        divider = v.findViewById(R.id.divider);
        thumbnailSmall = (SimpleDraweeView) v.findViewById(R.id.thumbnail);
        url = (TextView) v.findViewById(R.id.text_url);
        body = (TextView) v.findViewById(R.id.text_body);
        //thumbnail = (ImageView) v.findViewById(R.id.image_thumbnail);
        thumbnail = (SimpleDraweeView) v.findViewById(R.id.image_thumbnail);
        score = (TextView) v.findViewById(R.id.tv_score);
        comments = (TextView) v.findViewById(R.id.tv_comments);

        share = (ImageView) v.findViewById(R.id.iv_share);
        save = (ImageView) v.findViewById(R.id.image_save);
        hide = (ImageView) v.findViewById(R.id.image_hide);
    }
}
