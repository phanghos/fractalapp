package org.taitascioredev.viewholders;

import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.facebook.drawee.view.SimpleDraweeView;

import org.taitascioredev.fractal.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by roberto on 14/11/15.
 */
public class SubmissionVH extends RecyclerView.ViewHolder {

    @Nullable
    @BindView(R.id.card_view) public CardView card;

    @Nullable
    @BindView(R.id.ripple) public MaterialRippleLayout ripple;

    @BindView(R.id.layout_title) public LinearLayout layout;
    @BindView(R.id.layout_small_thumbnail) public RelativeLayout lySmallThumb;
    @BindView(R.id.btn_play) public ImageView btnPlay;
    @BindView(R.id.tv_title) public TextView title;
    @BindView(R.id.tv_date) public TextView date;
    @BindView(R.id.tv_author) public TextView author;
    @BindView(R.id.tv_subreddit) public TextView subreddit;
    @BindView(R.id.iv_thumbnail) public SimpleDraweeView thumbnail;
    @BindView(R.id.divider) public View divider;
    @BindView(R.id.tv_url) public TextView url;
    @BindView(R.id.tv_body) public TextView body;
    @BindView(R.id.tv_score) public TextView score;
    @BindView(R.id.tv_comments) public TextView comments;
    @BindView(R.id.iv_up) public ImageView up;
    @BindView(R.id.iv_down) public ImageView down;
    @BindView(R.id.iv_share) public ImageView share;

    public SubmissionVH(View v) {
        super(v);
        ButterKnife.bind(this, v);
    }
}