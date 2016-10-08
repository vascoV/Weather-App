package uk.ac.uclan.simpleweatherapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class CurrentWeather extends AppCompatActivity {

    public static final String TAG = "networkingandjson";

    //<---Declear all the textViews and ImageView--->
    private TextView cityView;
  //  private TextView iconView;
   // private ImageView imageView;
    private ImageView weatherImageView;
    private TextView weatherView;
    private ImageView map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_weather);

        //imageView = (ImageView) findViewById(R.id.icon);
      //  iconView = (TextView) findViewById(R.id.icon_field);
        weatherImageView = (ImageView) findViewById(R.id.icon_field);
        cityView = (TextView) findViewById(R.id.city_field);
        weatherView = (TextView) findViewById(R.id.weather_field);
        map =(ImageView) findViewById(R.id.mapImage);
    }

    // --- Action Bar functionality ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.action_search:
                startActivity(new Intent(CurrentWeather.this, AddCity.class));
            case R.id.favorites:
                startActivity(new Intent(CurrentWeather.this, ListOfCities.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // <<-------- JSON Connection -------->>
    protected void onResume() {
        super.onResume();
        String locationName = getIntent().getStringExtra("location"); // takes the location
        UpdateWeather(locationName);
    }

    private void UpdateWeather(final String locationName) {
        String URLadress = createUrl(locationName);
        new DownloadWebpageTask().execute(URLadress);

    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException ioe) {
                return "Error: " + ioe;
            }
        }

       @Override
       protected void onPostExecute(String result) {

           cityView.setText(processDataCity(result));
           weatherView.setText(processDataWeather(result));
       }
    }


    //<---Weather Api create URL--->
    private String createUrl(final String locationName) {
        try{
        return "http://api.openweathermap.org/data/2.5/weather?q=" + URLEncoder.encode(locationName, "UTF-8") + "&units=metric" + "&APPID=f9c77b9f1869000303544f07e9974062";
        } catch (UnsupportedEncodingException uee) {
         Log.e(TAG, uee.getMessage());
         return null;
         }
    }

    private String downloadUrl(final String urlAddress) throws IOException {
        InputStream inputStream = null;
        try {
            URL url = new URL(urlAddress);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "The response code is: " + response);
            inputStream = conn.getInputStream();

            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
            final StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            return stringBuilder.toString();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    // <---Jason process Data Take specific information only--->
    private String processDataCity(String json){
        String jsonResult = "";

        try {

            JSONObject JsonObject = new JSONObject(json);


            String cod = jsonHelperGetString(JsonObject, "cod");
            if(cod != null){
                if(cod.equals("200")){
                    jsonResult += jsonHelperGetString(JsonObject, "name")+ "," + " ";
                    final String name = jsonHelperGetString(JsonObject, "name") ;
                    JSONObject sys = jsonHelperGetJSONObject(JsonObject, "sys");
                    //<---Wikipedia information for the selected city--->
                    Button wiki = (Button) findViewById(R.id.wikipedia);
                    wiki.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri uri = Uri.parse("https://en.wikipedia.org/wiki/" + name );
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }
                    });
                    if(sys != null){
                        jsonResult += jsonHelperGetString(sys, "country") + "\n";
                    }
                    jsonResult += " ";
                    //<---Temperature--->
                    JSONObject main = jsonHelperGetJSONObject(JsonObject, "main");
                    if(main != null){
                        String temperature = jsonHelperGetString(main, "temp");
                        Double temp = Double.parseDouble(temperature);
                        temperature = String.format("%3.0f°C",temp);
                        jsonResult += temperature+ "\n";
                    }

                    JSONObject coord = jsonHelperGetJSONObject(JsonObject, "coord");
                    if(coord != null){
                        final String lon = jsonHelperGetString(coord, "lon");
                        final String lat = jsonHelperGetString(coord, "lat");
                        //<---GoogleMaps Current city Location---?
                        map.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri uri = Uri.parse("https://www.google.com/maps/@" + lat + "," + lon + ",10z");
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        });
                    }
                    jsonResult += "\n";

                }else if(cod.equals("404")){
                    String message = jsonHelperGetString(JsonObject, "message");
                    jsonResult += "cod 404: " + message;
                }
            }else{
                jsonResult += "cod == null";
            }

        } catch (JSONException e) {
            e.printStackTrace();
            jsonResult += e.getMessage();
        }

        return jsonResult;
    }
    private String processDataWeather(String json){
        String jsonResult = "";

        try {
            JSONObject JsonObject = new JSONObject(json);
            String cod = jsonHelperGetString(JsonObject, "cod");

                    // <---Weather Description --->
                    JSONArray weather = jsonHelperGetJSONArray(JsonObject, "weather");
                    if(weather != null){
                        for(int i=0; i<weather.length(); i++){
                            JSONObject thisWeather = weather.getJSONObject(i);
                            jsonResult += jsonHelperGetString(thisWeather, "description") + " \n";
                            jsonResult += "";
                        }
                    }



            /////////////////////////////////ICON/////////////////////////

            if(weather != null){
                for(int i=0; i<weather.length(); i++){
                    JSONObject thisWeather = weather.getJSONObject(i);
                    String icons = jsonHelperGetString(thisWeather, "icon");
                    String iconN = "a" + icons;
                    int name = getResources().getIdentifier(iconN,"drawable",getPackageName());
                    Drawable drawable = getResources().getDrawable(name);
                    weatherImageView.setImageDrawable(drawable);

                    jsonResult += "\n";
                }
            }
            //////////////////////////////////////////////////////

                    // <---Wind Speed Display--->
                    JSONObject wind = jsonHelperGetJSONObject(JsonObject, "wind");
                    if(wind != null){
                        jsonResult += "Wind: " + jsonHelperGetString(wind, "speed") + "m/s";
                        jsonResult += "\n";
                    }

                else if(cod.equals("404")){
                    String message = jsonHelperGetString(JsonObject, "message");
                    jsonResult += "cod 404: " + message;
                }
            else{
                jsonResult += "cod == null\n";
            }

        } catch (JSONException e) {
            e.printStackTrace();
            jsonResult += e.getMessage();
        }

        return jsonResult;
    }
    private String processDataIcon(String json){

        Log.d(TAG,"Json"+json);
        String jsonResult = "";

        try {
            JSONObject JsonObject = new JSONObject(json);
                    JSONArray weather = jsonHelperGetJSONArray(JsonObject, "weather");
                    if(weather != null){
                        for(int i=0; i<weather.length(); i++){
                            JSONObject thisWeather = weather.getJSONObject(i);
                            String icons = jsonHelperGetString(thisWeather, "icon");
                            String iconN = "a" + icons;
                            int name = getResources().getIdentifier(iconN,"drawable",getPackageName());
                            Drawable drawable = getResources().getDrawable(name);
                            weatherImageView.setImageDrawable(drawable);

                            jsonResult += "\n";
                        }
                    }

        } catch (JSONException e) {
            e.printStackTrace();
            jsonResult += e.getMessage();
        }

        return jsonResult;


    }
    // <---End of collecting Data--->

    private String jsonHelperGetString(JSONObject obj, String k){
        String v = null;
        try {
            v = obj.getString(k);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return v;
    }

    private JSONObject jsonHelperGetJSONObject(JSONObject obj, String k){
        JSONObject o = null;

        try {
            o = obj.getJSONObject(k);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return o;
    }

    private JSONArray jsonHelperGetJSONArray(JSONObject obj, String k){
        JSONArray a = null;

        try {
            a = obj.getJSONArray(k);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return a;
    }
}


