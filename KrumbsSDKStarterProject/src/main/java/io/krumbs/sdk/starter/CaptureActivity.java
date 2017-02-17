
package io.krumbs.sdk.starter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

import java.io.File;
import java.net.URL;


/**
 * Created by baconleung on 2/16/17.
 */

public class CaptureActivity extends AppCompatActivity {
 //   private TextView mDisplayText;
    private Button mButtonReturn;
    private ImageView mImageView;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        /* Typical usage of findViewById... */
      //  mDisplayText = (TextView) findViewById(R.id.tv_display);
        mButtonReturn = (Button) findViewById(R.id.bn_return);
        mImageView = (ImageView) findViewById(R.id.iv_image);

        Intent intentThatStartedThisActivity = getIntent();

        if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
            String imagePath = intentThatStartedThisActivity.getStringExtra(Intent.EXTRA_TEXT);
            File imgFile = new File(imagePath);
            if(imgFile.exists()){
                Bitmap mBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                mImageView.setImageBitmap(mBitmap);
            }
        }

        mButtonReturn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Context context = CaptureActivity.this;
                Class destinationActivity = MainActivity.class;
                Intent startChildActivityIntent = new Intent(context, destinationActivity);
                startActivity(startChildActivityIntent);
            }
        });



    }
}
