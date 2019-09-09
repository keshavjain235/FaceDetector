package com.keshav.facedetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView image;
    Button camera, detect;
    Bitmap imageD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init of widgets
        image = findViewById(R.id.image);
        camera = findViewById(R.id.camera);
        detect = findViewById(R.id.detect);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(i,100);
            }
        });

        detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    detectFace(imageD);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "No Image Clicked", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }//oncreate

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 100) {
                imageD = (Bitmap) data.getExtras().get("data");
                image.setImageBitmap(imageD);
            }
        }catch (Exception e){
            Toast.makeText(this, "No Image Clicked", Toast.LENGTH_SHORT).show();
        }
    }//onActivityResult

    //detection part
    void detectFace(Bitmap image) {

        final List<Rect> rectsangles = new ArrayList<>();

        //convert simple bitmap image to firebase image format
        FirebaseVisionImage imageF = FirebaseVisionImage.fromBitmap(imageD);

        //detector parameters
        FirebaseVisionFaceDetectorOptions highAccuracyOpts = new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();

        //creating the detector with local model
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(highAccuracyOpts);

        //now we r good to go with detection
        detector.detectInImage(imageF).addOnCompleteListener(new OnCompleteListener<List<FirebaseVisionFace>>() {
            @Override
            public void onComplete(@NonNull Task<List<FirebaseVisionFace>> task) {

                for(FirebaseVisionFace face: task.getResult()){
                    rectsangles.add(face.getBoundingBox());
                }
                drawRectangles(rectsangles);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(MainActivity.this, "No Face Detected", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void drawRectangles(List<Rect> rects) {

        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setAntiAlias(true);
        p.setFilterBitmap(true);
        p.setDither(true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1);
        p.setColor(Color.GREEN);


        Bitmap img = ((BitmapDrawable)image.getDrawable()).getBitmap();

        Bitmap bitmap = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(bitmap);

        c.drawBitmap(img, 0, 0, null);

        for(Rect r: rects) {
            c.drawRect(r.left, r.top, r.right, r.bottom, p);
        }

        image.setImageBitmap(bitmap);

    }


}//MainActivity
