package com.ruchiang.newsreader;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    static ArrayList<String> titles;
    static ArrayList<String> urlsList;
    static ArrayAdapter arrayAdapter;
    static SQLiteDatabase sqLiteDatabase;



//    public boolean isOnline() {
//        ConnectivityManager cm =
//                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo netInfo = cm.getActiveNetworkInfo();
//        return netInfo != null && netInfo.isConnectedOrConnecting();
//    }

    public void updateListView() {
        Cursor c = sqLiteDatabase.rawQuery("SELECT * FROM linktable", null);

        int urlIndex = c.getColumnIndex("url");
        int titleIndex = c.getColumnIndex("title");

        if (c.moveToFirst()) {
            titles.clear();
            urlsList.clear();

            do {
                titles.add(c.getString(titleIndex));
                urlsList.add(c.getString(urlIndex));
            } while (c.moveToNext());

            arrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titles = new ArrayList<>();
        urlsList = new ArrayList<>();



        try {
            sqLiteDatabase = this.openOrCreateDatabase("NEWS", MODE_PRIVATE, null);
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS linktable (title TEXT, url TEXT)");

        }catch (Exception e){
            Log.i("INFO","DB is not created");
        }

        if (DetectConnection.checkInternetConnection(this)) {
            Toast.makeText(getApplicationContext(),"internet is found",Toast.LENGTH_SHORT).show();

            try {
                URL url = new URL("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
                new fetchItem().execute(url);
            } catch (Exception e) {
            }

        }else{
            Toast.makeText(getApplicationContext(),"internet is not found",Toast.LENGTH_SHORT).show();
            updateListView();
        }


        ListView listView = (ListView) findViewById(R.id.listview);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,titles);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(getApplicationContext(), Web.class);

                intent.putExtra("index", i);

                startActivity(intent);

            }
        });




    }

    private class fetchItem extends AsyncTask<URL, Void, String > {
        String data;
        HttpURLConnection urlConnection;
        HttpURLConnection itemUrlConnection;
        InputStream in;
        int r;
        protected String doInBackground(URL... urls) {
            Log.i("INFO","do in background");

            try {
                urlConnection = (HttpURLConnection) urls[0].openConnection();
                in =  urlConnection.getInputStream();
                data = "";
                r = in.read();
                while (r != -1){
                    data += Character.toString((char)r);
                    r = in.read();
                }
                in.close();

                JSONArray jsonArray = new JSONArray(data);
                sqLiteDatabase.execSQL("DELETE FROM linktable");
                for(int i = 0; i< 10; i++){
                    String id = jsonArray.getString(i);
                    try {
                        URL itemurl = new URL("https://hacker-news.firebaseio.com/v0/item/" + id + ".json?print=pretty");
                        itemUrlConnection = (HttpURLConnection) itemurl.openConnection();
                        in = itemUrlConnection.getInputStream();
                        data = "";
                        r = in.read();
                        while(r != -1) {
                            data += Character.toString((char) r);
                            r = in.read();
                        }
                        in.close();


                        JSONObject jsonObject = new JSONObject(data);



                        String cmd = "INSERT INTO linktable (title, url) VALUES(?,?);";
                        SQLiteStatement statement = sqLiteDatabase.compileStatement(cmd);
                        statement.bindString(1, jsonObject.getString("title"));
                        if(jsonObject.isNull("url")) {
                            statement.bindString(2, "URL not found");
                        }else{
                            statement.bindString(2, jsonObject.getString("url"));
                        }
                        statement.execute();

                    }catch (Exception e){

                        e.printStackTrace();
                    }

                }
            }catch (Exception e){
                updateListView();

                Log.i("INFO","In this exception");
            }

            return "success";
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {
            updateListView();

            Log.i("INFO","In post execute");
        }
    }


}
