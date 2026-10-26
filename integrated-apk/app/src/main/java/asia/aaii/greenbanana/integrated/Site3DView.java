package asia.aaii.greenbanana.integrated;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.util.Log;
import java.io.DataInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public final class Site3DView extends GLSurfaceView {
  private final Scene scene;
  private final ScaleGestureDetector zoom;
  private float lastX,lastY;
  public Site3DView(Context ctx){
    super(ctx);
    setEGLContextClientVersion(2);
    setEGLConfigChooser(8,8,8,8,24,0);
    scene = new Scene(ctx.getApplicationContext());
    setRenderer(scene);
    setRenderMode(RENDERMODE_CONTINUOUSLY);
    zoom=new ScaleGestureDetector(ctx,new ScaleGestureDetector.SimpleOnScaleGestureListener(){
      @Override public boolean onScale(ScaleGestureDetector d){scene.zoom(d.getScaleFactor());return true;}
    });
  }
  public void focus(String key){scene.focus(key);}
  public void cameraMode(String mode){scene.cameraMode(mode);}
  public void setInterior(boolean yes){scene.interior=yes;}
  @Override public boolean onTouchEvent(MotionEvent e){
    zoom.onTouchEvent(e);
    if(e.getActionMasked()==MotionEvent.ACTION_DOWN){lastX=e.getX();lastY=e.getY();return true;}
    if(e.getActionMasked()==MotionEvent.ACTION_MOVE){
      if(e.getPointerCount()==1&&!zoom.isInProgress()){scene.yaw-=(e.getX()-lastX)*.005f;scene.pitch=clamp(scene.pitch-(e.getY()-lastY)*.005f,.12f,1.49f);}
      lastX=e.getX();lastY=e.getY();return true;
    }
    if(e.getActionMasked()==MotionEvent.ACTION_UP){performClick();return true;}
    return true;
  }
  @Override public boolean performClick(){super.performClick();return true;}
  private static float clamp(float v,float a,float b){return Math.max(a,Math.min(v,b));}
  private static class Mesh {int layer,vc,ic,vbo,ibo; ByteBuffer verts,idx;}
  private static class Scene implements GLSurfaceView.Renderer {
    final Context app;
    final List<Mesh> meshes=new ArrayList<>();
    volatile boolean interior;
    volatile float yaw=-.85f,pitch=.9f,dist=995,tx=400,ty=250,tz=0;
    int program,uMVP,aPos,aColor,width=1,height=1;
    float[] projection=new float[16],view=new float[16],mvp=new float[16];
    Scene(Context c){app=c;}
    int shader(int type,String src){
      int id=GLES20.glCreateShader(type);GLES20.glShaderSource(id,src);GLES20.glCompileShader(id);
      int[] state=new int[1];GLES20.glGetShaderiv(id,GLES20.GL_COMPILE_STATUS,state,0);
      if(state[0]!=1)throw new RuntimeException("GL shader error "+GLES20.glGetShaderInfoLog(id));
      return id;
    }
    void readModel() throws Exception {
      try(InputStream stream=app.getAssets().open("site_model.bin");DataInputStream in=new DataInputStream(stream)){
        byte[] magic=new byte[4];in.readFully(magic);
        if(magic[0]!='G'||magic[1]!='B'||magic[2]!='3'||magic[3]!='D')throw new Exception("Invalid 3D header");
        int version=in.readInt(),count=in.readInt();
        if(version!=1||count<1||count>500)throw new Exception("Unsupported model version");
        for(int n=0;n<count;n++){
          Mesh m=new Mesh();
          m.layer=in.readInt();m.vc=in.readInt();m.ic=in.readInt();
          if(m.vc<1||m.vc>60000||m.ic<3||m.ic>1000000)throw new Exception("3D mesh count out of range");
          byte[] verts=new byte[m.vc*16],idx=new byte[m.ic*2];in.readFully(verts);in.readFully(idx);
          m.verts=ByteBuffer.allocateDirect(verts.length).order(ByteOrder.LITTLE_ENDIAN);m.verts.put(verts).position(0);
          m.idx=ByteBuffer.allocateDirect(idx.length).order(ByteOrder.LITTLE_ENDIAN);m.idx.put(idx).position(0);
          meshes.add(m);
        }
      }
    }
    @Override public void onSurfaceCreated(GL10 gl,EGLConfig cfg){
      GLES20.glClearColor(.76f,.87f,.76f,1);
      GLES20.glEnable(GLES20.GL_DEPTH_TEST);GLES20.glDepthFunc(GLES20.GL_LESS);
      GLES20.glDisable(GLES20.GL_CULL_FACE);
      int v=shader(GLES20.GL_VERTEX_SHADER,"uniform mat4 uM;attribute vec3 aP;attribute vec4 aC;varying vec4 c;void main(){gl_Position=uM*vec4(aP,1.0);c=aC;}");
      int f=shader(GLES20.GL_FRAGMENT_SHADER,"precision mediump float;varying vec4 c;void main(){gl_FragColor=c;}");
      program=GLES20.glCreateProgram();GLES20.glAttachShader(program,v);GLES20.glAttachShader(program,f);GLES20.glLinkProgram(program);
      GLES20.glDeleteShader(v);GLES20.glDeleteShader(f);
      uMVP=GLES20.glGetUniformLocation(program,"uM");aPos=GLES20.glGetAttribLocation(program,"aP");aColor=GLES20.glGetAttribLocation(program,"aC");
      try{readModel();}catch(Exception e){Log.e("GBM3D","Original offline mesh missing",e);}
      for(Mesh m:meshes){
        int[] ids=new int[2];GLES20.glGenBuffers(2,ids,0);m.vbo=ids[0];m.ibo=ids[1];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,m.vbo);GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,m.verts.capacity(),m.verts,GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,m.ibo);GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER,m.idx.capacity(),m.idx,GLES20.GL_STATIC_DRAW);
        m.verts=null;m.idx=null;
      }
    }
    @Override public void onSurfaceChanged(GL10 gl,int w,int h){width=Math.max(1,w);height=Math.max(1,h);GLES20.glViewport(0,0,width,height);}
    @Override public void onDrawFrame(GL10 gl){
      GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
      if(meshes.isEmpty())return;
      float d=dist,near=Math.max(.18f,Math.min(90f,d*.038f)),far=Math.max(1900f,d+1600f);
      Matrix.perspectiveM(projection,0,48f,(float)width/height,near,far);
      float cp=(float)Math.cos(pitch),sp=(float)Math.sin(pitch),cy=(float)Math.cos(yaw),sy=(float)Math.sin(yaw);
      Matrix.setLookAtM(view,0,tx+d*cp*cy,ty+d*cp*sy,tz+d*sp,tx,ty,tz,0,0,1);
      Matrix.multiplyMM(mvp,0,projection,0,view,0);
      GLES20.glUseProgram(program);GLES20.glUniformMatrix4fv(uMVP,1,false,mvp,0);
      GLES20.glEnableVertexAttribArray(aPos);GLES20.glEnableVertexAttribArray(aColor);
      for(Mesh m:meshes){
        if(interior&&m.layer!=1)continue;
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,m.vbo);
        GLES20.glVertexAttribPointer(aPos,3,GLES20.GL_FLOAT,false,16,0);
        GLES20.glVertexAttribPointer(aColor,4,GLES20.GL_UNSIGNED_BYTE,true,16,12);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,m.ibo);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,m.ic,GLES20.GL_UNSIGNED_SHORT,0);
      }
      GLES20.glDisableVertexAttribArray(aPos);GLES20.glDisableVertexAttribArray(aColor);
    }
    void zoom(float factor){if(factor>0)dist=clamp(dist/factor,7f,1800f);}
    void cameraMode(String s){
      if(s.equals("Top")){pitch=1.48f;dist=interior?80:1050;}
      if(s.equals("Walk")){pitch=.22f;dist=interior?30:520;}
      if(s.equals("Drone")){pitch=.85f;dist=interior?120:1160;}
    }
    void focus(String key){
      interior=false;
      String[] ids={"BLOCK_A","BLOCK_B","BLOCK_C","BLOCK_D","BLOCK_E","BLOCK_F","NU","RE","FA","WH","AD","ST","SO","MA","WT"};
      float[][] points={{110,417},{330,417},{548,417},{110,230},{330,230},{548,230},{708,430},{703,307},{500,80},{308,80},{72,83},{207,77},{727,214},{670,80},{744,50}};
      for(int i=0;i<ids.length;i++)if(ids[i].equals(key)){tx=points[i][0];ty=points[i][1];dist=210;pitch=.92f;return;}
      tx=400;ty=250;dist=995;
    }
  }
}