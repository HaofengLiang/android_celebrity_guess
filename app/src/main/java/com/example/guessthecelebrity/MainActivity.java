package com.example.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView textView;
    private Button[] buttons = new Button[4];
    private ArrayList<Celebrity> celebrities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String url = "http://www.posh24.se/kandisar";
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        textView.setVisibility(View.INVISIBLE);
        textView.bringToFront();
        buttons[0] = findViewById(R.id.answer0);
        buttons[1] = findViewById(R.id.answer1);
        buttons[2] = findViewById(R.id.answer2);
        buttons[3] = findViewById(R.id.answer3);
        WebContentDownload task = new WebContentDownload();
        try {
            celebrities = task.execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getGame();
    }

    private class WebContentDownload extends AsyncTask<String, Void, ArrayList<Celebrity>> {

        @Override
        protected ArrayList<Celebrity> doInBackground(String... urls) {
            HttpURLConnection connection = null;
            StringBuffer buffer = new StringBuffer();
            try {
                URL url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream in  = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    buffer.append(current);
                    data = reader.read();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return getCelebrities(buffer.toString());
        }

        private ArrayList<Celebrity> getCelebrities(String webContent){
            Pattern pattern = Pattern.compile("<img src=\"(.*?)\" alt=\"(.*?)\"");
            Matcher matcher = pattern.matcher(webContent);
            ArrayList<Celebrity> celebrities = new ArrayList<>();
            while (matcher.find()) {
                try {
                    celebrities.add(new Celebrity(matcher.group(2), downloadImage(matcher.group(1))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return celebrities;
        }

        private Bitmap downloadImage(String imageUrl) throws IOException {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream in = connection.getInputStream();
            return BitmapFactory.decodeStream(in);
        }
    }

    private void getGame() {
        textView.setVisibility(View.INVISIBLE);
        Random rand = new Random();
        int targetIndex = rand.nextInt(celebrities.size());
        Celebrity target = celebrities.get(targetIndex);
        imageView.setImageBitmap(target.getImage());

        for (int i = 0; i < 4;) {
            int answerIndex = rand.nextInt(celebrities.size());
            if (answerIndex != targetIndex) {
                buttons[i].setText(celebrities.get(answerIndex).getName());
                buttons[i].setTag(false);
                i++;
            }
        }

        Button trueAnswer = buttons[rand.nextInt(buttons.length)];
        trueAnswer.setText(target.getName());
        trueAnswer.setTag(true);
    }

    public void answerSelected(View view) {
        boolean correct = (boolean) view.getTag();
        textViewAnimation(correct);
        if (correct) {
            getGame();
        }
    }

    private void textViewAnimation(boolean correct) {
        String display = correct ? "Correct!" : "Wrong :(";
        textView.setText(display);
        textView.setVisibility(View.VISIBLE);
        textView.setAlpha(0);
        textView.animate().alpha(1).setDuration(500);
    }


}
