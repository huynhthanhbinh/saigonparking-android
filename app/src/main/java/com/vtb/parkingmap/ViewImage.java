package com.vtb.parkingmap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class ViewImage extends AppCompatActivity {
    ImageView imgB;
    Button btnCamera;
    int REQUEST_CODE_CAMERA = 123;

    @SuppressLint("WrongThread")
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        imgB = findViewById(R.id.imgViewB);
        btnCamera = (Button) findViewById(R.id.btnImage);
        Drawable drawable;


//
//        Drawable myDrawable = getResources().getDrawable(R.drawable.plot);
//        Bitmap anImage      = ((BitmapDrawable) myDrawable).getBitmap();
//
//        // CHUYEN BITMAP THANH BYTE
//        ByteArrayOutputStream baos  = new ByteArrayOutputStream();
//        anImage.compress(Bitmap.CompressFormat.JPEG,100,baos);
//        byte[] b = baos.toByteArray();
//        Log.d("BYTE",""+b);
//
//
//        // CHUYỂN BYTE THANH BITMAP
//
//        Bitmap decodeByte = BitmapFactory.decodeByteArray(b,0,b.length);
//
//        // Test lại thử coi có giống BYTE1 k
//        ByteArrayOutputStream baos2  = new ByteArrayOutputStream();
//        decodeByte.compress(Bitmap.CompressFormat.JPEG,100,baos2);
//        byte[] b2 = baos.toByteArray();
//
//        Log.d("BITMAP",""+decodeByte);
//        imgB.setImageBitmap(decodeByte);

//        Resources r = this.getResources();
//        Bitmap bm  = BitmapFactory.decodeResource(r,R.drawable.plot);
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
//
//
//        byte[] byteArray = baos.toByteArray();

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ActivityCompat.requestPermissions(ViewImage.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_CAMERA && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CODE_CAMERA);
        } else {
            Toast.makeText(this, "Bạn không cho phép mở Camera! ", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK && data != null) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            bitmap = Bitmap.createScaledBitmap(bitmap, 1024, 768, false);

            int a = bitmap.getByteCount();


            Bitmap scaledBitmap = scaleDown(bitmap, 500, true);
            scaledBitmap = scaledBitmap.createScaledBitmap(bitmap, 1024, 768, false);
            int b = scaledBitmap.getByteCount();

//            Toast.makeText(ViewImage.this, bitmap.getWidth() + " , " + bitmap.getHeight(), Toast.LENGTH_SHORT).show();
//            int a = bitmap.getByteCount();
            Log.d("size of bitmap", a + " , " + b);
            imgB.setImageBitmap(bitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

//    public Bitmap resizeBitmap(Bitmap getBitmap, int maxSize) {
//        int width = getBitmap.getWidth();
//        int height = getBitmap.getHeight();
//        double x;
//
//        if (width >= height && width > maxSize) {
//            x = width / height;
//            width = maxSize;
//            height = (int) (maxSize / x);
//        } else if (height >= width && height > maxSize) {
//            x = height / width;
//            height = maxSize;
//            width = (int) (maxSize / x);
//        }
////        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
////        getBitmap.compress(Bitmap.CompressFormat.JPEG, 30, tmp);
////        byte[] bytetmp = tmp.toByteArray();
////        getBitmap = BitmapFactory.decodeByteArray(bytetmp, 0, bytetmp.length);
//
//        return Bitmap.createScaledBitmap(getBitmap, width, height, false);
//    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

}
