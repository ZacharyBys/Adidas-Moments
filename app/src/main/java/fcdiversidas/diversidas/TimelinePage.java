package fcdiversidas.diversidas;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import net.gotev.uploadservice.UploadService;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TimelinePage extends AppCompatActivity {

    Initializer initializer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline_page);

        initializer = new Initializer();

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

    }

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_VIDEO_CAPTURE = 2;

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
            String videoPath = videoUri.getPath();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String videoName = "MP4_" + timeStamp + "_";

            //ArrayList<Bitmap> frames = createGIF(videoUri);

            sendVideo(videoPath, videoName);
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

    private ArrayList<Bitmap> createGIF(Uri videoUri){
        File myVideo = new File(videoUri.getPath());
        MediaMetadataRetriever mmRetriever = new MediaMetadataRetriever();
        mmRetriever.setDataSource(myVideo.getAbsolutePath());

        // Array list to hold your frames
        ArrayList<Bitmap> frames = new ArrayList<Bitmap>();

        // Some kind of iteration to retrieve the frames and add it to Array list
        Bitmap bitmap = mmRetriever.getFrameAtTime(10000);
        frames.add(bitmap);

        return frames;
    }

    private void sendVideo(String path, String name){
        initializer.uploadBinary(this,path,name);
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
