package fcdiversidas.diversidas;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.R.attr.bitmap;
import static fcdiversidas.diversidas.TimelinePage.decodeSampledBitmapFromResource;

public class PinMoment extends AppCompatActivity {
    String pinID;
    ArrayList<Bitmap> pictureMap;
    int currentSlide = 0;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_VIDEO_CAPTURE = 2;
    Initializer initializer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        pinID = intent.getStringExtra("pinID");
        pictureMap = new ArrayList<Bitmap>();
        initializer = new Initializer();

        setContentView(R.layout.activity_pin_moment);

        Button reactionButton = (Button) findViewById(R.id.photobutton);
        reactionButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        Button videoButton = (Button) findViewById(R.id.videobutton);
        videoButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                dispatchTakeVideoIntent();
            }
        });

        ImageView imgView = (ImageView) findViewById(R.id.imageview);

        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setNextImage();
            }
        });

        /*Bitmap bitmap = decodeSampledBitmapFromResource(this.getResources(),R.drawable.map_pin,5,60);
        pictureMap.add(bitmap);
        bitmap = decodeSampledBitmapFromResource(this.getResources(),R.drawable.enter_bg,5,60);
        pictureMap.add(bitmap);
        bitmap = decodeSampledBitmapFromResource(this.getResources(),R.drawable.adidas_moments,5,60);
        pictureMap.add(bitmap);*/
        getImages();

        setNextImage();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode==REQUEST_TAKE_PHOTO && resultCode==RESULT_OK)
        {
            sendPic();
            finish();
        }

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = intent.getData();

            sendVideo(videoUri.toString());
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void sendPic() {

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;

        initializer.uploadBinary(this,mCurrentPhotoPath,pinID);

        //imageView.setImageBitmap(bitmap);
        /*if (lastImageID < 0) {
            ImageView iv = (ImageView) findViewById(R.id.timelineBase);
            iv.setImageBitmap(bitmap);
            lastImageID = R.id.timelineBase;
            return;
        }*/
        //addImageToTimeline(topMarge, bitmap);
        topMarge += 150;
    }

    private void sendVideo(String uri){
        initializer.uploadBinaryUri(this,uri);
    }

    String mCurrentPhotoPath;
    String imageFileName;
    int lastImageID = -1;
    int topMarge = 0;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setNextImage() {
        if (pictureMap.size() == 0){
            return;
        }
        ImageView view = (ImageView) findViewById(R.id.imageview);
        view.setImageBitmap(pictureMap.get(currentSlide));
        currentSlide++;
        if (currentSlide >= pictureMap.size()){
            currentSlide = 0;
        }
    }

    private void getImages(){
        final ArrayList<String> urllist = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(this);
        final Context cont = this;
        String url = String.format("http://ec2-54-236-246-164.compute-1.amazonaws.com:3000/video_urls?pinid=%1$s",pinID);
        StringRequest request = new StringRequest(Request.Method.GET,url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray array = new JSONArray();
                try {
                    array = new JSONArray(response);
                } catch (Exception e){

                }
                Log.d("response", response);
                for(int i =0;i<array.length();i++){
                    try {
                        String urls = "http://ec2-54-236-246-164.compute-1.amazonaws.com:3000/"+ array.getJSONObject(i).getString("mediaPath");
                       //ADD TO SLIDE SHOW
                        urllist.add(urls);

                    } catch (Exception e){
                        Log.d("errooooor", e.getMessage());
                    }
                }
                Log.d("urllist",urllist.toString());
                for (String url : urllist){
                     Glide.
                            with(cont).
                            asBitmap().
                            load(url).
                            into(new CustomTarget<Bitmap>(100,100)  {
                                @Override
                                public void onResourceReady (Bitmap res, Transition< ? super Bitmap > transition) {
                                    pictureMap.add(res);
                                    setNextImage();
                                }
                            });


                }

            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){

            }


        });
        queue.add(request);
        Log.d("request", "Request Sent");

    }
}