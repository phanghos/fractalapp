package org.taitascioredev.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.facebook.drawee.view.SimpleDraweeView;

import org.taitascioredev.fractal.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by saleventa7 on 7/7/2016.
 */
public class GalleryImageVH extends RecyclerView.ViewHolder {

    @BindView(R.id.image) public SimpleDraweeView image;

    public GalleryImageVH(View v) {
        super(v);
        ButterKnife.bind(this, v);
    }
}
