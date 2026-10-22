package com.example.lab6;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.lab6.model.MediaModel;

public class MediaDetailActivity extends AppCompatActivity {

    private ImageView ivFullImage;
    private VideoView videoView;
    private ImageButton btnBack;
    private TextView tvMediaName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_detail);

        ivFullImage = findViewById(R.id.ivFullImage);
        videoView = findViewById(R.id.videoView);
        btnBack = findViewById(R.id.btnBackMedia);
        tvMediaName = findViewById(R.id.tvMediaName);

        btnBack.setOnClickListener(v -> finish());

        MediaModel media = (MediaModel) getIntent().getSerializableExtra("media_item");
        if (media != null) {
            tvMediaName.setText(media.getDisplayName());
            Uri uri = media.getUri();

            if (media.isVideo()) {
                ivFullImage.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);

                MediaController mediaController = new MediaController(this);
                mediaController.setAnchorView(videoView);
                videoView.setMediaController(mediaController);
                videoView.setVideoURI(uri);
                videoView.start();
            } else {
                videoView.setVisibility(View.GONE);
                ivFullImage.setVisibility(View.VISIBLE);

                Glide.with(this)
                        .load(uri)
                        .into(ivFullImage);
            }
        }
    }
}