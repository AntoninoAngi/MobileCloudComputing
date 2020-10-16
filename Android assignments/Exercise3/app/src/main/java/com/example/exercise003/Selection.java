package com.example.exercise003;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class Selection extends AppCompatActivity {

    ImageView imageView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        imageView = (ImageView) findViewById(R.id.showImage);
        textView = (TextView) findViewById(R.id.showAuthor);
        Bundle bundle = getIntent().getExtras();

        if (bundle != null){
            Picasso.get().load(bundle.getString("foto")).into(imageView);
            textView.setText(bundle.getString("autore"));
        }

    }
}
