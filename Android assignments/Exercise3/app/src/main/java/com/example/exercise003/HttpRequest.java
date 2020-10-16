package com.example.exercise003;

import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
        //Supported HttpRequest methods
        public static enum Method{
            POST,PUT,GET;
        }
        URL url;
        HttpURLConnection con;
        OutputStream os;

        public HttpRequest(URL url)throws IOException{
            this.url=url;
            con = (HttpURLConnection)this.url.openConnection();
        }

        public HttpRequest(String url)throws IOException{ this(new URL(url)); Log.d("parameters", url); }


        private void prepareAll(Method method)throws IOException{
            con.setDoInput(true);
            con.setRequestMethod(method.name());
            if(method==Method.POST||method==Method.PUT){
                con.setDoOutput(true);
                os = con.getOutputStream();
            }
        }

         public HttpRequest prepare() throws IOException{
            prepareAll(Method.GET);
            return this;
         }

        public HttpRequest withData(String query) throws IOException{
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.close();
            return this;
        }
        public HttpRequest withData(HashMap<String,String> params) throws IOException{
            StringBuilder result=new StringBuilder();
            for(Map.Entry<String,String>entry : params.entrySet()){
                result.append((result.length()>0?"&":"")+entry.getKey()+"="+entry.getValue());//appends: key=value (for first param) OR &key=value(second and more)
                Log.d("parameters",entry.getKey()+"  ===>  "+ entry.getValue());
            }
            withData(result.toString());
            return this;
        }
        public String sendAndReadString() throws IOException{
            BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response=new StringBuilder();
            for(String line;(line=br.readLine())!=null;)response.append(line+"\n");
            Log.d("ressss",response.toString());
            return response.toString();
        }
}
