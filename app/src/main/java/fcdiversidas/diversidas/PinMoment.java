package fcdiversidas.diversidas;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import java.util.ArrayList;

import static android.R.attr.bitmap;
import static fcdiversidas.diversidas.TimelinePage.decodeSampledBitmapFromResource;

public class PinMoment extends AppCompatActivity {
    String pinID;
    ArrayList<Bitmap> pictureMap;
    int currentSlide = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        pinID = intent.getStringExtra("pinID");
        pictureMap = new ArrayList<Bitmap>();

        setContentView(R.layout.activity_pin_moment);

        TextView pintitle = (TextView) findViewById(R.id.pintitle);
        pintitle.setText(pinID);

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

                for (String url : urllist){
                     Glide.
                            with(cont).
                            asBitmap().
                            load(url).
                            into(new CustomTarget<Bitmap>(60,60)  {
                                @Override
                                public void onResourceReady (Bitmap res, Transition< ? super Bitmap > transition) {
                                    pictureMap.add(res);
                                }
                            });


                }

                setNextImage();
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
