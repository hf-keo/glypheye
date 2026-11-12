package dev.gravityeyes;

import android.app.Activity;
import android.os.Bundle;
import android.content.*;
import android.graphics.*;
import android.hardware.*;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.*;
import android.widget.*;
import android.provider.Settings;
import android.app.NotificationManager;
import android.content.ComponentName;

public final class MainActivity extends Activity implements SensorEventListener {
    private SensorManager sensorManager;
    private float gravityX, gravityY;
    @Override protected void onResume(){super.onResume();sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);if(sensorManager!=null){Sensor a=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);if(a!=null)sensorManager.registerListener(this,a,SensorManager.SENSOR_DELAY_GAME);}}
    @Override protected void onPause(){if(sensorManager!=null)sensorManager.unregisterListener(this);super.onPause();}
    @Override public void onSensorChanged(SensorEvent event){gravityX=event.values[0]/SensorManager.GRAVITY_EARTH;gravityY=event.values[1]/SensorManager.GRAVITY_EARTH;}
    @Override public void onAccuracyChanged(Sensor sensor,int accuracy){}
    private final Handler handler=new Handler(Looper.getMainLooper());
    private int duration=400,brightness=70,tilt=0;
    private String closing="center",sleeping="line",motionMode="gravity",lockBehavior="sleep";
    private int wanderInterval=1800,blinkInterval=5000,blinkDuration=180;
    private boolean randomBlink=false;
    private final java.util.Random blinkRandom=new java.util.Random();
    private long nextBlink=0,blinkStarted=0;
    private boolean blinking=false;
    private String previewReaction="";
    private long previewReactionStart=0;
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
    private void save(){EyeSettings.save(this,duration,brightness,tilt,closing,sleeping,motionMode,lockBehavior,wanderInterval,randomBlink,blinkInterval,blinkDuration);}
    private void testReaction(String type){
        previewReaction=type;
        previewReactionStart=SystemClock.uptimeMillis();
        EyeReactions.trigger(this,type,true);
        preview.invalidate();
        Toast.makeText(this,"Previewing "+type+" on app and widget",Toast.LENGTH_SHORT).show();
    }
    private void transition(float target){from=openness;to=target;started=SystemClock.uptimeMillis();status.setText(target==0?"CLOSING...":"OPENING...");}
    private void actionButton(LinearLayout row,String name,Runnable action){
        Button b=new Button(this);b.setText(name);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(52),1);lp.setMargins(dp(3),0,dp(3),0);
        row.addView(b,lp);b.setOnClickListener(v->action.run());
    }
    private LinearLayout row(LinearLayout parent){
        LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);parent.addView(r);return r;
    }
    private void slider(LinearLayout root,String name,int max,int progress,java.util.function.IntConsumer changed,java.util.function.Supplier<String> display){
        LinearLayout header=row(root);
        TextView title=label(name,14);header.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        TextView number=label(display.get(),14);number.setTextColor(0xffd6f36b);number.setPadding(dp(8),dp(10),0,dp(6));header.addView(number);
        SeekBar bar=new SeekBar(this);bar.setMax(max);bar.setProgress(progress);root.addView(bar);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar b,int p,boolean user){if(user){changed.accept(p);number.setText(display.get());save();preview.invalidate();}}
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
        duration=s.duration;brightness=s.brightness;tilt=s.tilt;closing=s.closing;sleeping=s.sleeping;motionMode=s.motion;lockBehavior=s.lockBehavior;wanderInterval=s.wanderInterval;randomBlink=s.randomBlink;blinkInterval=s.blinkInterval;blinkDuration=s.blinkDuration;
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(0xff101112);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(20),dp(16),dp(20),dp(24));
        scroll.addView(root);setContentView(scroll);
        TextView heading=label("GLYPH EYES",24);heading.setTypeface(null,android.graphics.Typeface.BOLD);root.addView(heading);
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
        slider(root,"Random glance interval",56,(wanderInterval-400)/100,p->wanderInterval=400+p*100,()->String.format(java.util.Locale.US,"%.1f s",wanderInterval/1000f));
        root.addView(label("BLINKING",18));
        Switch blinkSwitch=new Switch(this);blinkSwitch.setText("Random blinking");blinkSwitch.setTextColor(0xfff1f1f1);blinkSwitch.setChecked(randomBlink);root.addView(blinkSwitch);
        blinkSwitch.setOnCheckedChangeListener((button,checked)->{randomBlink=checked;nextBlink=0;blinking=false;save();});
        slider(root,"Average blink interval",96,(blinkInterval-2000)/250,p->{blinkInterval=2000+p*250;nextBlink=0;},()->String.format(java.util.Locale.US,"%.1f s",blinkInterval/1000f));
        slider(root,"Blink duration",22,(blinkDuration-80)/20,p->blinkDuration=80+p*20,()->blinkDuration+" ms");
        root.addView(label("NOTIFICATION REACTIONS",18));
        Switch notifySwitch=new Switch(this);
        notifySwitch.setText("React to incoming notifications");
        notifySwitch.setTextColor(0xfff1f1f1);
        notifySwitch.setChecked(EyeReactions.enabled(this));
        root.addView(notifySwitch);
        notifySwitch.setOnCheckedChangeListener((v,checked)->EyeReactions.setEnabled(this,checked));
        Button access=new Button(this);
        access.setText("Grant notification access");
        root.addView(access);
        access.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        Switch dnd=new Switch(this);dnd.setText("Respect Do Not Disturb");dnd.setTextColor(0xfff1f1f1);
        dnd.setChecked(EyeReactions.respectDnd(this));root.addView(dnd);
        dnd.setOnCheckedChangeListener((v,checked)->EyeReactions.setRespectDnd(this,checked));
        slider(root,"Reaction duration",27,(EyeReactions.duration(this)-300)/100,
            p->EyeReactions.setDuration(this,300+p*100),
            ()->EyeReactions.duration(this)+" ms");
        slider(root,"Notification cooldown",29,(EyeReactions.cooldown(this)-1000)/1000,
            p->EyeReactions.setCooldown(this,1000+p*1000),
            ()->EyeReactions.cooldown(this)/1000+" s");
        root.addView(label("App allowlist (package names, comma-separated; blank = all)",13));
        EditText allowlist=new EditText(this);allowlist.setSingleLine(false);
        allowlist.setMinLines(1);allowlist.setText(EyeReactions.allowlist(this));
        allowlist.setHint("e.g. com.whatsapp, org.telegram.messenger");
        root.addView(allowlist);
        allowlist.addTextChangedListener(new android.text.TextWatcher(){
            public void beforeTextChanged(CharSequence v,int start,int count,int after){}
            public void onTextChanged(CharSequence v,int start,int before,int count){EyeReactions.setAllowlist(MainActivity.this,v.toString());}
            public void afterTextChanged(android.text.Editable e){}
        });
        root.addView(label("TEST REACTIONS — app preview + 2×2 widget",14));
        LinearLayout tests=row(root);
        actionButton(tests,"Curious",()->testReaction(EyeReactions.CURIOUS));
        actionButton(tests,"Surprised",()->testReaction(EyeReactions.SURPRISED));
        tests=row(root);
        actionButton(tests,"Sleepy",()->testReaction(EyeReactions.SLEEPY));
        actionButton(tests,"Clear",()->{previewReaction="";preview.invalidate();});
        root.addView(label("CUSTOMIZATION",18));
        slider(root,"Sleep / wake duration",1100,duration-100,p->{duration=100+p;},()->duration+" ms");
        dropdown(root,"Closing style",new String[]{"Symmetrical closing","Upper eyelid drops","Lower eyelid rises"},new String[]{"center","top","bottom"},closing,v->closing=v);
        dropdown(root,"Sleeping eyes",new String[]{"Thin horizontal line","Completely dark","Curved sleepy eyes"},new String[]{"line","off","arc"},sleeping,v->sleeping=v);
        slider(root,"LED brightness",85,brightness-15,p->brightness=15+p,()->brightness+"%");
        slider(root,"Pupil horizontal tilt",4,tilt+2,p->tilt=p-2,()->(tilt>0?"+":"")+tilt);
        Button toy=new Button(this);toy.setText("Open Glyph Toys");root.addView(toy);
        toy.setOnClickListener(v->{try{startActivity(new Intent().setComponent(new ComponentName("com.nothing.thirdparty","com.nothing.thirdparty.matrix.toys.manager.ToysManagerActivity")));}catch(Exception e){Toast.makeText(this,"Open Settings → Glyph Interface → Glyph Toys manually.",Toast.LENGTH_LONG).show();}});
        root.addView(label("Settings save automatically. Gravity mode uses your phone accelerometer in both the preview and rear Glyph Toy. Reopen the Glyph Toy after changing movement mode. Nothing OS controls AOD visibility.",12));
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
            pupilMotion.update(physics,motionMode,gravityX,gravityY,dt,now,wanderInterval);
            if(randomBlink && !blinking && started==0 && openness>=0.99f){
                if(nextBlink==0)nextBlink=now+blinkInterval/2+(long)(blinkRandom.nextFloat()*blinkInterval);
                if(now>=nextBlink){blinking=true;blinkStarted=now;nextBlink=0;}
            }
            float blinkOpenness=1f;
            if(blinking){float phase=(now-blinkStarted)/(float)Math.max(80,blinkDuration);
                if(phase>=1f){blinking=false;nextBlink=0;}else blinkOpenness=phase<0.5f?1f-2f*phase:2f*phase-1f;
            }
            if(started>0){
                float t=Math.min(1f,(now-started)/(float)Math.max(100,duration));
                float eased=t*t*(3f-2f*t);openness=from+(to-from)*eased;
                if(t>=1){started=0;openness=to;status.setText(openness<.13f?"SLEEPING":"AWAKE");if(blinkReturn&&to==0){blinkReturn=false;transition(1);}}
            }
            if(loop&&started==0&&(lastLoop==0||now-lastLoop>1250)){lastLoop=now;transition(to==0?1:0);}
            int[] pixels=EyeRenderer.pixels(physics,openness*blinkOpenness,closing,sleeping,tilt);
            if(!previewReaction.isEmpty()){
                if(now-previewReactionStart>=EyeReactions.duration(MainActivity.this))previewReaction="";
                else pixels=EyeReactions.apply(physics,EyeSettings.load(MainActivity.this),openness*blinkOpenness,previewReaction,now-previewReactionStart);
            }
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
