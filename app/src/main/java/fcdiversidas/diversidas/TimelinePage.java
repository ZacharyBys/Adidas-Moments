package fcdiversidas.diversidas;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.gotev.uploadservice.UploadService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Exchanger;
import java.util.concurrent.RunnableFuture;

import io.socket.emitter.Emitter;

import static java.lang.System.out;

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

                for (TimelinePin pin : pinArray){
                    Log.d("pin", Long.toString(pin.timestamp));
                }

                for (TimelinePin pin : pinArray) {
                    addPinToTimeline(pin);
                }

            }
        });

        receivePins();

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
                    String pinid = null;
                    String type = null;
                    JSONObject data = (JSONObject) args[0];
                    try {
                        timestamp = data.getLong("time");
                        pinid = data.getString("_id");
                        type = data.getString("type");
                    } catch (Exception e){

                    }
                    //append the pin
                    TimelinePin newPin = new TimelinePin(timestamp, pinid, type);
                    pinArray.add(newPin);
                    addPinToTimeline(newPin);
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

        ImageView imageView = (ImageView)  findViewById(R.id.timelineBase);

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
            ImageView iv = (ImageView) findViewById(R.id.timelineBase);
            iv.setImageBitmap(bitmap);
            lastImageID = R.id.timelineBase;
            return;
        }
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

    private void addPinToTimeline(final TimelinePin pin){
        final Context cont = this;
        final String pinID = pin.pinid;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageButton ib = new ImageButton(cont);
                RelativeLayout rl = (RelativeLayout) findViewById(R.id.timeline);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);

                lp.addRule(RelativeLayout.BELOW, R.id.timelineBase);
                Bitmap icon = decodeSampledBitmapFromResource(cont.getResources(),R.drawable.map_pin,5,15);
               // Bitmap newBit = Bitmap.createScaledBitmap(
                 //       icon, 150, 50, false);

                ib.setImageBitmap(icon);
                double percentage = ((int) pin.timestamp)/95.;
                double position = ((rl.getHeight())* percentage)-15;
                lp.setMargins(110,(int) position,0,0);
                rl.addView(ib, lp);
                
                ib.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {
                        Toast toast = Toast.makeText(cont, pinID, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }
        });
    }

    private void receivePins(){
        //pinArray;
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://ec2-54-236-246-164.compute-1.amazonaws.com:3000/pins";
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
                        long time = array.getJSONObject(i).getLong("time");
                        String id = array.getJSONObject(i).getString("_id");
                        String pintype = array.getJSONObject(i).getString("type");
                        TimelinePin pin = new TimelinePin(time,id,pintype);
                        pinArray.add(pin);
                    } catch (Exception e){
                        Log.d("errooooor", e.getMessage());
                    }
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
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }
}
