package com.mualab.org.user.util.transformers;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by dharmraj on 23/2/18.
 */

public class ParallaxPageTransformer implements ViewPager.PageTransformer {

    public void transformPage(View view, float position) {

        int pageWidth = view.getWidth();

        if(position < -1){ //[-infinity,1)
            //off to the left by a lot
            //view.setRotation(0);
            view.setAlpha(1);
        }else if(position <= 1){ //[-1,1]
            view.setTranslationX((-position) * pageWidth); //shift the view over
            // Fade the page relative to its distance from the center
           // view.setAlpha(Math.max(minAlpha, 1 - Math.abs(position)/3));
           // dummyImageView.setTranslationX(-position * (pageWidth / 2)); //Half the normal speed
        }else{ //(1, +infinity]
            //off to the right by a lot
            view.setAlpha(1);
        }


    }
}