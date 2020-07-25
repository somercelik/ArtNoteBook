package com.somercelik.artnotebook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

//VİDEO 14 ORTADA KALDIK

public class DetailActivity extends AppCompatActivity {
    Button saveButton;
    Bitmap selectedImage;
    ImageView imageView;
    EditText artNameEditText, painterNameEditText, yearEditText;
    String sqlString = "INSERT INTO arts (artname, paintername, year, image) VALUES (?, ?, ?, ?)";
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        saveButton = findViewById(R.id.saveButton);
        imageView = findViewById(R.id.imageView);
        artNameEditText = findViewById(R.id.artNameEditText);
        painterNameEditText = findViewById(R.id.painterNameEditText);
        yearEditText = findViewById(R.id.yearEditText);

        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null);

        checkIntent();
    }

    public void checkIntent() {                                   //MainActivity'den gelinme amacını sorgulayan metod
        Intent receivedIntent = getIntent();
        String initializationMode = receivedIntent.getStringExtra("info");
        if (initializationMode.matches("view")) {           //Eğer kullanıcı liste elemanlarından birine basıp art'ı görmek istiyorsa
            int receivedId = receivedIntent.getIntExtra("artId", 1);    //Diğer Activity'de tıklanan değerin id'si alınır
            saveButton.setVisibility(View.INVISIBLE);             //Save butonunu gizle
            listFromDatabaseById(receivedId);                     //Gelen id ile sorgu yapıp detayları editText'lere yazdır
        } else {
            artNameEditText.setText("");
            painterNameEditText.setText("");
            yearEditText.setText("");
            Bitmap defaultSelectImage = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.add_image);
            imageView.setImageBitmap(defaultSelectImage);
            saveButton.setVisibility(View.VISIBLE);
        }

    }

    //ID'ye göre veritabanından verileri çeken ve listeleyen metod
    public void listFromDatabaseById(int id) {
        try {
            Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", new String[]{String.valueOf(id)});     //Verilen id'ye sahip nesneyi getir
            int artNameIndex = cursor.getColumnIndex("artname");
            int painterNameIndex = cursor.getColumnIndex("paintername");
            int yearIndex = cursor.getColumnIndex("year");
            int imageIndex = cursor.getColumnIndex("image");

            //Bu nesneyi EditText'lere yaz
            while (cursor.moveToNext()) {
                artNameEditText.setText(cursor.getString(artNameIndex));
                painterNameEditText.setText(cursor.getString(painterNameIndex));
                yearEditText.setText(cursor.getString(yearIndex));
                byte[] bytes = cursor.getBlob(imageIndex);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView.setImageBitmap(bitmap);
            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public void selectImageFromGallery(View view) {
        //Eğer kullanıcı önceden izin vermemişse
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DetailActivity.this,      //İzin iste
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},    //İstenen izinleri bir dizide ver
                    1);                                             //İstem kodu da 1 olsun,
        } else {                                                    //Eğer kullanıcı önceden izin vermişse galeriyi aç
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);    //Seçme aksiyonu yapılıp galeri açılacak olan intent
            startActivityForResult(galleryIntent, 2);    //Bir sonuç alacağımız intent 2 istem koduyla başlatıldı
        }

    }


    //Kullanıcı izninin ardından Galeri açılmalı, bunu aşağıda gerçekleştiriyoruz.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 1) {                 //READ_EXTERNAL_STORAGE izin ekranının sonucunu bulacağız
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {  //Verilen izinler dizisi dolu ve verilen izin olumlu ise
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);    //Seçme aksiyonu yapıp galeriyi açacak olan intent
                startActivityForResult(galleryIntent, 2);    //Bir sonuç alacağımız intent 2 istem koduyla başlatıldı
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //Galeriden gelen resmi ne yapacağız arkadaşım, ha burda onu ayarliyruk
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {      //Galeriden gelinmişse ve bir dosya seçilmişse ve bu dosya null değilse
            Uri imageUri = data.getData();                                      //Dönen verinin yolunu bir değişkene aldık
            try {
                if (Build.VERSION.SDK_INT >= 28) {//Eğer API Level 28 veya daha yeni bir Android sürümü ise aşağıdaki şekilde yerleştir
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), imageUri);
                    selectedImage = ImageDecoder.decodeBitmap(source);
                    imageView.setImageBitmap(selectedImage);

                } else {//Eğer API Level 28'den eski bir Android sürümü ise aşağıdaki şekilde imageView'a ata
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    imageView.setImageBitmap(selectedImage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void save(View view) {
        String artName = artNameEditText.getText().toString();          //Buradaki değerler veritabanına kaydedilmek için hazır
        String painterName = painterNameEditText.getText().toString();  //
        String year = yearEditText.getText().toString();                //

        Bitmap compressedImage = reduceSizeOfBitmap(selectedImage, 300);    //Önce resmin boyutunu küçültüyoruz.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();           //
        compressedImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream);//Kalitesini %50 düşürüp outputStream'e aldık
        byte[] byteArray = outputStream.toByteArray();            //Sonra bu outputStream'i byte dizisine alıp veritabanına yazılabilir hale getirdik.
        saveToDatabase(artName, painterName, year, byteArray);    //Database'e kaydedilmesi gereken değerleri ilgili metoda gönderdik
        //Metod uzun olmaması için ayrılmıştır.

        //finish();              //Bununla bitirirsek save butonuna basıldığında MainActivity'de eklediğimiz değer listelenmez
        //Çünkü bunu finish ettik ve diğerinde onCreate metodu değil onResume çağırıldı.

        Intent saveAndGoBackIntent = new Intent(DetailActivity.this, MainActivity.class);   //Main'e intent oluşturduk
        saveAndGoBackIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);       //Boştaki Activity'leri kapattık.
        startActivity(saveAndGoBackIntent);     //Anasayfaya yönlendirdik.
    }

    public void saveToDatabase(String artName, String painterName, String year, byte[] byteArray) {
        try {
            database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR, paintername VARCHAR, year VARCHAR, image BLOB)");
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1, artName);     //sqlString'deki soru işaretleri yerine hangi değerler konması gerektiğini tek tek belirttik
            sqLiteStatement.bindString(2, painterName);
            sqLiteStatement.bindString(3, year);
            sqLiteStatement.bindBlob(4, byteArray);
            sqLiteStatement.execute();                          //Sorguyu çalıştırdık
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Veritabanına kaydedeceğimiz resmin boyutlarını düşürmezsek DB gereksiz şişebilir hatta uygulamayı göçertebilir.
    //Bunun için en/boy oranını bozmadan resmi verilen maxSize'a düşürüyoruz aşağıdaki metod yardımıyla
    public Bitmap reduceSizeOfBitmap(@org.jetbrains.annotations.NotNull Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();              //x ve y deki piksel sayılarını aldık
        int height = bitmap.getHeight();
        float aspectRatio = (float) width / (float) height;     //en-boy oranını aldık ki koruyabilelim
        if (aspectRatio > 1) {                                  //eğer imaj yatay ise
            width = maxSize;                                    //genişliği ufalt
            height = (int) (width / aspectRatio);               //yüksekliği de en boy oranına böl
        } else {                                                //Dikey ise
            height = maxSize;                                   //yüksekliği ufalt
            width = (int) (height * aspectRatio);               //genişliği en boy oranıyla çarp
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, false);     //yeni boyuttaki görseli döndür
    }


}