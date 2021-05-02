package com.example.moviedatabase;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DisplayMovie extends AppCompatActivity {

    private final int GET_GALLERY_IMAGE = 200;

    private DBHelper mydb;
    TextView name;
    TextView direcotr;
    TextView year;
    TextView nation;
    TextView rating;
    TextView imgUrl;

    ImageView imageView;
    Button imgbut;
    int id = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_movie);

        name = findViewById(R.id.editTextName);
        direcotr = findViewById(R.id.editTextDirector);
        year = findViewById(R.id.editTextYear);
        nation = findViewById(R.id.editTextCountry);
        rating = findViewById(R.id.editTextGrade);
        imgUrl = findViewById(R.id.editTextImgUrl);
        imageView = findViewById(R.id.imageView);
        imgbut = findViewById(R.id.imgbutton);

        mydb = new DBHelper(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int Value = extras.getInt("id");
            if (Value > 0) {
                Cursor rs = mydb.getData(Value);
                id = Value;
                rs.moveToFirst();
                String n = rs.getString(rs.getColumnIndex(DBHelper.MOVIES_COLUMN_NAME));
                String d = rs.getString(rs.getColumnIndex(DBHelper.MOVIES_COLUMN_DIRECTOR));
                String y = rs.getString(rs.getColumnIndex(DBHelper.MOVIES_COLUMN_YEAR));
                String na = rs.getString(rs.getColumnIndex(DBHelper.MOVIES_COLUMN_NATION));
                String r = rs.getString(rs.getColumnIndex(DBHelper.MOVIES_COLUMN_RATING));
                String i = rs.getString(rs.getColumnIndex(DBHelper.MOVIES_COLUMN_IMAGE));
                if (!rs.isClosed()) {
                    rs.close();
                }
                Button b = findViewById(R.id.save); // button1 <- what? ????????
                b.setVisibility(View.INVISIBLE);

                name.setText((CharSequence) n);
                direcotr.setText((CharSequence) d);
                year.setText((CharSequence) y);
                nation.setText((CharSequence) na);
                rating.setText((CharSequence) r);
                imgUrl.setText((CharSequence) i);


                try {
                    imageView.setImageURI(Uri.parse(i));
                    imageView.setVisibility(View.VISIBLE);
                } catch (Exception e){
                    imageView.setImageURI(Uri.parse(i));
                }



                imgbut.setText("이미지 변경");
            }
        }
    }

    public void insert(View view) {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int Value = extras.getInt("id");
            //if (Value > 0) {
            if (mydb.insertMovie(name.getText().toString(), direcotr.getText().toString(),
                    year.getText().toString(), nation.getText().toString(), rating.getText().toString(), imgUrl.getText().toString())) {
                Toast.makeText(getApplicationContext(), "추가되었음", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "추가되지 않았음", Toast.LENGTH_SHORT).show();
            }
            finish();
            //}
        }
    }

    public void delete(View view) {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int Value = extras.getInt("id");
            if (Value > 0) {
                mydb.deleteMovie(id);
                Toast.makeText(getApplicationContext(), "삭제되었음", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "삭제되지 않았음", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void edit(View view) {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int Value = extras.getInt("id");
            if (Value > 0) {
                if (mydb.updateMovie(id, name.getText().toString(), direcotr.getText().toString(),
                        year.getText().toString(), nation.getText().toString(), rating.getText().toString(), imgUrl.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "수정되었음", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(getApplicationContext(), "수정되지 않았음", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void movieImgButton(View view) {
        //권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // 권한이 허용되지 않음, 권한 요청
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else{
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intent, GET_GALLERY_IMAGE);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri selectedImageUri = data.getData();
            imgUrl.setText(getPathFromURI(selectedImageUri));
            //imageview.setImageURI(selectedImageUri);

        }
    }

    public String getPathFromURI(Uri ContentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver()
                .query(ContentUri, proj, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            res = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            cursor.close();
        }
        return res;
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //권한을 허용 했을 경우
        if(requestCode == 1) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intent, GET_GALLERY_IMAGE);
        }
    }


}
