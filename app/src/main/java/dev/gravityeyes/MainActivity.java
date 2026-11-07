package dev.gravityeyes;

import android.app.Activity;
import android.os.Bundle;
import android.content.*;
import android.graphics.*;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.*;
import android.widget.*;

public final class MainActivity extends Activity {
    private final Handler handler=new Handler(Looper.getMainLooper());
    private int duration=400,brightness=70,tilt=0;
    private String closing="center",sleeping="line",motionMode="gravity",lockBehavior="sleep";
    private int wanderInterval=1800;
    private Preview preview;
    private TextView status;
    private boolean loop=false;
    private float openness=1f,from=1f,to=1f;
    private long started=0,lastLoop=0;
    private boolean blinkReturn=false;
    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private TextView label(String s,int size){
        TextView t=new TextView(this);t.setText(s);t.setTextColor(0xfff1f1f1);t.setTextSize(size);
        t.setPadding(0,dp(10),0,dp(6));return t;
    }
    private void save(){EyeSettings.save(this,duration,brightness,tilt,closing,sleeping,motionMode,lockBehavior,wanderInterval);}
    private void transition(float target){from=openness;to=target;started=SystemClock.uptimeMillis();status.setText(target==0?"CLOSING...":"OPENING...");}
    private void actionButton(LinearLayout row,String name,Runnable action){
        Button b=new Button(this);b.setText(name);b.setTextAllCaps(false);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(52),1);lp.setMargins(dp(3),0,dp(3),0);
        row.addView(b,lp);b.setOnClickListener(v->action.run());
    }
    private LinearLayout row(LinearLayout parent){
        LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);parent.addView(r);return r;
    }
    private void slider(LinearLayout root,String name,int max,int progress,java.util.function.IntConsumer changed){
        TextView title=label(name,14);root.addView(title);
        SeekBar bar=new SeekBar(this);bar.setMax(max);bar.setProgress(progress);root.addView(bar);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar b,int p,boolean user){if(user){changed.accept(p);save();preview.invalidate();}}
            public void onStartTrackingTouch(SeekBar b){}
            public void onStopTrackingTouch(SeekBar b){}
        });
    }
    private void dropdown(LinearLayout root,String name,String[] labels,String[] values,String current,java.util.function.Consumer<String> changed){
        root.addView(label(name,14));
        Spinner spin=new Spinner(this);ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,labels);
        spin.setAdapter(adapter);root.addView(spin);
        for(int i=0;i<values.length;i++)if(values[i].equals(current))spin.setSelection(i);
        spin.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(android.widget.AdapterView<?> p){}
            public void onItemSelected(android.widget.AdapterView<?> p,View v,int index,long id){
                changed.accept(values[index]);save();preview.invalidate();
            }
        });
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(0xff101112);getWindow().setNavigationBarColor(0xff101112);
        EyeSettings s=EyeSettings.load(this);
        duration=s.duration;brightness=s.brightness;tilt=s.tilt;closing=s.closing;sleeping=s.sleeping;motionMode=s.motion;lockBehavior=s.lockBehavior;wanderInterval=s.wanderInterval;
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(0xff101112);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(20),dp(16),dp(20),dp(24));
        scroll.addView(root);setContentView(scroll);
        TextView heading=label("GLYPH EYES",24);heading.setTypeface(null,1);root.addView(heading);
        root.addView(label("13 × 13 MATRIX  /  ANIMATION LAB",12));
        status=label("AWAKE",13);status.setTextColor(0xffd6f36b);root.addView(status);
        preview=new Preview();root.addView(preview,new LinearLayout.LayoutParams(-1,dp(290)));
        LinearLayout r=row(root);
        actionButton(r,"☾ Sleep",()->{loop=false;blinkReturn=false;transition(0);});
        actionButton(r,"☀ Wake",()->{loop=false;blinkReturn=false;transition(1);});
        r=row(root);
        actionButton(r,"↻ Auto preview",()->{loop=!loop;lastLoop=0;});
        actionButton(r,"◉ Blink",()->{loop=false;blinkReturn=true;transition(0);});
        root.addView(label("PUPIL BEHAVIOR",18));
        dropdown(root,"Pupil movement",new String[]{"Follow gravity","Random wandering"},new String[]{"gravity","random"},motionMode,v->{motionMode=v;preview.resetMotion();});
        dropdown(root,"When screen locks",new String[]{"Close eyes (sleep)","Keep eyes open"},new String[]{"sleep","open"},lockBehavior,v->lockBehavior=v);
        slider(root,"Random glance interval (400–6000 ms)",56,(wanderInterval-400)/100,p->wanderInterval=400+p*100);
        root.addView(label("CUSTOMIZATION",18));
        slider(root,"Eyelid animation duration: change then preview",1100,duration-100,p->{duration=100+p;});
        dropdown(root,"Closing style",new String[]{"Symmetrical closing","Upper eyelid drops","Lower eyelid rises"},new String[]{"center","top","bottom"},closing,v->closing=v);
        dropdown(root,"Sleeping eyes",new String[]{"Thin horizontal line","Completely dark","Curved sleepy eyes"},new String[]{"line","off","arc"},sleeping,v->sleeping=v);
        slider(root,"LED brightness",85,brightness-15,p->brightness=15+p);
        slider(root,"Pupil horizontal tilt",4,tilt+2,p->tilt=p-2);
        Button toy=new Button(this);toy.setText("Open Glyph Toys");root.addView(toy);
        toy.setOnClickListener(v->{try{startActivity(new Intent().setComponent(new ComponentName("com.nothing.thirdparty","com.nothing.thirdparty.matrix.toys.manager.ToysManagerActivity")));}catch(Exception e){Toast.makeText(this,"Open Settings → Glyph Interface → Glyph Toys manually.",Toast.LENGTH_LONG).show();}});
        root.addView(label("Settings save automatically. The preview uses simulated gravity when Gravity is selected; the rear Glyph Toy uses the real accelerometer. Reopen the Glyph Toy after changing movement mode. Nothing OS controls AOD visibility.",12));
    }
    private final class Preview extends View {
        private final Paint paint=new Paint(3);
        private final EyePhysics physics=new EyePhysics();
        private final PupilMotion pupilMotion=new PupilMotion();
        private long lastDraw=0;
        void resetMotion(){pupilMotion.reset();}
        Preview(){super(MainActivity.this);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            c.drawColor(0xff080909);
            long now=SystemClock.uptimeMillis();
            float dt=lastDraw==0?1f/30f:Math.min(0.05f,(now-lastDraw)/1000f);
            lastDraw=now;
            pupilMotion.update(physics,motionMode,0f,0f,dt,now,wanderInterval);
            if(started>0){
                float t=Math.min(1f,(now-started)/(float)Math.max(100,duration));
                float eased=t*t*(3f-2f*t);openness=from+(to-from)*eased;
                if(t>=1){started=0;openness=to;status.setText(openness<.13f?"SLEEPING":"AWAKE");if(blinkReturn&&to==0){blinkReturn=false;transition(1);}}
            }
            if(loop&&started==0&&(lastLoop==0||now-lastLoop>1250)){lastLoop=now;transition(to==0?1:0);}
            int[] pixels=EyeRenderer.pixels(physics,openness,closing,sleeping,tilt);
            float unit=Math.min(getWidth(),getHeight())/15f;
            float left=(getWidth()-13*unit)/2f,top=(getHeight()-13*unit)/2f;
            for(int y=0;y<13;y++)for(int x=0;x<13;x++){
                float px=left+(x+.5f)*unit,py=top+(y+.5f)*unit;
                paint.setColor(0xff202224);paint.setShadowLayer(0,0,0,0);c.drawCircle(px,py,unit*.13f,paint);
                if(pixels[y*13+x]!=0xff000000){
                    paint.setColor(Color.argb(Math.min(255,brightness*255/100),245,255,238));
                    paint.setShadowLayer(unit*.32f,0,0,0xffd6f36b);
                    c.drawCircle(px,py,unit*.28f,paint);paint.clearShadowLayer();
                }
            }
            postInvalidateDelayed(33);
        }
    }
}
