package com.example.jeyun.main_map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


import java.util.ArrayList;

/**
 * Created by jeyun on 18. 1. 11.
 */

public class MoveObject extends View {

    public MoveMarker MU;
    Bitmap image_me, image_me_rotate;
    Bitmap Image;
    Bitmap resize;
    Context context;
    ArrayList<Integer[]> expectedCircle = new ArrayList<Integer[]>();
    Paint paint;
    int pos_ZeroX = 30;
    int pos_ZeroY = 98;
    int bitmapSizeX, bitmapSizeY;

    public MoveObject(Context context, int id) {
        super(context);
        // TODO Auto-generated constructor stub
        this.context = context;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 16;
        image_me = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker, options);
        image_me_rotate = createRotateImage(image_me, 180);
        image_me.recycle();
        Image = BitmapFactory.decodeResource(context.getResources(), id);
        System.out.println("Ori image : " + Image.getWidth() + ", " + Image.getHeight() + "\timage2 : " + image_me_rotate.getWidth() + ", " + image_me_rotate.getHeight());
        paint = new Paint();
        paint.setColor(Color.rgb( 96, 255, 0));

    }

    public void setSelectedImage(Context context, int id) {
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        MU.TouchProcess(event);
        invalidate();
        return (true);
    }

    @Override
    public void draw(Canvas canvas) {
        // TODO Auto-generated method stub
        if(MU == null ) {
            resize = Bitmap.createScaledBitmap(Image, canvas.getWidth(), canvas.getHeight(), true);
            MU = new MoveMarker(resize, image_me_rotate, context);
            Image.recycle();
        }
        canvas.drawBitmap(MU.getImage(), null,  MU.getRect(), null);
        canvas.drawBitmap(MU.getMyImage(), null, MU.getRect2(), null);
        /*for(int i = 0; i < expectedCircle.size(); i++) {
            System.out.println("radius:" + MU.getRadius() + ", drawCircle : " + i + ": x,y: " + expectedCircle.get(i)[0] + ", " + expectedCircle.get(i)[1]);
            canvas.drawCircle(MU.getPosition(expectedCircle.get(i)[0], expectedCircle.get(i)[1])[0],MU.getPosition(expectedCircle.get(i)[0], expectedCircle.get(i)[1])[1], MU.getRadius(), paint);
        }*/

        canvas.drawCircle((float)MU.MarkerX,(float)MU.MarkerY, MU.getRadius(), paint);

        super.draw(canvas);
    }

    public void moveStep(int x, int y) {
        Log.i("MoveObject", "step is " + x + ", " + y);
        MU.movebyStep(x, y);
        invalidate();
    }

    public void pressButton(String event) {
        int x = 0;
        int y = 0;
        if (event.equals("UP")) {
            x = 0;
            y = 1;
        } else if (event.equals("DOWN")) {
            x = 0;
            y = -1;
        } else if (event.equals("LEFT")) {
            x = -1;
            y = 0;
        } else if (event.equals("RIGHT")) {
            x = 1;
            y = 0;
        }
        System.out.println("X = " + x + ", Y = " + y);
        MU.buttonMove(x, y);
        invalidate();
    }

    public void rotateMarkerImage(float degree) {
        image_me = image_me_rotate;
        image_me_rotate = createRotateImage(image_me, degree);
        image_me.recycle();
        MU.setMarkerImage(image_me_rotate);
        invalidate();
    }

    public Bitmap createRotateImage(Bitmap src, float degree) {
        // Matrix 객체 생성
        Matrix matrix = new Matrix();
        // 회전 각도 셋팅
        matrix.postRotate(degree);
        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),src.getHeight(), matrix, true);
    }

    public void addExpectedCircle(int x, int y) {
        expectedCircle.add(new Integer[] {x, y});
    }

    public void clearExpectedCircle() {
        expectedCircle.clear();
    }
}