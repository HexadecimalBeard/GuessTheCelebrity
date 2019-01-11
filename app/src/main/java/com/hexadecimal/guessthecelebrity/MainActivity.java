package com.hexadecimal.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebURLs = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    int chosenCeleb = 0;
    ImageView imageView;
    String[] answers = new String[4];
    int locationOfCorrectAnswer = 0;
    Button button0;
    Button button1;
    Button button2;
    Button button3;

    public void celebChosen(View view){

        if(view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))){         // button'un tag'ını - view.getTag().toString - bu sekilde aldik
            Toast.makeText(getApplicationContext(),"Correct!", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(getApplicationContext(),"Wrong! It was " + " " + celebNames.get(chosenCeleb), Toast.LENGTH_SHORT).show();
        }
        newQuestion(); // her secim sonunda yeni soru urettik
    }

    public static class ImageDownloader extends AsyncTask<String, Void, Bitmap>{
        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
                return myBitmap;

            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }
    public static class DownloadTask extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls) {

            String result = "";

            URL url;
            HttpURLConnection urlConnection = null;
            try{
                url = new URL(urls[0]);                 // indirilecek url'yi aldik
                urlConnection= (HttpURLConnection) url.openConnection();  // bir browser gibi url'yi actik

                InputStream in = urlConnection.getInputStream();            // gelen verileri almak icin input stream olusturduk
                InputStreamReader reader = new InputStreamReader(in);       // gelen verileri okumak icin olusturduk

                int data = reader.read();                                   // karakter karakter gelen verileri okudu

                while (data != -1){                                 // gelen veriler -1 olmadigi surece verileri kaydetmeye devam edecegiz
                    char current = (char) data;                     // karakter karakter okudugu icin her gelen data'yi char'e (karakter) cevirdik
                    result += current;
                    data = reader.read();
                }
                return result;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }
    public void newQuestion(){
        try {
            Random rand = new Random();
            chosenCeleb = rand.nextInt(celebURLs.size());      // 0 ile indirilen celebrity resimlerinin sayisina kadar bir random sayi urettik

            ImageDownloader imageTask = new ImageDownloader();
            Bitmap celebImage = imageTask.execute(celebURLs.get(chosenCeleb)).get();  // secilen celebrity'nin bulundugu url'yi verdik

            imageView.setImageBitmap(celebImage);
            locationOfCorrectAnswer = rand.nextInt(4);
            int incorrectAnswerLocation;

            for (int i = 0; i < 4; i++) {
                if (i == locationOfCorrectAnswer) {
                    answers[i] = celebNames.get(chosenCeleb);
                } else {
                    incorrectAnswerLocation = rand.nextInt(celebURLs.size());

                    while (incorrectAnswerLocation == chosenCeleb) {
                        incorrectAnswerLocation = rand.nextInt(celebURLs.size());
                    }
                    answers[i] = celebNames.get(incorrectAnswerLocation);
                }
            }
            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);
            button3.setText(answers[3]);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloadTask task = new DownloadTask();
        String result = null;
        imageView = findViewById(R.id.imageView);
        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        
        try{
            result = task.execute("http://www.posh24.se/kandisar").get();

            String[] splitResult = result.split("<div class=\"listedArticles\">");   // site html kodunda sadece bir tane <div class="channelList">
                                                                                        // oldugu icin buradan sonra ayirmaya basladik
            Pattern p = Pattern.compile("img src=\"(.*?)\"");          // <img src=\ ile baslayan tüm kısımlari alir
            Matcher m = p.matcher(splitResult[0]);

            while (m.find()){
                celebURLs.add(m.group(1));          // celebrity resimlerini depoladigimiz yer
            }

            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);

            while (m.find()){
                celebNames.add(m.group(1));        // celebrity isimlerini depoladigimiz yer
            }
            newQuestion();              // veriler alindiktan sonra soru urettik

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
