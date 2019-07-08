package edu.illinois.cs.cs125.robolectricsecurity.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

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

    File createTempFile() throws IOException {
        File file = new File(getFilesDir().getAbsolutePath() + "/test.txt");
        file.createNewFile();
        return file;
    }

    String readFileContents(File file) throws IOException {
        return new BufferedReader(new FileReader(file)).readLine();
    }

    char findFirstLowercase(String[] strings) throws Throwable {
        return Arrays.stream(strings).filter(s -> Character.isLowerCase(s.charAt(0)))
                .map(s -> s.charAt(0))
                .findFirst().orElseThrow(() -> new RuntimeException("Not found"));
    }

    int addToTwo(int number) {
        return Adder.add(number, 2);
    }

    String getAppName(Resources resources) {
        return resources.getString(R.string.app_name);
    }

    String getString(Resources resources, int id) {
        return resources.getString(id);
    }

    void tryListFiles() {
        for (File f : new File("/").listFiles()) {
            Toast.makeText(this, f.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    void tryWriteFile() throws IOException {
        new File("~/unsafe.txt").createNewFile();
    }

    void trySneakyWriteFile() throws IOException {
        new File(getFilesDir() + "/../../../unsafe.txt").createNewFile();
    }

    void tryHttpRequest() throws IOException {
        URL google = new URL("http://www.google.com/");
        google.openConnection().getContent();
    }

    void tryExit() {
        System.exit(125);
    }

    void tryRemoveSecurityManager() {
        System.setSecurityManager(null);
    }

    void tryRunProgram() throws IOException {
        Runtime.getRuntime().exec("calc");
    }

}
