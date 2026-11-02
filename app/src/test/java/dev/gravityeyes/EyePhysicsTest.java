package dev.gravityeyes;
import org.junit.Test;
import static org.junit.Assert.*;
public class EyePhysicsTest {
    @Test public void staysBoundedUnderShakesAndDelayedFrames() {
        EyePhysics p = new EyePhysics();
        for(int i=0;i<20000;i++) {
            p.step((float)Math.sin(i)*5,(float)Math.cos(i*.3)*5,i%19==0 ? 2f : 1f/30);
            for(int j=0;j<2;j++) assertTrue(Math.hypot(p.x[j],p.y[j]) <= 1.00001);
        }
    }
    @Test public void settlesAtCenterWhenFlat() {
        EyePhysics p = new EyePhysics();
        for(int i=0;i<150;i++) p.step(1,1,1f/30);
        for(int i=0;i<600;i++) p.step(0,0,1f/30);
        assertEquals(0,p.x[0],0.001); assertEquals(0,p.y[1],0.001);
    }
    @Test public void followsGravityAndRebounds() {
        EyePhysics p = new EyePhysics(); boolean rebound=false; float before=0;
        for(int i=0;i<180;i++) { p.step(1,0,1f/120); if(p.x[0]<before) rebound=true; before=p.x[0]; }
        assertTrue(p.x[0]>0.5); assertTrue(rebound);
    }
    @Test public void rendersTwoEyesWithin13Square() {
        int[] image = EyeRenderer.pixels(new EyePhysics());
        assertEquals(169,image.length); assertEquals(0xffffffff,image[5*13+3]);
        assertEquals(0xff000000,image[6*13+3]); assertEquals(0xff000000,image[6*13+9]);
        assertEquals(0xff000000,image[6*13+6]);
    }
}
