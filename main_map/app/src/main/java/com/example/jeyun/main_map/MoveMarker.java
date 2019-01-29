package com.example.jeyun.main_map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;

public class MoveMarker {

    private float X;
    private float Y;
    public static double MarkerX = 0;
    public static double MarkerY = 0;

    // 0,0 좌표
    public static double positionX = 31;
    public static double positionY = 97.5;
    int MAX_PIXEL_WIDTH = 500;
    int MAX_PIXEL_HEIGHT = 1000;
    private float Width;
    private float Height;

    //처음 이미지를 선택했을 때, 이미지의 X,Y 값과 클릭 지점 간의 거리
    private float offsetX;
    private float offsetY;

    // 드래그
    int posX1 = 0, posX2 = 0, posY1 = 0, posY2 = 0;

    float oldDist = 1f;
    float newDist = 1f;

    // 드래그/ 핀치줌 모드 구분
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;

    int mode = NONE;

    //이미지
    private Bitmap Image;
    private Bitmap myimage;
    float tempscale = (float) 1;
    Context context;
    DisplayMetrics dm;
    int width;
    int height;

    private float step_interval = (float) 0.15; //0.9에서 수정

    //Image를 인자로 받는다.
    public MoveMarker(Bitmap Image, Bitmap Image2, Context context) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.Image = Image;
        myimage = Image2;
        setSize(Image.getHeight(), Image.getWidth());
        dm = context.getResources().getDisplayMetrics();
        width = dm.widthPixels;
        height = dm.heightPixels;
        //마커의 현재 위치 : positionX, positionY, 엑셀 값 참조해서 할 것. (0, 0)위치 = 31, 98 // 마커가 위로 == y--, 마커가 왼쪽으로 = x--;

        setXY(0, 0);

    }

    public void TouchProcess(MotionEvent event) {
        int act = event.getAction();
        switch (act & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:    //첫번째 손가락 터치
                if (InObject(event.getX(), event.getY())) {//손가락 터치 위치가 이미지 안에 있으면 DragMode가 시작된다.
                    posX1 = (int) event.getX();
                    posY1 = (int) event.getY();
                    offsetX = posX1 - X;
                    offsetY = posY1 - Y;

                    //Log.d("zoom", "mode=DRAG");
                    Log.i("touchPosition", "eventX=" + (int) event.getX() + " eventY=" + event.getY());

                    mode = DRAG;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {   // 드래그 중이면, 이미지의 X,Y값을 변환시키면서 위치 이동.
                    X = posX2 - offsetX;
                    Y = posY2 - offsetY;
                    posX2 = (int) event.getX();
                    posY2 = (int) event.getY();
                    //  Log.i("dragPosition", "eventX=" + (int) event.getX() + " eventY=" + event.getY() + "\nposX1=" + posX1 + " posY1=" + posY1);
                    if (Math.abs(posX2 - posX1) > 20 || Math.abs(posY2 - posY1) > 20) {
                        posX1 = posX2;
                        posY1 = posY2;
                        Log.d("drag", "mode=DRAG");
                    }

                } else if (mode == ZOOM) {    // 핀치줌 중이면, 이미지의 거리를 계산해서 확대를 한다.
                    newDist = spacing(event);

                    if (newDist - oldDist > 20) {  // zoom in
                        float scale = (float) Math.sqrt(((newDist - oldDist) * (newDist - oldDist)) / (Height * Height + Width * Width));
                        Y = Y - (Height * scale / 2);
                        X = X - (Width * scale / 2);

                        Height = Height * (1 + scale);
                        Width = Width * (1 + scale);
                        tempscale *= 1 + scale;
                        oldDist = newDist;
                        //    Log.i("zoom", "Width=" + Width + " Height=" + Height + " scale=" + scale);
                    } else if (oldDist - newDist > 20) {  // zoom out
                        float scale = (float) Math.sqrt(((newDist - oldDist) * (newDist - oldDist)) / (Height * Height + Width * Width));
                        scale = 0 - scale;
                        Y = Y - (Height * scale / 2);
                        X = X - (Width * scale / 2);

                        Height = Height * (1 + scale);
                        Width = Width * (1 + scale);
                        tempscale *= 1 + scale;
                        oldDist = newDist;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:    // 첫번째 손가락을 떼었을 경우
            case MotionEvent.ACTION_POINTER_UP:  // 두번째 손가락을 떼었을 경우
                mode = NONE;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //두번째 손가락 터치(손가락 2개를 인식하였기 때문에 핀치 줌으로 판별)
                mode = ZOOM;
                newDist = spacing(event);
                oldDist = spacing(event);
                Log.d("zoom", "newDist=" + newDist);
                Log.d("zoom", "oldDist=" + oldDist);
                Log.d("zoom", "mode=ZOOM");

                break;
            case MotionEvent.ACTION_CANCEL:
            default:
                break;
        }

    }

    //Rect 형태로 넘겨준다.
    public Rect getRect() {
        Rect rect = new Rect();
        rect.set((int) X, (int) Y, (int) (X + Width), (int) (Y + Height));
        return rect;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);

    }

    public boolean InObject(float eventX, float eventY) {
        if (eventX < (X + Width + 30) && eventX > X - 30 && eventY < Y + Height + 30 && eventY > Y - 30) {
            return true;
        }
        return false;
    }

    public void setSize(float Height, float Width) {
        this.Height = Height;
        this.Width = Width;
    }

    public void setXY(float X, float Y) {
        this.X = X;
        this.Y = Y;
    }

    public Bitmap getImage() {
        return Image;
    }

    public Bitmap getMyImage() {
        return myimage;
    }

    //마커의 위치를 지정하는 함수
    public Rect getRect2() {
        Rect rect = new Rect();
        rect.set((int) X + (int) ((Width / MAX_PIXEL_WIDTH*10) * (positionX + MarkerX*2) - myimage.getWidth() / 2 * tempscale + Width / MAX_PIXEL_WIDTH*5), ((int) Y + (int) (((int) (Height / MAX_PIXEL_HEIGHT*10 * (positionY - MarkerY*2))) + Height / MAX_PIXEL_HEIGHT*5 - myimage.getHeight() / 2 * tempscale)),
                (int) X + (int) ((Width / MAX_PIXEL_WIDTH*10) * (positionX + MarkerX*2) + myimage.getWidth() / 2 * tempscale + Width / MAX_PIXEL_WIDTH*5 + MarkerX), (int) Y + (int) (((int) (Height / MAX_PIXEL_HEIGHT*10 * (positionY - MarkerY*2))) + myimage.getHeight() / 2 * tempscale + Height / MAX_PIXEL_HEIGHT*5));
        return rect;
    }

    public float getRadius() {
        return 20*tempscale;
    }

    //전환
    public int[] getPosition(double x, double y) {
        return new int[] { (int) ( X + ((Width / MAX_PIXEL_WIDTH*10) * (positionX + (x*2)) + Width / MAX_PIXEL_WIDTH*5)),
                (int) ( Y + (( (Height / MAX_PIXEL_HEIGHT*10 * (positionY - y*2))) + Height / MAX_PIXEL_HEIGHT*5)) };
    }

    public void buttonMove(int x, int y) {
        MarkerX += x;
        MarkerY += y;
    }

    public void movebyStep(int x, int y) {
        MarkerX += x * ( step_interval) / (0.9);
        MarkerY += y * ( step_interval) / (0.9);
    }

    public void setMarkerImage(Bitmap markerImage) {
        myimage.recycle();
        myimage = markerImage;
    }

}


