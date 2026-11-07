package ru.givler.mbo.client.render.light;

import com.google.gson.*;
import java.io.InputStreamReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Small cuboid model reader for the original LastDawn torch geometry and UVs. */
final class PortableLightModel {
  private final JsonObject model;
  PortableLightModel() { this("torch"); }
  PortableLightModel(String name) {
    try (InputStreamReader in = new InputStreamReader(getClass().getResourceAsStream(
        "/assets/mbo/portablelights/"+name+".json"), "UTF-8")) {
      model = new JsonParser().parse(in).getAsJsonObject();
    } catch (Exception e) { throw new IllegalStateException("Missing portable torch model", e); }
  }
  void render() {
    GL11.glPushMatrix();
    try {
      GL11.glTranslatef(-.5F, 0, -.5F);
      GL11.glDisable(GL11.GL_CULL_FACE);
      for (JsonElement element : model.getAsJsonArray("elements")) {
        JsonObject box=element.getAsJsonObject();
        float[] a=vector(box.getAsJsonArray("from")), b=vector(box.getAsJsonArray("to"));
        JsonObject faces=box.getAsJsonObject("faces");
        for (java.util.Map.Entry<String,JsonElement> entry:faces.entrySet()) {
          JsonObject face=entry.getValue().getAsJsonObject();
          String texture=face.get("texture").getAsString();
          while(texture.startsWith("#")) texture=model.getAsJsonObject("textures").get(texture.substring(1)).getAsString();
          net.minecraft.util.IIcon icon=PortableLights.icon(texture);
          if(icon==null)continue;
          Minecraft.getMinecraft().getTextureManager().bindTexture(net.minecraft.client.renderer.texture.TextureMap.locationBlocksTexture);
          float[] uv=vector(face.getAsJsonArray("uv"));
          float[][] p=vertices(entry.getKey(),a,b);
          Tessellator t=Tessellator.instance;t.startDrawingQuads();
          float[] normal=normal(entry.getKey());t.setNormal(normal[0],normal[1],normal[2]);
          for(int i=0;i<4;i++) t.addVertexWithUV(p[i][0]/16,p[i][1]/16,p[i][2]/16,
              icon.getInterpolatedU(uv[(i==0 || i==3)?0:2]),icon.getInterpolatedV(uv[i<2?3:1]));
          t.draw();
        }
      }
    } finally { GL11.glPopMatrix(); }
  }
  private static float[] vector(JsonArray values) { float[] result=new float[values.size()];for(int i=0;i<result.length;i++)result[i]=values.get(i).getAsFloat();return result; }
  private static float[] normal(String face) {
    switch(face) {case "up":return new float[]{0,1,0};case "down":return new float[]{0,-1,0};case "east":return new float[]{1,0,0};case "west":return new float[]{-1,0,0};case "south":return new float[]{0,0,1};default:return new float[]{0,0,-1};}
  }
  private static float[][] vertices(String face,float[] a,float[] b) {
    float x=a[0],y=a[1],z=a[2],X=b[0],Y=b[1],Z=b[2];
    switch(face) {
      case "up":return new float[][]{{x,Y,z},{X,Y,z},{X,Y,Z},{x,Y,Z}};
      case "down":return new float[][]{{x,y,Z},{X,y,Z},{X,y,z},{x,y,z}};
      case "east":return new float[][]{{X,y,Z},{X,y,z},{X,Y,z},{X,Y,Z}};
      case "west":return new float[][]{{x,y,z},{x,y,Z},{x,Y,Z},{x,Y,z}};
      case "south":return new float[][]{{x,y,Z},{X,y,Z},{X,Y,Z},{x,Y,Z}};
      default:return new float[][]{{X,y,z},{x,y,z},{x,Y,z},{X,Y,z}};
    }
  }
}
