package com.shreeganesh.movies;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpConnection;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView tvData;
    private EditText etMovieName;
    private ImageView ivThumbNail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvData = (TextView) findViewById(R.id.tvData);
        etMovieName = (EditText) findViewById(R.id.etMovieName);
        ivThumbNail = (ImageView)findViewById(R.id.movieThumbnail);
        Button btnHit = (Button) findViewById(R.id.btnHit);
        btnHit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tokens = etMovieName.getText().toString().replace(" ","+");
                String url = "http://www.omdbapi.com/?t="+tokens;
                new JSONTask().execute(url);
                Log.i("MyApp","Button clicked");
            }
        });
    }
    private class JSONTask extends AsyncTask<String, String, JSONObject>{
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        @Override
        protected JSONObject doInBackground(String... params) {
            Log.i("MyApp","Doing work in background...");
            try {
                Log.i("MyApp","Hitting on "+params[0]);
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer stringBuffer = new StringBuffer();
                String line = "";
                while ((line=reader.readLine())!=null){
                    stringBuffer.append(line);
                }
                //return stringBuffer.toString();
                JSONObject parentObject = new JSONObject(stringBuffer.toString());
                //check for response
                if(parentObject.getString("Response").equals("True"))
                    return  parentObject;
                else
                    return null;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(connection!=null){
                    connection.disconnect();
                }
                try{
                    if(reader!=null) {
                        reader.close();
                    }
                }catch (IOException io){

                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject parentObject) {
            super.onPostExecute(parentObject);
            try {
                if(parentObject!=null){
                    Log.i("MyApp","Result returned..phew!");
                    String title = parentObject.getString("Title");
                    String year = parentObject.getString("Year");
                    String thumbnailUrl = parentObject.getString("Poster");
                    tvData.setText(title+" - "+year);
                    if (thumbnailUrl != null)
                        new ImageLoadTask().execute(thumbnailUrl);
                }else{
                    Log.i("MyApp","Movie not found!");
                    tvData.setText("Movie not found!");
                    Drawable res = getResources().getDrawable(R.drawable.movie_image);
                    ivThumbNail.setImageDrawable(res);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private class ImageLoadTask extends  AsyncTask<String, Void, Bitmap>{
        HttpURLConnection connection = null;
        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                Log.i("MyApp","Hitting on "+params[0]);
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(connection!=null){
                    connection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap thumbnail) {
            super.onPostExecute(thumbnail);
            if(thumbnail!=null)
                ivThumbNail.setImageBitmap(thumbnail);
            else{
                Drawable res = getResources().getDrawable(R.drawable.movie_image);
                ivThumbNail.setImageDrawable(res);
            }
        }
    }

}


