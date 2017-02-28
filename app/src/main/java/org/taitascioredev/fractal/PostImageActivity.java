package org.taitascioredev.fractal;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by roberto on 23/04/15.
 */
public class PostImageActivity extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {

    private String mCurrentPhotoPath;
    private String url;
    private Bitmap bm;
    private List<File> tempFiles = new ArrayList<>();
    private PhotoViewAttacher mAttacher;

    private ImageView image;
    private ProgressWheel wheel;
    private Toolbar toolbar;
    private TextView toolbarTitle;

    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_image);

        //getSupportActionBar().hide();
        image = (ImageView) findViewById(R.id.image_post);
        wheel = (ProgressWheel) findViewById(R.id.progress_wheel);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        toolbarTitle = (TextView) findViewById(R.id.tv_title);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(null);
        toolbar.inflateMenu(R.menu.menu_post_image2);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_share:
                        String path = saveImage(bm, false);
                        if (path == null)
                            return true;
                        //galleryAddPic(path);
                        File f = new File(path);
                        tempFiles.add(f);
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:" + path));
                        i.setType("image/jpeg");
                        startActivity(i);
                        return true;
                    default:
                        return false;
                }
            }
        });
        url = getIntent().getStringExtra("url");
        new DownloadImage().execute(url);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (File f : tempFiles) f.delete();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.item_save:
                String path = saveImage(bm, true);
                if (path != null)
                    galleryAddPic(path);
                return true;
            case R.id.item_copy:
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("image_url", url);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "URL copied to the clipboard", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }

    private String saveImage(Bitmap bitmap, boolean permanent) {
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File dir = new File(root + "/Fractal");
        if (!dir.exists())
            dir.mkdirs();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";
        File file = new File (dir, imageFileName);
        if (file.exists()) {
            Log.d("debug", "IMAGE EXISTS");
            Toast.makeText(this, "Image already on device", Toast.LENGTH_SHORT).show();
            return null;
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();
            galleryAddPic(file.getAbsolutePath());
            if (permanent)
                Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show();
            Log.d("debug", "IMAGE SAVED SUCCESSFULLY");
            return file.getAbsolutePath();
        } catch (Exception e) {
            if (permanent)
                Toast.makeText(this, "Image could not be saved", Toast.LENGTH_SHORT).show();
            Log.d("debug", "ERROR SAVING IMAGE");
            return null;
        }
    }

    private void galleryAddPic(String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }

    private String getImageName(String path) {
        int index = path.lastIndexOf("/");
        return path.substring(index + 1);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDisplay(holder);
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //toolbar.setVisibility(View.GONE);
            wheel.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                InputStream is = new URL(params[0]).openStream();
                return BitmapFactory.decodeStream(is);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            wheel.setVisibility(View.GONE);
            //toolbar.setVisibility(View.VISIBLE);
            if (bitmap != null) {
                image.setImageBitmap(bitmap);
                mAttacher = new PhotoViewAttacher(image);
                mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                    @Override
                    public void onViewTap(View view, float v, float v2) {
                        ActionBar actionBar = getSupportActionBar();
                        if (actionBar.isShowing()) {
                            actionBar.hide();
                            toolbar.animate().translationYBy(toolbar.getHeight()).setInterpolator(new AccelerateInterpolator()).start();
                        } else {
                            actionBar.show();
                            toolbar.animate().translationYBy(-toolbar.getHeight()).setInterpolator(new DecelerateInterpolator()).start();
                        }
                    }
                });
                //toolbarTitle.setText(getImageName(url));
                bm = bitmap;
            }
        }
    }
}
