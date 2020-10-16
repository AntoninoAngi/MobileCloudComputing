package com.example.exercise003;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    String jsonURL;
    ListView listView;
    ArrayList<PhotoModel> photosModelArrayList;
    PhotoAdapter photoAdapter;
    EditText url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.lv);
        url = (EditText) findViewById(R.id.editText);

    }

    public void loadClick(View v){
        jsonURL = url.getText().toString();
        fetchJSON();

    }

@SuppressLint("StaticFieldLeak")
    private void fetchJSON(){
        new AsyncTask<Void, Void, String>(){
            protected String doInBackground(Void[] params) {
                String response="";
                HashMap<String, String> map=new HashMap<>();
                try {
                    HttpRequest req = new HttpRequest(jsonURL);
                    response = req.prepare().sendAndReadString();
                } catch (Exception e) {
                    response=e.getMessage();
                }
                return response;
            }
            protected void onPostExecute(String result) {
                Log.d("newwss",result);
                onTaskCompleted(result,1);
            }
        }.execute();
    }

    public void onTaskCompleted(String response, int serviceCode) {
        Log.d("jsonresponse", response);
        switch (serviceCode) {
            case 1:

                photosModelArrayList = getInfo(response);
                photoAdapter = new PhotoAdapter(this,photosModelArrayList);
                listView.setAdapter(photoAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        Intent intent = new Intent (MainActivity.this, Selection.class);
                        intent.putExtra("foto", photosModelArrayList.get(position).getImgURL());
                        intent.putExtra("autore", photosModelArrayList.get(position).getName());
                        startActivity(intent);

                    }
                });
        }
    }

    public ArrayList<PhotoModel> getInfo(String response) {
        ArrayList<PhotoModel> photoModelArrayList = new ArrayList<>();
        try {

                JSONArray dataArray = new JSONArray(response);

                for (int i = 0; i < dataArray.length(); i++) {

                    PhotoModel photosModel = new PhotoModel();
                    JSONObject dataobj = dataArray.getJSONObject(i);
                    photosModel.setName(dataobj.getString("author"));
                    photosModel.setImgURL(dataobj.getString("photo"));
                    photoModelArrayList.add(photosModel);

                }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return photoModelArrayList;
    }

}