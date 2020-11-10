package com.example.minibazaar.NonProjectFiles;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;
import com.example.minibazaar.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OrderTrackTest extends AppCompatActivity {

    private static final int MAX_LENGTH = 32;
    private String orderID;
    private final String TAG = this.getClass().getSimpleName()+"_xlr8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_track_test);

        orderID = new SimpleDateFormat("dd.MMM.yyyy-HH.mm.ss").format(new Date());

        Log.d(TAG,orderID);

        SimpleDateFormat format1=new SimpleDateFormat("dd.MMM.yyyy-HH.mm.ss");
        try {
            Date dt1=format1.parse(orderID);
            Calendar c = Calendar.getInstance();
            c.setTime(dt1);
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            Log.d(TAG,"Day: "+dayOfWeek);
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d(TAG,e.getMessage());
        }

        Log.d(TAG,"RandomStr: "+getRandomString(5));


    }

    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";

    private static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }


}