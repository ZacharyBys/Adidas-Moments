package fcdiversidas.diversidas;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

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
    String mins;
    ArrayList<Bitmap> pictureMap;
    int currentSlide = 0;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_VIDEO_CAPTURE = 2;
    Initializer initializer;
    public Typeface opensemi;
    boolean cycle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        cycle = true;
        pinID = intent.getStringExtra("pinID");
        mins = intent.getStringExtra("time");

        pictureMap = new ArrayList<Bitmap>();
        initializer = new Initializer();

        setContentView(R.layout.activity_pin_moment);

        opensemi = Typeface.createFromAsset(getAssets(),
                "OpenSans-Semibold.ttf");

        /*Button videoButton = (Button) findViewById(R.id.videobutton);
        videoButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                dispatchTakeVideoIntent();
            }
        });*/

        ImageView backbutton = (ImageView) findViewById(R.id.backbutton);
        backbutton.bringToFront();
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PinMoment.this, TimelinePage.class);
                startActivity(intent);
            }
        });

        Button but = (Button) findViewById(R.id.screenbutton);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setNextImage();
                resetLike();
            }
        });

        ImageView view = (ImageView) findViewById(R.id.soccerball);
        Bitmap icon = decodeSampledBitmapFromResource(this.getResources(),R.drawable.ball,75, 75);
        view.setImageBitmap(icon);

        ImageView likebutton = (ImageView) findViewById(R.id.likebutton);
        Bitmap icon2 = decodeSampledBitmapFromResource(this.getResources(),R.drawable.likebutton01,50, 50);
        likebutton.setImageBitmap(icon2);

        TextView addreaction = (TextView) findViewById(R.id.addreaction);
        addreaction.setTypeface(opensemi);

        TextView minute = (TextView) findViewById(R.id.minute);
        minute.setText(mins + "'");
        minute.setTypeface(opensemi);
        minute.bringToFront();

        final Context cont = this;

        addreaction.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        likebutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ImageView like = (ImageView) findViewById(R.id.likebutton);
                Bitmap icon = decodeSampledBitmapFromResource(cont.getResources(), R.drawable.likebutton02,50,50);
                like.setImageBitmap(icon);
            }
        });

        getImages();

        setNextImage();
    }

    private void resetLike(){
        ImageView like = (ImageView) findViewById(R.id.likebutton);
        Bitmap icon = decodeSampledBitmapFromResource(this.getResources(), R.drawable.likebutton01,50,50);
        like.setImageBitmap(icon);
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
        VideoView videoview = (VideoView) findViewById(R.id.videoview);
        if (pictureMap.size() == 0){
            return;
        }

            if (currentSlide == pictureMap.size()) {
                videoview.setVisibility(View.VISIBLE);
                ImageView view = (ImageView) findViewById(R.id.imageview);
                view.setVisibility(View.INVISIBLE);
                String path = "android.resource://" + getPackageName() + "/" + R.raw.zachvideo;
                videoview.setVideoURI(Uri.parse(path));
                videoview.start();
                currentSlide = 0;
                return;
            }

        ImageView view = (ImageView) findViewById(R.id.imageview);
        view.setImageBitmap(pictureMap.get(currentSlide));
        view.setVisibility(View.VISIBLE);
        videoview.setVisibility(View.GONE);
        videoview.stopPlayback();

        currentSlide++;
        if (currentSlide > pictureMap.size()){
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
                        //urllist.add(urls);
                        Glide.
                                with(cont).
                                asBitmap().
                                load(urls).
                                into(new CustomTarget<Bitmap>(200,200)  {
                                    @Override
                                    public void onResourceReady (Bitmap res, Transition< ? super Bitmap > transition) {
                                        pictureMap.add(res);
                                        if (cycle) {
                                            setNextImage();
                                            cycle = false;
                                        }
                                    }
                                });
                    } catch (Exception e){
                        Log.d("errooooor", e.getMessage());
                    }
                }
                Log.d("urllist",urllist.toString());

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
