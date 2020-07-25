package com.somercelik.artnotebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView artListView;
    ArrayList<String> nameArray;
    ArrayList<Integer> idArray;
    ArrayAdapter arrayAdapter;
    SQLiteDatabase database;
    String deleteByIdSql = "DELETE FROM arts WHERE id = ?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        artListView = findViewById(R.id.artListView);
        nameArray = new ArrayList<>();
        idArray = new ArrayList<>();
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nameArray);
        artListView.setAdapter(arrayAdapter);
        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null);
        artListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                deleteElementByIndex(i);
                return true;
            }
        });
        artListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {      //Listeden bir item'a tıklandığında
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intentToViewArtDetails = new Intent(MainActivity.this, DetailActivity.class);
                intentToViewArtDetails.putExtra("artId", idArray.get(position));
                intentToViewArtDetails.putExtra("info", "view");
                startActivity(intentToViewArtDetails);
            }
        });
        listDataFromDatabase();
    }

    public void deleteElementByIndex(final int index) {
        AlertDialog.Builder deletionAlert = new AlertDialog.Builder(MainActivity.this);
        deletionAlert.setTitle("Delete");
        deletionAlert.setMessage("Do you want to delete this art?");
        deletionAlert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        deletionAlert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SQLiteStatement statement = database.compileStatement(deleteByIdSql);
                statement.bindString(1, String.valueOf(idArray.get(index)));
                statement.execute();
                Intent renewActivityIntent = new Intent(MainActivity.this, MainActivity.class);
                renewActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(renewActivityIntent);
            }
        });
        deletionAlert.setCancelable(false);
        deletionAlert.show();

    }

    public void listDataFromDatabase() {

        try {
            Cursor cursor = database.rawQuery("SELECT * FROM arts", null);
            int nameIndex = cursor.getColumnIndex("artname");
            int idIndex = cursor.getColumnIndex("id");
            while (cursor.moveToNext()) {
                nameArray.add(cursor.getString(nameIndex));
                idArray.add(cursor.getInt(idIndex));
            }
            arrayAdapter.notifyDataSetChanged();
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //Activity'de gösterilecek menülerin initialize edildiği blok
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();          //MenuInflater nesnesi aldık
        menuInflater.inflate(R.menu.add_art_menu, menu);        //Bu nesneyi kullanarak xml'ini oluşturduğumuz add_art_menu'yu aldık ve inflate ettik

        return super.onCreateOptionsMenu(menu);
    }

    //Menüde Add art'a basıldığında olacaklar...Oynat bakalım.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.add_art_item) {            //Eğer tıklanan Add art seçeneği ise
            Intent addItemIntent = new Intent(MainActivity.this, DetailActivity.class);
            addItemIntent.putExtra("info", "add");  //Bunun bir art ekleme isteği olduğunu sonraki activity'ye belirtiyoruz.
            startActivity(addItemIntent);
        }

        return super.onOptionsItemSelected(item);
    }
}