package ru.givler.mbo.client.render.decormodels;

/** RenderHelper's fixed-function TESR lights, evaluated before chunk compilation. */
public final class DecorLighting {
    private DecorLighting() {}
    private static final double LENGTH=Math.sqrt(.20000000298023224*.20000000298023224
        +1+.699999988079071*.699999988079071);
    public static int shade(int normal,int meta) {
        double x=(byte)normal/127.0, y=(byte)(normal>>8)/127.0, z=(byte)(normal>>16)/127.0;
        double rx=x,rz=z;
        switch(meta&3) {
            case 0: rx=-x;rz=-z;break;
            case 1: rx=z;rz=-x;break;
            case 3: rx=-z;rz=x;break;
            default:break;
        }
        // Packed normals deliberately aren't renormalized: GL_RESCALE_NORMAL with
        // unit-scale rotations preserves the same packed-normal length in the TESR.
        double dot=(.20000000298023224*rx-.699999988079071*rz)/LENGTH;
        double up=y/LENGTH;
        double value=.4000000059604645+.6000000238418579*(Math.max(0,up+dot)+Math.max(0,up-dot));
        return Math.max(0,Math.min(255,(int)Math.round(value*255)));
    }
}
