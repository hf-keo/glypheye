package dev.gravityeyes;

import java.util.Random;

/** Smooth shared gaze controller for preview and rear Glyph renderer. */
public final class PupilMotion {
    private final Random random=new Random();
    private float targetX,targetY,lookX,lookY;
    private long nextTarget;
    public void reset(){targetX=targetY=lookX=lookY=0;nextTarget=0;}
    public void update(EyePhysics eyes,String mode,float gx,float gy,float dt,long now,int intervalMs){
        if("random".equals(mode)){
            if(now>=nextTarget){
                // Most glances are directional, with occasional centered rests.
                float angle=(float)(random.nextDouble()*Math.PI*2);
                float radius=random.nextFloat()<0.20f?0f:(0.35f+random.nextFloat()*0.65f);
                targetX=(float)Math.cos(angle)*radius;
                targetY=(float)Math.sin(angle)*radius;
                int base=Math.max(400,Math.min(6000,intervalMs));
                nextTarget=now+Math.max(250,(long)(base*(0.7f+random.nextFloat()*0.6f)));
            }
            float blend=1f-(float)Math.exp(-Math.max(0f,Math.min(dt,0.05f))*5f);
            lookX+=(targetX-lookX)*blend;
            lookY+=(targetY-lookY)*blend;
            eyes.step(lookX,lookY,dt);
        }else{
            nextTarget=0;
            eyes.step(gx,gy,dt);
        }
    }
}
