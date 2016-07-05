package org.taitascioredev.fractal;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * Created by roberto on 03/07/16.
 */
public class PostVideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_video);

        String url = getIntent().getStringExtra("url");
        url = url.replaceAll("gifv", "mp4");
        Log.d("URL", url);

        /*
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        holder = surfaceView.getHolder();
        holder.addCallback(this);
        */

        VideoView videoView = (VideoView) findViewById(R.id.video_view);
        Uri uri = Uri.parse(url);
        videoView.setVideoURI(uri);
        MediaController controller = new MediaController(this);
        controller.setAnchorView(videoView);
        videoView.setMediaController(controller);
        videoView.start();
    }
}
