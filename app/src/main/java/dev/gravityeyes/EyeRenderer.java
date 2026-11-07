package dev.gravityeyes;
public final class EyeRenderer {
    private EyeRenderer(){}
    public static int[] pixels(EyePhysics p){return pixels(p,1f,"center","line",0);}
    public static int[] pixels(EyePhysics p,float openness){return pixels(p,openness,"center","line",0);}
    public static int[] pixels(EyePhysics p,float openness,String closing,String sleeping,int tilt){
        int[] out=new int[169];java.util.Arrays.fill(out,0xff000000);
        float o=Math.max(0f,Math.min(1f,openness));
        for(int cx:new int[]{3,9}){
            if(o<0.13f){
                if("line".equals(sleeping))for(int x=cx-2;x<=cx+2;x++)out[7*13+x]=0xffffffff;
                else if("arc".equals(sleeping))for(int dx=-2;dx<=2;dx++)out[(7+(Math.abs(dx)==2?0:1))*13+cx+dx]=0xffffffff;
                continue;
            }
            int height=Math.max(1,Math.round(6*o));
            int top="top".equals(closing)?4:("bottom".equals(closing)?10-height:7-height/2);
            top=Math.max(0,Math.min(13-height,top));
            int bottom=top+height-1;
            for(int y=top;y<=bottom;y++)for(int x=cx-2;x<=cx+2;x++){
                if(height>=4&&(y==top||y==bottom)&&Math.abs(x-cx)==2)continue;
                out[y*13+x]=0xffffffff;
            }
            if(height>=4){
                int px=Math.max(cx-2,Math.min(cx+2,cx+Math.round(p.x[cx==3?0:1])+tilt));
                int py=Math.max(top,Math.min(bottom,6+Math.round(p.y[cx==3?0:1])));
                out[py*13+px]=0xff000000;
                if(py+1<=bottom)out[(py+1)*13+px]=0xff000000;
            }
        }
        return out;
    }
}
