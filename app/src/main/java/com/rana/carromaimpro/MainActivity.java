package com.rana.carromaimpro;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Typeface;
import java.util.Locale;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(new BoardView());
    }

    class BoardView extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        float left, top, size, pocketR, ballR;
        float sx, sy, tx, ty, svx=0, svy=0, tvx=0, tvy=0;
        boolean initialized=false, dragging=false, moving=false, pocketed=false;
        RectF shootButton=new RectF(), resetButton=new RectF();
        String status="Drag the white striker, then tap SHOOT";
        BoardView(){ super(MainActivity.this); setLayerType(View.LAYER_TYPE_SOFTWARE,null); }

        void fill(Paint paint,int color){paint.setColor(color);paint.setStyle(Paint.Style.FILL);}
        void circle(Canvas c,float x,float y,float r,int color){
            fill(p,color); c.drawCircle(x,y,r,p);
        }
        void text(Canvas c,String s,float x,float y,float sizeText,int color,boolean bold){
            fill(p,color); p.setTextSize(sizeText); p.setTypeface(bold?Typeface.DEFAULT_BOLD:Typeface.DEFAULT); c.drawText(s,x,y,p);
        }
        void setup(){
            float w=getWidth(),h=getHeight();
            size=Math.min(w-36,h-330);
            size=Math.max(260,size);
            left=(w-size)/2f; top=(h-size)/2f-35;
            pocketR=size*.055f; ballR=size*.027f;
            if(!initialized){sx=w/2;sy=top+size*.78f;tx=w/2;ty=top+size*.43f;initialized=true;}
            sx=clamp(sx,left+size*.12f,left+size*.88f);
            sy=clamp(sy,top+size*.12f,top+size*.88f);
            tx=clamp(tx,left+size*.12f,left+size*.88f);
            ty=clamp(ty,top+size*.12f,top+size*.88f);
        }
        float clamp(float v,float a,float b){return Math.max(a,Math.min(b,v));}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c); c.drawColor(Color.rgb(25,32,39)); setup(); if(moving) step();
            text(c,"RANA CARROM AIM PRO",22,42,25,Color.WHITE,true);
            fill(p,Color.rgb(184,119,62)); c.drawRoundRect(left,top,left+size,top+size,18,18,p);
            fill(p,Color.rgb(247,221,180)); c.drawRoundRect(left+18,top+18,left+size-18,top+size-18,10,10,p);
            float pr=pocketR;
            circle(c,left+pr,top+pr,pr,Color.BLACK);circle(c,left+size-pr,top+pr,pr,Color.BLACK);
            circle(c,left+pr,top+size-pr,pr,Color.BLACK);circle(c,left+size-pr,top+size-pr,pr,Color.BLACK);

            float dx=tx-sx,dy=ty-sy,dist=(float)Math.hypot(dx,dy);
            if(dist>1 && !pocketed){
                float ux=dx/dist,uy=dy/dist;
                p.setColor(Color.rgb(35,105,220));p.setStrokeWidth(4);p.setStyle(Paint.Style.STROKE);
                c.drawLine(sx,sy,tx+ux*size*.24f,ty+uy*size*.24f,p);p.setStyle(Paint.Style.FILL);
                drawPrediction(c,ux,uy);
            }
            if(!pocketed) circle(c,tx,ty,ballR*1.35f,Color.RED);
            circle(c,sx,sy,ballR*1.65f,Color.WHITE);

            double angle=Math.toDegrees(Math.atan2(-dy,dx)); if(angle<0)angle+=360;
            text(c,String.format(Locale.US,"Aim angle: %.1f°",angle),20,top+size+42,18,Color.WHITE,false);
            text(c,"Pocket prediction: "+(pocketed?"Potted!":predictionName()),20,top+size+70,17,Color.rgb(255,220,130),true);
            text(c,"Best shot: "+bestShot(),20,top+size+98,16,Color.WHITE,false);
            float by=top+size+120;
            shootButton.set(20,by, getWidth()/2f-8,by+54);
            resetButton.set(getWidth()/2f+8,by,getWidth()-20,by+54);
            fill(p,Color.rgb(27,145,85));c.drawRoundRect(shootButton,12,12,p);
            fill(p,Color.rgb(65,85,110));c.drawRoundRect(resetButton,12,12,p);
            text(c,"SHOOT",shootButton.left+shootButton.width()/2-28,by+35,18,Color.WHITE,true);
            text(c,"RESET",resetButton.left+resetButton.width()/2-28,by+35,18,Color.WHITE,true);
            text(c,status,20,by+82,14,Color.LTGRAY,false);
            if(moving) postInvalidateDelayed(16);
        }
        void drawPrediction(Canvas c,float ux,float uy){
            float ex=tx+ux*size*.24f,ey=ty+uy*size*.24f;
            p.setColor(Color.rgb(235,155,35));p.setStrokeWidth(3);p.setStyle(Paint.Style.STROKE);
            c.drawLine(tx,ty,ex,ey,p);p.setStyle(Paint.Style.FILL);
        }
        String predictionName(){
            if(pocketed)return "Potted";
            float ux=tx-sx,uy=ty-sy,d=(float)Math.hypot(ux,uy);if(d<1)return "Adjust aim";
            ux/=d;uy/=d;
            float best=Float.MAX_VALUE;String name="Board";
            float[][] pts={{left+pocketR,top+pocketR},{left+size-pocketR,top+pocketR},{left+pocketR,top+size-pocketR},{left+size-pocketR,top+size-pocketR}};
            String[] names={"Top-left","Top-right","Bottom-left","Bottom-right"};
            for(int i=0;i<4;i++){
                float vx=pts[i][0]-tx,vy=pts[i][1]-ty;
                float t=vx*ux+vy*uy;
                float perp=Math.abs(vx*uy-vy*ux);
                if(t>0 && perp< size*.18f && t<best){best=t;name=names[i];}
            }
            return name;
        }
        String bestShot(){
            String pocket=predictionName();
            if(pocket.equals("Board"))return "Change angle toward a corner pocket";
            return "Aim toward "+pocket;
        }
        void shoot(){
            if(moving)return;
            float dx=tx-sx,dy=ty-sy,d=(float)Math.hypot(dx,dy);
            if(d<ballR*2){status="Move striker farther from the red coin";return;}
            float power=Math.min(18f,d/12f);
            svx=dx/d*power;svy=dy/d*power;moving=true;status="Shot in motion...";
            postInvalidate();
        }
        void reset(){
            sx=left+size/2;sy=top+size*.78f;tx=left+size/2;ty=top+size*.43f;
            svx=svy=tvx=tvy=0;moving=false;pocketed=false;status="Board reset. Set your next aim.";
            invalidate();
        }
        void step(){
            if(!moving)return;
            sx+=svx;sy+=svy;tx+=tvx;ty+=tvy;
            svx*=.985f;svy*=.985f;tvx*=.985f;tvy*=.985f;
            float minX=left+ballR*2,maxX=left+size-ballR*2,minY=top+ballR*2,maxY=top+size-ballR*2;
            if(sx<minX||sx>maxX){svx=-svx*.72f;sx=clamp(sx,minX,maxX);}
            if(sy<minY||sy>maxY){svy=-svy*.72f;sy=clamp(sy,minY,maxY);}
            if(tx<minX||tx>maxX){tvx=-tvx*.72f;tx=clamp(tx,minX,maxX);}
            if(ty<minY||ty>maxY){tvy=-tvy*.72f;ty=clamp(ty,minY,maxY);}
            float dx=tx-sx,dy=ty-sy,d=(float)Math.hypot(dx,dy);
            if(d<ballR*3 && d>0){
                float nx=dx/d,ny=dy/d;
                float impulse=(svx*nx+svy*ny);
                if(impulse>0){tvx+=nx*impulse*.9f;tvy+=ny*impulse*.9f;svx*=.25f;svy*=.25f;}
            }
            float[][] holes={{left+pocketR,top+pocketR},{left+size-pocketR,top+pocketR},{left+pocketR,top+size-pocketR},{left+size-pocketR,top+size-pocketR}};
            for(float[] h:holes) if(Math.hypot(tx-h[0],ty-h[1])<pocketR*.72f){pocketed=true;moving=false;status="Great! Red coin reached a pocket.";return;}
            if(Math.hypot(svx,svy)+Math.hypot(tvx,tvy)<.18f){moving=false;status="Shot finished. Adjust and try again.";}
        }
        @Override public boolean onTouchEvent(MotionEvent e){
            float x=e.getX(),y=e.getY();
            if(e.getAction()==MotionEvent.ACTION_DOWN){
                if(shootButton.contains(x,y)){shoot();return true;}
                if(resetButton.contains(x,y)){reset();return true;}
                if(!moving && Math.hypot(x-sx,y-sy)<ballR*3.5f){dragging=true;return true;}
                return true;
            }
            if(e.getAction()==MotionEvent.ACTION_MOVE && dragging){
                sx=clamp(x,left+size*.12f,left+size*.88f);sy=clamp(y,top+size*.12f,top+size*.88f);invalidate();return true;
            }
            if(e.getAction()==MotionEvent.ACTION_UP){dragging=false;return true;}
            return true;
        }
    }
}