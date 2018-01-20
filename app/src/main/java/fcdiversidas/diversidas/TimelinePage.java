package fcdiversidas.diversidas;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import net.gotev.uploadservice.UploadService;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.RunnableFuture;

import io.socket.emitter.Emitter;

public class TimelinePage extends AppCompatActivity {

    Initializer initializer;
    WebSocketHelper webSocketHelper;
    ArrayList<TimelinePin> pinArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline_page);

        initializer = new Initializer();
        webSocketHelper = new WebSocketHelper();
        pinArray = new ArrayList<TimelinePin>();

        Button reactionButton = (Button) findViewById(R.id.photobutton);
        reactionButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        webSocketHelper.socket.on("pins", onNewPin);



        Button videoButton = (Button) findViewById(R.id.videobutton);
        videoButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                dispatchTakeVideoIntent();
            }
        });

    }

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_VIDEO_CAPTURE = 2;
    private Emitter.Listener onNewPin = new Emitter.Listener(){

        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    long timestamp = 0;
                    int pinid = 0;
                    String type = null;
                    JSONObject data = (JSONObject) args[0];
                    try {
                        timestamp = data.getLong("time");
                        pinid = data.getInt("_id");
                        type = data.getString("pintype");
                    } catch (Exception e){

                    }
                    //append the pin
                    Log.d("pininfo", Long.toString(timestamp));

                }
            });

        }
    };
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
                this.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
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
        }

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = intent.getData();

            sendVideo(videoUri.toString());
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void sendPic() {
        // Get the dimensions of the View

        ImageView imageView = (ImageView)  findViewById(R.id.imageView);

        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        initializer.uploadBinary(this,mCurrentPhotoPath,imageFileName);

        //imageView.setImageBitmap(bitmap);
        if (lastImageID < 0) {
            ImageView iv = (ImageView) findViewById(R.id.imageView);
            iv.setImageBitmap(bitmap);
            lastImageID = R.id.imageView;
            return;
        }
        addImageToTimeline(topMarge, bitmap);
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

    private void addImageToTimeline(int position, Bitmap image){
        ImageView iv = new ImageView(this);
        iv.setImageBitmap(image);
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.timeline);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        lp.addRule(RelativeLayout.BELOW, lastImageID );
        lp.setMargins(75,position,0,0);
        rl.addView(iv, lp);
    }

}
