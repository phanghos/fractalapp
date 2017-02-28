package org.taitascioredev.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.taitascioredev.fractal.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by roberto on 28/08/16.
 */
public class EmptyVH extends RecyclerView.ViewHolder {

    @BindView(R.id.tv_empty) public TextView empty;

    public EmptyVH(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
