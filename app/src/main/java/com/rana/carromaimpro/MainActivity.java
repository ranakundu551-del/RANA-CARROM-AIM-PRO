package com.rana.carromaimpro;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Typeface;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(new BoardView());
    }

    class BoardView extends View {
        Paint p = new Paint(3);
        float sx, sy, tx, ty;
        boolean dragging = false;
        BoardView() { super(MainActivity.this); }
        void circle(Canvas c, float x, float y, float r, int color) {
            p.setColor(color); p.setStyle(Paint.Style.FILL); c.drawCircle(x,y,r,p);
        }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w=getWidth(), h=getHeight();
            c.drawColor(Color.rgb(25,32,39));
            p.setColor(Color.WHITE); p.setTextSize(28); p.setTypeface(Typeface.DEFAULT_BOLD);
            c.drawText("RANA CARROM AIM PRO", 24, 48, p);
            float size=Math.min(w-40,h-240), left=(w-size)/2, top=(h-size)/2;
            p.setColor(Color.rgb(190,126,67)); c.drawRoundRect(left,top,left+size,top+size,18,18,p);
            p.setColor(Color.rgb(246,220,177)); c.drawRoundRect(left+22,top+22,left+size-22,top+size-22,12,12,p);
            float r=size*.055f;
            circle(c,left+35,top+35,r,Color.BLACK); circle(c,left+size-35,top+35,r,Color.BLACK);
            circle(c,left+35,top+size-35,r,Color.BLACK); circle(c,left+size-35,top+size-35,r,Color.BLACK);
            if(sx==0){sx=w/2;sy=top+size*.78f;tx=w/2;ty=top+size*.38f;}
            p.setColor(Color.rgb(40,110,210)); p.setStrokeWidth(4); p.setStyle(Paint.Style.STROKE);
            c.drawLine(sx,sy,tx,ty,p); p.setStyle(Paint.Style.FILL);
            circle(c,tx,ty,r*.72f,Color.RED); circle(c,sx,sy,r,Color.WHITE);
            double angle=Math.toDegrees(Math.atan2(sy-ty,tx-sx));
            p.setColor(Color.WHITE); p.setTextSize(20); p.setTypeface(Typeface.DEFAULT);
            c.drawText("Aim angle: "+String.format(java.util.Locale.US,"%.1f",angle)+"°",24,h-105,p);
            c.drawText("Drag the white striker to practice aiming.",24,h-68,p);
            c.drawText("Practice simulator • Not connected to Carrom Pool",24,h-34,p);
        }
        @Override public boolean onTouchEvent(MotionEvent e) {
            float x=e.getX(), y=e.getY();
            if(e.getAction()==MotionEvent.ACTION_DOWN || e.getAction()==MotionEvent.ACTION_MOVE) {
                if(e.getAction()==MotionEvent.ACTION_DOWN || dragging) { sx=x; sy=y; dragging=true; invalidate(); }
                return true;
            }
            if(e.getAction()==MotionEvent.ACTION_UP) { dragging=false; invalidate(); return true; }
            return true;
        }
    }
}
