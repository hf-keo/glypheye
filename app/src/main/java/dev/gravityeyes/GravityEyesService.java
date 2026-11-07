package dev.gravityeyes;

import android.app.Service;
import android.content.*;
import android.graphics.Bitmap;
import android.hardware.*;
import android.os.*;
import android.util.Log;
import com.nothing.ketchum.*;

/** Bound only while selected by Nothing's AOD Toy host. No foreground-service workaround. */
public final class GravityEyesService extends Service implements SensorEventListener {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final EyePhysics eyes = new EyePhysics();
    private final PupilMotion motion = new PupilMotion();
    private final java.util.Random blinkRandom = new java.util.Random();
    private long nextBlink, blinkStarted;
    private boolean blinking;
    private GlyphMatrixManager matrix;
    private SensorManager sensors;
    private boolean active, ready, screenAwake = true, receiverRegistered;
    private long eyelidStarted;
    private float eyelidFrom = 1f, eyelidTo = 1f;
    private EyeSettings settings;
    private long eyelidDuration() { return settings == null ? 400L : settings.duration; }
    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) animateEyelids(false);
            else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) animateEyelids(true);
        }
    };
    private void animateEyelids(boolean awake) {
        long now = SystemClock.uptimeMillis();
        eyelidFrom = openness(now);
        settings = EyeSettings.load(GravityEyesService.this);
        eyelidTo = awake || "open".equals(settings.lockBehavior) ? 1f : 0f;
        eyelidStarted = now;
        screenAwake = awake;
        if (active && ready) { settings = EyeSettings.load(GravityEyesService.this); handler.removeCallbacks(tick); handler.post(tick); }
    }
    private float openness(long now) {
        if (eyelidStarted == 0) return (screenAwake || (settings!=null && "open".equals(settings.lockBehavior))) ? 1f : 0f;
        float t = Math.min(1f, Math.max(0f, (now - eyelidStarted) / (float) eyelidDuration()));
        t = t*t*(3f-2f*t);
        return eyelidFrom + (eyelidTo-eyelidFrom)*t;
    }
    private float gx, gy;
    private long lastFrame, lastSensor;
    private final Bitmap bitmap = Bitmap.createBitmap(13, 13, Bitmap.Config.ARGB_8888);
    private final Messenger messenger = new Messenger(new Handler(Looper.getMainLooper()) {
        @Override public void handleMessage(Message msg) {
            if (msg.what == GlyphToy.MSG_GLYPH_TOY &&
                GlyphToy.EVENT_AOD.equals(msg.getData().getString(GlyphToy.MSG_GLYPH_TOY_DATA))) {
                // Refresh without starting a duplicate loop on the host's minute tick.
                if (active && ready) { handler.removeCallbacks(tick); handler.post(tick); }
            } else super.handleMessage(msg);
        }
    });
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!active || !ready) return;
            long now = SystemClock.elapsedRealtimeNanos();
            float dt = lastFrame == 0 ? 1f/30 : (now-lastFrame)*1e-9f;
            lastFrame = now;
            long frameTime=SystemClock.uptimeMillis();
            motion.update(eyes,settings.motion,gx,gy,dt,frameTime,settings.wanderInterval);
            float eyeOpen=openness(frameTime);
            if(settings.randomBlink && eyeOpen>=0.99f && !blinking){
                if(nextBlink==0)nextBlink=frameTime+settings.blinkInterval/2+(long)(blinkRandom.nextFloat()*settings.blinkInterval);
                if(frameTime>=nextBlink){blinking=true;blinkStarted=frameTime;nextBlink=0;}
            }
            if(blinking){float phase=(frameTime-blinkStarted)/(float)Math.max(80,settings.blinkDuration);
                if(phase>=1f){blinking=false;nextBlink=0;}
                else if(eyeOpen>=0.99f)eyeOpen*=phase<0.5f?1f-2f*phase:2f*phase-1f;
            }
            try {
                bitmap.setPixels(EyeRenderer.pixels(eyes, eyeOpen, settings.closing, settings.sleeping, settings.tilt), 0, 13, 0, 0, 13, 13);
                GlyphMatrixObject object = new GlyphMatrixObject.Builder().setImageSource(bitmap).setBrightness(Math.max(1, Math.min(255, Math.round(settings.brightness * 2.55f)))).build();
                // Use the structured frame: SDK selects the device's matrix length.
                matrix.setMatrixFrame(new GlyphMatrixFrame.Builder().addTop(object).build(GravityEyesService.this));
                handler.postDelayed(this, 33);
            } catch (Exception e) { Log.e("GravityEyes", "Matrix frame failed; stopping toy", e); stopToy(); }
        }
    };
    @Override public IBinder onBind(Intent intent) {
        active = true; lastFrame = 0; lastSensor = 0; gx = gy = 0;
        settings = EyeSettings.load(this);
        motion.reset();nextBlink=0;blinking=false;
        PowerManager power = (PowerManager)getSystemService(POWER_SERVICE);
        screenAwake = power == null || power.isInteractive();
        eyelidStarted = 0; eyelidFrom = eyelidTo = (screenAwake || "open".equals(settings.lockBehavior)) ? 1f : 0f;
        if (!receiverRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            registerReceiver(screenReceiver, filter);
            receiverRegistered = true;
        }
        sensors = (SensorManager)getSystemService(SENSOR_SERVICE);
        matrix = GlyphMatrixManager.getInstance(getApplicationContext());
        try {
            matrix.init(new GlyphMatrixManager.Callback() {
                @Override public void onServiceConnected(ComponentName name) {
                    if (!active) return;
                    try {
                        ready = matrix.register(Glyph.DEVICE_25111p);
                        if (!ready) { stopToy(); return; }
                        Sensor accelerometer = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                        if (accelerometer != null && "gravity".equals(settings.motion)) sensors.registerListener(GravityEyesService.this, accelerometer, SensorManager.SENSOR_DELAY_GAME, handler);
                        handler.removeCallbacks(tick); handler.post(tick);
                    } catch (Exception e) { Log.e("GravityEyes", "SDK setup failed", e); stopToy(); }
                }
                @Override public void onServiceDisconnected(ComponentName name) {
                    ready = false; handler.removeCallbacks(tick);
                    if (sensors != null) sensors.unregisterListener(GravityEyesService.this);
                }
            });
        } catch (Exception e) { Log.e("GravityEyes", "SDK unavailable", e); stopToy(); }
        return messenger.getBinder();
    }
    @Override public void onSensorChanged(SensorEvent event) {
        // Android accelerometer reports support force: invert for gravity.
        // Rear view mirrors device X; device +Y points upward, so rear-screen down is +aY.
        float dt = lastSensor == 0 ? 0.02f : Math.min(0.2f, (event.timestamp-lastSensor)*1e-9f);
        lastSensor = event.timestamp;
        float alpha = 1f - (float)Math.exp(-dt/0.12f);
        gx += alpha*(event.values[0]/SensorManager.GRAVITY_EARTH-gx);
        gy += alpha*(event.values[1]/SensorManager.GRAVITY_EARTH-gy);
    }
    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    private void stopToy() {
        active = ready = false;
        handler.removeCallbacksAndMessages(null);
        if (sensors != null) sensors.unregisterListener(this);
        if (receiverRegistered) { unregisterReceiver(screenReceiver); receiverRegistered = false; }
        GlyphMatrixManager old = matrix; matrix = null;
        if (old != null) { try { old.turnOff(); } catch (Exception e) { Log.w("GravityEyes", "Turn off failed", e); }
            try { old.unInit(); } catch (Exception e) { Log.w("GravityEyes", "Unbind failed", e); } }
    }
    @Override public boolean onUnbind(Intent intent) { stopToy(); return false; }
    @Override public void onDestroy() { stopToy(); bitmap.recycle(); super.onDestroy(); }
}
