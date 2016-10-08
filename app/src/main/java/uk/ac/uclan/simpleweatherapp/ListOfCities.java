package uk.ac.uclan.simpleweatherapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import uk.ac.uclan.simpleweatherapp.DataBase.DatabaseOpenHelper;

public class ListOfCities extends AppCompatActivity {

    private ListView listView;

    public void SearchAction(MenuItem mi) {
        startActivity(new Intent(ListOfCities.this, AddCity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_cities);


        listView = (ListView) findViewById(R.id.listview);
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
                startActivity(new Intent(ListOfCities.this, AddCity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onResume(){
        super.onResume();
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

        final ArrayAdapter arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_expandable_list_item_1, titles);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

                Intent intent = new Intent(ListOfCities.this, CurrentWeather.class);
                intent.putExtra("location", titles[pos]);
                startActivity(intent);
                Toast.makeText(ListOfCities.this, titles[pos], Toast.LENGTH_SHORT).show();
            }
        });

        // <---Long Click for deleting a City--->
        listView .setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int pos, long id) {

                arrayAdapter.notifyDataSetChanged();

                //Alert Dialog for deleting an entry
                AlertDialog.Builder deleteDialog = new AlertDialog.Builder(ListOfCities.this);

                // Setting Dialog Title
                deleteDialog.setTitle("Confirm Delete...");

                // Setting Dialog Message
                deleteDialog.setMessage("Are you sure you want delete this city?");

                // Setting Icon to Dialog
                deleteDialog.setIcon(R.drawable.delete);

                // Setting Positive "Yes" Btn
                deleteDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                deleteEntry(titles[pos]);
                                finish();
                                startActivity(getIntent());
                            }
                        });

                // Setting Negative "NO" Btn
                deleteDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                deleteDialog.show();
                return true;
            }
        });
    }
    public void deleteEntry(String title){
        DatabaseOpenHelper doh = new DatabaseOpenHelper(this);
        SQLiteDatabase db = doh.getWritableDatabase();
        db.delete("entries", "title=" + DatabaseUtils.sqlEscapeString(title) + " ", null);
    }

}
