package fcdiversidas.diversidas;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class HomePage extends AppCompatActivity {
    public Typeface openSansRegular;
    public Typeface openSansSemiBold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        openSansRegular = Typeface.createFromAsset(getAssets(),
                "OpenSans-Regular.ttf");
        openSansSemiBold = Typeface.createFromAsset(getAssets(),
                "OpenSans-Semibold.ttf");


        Button enterButton = (Button) findViewById(R.id.enter);
        enterButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, LeagueActivity.class);
                startActivity(intent);
            }
        });

        enterButton.setTypeface(openSansSemiBold);
        enterButton.setText("Enter");

        TextView mainslogan = (TextView) findViewById(R.id.mainslogan);
        mainslogan.setTypeface(openSansSemiBold);
        TextView subslogan = (TextView) findViewById(R.id.subslogan);
        subslogan.setTypeface(openSansSemiBold);
    }


}
