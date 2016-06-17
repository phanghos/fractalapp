package org.taitascioredev.viewholders;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.taitascioredev.fractal.R;

/**
 * Created by roberto on 15/11/15.
 */
public class CommentViewHolder extends RecyclerView.ViewHolder {

    public CardView card;
    public RelativeLayout layout;
    public TextView title;
    public TextView votes;
    public ImageView up;
    public ImageView down;
    public TextView comments;
    public TextView author;
    public TextView subreddit;
    public TextView date;
    public TextView commentAuthor;
    public TextView commentBody;

    public ImageView popup;
    public ImageView share;
    public ImageView save;
    public ImageView hide;

    public CommentViewHolder(View v) {
        super(v);

        card = (CardView) v.findViewById(R.id.card_view);
        layout = (RelativeLayout) v.findViewById(R.id.layout_title);
        title = (TextView) v.findViewById(R.id.tv_title);
        votes = (TextView) v.findViewById(R.id.text_votes);
        up = (ImageView) v.findViewById(R.id.image_up);
        down = (ImageView) v.findViewById(R.id.image_down);
        comments = (TextView) v.findViewById(R.id.text_comments);
        author = (TextView) v.findViewById(R.id.tv_author);
        subreddit = (TextView) v.findViewById(R.id.tv_subreddit);
        date = (TextView) v.findViewById(R.id.tv_date);
        commentAuthor = (TextView) v.findViewById(R.id.text_comment_author);
        commentBody = (TextView) v.findViewById(R.id.text_comment_body);

        popup = (ImageView) v.findViewById(R.id.iv_options);
        share = (ImageView) v.findViewById(R.id.image_share);
        save = (ImageView) v.findViewById(R.id.image_save);
        hide = (ImageView) v.findViewById(R.id.image_hide);
    }
}
