package org.taitascioredev.adapters;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.taitascioredev.fractal.ImgurImage;
import org.taitascioredev.fractal.PostImageActivity;
import org.taitascioredev.fractal.R;
import org.taitascioredev.viewholders.GalleryImageVH;

/**
 * Created by saleventa7 on 7/7/2016.
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryImageVH> {

    private final AppCompatActivity context;
    private final ImgurImage[] images;

    public GalleryAdapter(AppCompatActivity context, ImgurImage[] images) {
        this.context = context;
        this.images  = images;
    }

    @Override
    public GalleryImageVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_image_row_layout, parent, false);
        return new GalleryImageVH(v);
    }

    @Override
    public void onBindViewHolder(GalleryImageVH vh, int position) {
        final ImgurImage img = images[position];
        Uri uri = Uri.parse(img.link);
        vh.image.setImageURI(uri);
        Log.d("Adapter", img.link);

        vh.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, PostImageActivity.class);
                i.putExtra("url", img.link);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.length;
    }
}
