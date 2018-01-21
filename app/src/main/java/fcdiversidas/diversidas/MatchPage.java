package fcdiversidas.diversidas;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class MatchPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_page);

        ImageView backbutton = (ImageView) findViewById(R.id.backbutton);
        backbutton.bringToFront();
    }
}
