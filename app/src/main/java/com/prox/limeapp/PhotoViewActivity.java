package com.prox.limeapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class PhotoViewActivity extends AppCompatActivity {

    PhotoView photoView;

    String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);

        imageUrl = getIntent().getStringExtra("image");
        photoView = findViewById(R.id.photoView);

    }

    @Override
    protected void onStart() {
        super.onStart();

        Picasso.get().load(imageUrl).into(photoView, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(Exception e) {
                Picasso.get().load(imageUrl).into(photoView);
            }
        });

    }
}