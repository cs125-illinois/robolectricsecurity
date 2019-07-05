package edu.illinois.cs.cs125.robolectricsecurity.test;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private LinearLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLayout = findViewById(R.id.mainLayout);
    }

    void createUI() {
        TextView label = new TextView(this);
        label.setText("Not clicked yet");
        label.setTag("label");
        Button button = new Button(this);
        button.setText("Click");
        button.setOnClickListener(unused -> label.setText("Clicked"));
        button.setTag("button");
        mainLayout.addView(label);
        mainLayout.addView(button);
    }

    void createImageView() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        ImageView imageView = new ImageView(this);
        imageView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
        imageView.setTag("image");
        mainLayout.addView(imageView);
    }

}
