package org.taitascioredev.viewholders;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.facebook.drawee.view.SimpleDraweeView;

import org.taitascioredev.fractal.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by saleventa7 on 7/5/2016.
 */
public class CommentVH extends RecyclerView.ViewHolder {

    @BindView(R.id.ripple) public MaterialRippleLayout ripple;
    @BindView(R.id.indentation_line) public View line;
    @BindView(R.id.layout) public LinearLayout layout;
    @BindView(R.id.tv_author) public TextView author;
    @BindView(R.id.tv_body) public TextView body;
    @BindView(R.id.iv_thumbnail) public SimpleDraweeView thumbnail;
    @BindView(R.id.btn_play) public ImageView btnPlay;

    public CommentVH(View v) {
        super(v);
        ButterKnife.bind(this, v);
    }
}