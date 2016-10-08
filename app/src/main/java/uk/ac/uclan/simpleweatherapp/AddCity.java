package uk.ac.uclan.simpleweatherapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import uk.ac.uclan.simpleweatherapp.DataBase.DatabaseOpenHelper;

public class AddCity extends AppCompatActivity {

    private EditText titleEditText;
    private EditText bodyEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);

        titleEditText = (EditText) findViewById(R.id.editTextTitle);
        bodyEditText = (EditText) findViewById(R.id.editTextBody);

        //<---Button for adding a city to the list--->
        Button addButton = (Button) findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add();

            }
        });

        //<---Btn that takes you to the list of cities--->
        Button favButton = (Button) findViewById(R.id.favoritesButton);
        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddCity.this, ListOfCities.class));
            }
        });
    }

        //<---The add function is executed when the add button is pressed--->
        public void add() {
            String title = titleEditText.getText().toString().trim();
            String body = bodyEditText.getText().toString().trim();
            DatabaseOpenHelper doh = new DatabaseOpenHelper(this);
            SQLiteDatabase db = doh.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM entries", null);
            int numOfRows = cursor.getCount();
            final String [] titles = new String [numOfRows];
            final String [] bodies = new String[numOfRows];
            cursor.moveToFirst();
            int columnTitleIndex = cursor.getColumnIndex("title");
            int columnBodyIndex = cursor.getColumnIndex("body");
            for ( int i=0; i<numOfRows; i++) {
                titles[i] = cursor.getString(columnTitleIndex);
                bodies[i] = cursor.getString(columnBodyIndex);
                cursor.moveToNext();
            }
            cursor.close();
            boolean exists = false;
            //<---Checking if a city exist in the list--->
            String cityName = title.replaceAll(" ","");

            for(int i = 0; i < numOfRows; i++)
            {
                String dbCityName = titles[i].replaceAll(" ","");
                if (dbCityName.equalsIgnoreCase(cityName)){
                    exists = true;
                }
            }
            //<---Show toast message of the text editor is empty and if a city is alredy in the list-->
            if (title.isEmpty()) {
                Toast.makeText(this, "Please Enter a city!", Toast.LENGTH_SHORT).show();
            }else if(exists){
                Toast.makeText(this, "Already registered", Toast.LENGTH_SHORT).show();
            } else {
                ContentValues contentValues = new ContentValues(); // a map data structure
                contentValues.put("title", title);
                contentValues.put("body", body);
                db.insert("entries", null, contentValues);
                startActivity(new Intent(AddCity.this, ListOfCities.class));
                finish(); // causes this activity to terminate, and return to the parent one
            }
        }
    }