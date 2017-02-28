package org.taitascioredev.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.taitascioredev.fractal.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by roberto on 12/08/16.
 */
public class ActionAdapter extends RecyclerView.Adapter<ActionAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_icon) ImageView icon;
        @BindView(R.id.tv_action) TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.action_row_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int pos) {
        switch (pos) {
            case 0:
                vh.icon.setImageResource(R.drawable.ic_arrow_downward_grey_24dp);
                vh.text.setText("Down vote");

                break;
            case 1:
                vh.icon.setImageResource(R.drawable.ic_arrow_upward_grey_24dp);
                vh.text.setText("Up vote");

                break;
            case 2:
                vh.icon.setImageResource(R.drawable.ic_share_grey_24dp);
                vh.text.setText("Share");

                break;
            case 3:
                vh.icon.setImageResource(R.drawable.ic_content_copy_grey_24dp);
                vh.text.setText("Copy text");

                break;
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
