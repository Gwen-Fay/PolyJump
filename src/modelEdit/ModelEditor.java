package modelEdit;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import render.Loader;
import main.Camera;
import main.Light;
import render.Renderer;

/**
 * Model Editor display
 */
public class ModelEditor implements Runnable {
	
	private int vaoID;
	private int rID,cID,nID,gID;
	private FloatBuffer rBuf, cBuf,nBuf,gBuf;

	private final int MAX_POINTS = 10000000;

	private float shineDamper = 10;
	private float reflectivity = 1;

	private boolean exit = false;

	public Vector4f updateBgColor = new Vector4f(1,0,1,1);
	public Vector3f updateLColor = new Vector3f(1,1,1);
	private Vector4f backgroundColor = new Vector4f(1,0,1,1);

	private Vert vertCursor1 = new Vert();
	private Vert vertCursor2 = new Vert();
	private Vert vertCursor3 = new Vert();
	private int[] cursor1 = new int[]{0,0,0};
	private int[] cursor2 = new int[]{0,0,0};
	private int[] cursor3 = new int[]{0,0,0};
	private int[] brushSize = new int[]{1,1,1};

	public List<Vert> brush = new ArrayList<>();
	private List<Vert> clipboard = new ArrayList<>();
	public List<Vert> selected = new ArrayList<>();

	private static long lastFrameTime =Sys.getTime()*1000/Sys.getTimerResolution();
	private static float delta;

	private ModelRenderer renderer;
	private Loader loader;

	public float 	glow = 0.0f,
							spec = 0.0f,
							colorVariance = 0.0f,
							normalVariance = 0.0f;
	private int selectChance = 100;

	Light light;
	Camera camera;

	public Frame currentFrame;
	public List<Frame> frames = new ArrayList<Frame>();
    String fileName;
	public List<FrameAnimation> animations = new ArrayList<>();
	public FrameAnimation currentAnim = null;

	Canvas canvas;

	public int action = 0;
	public final int BRUSH_BIT =1,
							MOVE_BIT = 2,
							DESLECT_BIT = 4,
							NORMAL_BIT = 8,
							REVOLVE_BIT = 16,
							COLOR_BIT = 32,
							GLOW_BIT = 64,
							SAVE_BIT = 128,
							READ_BIT = 256,
							PLAY_BIT = 512;

	public final int NORMAL_SELECT = 0,
							ADD_SELECT = 1,
							SUB_SELECT = 2,
							INT_SELECT = 3;
	public int selectMode = NORMAL_SELECT;

	public final int BOX = 0,
							DIAMOND = 1,
							SPHERE = 2,
							TRIANGLE = 3;
	public int brushMode = BOX;

	public final int TWARD = 0,
							AWAY =1,
							LINE = 2;
	public int normalMode = AWAY;

	public final int X=0,
							Y=1,
							Z=2;
	public int revolveAxis = X;

	public boolean isSelect = false;
	public boolean isPaintBrush = true;
	public boolean isBrush = true;
	public boolean isRevolve = false;
	public boolean displayBrush = true;

	public boolean loading = false;

	private float animTime = 0;
	public int animFrame = 0;

	public boolean clearBrush = false;

	public ModelEditor(Canvas canvas){

		this.canvas = canvas;
	}

	public void run() {
		/* Setup */
        {
            Frame f = new Frame();
			f.name = "newFrame";
            frames.add(f);
            currentFrame = f;
        }

		loader = new Loader();
		renderer = new ModelRenderer(camera,loader, canvas);

		//setup cursors
		vertCursor1.position = new Vector3f(cursor1[0]/32f,cursor1[1]/32f,cursor1[2]/32f);
		vertCursor1.color = new Vector4f(0,0,1,0.5f);
		//cursors.add(currentSelected);

		vertCursor2.position = new Vector3f(vertCursor1.position);

		vertCursor3.position = new Vector3f(vertCursor1.position);
		//cursors.add(previousSelected);


		setUpBuffers();


		//shader =

		camera = new Camera();
		camera.focus = new Vector3f(0,0,0);
		light = new Light();
		light.color = new Vector3f(1,1,1);

		light.position.z=-10;

		updateVbos();

		while(!Display.isCloseRequested() && !exit){

			calcDeltaTime();

			// Render
			renderer.renderModelEdit(camera,light,vaoID,shineDamper,reflectivity,currentFrame.verts.size(),
					vertCursor1, vertCursor2, vertCursor3, selected, brush, backgroundColor);

			// game logic
			camera.thirdPersonMove();
			doAction();
			moveCursor(camera);

		}

		/* close game */
		GL30.glDeleteVertexArrays(vaoID);
		GL15.glDeleteBuffers(rID);
		GL15.glDeleteBuffers(cID);
		GL15.glDeleteBuffers(nID);
		GL15.glDeleteBuffers(gID);

		renderer.cleanUp();
		Display.destroy();

	}


	/**
	 * handles input from ModelJFrame
     */
	private void doAction(){


		if((action & PLAY_BIT)== PLAY_BIT) {
			animTime += delta;
			if(animTime > currentAnim.lengths.get(animFrame)){
				animTime = 0;
				if (animFrame >= currentAnim.lengths.size() - 1){
					animFrame = 0;
				}
				else{
					animFrame++;
				}
				currentFrame = currentAnim.frames.get(animFrame);
				updateVbos();
			}

		}else {
			if ((action & MOVE_BIT) == MOVE_BIT) {
				moveBrush();
				action &= ~MOVE_BIT;
			}
			if ((action & BRUSH_BIT) == BRUSH_BIT) {
				if (displayBrush) {
					if (isPaintBrush) {
						addBrush();
					}
					if (isSelect) {
						selectBrush();
					}
					brush.clear();
				}
				action &= ~BRUSH_BIT;
			}
			if ((action & DESLECT_BIT) == DESLECT_BIT) {
				deselectChance();
				action &= ~DESLECT_BIT;
			}
			if ((action & NORMAL_BIT) == NORMAL_BIT) {
				changeNormals();
				action &= ~NORMAL_BIT;
			}
			if ((action & REVOLVE_BIT) == REVOLVE_BIT) {
				brush.clear();
				revolve();
				action &= ~REVOLVE_BIT;
			}
			if ((action & COLOR_BIT) == COLOR_BIT) {
				colorSelected();
				action &= ~COLOR_BIT;
			}
			if ((action & GLOW_BIT) == GLOW_BIT) {
				changeGlow();
				action &= ~GLOW_BIT;
			}
			if ((action & SAVE_BIT) == SAVE_BIT) {

				write();
				action &= ~SAVE_BIT;
			}
			if ((action & READ_BIT) == READ_BIT) {
				read();
				action &= ~READ_BIT;
				loading = false;
			}
		}
	}

	/**
	 * updates brush, so if cursor moved, the brush will move on screen
     */
	 private void moveBrush(){

		 backgroundColor = new Vector4f(updateBgColor);
		 light.color = new Vector3f(updateLColor);
		 System.out.println(backgroundColor);
		 if(clearBrush){
			 brush.clear();
			 clearBrush = false;
		 }
		if(isBrush){
			switch(brushMode){
			case BOX:
				brushVertsCube();
				break;
			case DIAMOND:
				brushVertsDiamond();
				break;

			case SPHERE:
				brushVertsSphere();
				break;

			case TRIANGLE:
				brushVertsTriangle();
				break;
			}
		}
		else{
			updateVbos();
		}


		if(!displayBrush){
			brush.clear();
		}
	}

	public void setPos1(int x, int y, int z){
		cursor1[0] = x;
		cursor1[1] = y;
		cursor1[2] = z;
		vertCursor1.position = new Vector3f(cursor1[0]/32f,cursor1[1]/32f,cursor1[2]/32f);
	}
	public void setPos2(int x, int y, int z){
		cursor2[0] = x;
		cursor2[1] = y;
		cursor2[2] = z;
		vertCursor2.position = new Vector3f(cursor2[0]/32f,cursor2[1]/32f,cursor2[2]/32f);
	}
	public void setPos3(int x, int y, int z){
		cursor3[0] = x;
		cursor3[1] = y;
		cursor3[2] = z;
		vertCursor3.position = new Vector3f(cursor3[0]/32f,cursor3[1]/32f,cursor3[2]/32f);
	}
	public void setBrushSize(int x, int y, int z){
		brushSize[0] = x;
		brushSize[1] = y;
		brushSize[2] = z;
	}

	public void setColor(float r, float g, float b, float a){
		Vert.sellectedColor = new Vector4f(r,g,b,a);
	}
	public void setChance(int chance){
		selectChance = chance;
	}
	public void setColorVariance(float v){
		colorVariance = v;
	}
	public void setSpec(float glow, float spec){
		this.glow = glow;
		this.spec = spec;
	}

	/**
	 * handles moving a cursor in a dirrection, based on angle camera is facing
	 * @param x
	 * @param z
	 * @param camera
     * @param vec
     */
	private void moveDir(int x, int z, Camera camera, int[] vec){
		if(((camera.rotation.y%360)+360)%360<45 ||((camera.rotation.y%360)+360)%360>=315){
			vec[0]+=x;
			vec[2]-=z;
		}
		if(((camera.rotation.y%360)+360)%360>=45 &&((camera.rotation.y%360)+360)%360<135){
			vec[0]+=z;
			vec[2]+=x;
		}
		if(((camera.rotation.y%360)+360)%360>=135 &&((camera.rotation.y%360)+360)%360<225){
			vec[0]-=x;
			vec[2]+=z;
		}
		if(((camera.rotation.y%360)+360)%360>=225 &&((camera.rotation.y%360)+360)%360<315){
			vec[0]-=z;
			vec[2]-=x;
		}
	}

	/**
	 * handles moving cursor, and other keyboard inputs
	 * @param camera
     */
	private void moveCursor(Camera camera){

		int move = 1;
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)){
			move = 8;
		}

		while (Keyboard.next()) {

			if (Keyboard.getEventKeyState()) {

				if (Keyboard.getEventKey() == Keyboard.KEY_W) {
					moveDir(0, move, camera, cursor1);
					moveBrush();
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_S) {
					moveDir(0, -move, camera, cursor1);
					moveBrush();
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_A) {
					moveDir(-move, 0, camera, cursor1);
					moveBrush();
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_D) {
					moveDir(move, 0, camera, cursor1);
					moveBrush();
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_Q) {
					cursor1[1] -= move;
					moveBrush();
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_E) {
					cursor1[1] += move;
					moveBrush();
				}

				if (Keyboard.getEventKey() == Keyboard.KEY_UP) {
					moveDir(0, move, camera, cursor2);
					moveBrush();
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_DOWN) {
					moveDir(0, -move, camera, cursor2);
					moveBrush();
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_LEFT) {
					moveDir(-move, 0, camera, cursor2);
					moveBrush();
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_RIGHT) {
					moveDir(move, 0, camera, cursor2);
					moveBrush();
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_ADD) {
					cursor2[1] -= move;
					moveBrush();
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_SUBTRACT) {
					cursor2[1] += move;
					moveBrush();
				}

				if(Keyboard.getEventKey() == Keyboard.KEY_DELETE){
					deleteSelected();
				}


				if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
					if (vertCursor2.position.equals(vertCursor1.position)) {
						vertCursor3.position = new Vector3f(vertCursor1.position);
						cursor3[0] = cursor1[0];
						cursor3[1] = cursor1[1];
						cursor3[2] = cursor1[2];
					}

					if (isRevolve) {
						brush.clear();
						revolve();
					}

					vertCursor2.position = new Vector3f(vertCursor1.position);
					cursor2[0] = cursor1[0];
					cursor2[1] = cursor1[1];
					cursor2[2] = cursor1[2];
					moveBrush();

				}


				if (Keyboard.getEventKey() == Keyboard.KEY_RETURN || Keyboard.getEventKey() == Keyboard.KEY_NUMPADENTER) {


					if (isRevolve) {
						if (isBrush) {
							addBrush();
						} else {
							selectBrush();
						}

					} else {
						selectVerts();
					}
					moveBrush();
				}

				vertCursor1.position = new Vector3f(cursor1[0]/32f,cursor1[1]/32f,cursor1[2]/32f);
				vertCursor2.position = new Vector3f(cursor2[0]/32f,cursor2[1]/32f,cursor2[2]/32f);
				vertCursor3.position = new Vector3f(cursor3[0]/32f,cursor3[1]/32f,cursor3[2]/32f);

				if(Keyboard.getEventKey() == Keyboard.KEY_L) {
					light.position = new Vector3f(camera.position);
				}
			}
		}
	}

	/**
	 * revolve tool.
     */
	private void revolve(){
		if(revolveAxis ==X){
			for(Vert vert:selected){
				int centerY = (int)cursor1[1];
				int centerZ = (int)cursor1[2];
				int xOffset = (int)(vert.position.x*32)-cursor1[0];

				int R = (int)(Math.sqrt(Math.pow(centerY-(int)(vert.position.y*32),2)+Math.pow(centerZ-(int)(vert.position.z*32),2)));

				int y = 0;
				int z = R;

				int d = (5-(R * 4))/4;

				Vector3f axis = new Vector3f(1,0,0);

				float zOffset =cursor1[2] - vert.position.z;
				float yOffset =  cursor1[1] - vert.position.y ;
				float extraOffset= (float)Math.atan(yOffset/zOffset);
				if(zOffset<0){
					extraOffset +=(float)Math.PI;
				}

				do{
					//Offsets need to be exactly what they are
					float angleQ1 = (float)Math.atan(z/(float)y);
					revolveBrushPoint(cursor1[0]+xOffset,centerY+y,centerZ+z,vert,angleQ1+(float)Math.PI/2f + extraOffset,axis);
					revolveBrushPoint(cursor1[0]+xOffset,centerY-y,centerZ+z,vert,-angleQ1 -(float)Math.PI/2f + extraOffset,axis);
					revolveBrushPoint(cursor1[0]+xOffset,centerY+y,centerZ-z,vert,-angleQ1+(float)Math.PI/2f + extraOffset,axis);
					revolveBrushPoint(cursor1[0]+xOffset,centerY-y,centerZ-z,vert,angleQ1 -(float)Math.PI/2f +extraOffset,axis);

					revolveBrushPoint(cursor1[0]+xOffset,centerY+z,centerZ+y,vert,-angleQ1 +(float)Math.PI+ extraOffset,axis);
					revolveBrushPoint(cursor1[0]+xOffset,centerY-z,centerZ+y,vert,angleQ1 +(float)Math.PI +extraOffset,axis);
					revolveBrushPoint(cursor1[0]+xOffset,centerY+z,centerZ-y,vert,angleQ1+extraOffset,axis);
					revolveBrushPoint(cursor1[0]+xOffset,centerY-z,centerZ-y,vert,-angleQ1+extraOffset,axis);

					if(d<0){
						d += 2*y+1;
					}else{
						d += 2 * (y-z) +1;
						z--;

						angleQ1 = (float)Math.atan(z/(float)y);
						revolveBrushPoint(cursor1[0]+xOffset,centerY+y,centerZ+z,vert,angleQ1+(float)Math.PI/2f+extraOffset,axis);
						revolveBrushPoint(cursor1[0]+xOffset,centerY-y,centerZ+z,vert,-angleQ1 -(float)Math.PI/2f+extraOffset,axis);
						revolveBrushPoint(cursor1[0]+xOffset,centerY+y,centerZ-z,vert,-angleQ1+(float)Math.PI/2f+extraOffset,axis);
						revolveBrushPoint(cursor1[0]+xOffset,centerY-y,centerZ-z,vert,angleQ1 -(float)Math.PI/2f+extraOffset,axis);

						revolveBrushPoint(cursor1[0]+xOffset,centerY+z,centerZ+y,vert,-angleQ1 +(float)Math.PI+extraOffset,axis);
						revolveBrushPoint(cursor1[0]+xOffset,centerY-z,centerZ+y,vert,angleQ1 +(float)Math.PI+extraOffset,axis);
						revolveBrushPoint(cursor1[0]+xOffset,centerY+z,centerZ-y,vert,angleQ1+extraOffset,axis);
						revolveBrushPoint(cursor1[0]+xOffset,centerY-z,centerZ-y,vert,-angleQ1+extraOffset,axis);
					}
					y++;

				}while(y<=z);
			}
		}else if(revolveAxis==Y){
			for(Vert vert:selected){
				int centerX = (int)cursor1[0];
				int centerZ = (int)cursor1[2];
				int yOffset = (int)(vert.position.y*32)-cursor1[1];

				int R = (int)(Math.sqrt(Math.pow(centerX-(int)(vert.position.x*32),2)+Math.pow(centerZ-(int)(vert.position.z*32),2)));

				int x = 0;
				int z = R;

				int d = (5-(R * 4))/4;

				Vector3f axis = new Vector3f(0,1,0);

				float zOffset =cursor1[2] - vert.position.z;
				float xOffset =  cursor1[0] - vert.position.x ;
				float extraOffset= (float)Math.atan(zOffset/xOffset);
				if(xOffset>=0){
					extraOffset +=(float)Math.PI;
				}

				do{
					float angleQ1 = (float)Math.atan(x/(float)z);
					revolveBrushPoint(centerX+x,cursor1[1]+yOffset,centerZ+z,vert,angleQ1-(float)Math.PI/2f + extraOffset,axis);
					revolveBrushPoint(centerX-x,cursor1[1]+yOffset,centerZ+z,vert,-angleQ1-(float)Math.PI/2f + extraOffset,axis);
					revolveBrushPoint(centerX+x,cursor1[1]+yOffset,centerZ-z,vert,-angleQ1+(float)Math.PI/2f + extraOffset,axis);
					revolveBrushPoint(centerX-x,cursor1[1]+yOffset,centerZ-z,vert,angleQ1+(float)Math.PI/2f +extraOffset ,axis);

					revolveBrushPoint(centerX+z,cursor1[1]+yOffset,centerZ+x,vert,-angleQ1 + extraOffset ,axis);
					revolveBrushPoint(centerX-z,cursor1[1]+yOffset,centerZ+x,vert,angleQ1+(float)Math.PI + extraOffset,axis);
					revolveBrushPoint(centerX+z,cursor1[1]+yOffset,centerZ-x,vert,angleQ1 + extraOffset,axis);
					revolveBrushPoint(centerX-z,cursor1[1]+yOffset,centerZ-x,vert,-angleQ1+(float)Math.PI + extraOffset,axis);

					if(d<0){
						d += 2*x+1;
					}else{
						d += 2 * (x-z) +1;
						z--;

						angleQ1 = (float)Math.atan(x/(float)z);
						revolveBrushPoint(centerX+x,cursor1[1]+yOffset,centerZ+z,vert,angleQ1-(float)Math.PI/2f + extraOffset,axis);
						revolveBrushPoint(centerX-x,cursor1[1]+yOffset,centerZ+z,vert,-angleQ1-(float)Math.PI/2f + extraOffset,axis);
						revolveBrushPoint(centerX+x,cursor1[1]+yOffset,centerZ-z,vert,-angleQ1+(float)Math.PI/2f + extraOffset,axis);
						revolveBrushPoint(centerX-x,cursor1[1]+yOffset,centerZ-z,vert,angleQ1+(float)Math.PI/2f + extraOffset,axis);

						revolveBrushPoint(centerX+z,cursor1[1]+yOffset,centerZ+x,vert,-angleQ1 + extraOffset,axis);
						revolveBrushPoint(centerX-z,cursor1[1]+yOffset,centerZ+x,vert,angleQ1+(float)Math.PI + extraOffset,axis);
						revolveBrushPoint(centerX+z,cursor1[1]+yOffset,centerZ-x,vert,angleQ1 + extraOffset,axis);
						revolveBrushPoint(centerX-z,cursor1[1]+yOffset,centerZ-x,vert,-angleQ1+(float)Math.PI + extraOffset,axis);
					}
					x++;

				}while(x<=z);
			}
		}else if(revolveAxis ==Z){
			for(Vert vert:selected){
				int centerX = (int)cursor1[0];
				int centerY = (int)cursor1[1];
				int zOffset = (int)(vert.position.z*32)-cursor1[2];

				int R = (int)(Math.sqrt(Math.pow(centerY-(int)(vert.position.y*32),2)+Math.pow(centerX-(int)(vert.position.x*32),2)));

				int x = 0;
				int y = R;


				int d = (5-(R * 4))/4;

				Vector3f axis = new Vector3f(0,0,1);
				float xOffset =cursor1[0] - vert.position.x;
				float yOffset =  cursor1[1] - vert.position.y ;
				float extraOffset= (float)Math.atan(yOffset/xOffset);
				if(xOffset>0){
					extraOffset +=(float)Math.PI;
				}


				do{
					float angleQ1 = (float)Math.atan(x/(float)y);
					revolveBrushPoint(centerX+x,centerY+y,cursor1[2]+zOffset,vert,-angleQ1+(float)Math.PI/2f+extraOffset,axis);
					revolveBrushPoint(centerX+x,centerY-y,cursor1[2]+zOffset,vert,+angleQ1-(float)Math.PI/2f+extraOffset,axis);
					revolveBrushPoint(centerX-x,centerY+y,cursor1[2]+zOffset,vert,+angleQ1+(float)Math.PI/2f+extraOffset,axis);
					revolveBrushPoint(centerX-x,centerY-y,cursor1[2]+zOffset,vert,-angleQ1-(float)Math.PI/2f+extraOffset,axis);

					revolveBrushPoint(centerX+y,centerY+x,cursor1[2]+zOffset,vert,angleQ1+extraOffset,axis);
					revolveBrushPoint(centerX+y,centerY-x,cursor1[2]+zOffset,vert,-angleQ1+extraOffset,axis);
					revolveBrushPoint(centerX-y,centerY+x,cursor1[2]+zOffset,vert,-angleQ1+(float)Math.PI+extraOffset,axis);
					revolveBrushPoint(centerX-y,centerY-x,cursor1[2]+zOffset,vert,angleQ1+(float)Math.PI+extraOffset,axis);

					if(d<0){
						d += 2*x+1;
					}else{
						d += 2 * (x-y) +1;
						y--;

						angleQ1 = (float)Math.atan(x/(float)y);
						revolveBrushPoint(centerX+x,centerY+y,cursor1[2]+zOffset,vert,-angleQ1+(float)Math.PI/2f+extraOffset,axis);
						revolveBrushPoint(centerX+x,centerY-y,cursor1[2]+zOffset,vert,+angleQ1-(float)Math.PI/2f+extraOffset,axis);
						revolveBrushPoint(centerX-x,centerY+y,cursor1[2]+zOffset,vert,+angleQ1+(float)Math.PI/2f+extraOffset,axis);
						revolveBrushPoint(centerX-x,centerY-y,cursor1[2]+zOffset,vert,-angleQ1-(float)Math.PI/2f+extraOffset,axis);

						revolveBrushPoint(centerX+y,centerY+x,cursor1[2]+zOffset,vert,angleQ1+extraOffset,axis);
						revolveBrushPoint(centerX+y,centerY-x,cursor1[2]+zOffset,vert,-angleQ1+extraOffset,axis);
						revolveBrushPoint(centerX-y,centerY+x,cursor1[2]+zOffset,vert,-angleQ1+(float)Math.PI+extraOffset,axis);
						revolveBrushPoint(centerX-y,centerY-x,cursor1[2]+zOffset,vert,angleQ1+(float)Math.PI+extraOffset,axis);
					}
					x++;

				}while(x<=y);
			}
		}
	}

	/**
	 * adds the x,y,z point to brush
	 * @param x
	 * @param y
	 * @param z
	 * @param vert
	 * @param angle
	 * @param axis
	 */
	public void revolveBrushPoint(int x, int y, int z, Vert vert, float angle, Vector3f axis){
		Vert brushVert = new Vert();
		brushVert.specular = spec;
		brushVert.glow = glow;
		brushVert.position = new Vector3f(x/32f,y/32f,z/32f);
		brushVert.color = vert.color;;
		Vector4f normal = new Vector4f(vert.normal.x,vert.normal.y,vert.normal.z,0);

		Matrix4f rotate = new Matrix4f();
		rotate.setIdentity();
		rotate.rotate(angle,axis);
		Matrix4f.transform(rotate,normal,normal);

		brushVert.normal = new Vector3f(normal.x, normal.y, normal.z);

		brush.add(brushVert);
	}
	/**
	 * shows chosen axis in "display" so that you can visualize the brush's axis
	 */


	/**
	 * puts selected in the clipboard
	 */
	private void copySelected(){
		clipboard.clear();
		for(Vert vert:selected){
			Vert newVert =new Vert(vert);
			Vector3f.sub(newVert.position, vertCursor1.position, newVert.position);
			clipboard.add(newVert);
		}
	}


	/**
	 * tool for changing the normal vector of selected verts
	 */
	private void changeNormals(){
		Random r = new Random();
		if(normalMode ==AWAY){
			for(Vert vert:selected){
				Vector3f.sub(vert.position, vertCursor1.position, vert.normal);
				float x = (float)r.nextGaussian()*normalVariance;
				float y = (float)r.nextGaussian()*normalVariance;
				float z = (float)r.nextGaussian()*normalVariance;
				Vector3f.add(new Vector3f(x,y,z), vert.normal, vert.normal);
				if (vert.normal.length()!=0){
					vert.normal.normalise();}

			}

		}else if(normalMode == TWARD){
			for(Vert vert:selected){
				Vector3f.sub(vertCursor1.position,vert.position, vert.normal);
				float x = (float)r.nextGaussian()*normalVariance;
				float y = (float)r.nextGaussian()*normalVariance;
				float z = (float)r.nextGaussian()*normalVariance;
				Vector3f.add(new Vector3f(x,y,z), vert.normal, vert.normal);
				if (vert.normal.length()!=0){
					vert.normal.normalise();}

			}

		}else if(normalMode ==LINE){
			for(Vert vert:selected){
				Vector3f.sub(vertCursor1.position,vertCursor2.position, vert.normal);
				float x = (float)r.nextGaussian()*normalVariance;
				float y = (float)r.nextGaussian()*normalVariance;
				float z = (float)r.nextGaussian()*normalVariance;
				Vector3f.add(new Vector3f(x,y,z), vert.normal, vert.normal);
				if (vert.normal.length()!=0){
					vert.normal.normalise();}

			}

		}
		updateVbos();
	}

	private void changeGlow(){
		for(Vert vert:selected){
			vert.specular = spec;
			vert.glow = glow;
		}
		updateVbos();
	}

	/**
	 * tool for changing color of selected verts
	 */
	private void colorSelected(){
		Random r = new Random();
		for(Vert vert:selected){
			Vector4f color = new Vector4f(Vert.sellectedColor);
			Vector4f offset = new Vector4f((float) r.nextGaussian(), (float) r.nextGaussian(), (float) r.nextGaussian(),0);
			offset.scale(colorVariance);
			Vector4f.add(offset,color,color);
			vert.color = color;
		}
		updateVbos();
	}

	public void changeSelectedColor(float r, float g, float b){
		Vert.sellectedColor = new Vector4f(r,g,b,1);
	}

	/**
	 * tool for randomly removing verts from selected.
	 */
	private void deselectChance(){
		List<Vert> newSelected = new ArrayList<Vert>();
		Random r = new Random();
		for (Vert vert:selected){
			if(r.nextFloat()<selectChance/100f){
				newSelected.add(vert);
			}
		}
		selected.clear();
		selected = newSelected;
	}

	/**
	 * box select
	 */
	private void selectVerts(){

		if(selectMode==NORMAL_SELECT){
			selected.clear();
		}

		List<Vert> intersection = new ArrayList<Vert>();

		int[] min = {Math.min(cursor1[0],cursor2[0]),Math.min(cursor1[1],cursor2[1]),Math.min(cursor1[2],cursor2[2])};
		for(int i=0;i<Math.abs(cursor1[0]-cursor2[0]);i++){
			for(int j=0;j<Math.abs(cursor1[1]-cursor2[1]);j++){
				for(int k=0;k<Math.abs(cursor1[2]-cursor2[2]);k++){

					Vert vert = currentFrame.octVerts.get(new int[]{(min[0]+i),(min[1]+j),(min[2]+k)});
					if(vert != null){
						if(!selected.contains(vert)){
							if(selectMode != SUB_SELECT){
								selected.add(vert);
							}
						}else{
							if(selectMode == SUB_SELECT){
								selected.remove(vert);
							}
							if(selectMode == INT_SELECT){
								intersection.add(vert);
							}
						}
					}
				}
			}
		}

		if(selectMode == INT_SELECT){
			selected.clear();
			selected = intersection;
		}

		updateVbos();
	}

	private void brushVertsCube(){

		brush.clear();

		for(int i = 0;i<brushSize[0];i++){
			for(int j = 0;j<brushSize[1];j++){
				for(int k = 0;k<brushSize[2];k++){
					Vector3f pos = new Vector3f((cursor1[0]-brushSize[0]/2+i)/32f,(cursor1[1]-brushSize[1]/2+j)/32f
							,(cursor1[2]-brushSize[2]/2+k)/32f);

					Vert vert = new Vert();
					vert.specular = spec;
					vert.glow = glow;
					vert.position= pos;
					vert.color = new Vector4f(Vert.sellectedColor);


					//code for choosing the correct normal for a vert
					Vector3f normal = new Vector3f(0,0,0);
					if(i==0){
						if(j==0){
							if(k==0){
								normal = new Vector3f(-1,-1,-1);
								normal.normalise();
							}else if(k == brushSize[2] -1){
								normal = new Vector3f(-1,-1,1);
								normal.normalise();
							}
							else{
								normal = new Vector3f(-1,-1,0);
								normal.normalise();
							}
						}else if(j == brushSize[1] - 1){
							if(k==0){
								normal = new Vector3f(-1,1,-1);
								normal.normalise();
							}else if(k == brushSize[2] -1){
								normal = new Vector3f(-1,1,1);
								normal.normalise();
							}
							else{
								normal = new Vector3f(-1,1,0);
								normal.normalise();
							}
						}else{
							if(k==0){
								normal = new Vector3f(-1,0,-1);
								normal.normalise();
							}else if(k == brushSize[2] -1){
								normal = new Vector3f(-1,0,1);
								normal.normalise();
							}else{
								normal = new Vector3f(-1,0,0);
								normal.normalise();
							}
						}

					}else if(i== brushSize[0] - 1){
						if(j==0){
							if(k==0){
								normal = new Vector3f(1,-1,-1);
								normal.normalise();
							}else if(k == brushSize[2] -1){
								normal = new Vector3f(1,-1,1);
								normal.normalise();
							}
							else{
								normal = new Vector3f(1,-1,0);
								normal.normalise();
							}
						}else if(j == brushSize[1] - 1){
							if(k==0){
								normal = new Vector3f(1,1,-1);
								normal.normalise();
							}else if(k == brushSize[2] -1){
								normal = new Vector3f(1,1,1);
								normal.normalise();
							}
							else{
								normal = new Vector3f(1,1,0);
								normal.normalise();
							}
						}else{
							if(k==0){
								normal = new Vector3f(1,0,-1);
								normal.normalise();
							}else if(k == brushSize[2] -1){
								normal = new Vector3f(1,0,1);
								normal.normalise();
							}
							else{
								normal = new Vector3f(1,0,0);
								normal.normalise();
							}
						}
					}else{
						if(j==0){
							if(k==0){
								normal = new Vector3f(0,-1,-1);
								normal.normalise();
							}else if(k == brushSize[2] -1){
								normal = new Vector3f(0,-1,1);
								normal.normalise();
							}else{
								normal = new Vector3f(0,-1,0);
								normal.normalise();
							}

						}else if(j == brushSize[1] - 1){
							if(k==0){
								normal = new Vector3f(0,1,-1);
								normal.normalise();
							}else if(k == brushSize[2] -1){
								normal = new Vector3f(0,1,1);
								normal.normalise();
							}else{
								normal = new Vector3f(0,1,0);
								normal.normalise();
							}
						}else{
							if(k==0){
								normal = new Vector3f(0,0,-1);
								normal.normalise();
							}else if(k == brushSize[2] -1){
								normal = new Vector3f(0,0,1);
								normal.normalise();
							}
						}
					}


					vert.normal = normal;
					if (vert.normal.length()!=0){
						vert.normal.normalise();
					}
					brush.add(vert);
				}
			}

		}

		updateVbos();
	}

	private void brushVertsDiamond(){
		brush.clear();
		for(int i = 0;i<brushSize[0];i++){
			for(int j = 0;j<brushSize[1];j++){
				for(int k = 0;k<brushSize[2];k++){
					double diamondDistance = Math.abs(i-brushSize[0]/2)/(float)brushSize[0] + Math.abs(j-brushSize[1]/2)/(float)brushSize[1] + Math.abs(k-brushSize[2]/2)/(float)brushSize[2];
					if(diamondDistance <0.5){
						Vector3f pos = new Vector3f((cursor1[0]-brushSize[0]/2+i)/32f,(cursor1[1]-brushSize[1]/2+j)/32f
								,(cursor1[2]-brushSize[2]/2+k)/32f);

						int x = i-(int)(brushSize[0]/2f);
						int y = j-(int)(brushSize[1]/2f);
						int z = k-(int)(brushSize[2]/2f);

						Vector3f normal = new Vector3f(Math.signum(x),Math.signum(y),Math.signum(z));

						Vert vert = new Vert();
						vert.specular = spec;
						vert.glow = glow;
						vert.position= pos;
						vert.color = new Vector4f(Vert.sellectedColor);
						vert.normal = normal;
						if (vert.normal.length()!=0){
							vert.normal.normalise();}
						brush.add(vert);
					}
				}
			}
		}
		updateVbos();
	}

	private void brushVertsSphere(){
		brush.clear();
		for(int i = 0;i<brushSize[0];i++){
			for(int j = 0;j<brushSize[1];j++){
				for(int k = 0;k<brushSize[2];k++){
					double sphereDistance = Math.sqrt(Math.pow((i-brushSize[0]/2)/(float)brushSize[0] ,2)+ Math.pow((j-brushSize[1]/2)/(float)brushSize[1] ,2)
							+ Math.pow((k-brushSize[2]/2)/(float)brushSize[2],2));
					if(sphereDistance <0.5){
						Vector3f pos = new Vector3f((cursor1[0]-brushSize[0]/2+i)/32f,(cursor1[1]-brushSize[1]/2+j)/32f
								,(cursor1[2]-brushSize[2]/2+k)/32f);
						Vert vert = new Vert();
						vert.specular = spec;
						vert.glow = glow;
						vert.position= pos;
						vert.color = new Vector4f(Vert.sellectedColor);;
						vert.normal = new Vector3f(i-brushSize[0]/2,j-brushSize[1]/2,k-brushSize[2]/2);
						if (vert.normal.length()!=0){
							vert.normal.normalise();}
						brush.add(vert);
					}
				}
			}
		}
		updateVbos();
	}

	//ToDo, add clipboard back in
	private void brushVertsClipboard(){
		brush.clear();
		for(Vert vert:clipboard){
			Vert newVert = new Vert(vert);
			Vector3f pos = new Vector3f(cursor1[0]/32f,cursor1[1]/32f,cursor1[2]/32f);
			Vector3f.add(pos, newVert.position, newVert.position);
			brush.add(newVert);
		}
		updateVbos();
	}

	private void brushVertsTriangle(){

		 /* Special cases of lines*/
		 if(isSamePos(cursor1,cursor2)){

			 brush.clear();
			 brushVertsLine(cursor1,cursor3, new Vector3f(0,0,0));
			 updateVbos();

		 }else if(isSamePos(cursor2,cursor3)){

			 brush.clear();
			 brushVertsLine(cursor1,cursor3,new Vector3f(0,0,0));
			 updateVbos();

		 }else if(isSamePos(cursor1,cursor3)){

			 brush.clear();
			 brushVertsLine(cursor1,cursor2,new Vector3f(0,0,0));
			 updateVbos();

		 /* Triangle */
		 }else {
			 brushTriangle(cursor1,cursor2,cursor3);
		 }
	}


	/**
	 * Modified Bresenham's line algorithm.
	 * will put verts only touching on a "face",
	 * as opposed to an "edge" or "corner".
	 *
	 *
	 * @param cursorA
	 * @param cursorB
	 * @param normal
	 */
	private void brushVertsLine(int[] cursorA, int[] cursorB, Vector3f normal){

		int[] point = new int[3];
		point[0]= cursorA[0];
		point[1]= cursorA[1];
		point[2]= cursorA[2];

		int dx = cursorB[0] - cursorA[0];
		int dy = cursorB[1] - cursorA[1];
		int dz = cursorB[2] - cursorA[2];

		int x_inc = (dx<0)? -1 : 1;
		int y_inc = (dy<0)? -1 : 1;
		int z_inc = (dz<0)? -1 : 1;

		int l=Math.abs(dx);
		int m=Math.abs(dy);
		int n=Math.abs(dz);

		int dx2 = l<<1;
		int dy2 = m<<1;
		int dz2 = n<<1;

		int err_1, err_2;

		if ((l >= m) && (l >= n)) {
	        err_1 = dy2 - l;
	        err_2 = dz2 - l;
	        for (int i = 0; i < l; i++) {

	        	addVertToBrush(point,normal);

	            if (err_1 > 0) {
	                point[1] += y_inc;
	                err_1 -= dx2;
	                addVertToBrush(point,normal);
	            }
	            if (err_2 > 0) {
	                point[2] += z_inc;
	                err_2 -= dx2;
	                addVertToBrush(point,normal);
	            }
	            err_1 += dy2;
	            err_2 += dz2;
	            point[0] += x_inc;
	        }
	    } else if ((m >= l) && (m >= n)) {
	        err_1 = dx2 - m;
	        err_2 = dz2 - m;
	        for (int i = 0; i < m; i++) {

	        	addVertToBrush(point,normal);

	            if (err_1 > 0) {
	                point[0] += x_inc;
	                err_1 -= dy2;
	                addVertToBrush(point,normal);
	            }
	            if (err_2 > 0) {
	                point[2] += z_inc;
	                err_2 -= dy2;
	                addVertToBrush(point,normal);
	            }
	            err_1 += dx2;
	            err_2 += dz2;
	            point[1] += y_inc;
	        }
	    } else {
	        err_1 = dy2 - n;
	        err_2 = dx2 - n;
	        for (int i = 0; i < n; i++) {

	        	addVertToBrush(point,normal);

	            if (err_1 > 0) {
	                point[1] += y_inc;
	                err_1 -= dz2;
	                addVertToBrush(point,normal);
	            }
	            if (err_2 > 0) {
	                point[0] += x_inc;
	                err_2 -= dz2;
	                addVertToBrush(point,normal);
	            }
	            err_1 += dy2;
	            err_2 += dx2;
	            point[2] += z_inc;
	        }
	    }

		addVertToBrush(point,normal);



	}
	/**
	 * adds a single point to the brush
	 * @param point
	 * @param normal
	 */
	private void addVertToBrush(int[] point, Vector3f normal){

		boolean noDuplicate = true;
		Vector3f pos = new Vector3f(point[0]/32f,point[1]/32f,point[2]/32f);

		for(Vert vert:brush){
			if(vert.position.equals(pos)){noDuplicate=false;}
		}

		if(noDuplicate){
			Vert vert = new Vert();
			vert.specular = spec;
			vert.glow = glow;
	    	vert.position= pos;
			vert.color = new Vector4f(Vert.sellectedColor);
			vert.normal = normal;
			brush.add(vert);
		}
	}

	/**
	 * Uses Line alg to make a line between 2 verts, and at each triangle vert it creates in the brush,
	 * it makes another line to it from the 3rd triangle vert. Because it doesn't go "diagonaly",
	 * this does not leave gaps in the triangle brush.
	 * @param cursorA
	 * @param cursorB
	 * @param cursorC
	 */
	private void brushTriangle(int[] cursorA, int[] cursorB, int[] cursorC){
		brush.clear();


		Vector3f normal = new Vector3f();
		Vector3f left = new Vector3f(cursorA[0] - cursorB[0], cursorA[1] - cursorB[1], cursorA[2] - cursorB[2]);
		Vector3f right = new Vector3f(cursorA[0] - cursorC[0], cursorA[1] - cursorC[1], cursorA[2] - cursorC[2]);
		Vector3f.cross(left, right, normal);
		if (normal.length()!=0){
			normal.normalise();}

		int[] point = new int[3];
		point[0]= cursorA[0];
		point[1]= cursorA[1];
		point[2]= cursorA[2];

		int dx = cursorB[0] - cursorA[0];
		int dy = cursorB[1] - cursorA[1];
		int dz = cursorB[2] - cursorA[2];

		int x_inc = (dx<0)? -1 : 1;
		int y_inc = (dy<0)? -1 : 1;
		int z_inc = (dz<0)? -1 : 1;

		int l=Math.abs(dx);
		int m=Math.abs(dy);
		int n=Math.abs(dz);

		int dx2 = l<<1;
		int dy2 = m<<1;
		int dz2 = n<<1;

		int err_1, err_2;

		if ((l >= m) && (l >= n)) {
	        err_1 = dy2 - l;
	        err_2 = dz2 - l;
	        for (int i = 0; i < l; i++) {

	        	brushVertsLine(point, cursorC, normal);

	            if (err_1 > 0) {
	                point[1] += y_inc;
	                err_1 -= dx2;
	                brushVertsLine(point, cursorC, normal);
	            }
	            if (err_2 > 0) {
	                point[2] += z_inc;
	                err_2 -= dx2;
	                brushVertsLine(point, cursorC, normal);
	            }
	            err_1 += dy2;
	            err_2 += dz2;
	            point[0] += x_inc;
	        }

	    } else if ((m >= l) && (m >= n)) {
	        err_1 = dx2 - m;
	        err_2 = dz2 - m;
	        for (int i = 0; i < m; i++) {

	        	brushVertsLine(point, cursorC, normal);

	            if (err_1 > 0) {
	                point[0] += x_inc;
	                err_1 -= dy2;
	                brushVertsLine(point, cursorC, normal);
	            }
	            if (err_2 > 0) {
	                point[2] += z_inc;
	                err_2 -= dy2;
	                brushVertsLine(point, cursorC, normal);
	            }
	            err_1 += dx2;
	            err_2 += dz2;
	            point[1] += y_inc;
	        }
	    } else {
	        err_1 = dy2 - n;
	        err_2 = dx2 - n;
	        for (int i = 0; i < n; i++) {

	        	brushVertsLine(point, cursorC, normal);

	            if (err_1 > 0) {
	                point[1] += y_inc;
	                err_1 -= dz2;
	                brushVertsLine(point, cursorC, normal);
	            }
	            if (err_2 > 0) {
	                point[0] += x_inc;
	                err_2 -= dz2;
	                brushVertsLine(point, cursorC, normal);
	            }
	            err_1 += dy2;
	            err_2 += dx2;
	            point[2] += z_inc;
	        }
	    }

		brushVertsLine(point, cursorC, normal);

		updateVbos();
	}

	/**
	 * @param cursorA
	 * @param cursorB
	 * @return
	 */
	private boolean isSamePos(int[] cursorA, int[] cursorB){
		return (cursorA[0]==cursorB[0] && cursorA[1]==cursorB[1] && cursorA[2]==cursorB[2]);
	}

	/**
	 * adds brush to model
	 */
	public void addBrush(){
		Random r = new Random();

		for (Vert vert:brush){
			if(r.nextFloat()<selectChance/100f) {
				Vector4f offset = new Vector4f((float) r.nextGaussian(), (float) r.nextGaussian(), (float) r.nextGaussian(),0);
				offset.scale(colorVariance);
				Vector4f.add(offset, vert.color, vert.color);
				//Vert oldVert = modelGrid[(int)(32*vert.position.x)][(int)(32*vert.position.y)][(int)(32*vert.position.z)];
				int[] pos = new int[]{(int) (32 * vert.position.x), (int) (32 * vert.position.y), (int) (32 * vert.position.z)};
				Vert oldVert = currentFrame.octVerts.get(pos);
				if (oldVert != null) {
					currentFrame.verts.remove(oldVert);
					currentFrame.octVerts.remove(pos);
				}

				//modelGrid[(int)(32*vert.position.x)][(int)(32*vert.position.y)][(int)(32*vert.position.z)] = vert;
				currentFrame.octVerts.add(vert, pos);
				currentFrame.verts.add(vert);
				//vert.color = new Vector4f(Vert.sellectedColor);
			}

		}

		updateVbos();
	}

	/**
	 * adds brush to selected
	 */
	private void selectBrush(){
		if(selectMode == NORMAL_SELECT){
			selected.clear();
		}
		List<Vert> intersection = new ArrayList<Vert>();
		for(Vert brushVert:brush){
			//Vert vert = modelGrid[(int)(32*brushVert.position.x)][(int)(32*brushVert.position.y)][(int)(32*brushVert.position.z)];
			int[] pos = new int[]{(int)(32*brushVert.position.x),(int)(32*brushVert.position.y),(int)(32*brushVert.position.z)};
			Vert vert = currentFrame.octVerts.get(pos);
			if(vert!=null){
				if(!selected.contains(vert)){
					if(selectMode != SUB_SELECT){
						selected.add(vert);
					}
				}else{
					if(selectMode == SUB_SELECT){
						selected.remove(vert);
					}
					if(selectMode == INT_SELECT){
						intersection.add(vert);
					}
				}
			}
		}
		if(selectMode == INT_SELECT){
			selected.clear();
			selected = intersection;
		}

		updateVbos();
	}

	private void deleteSelected(){

		for(Vert vert : selected){
            currentFrame.octVerts.remove(new int[]{(int)(vert.position.x*32),(int)(vert.position.y*32),(int)(vert.position.z*32)});
            currentFrame.verts.remove(vert);
		}
		selected.clear();
		updateVbos();
	}

	/**
	 * Creates initial conditions for VBO buffers for the model editor
	 */
	private void setUpBuffers(){
		vaoID = GL30.glGenVertexArrays();
		Loader.addVao(vaoID);
		GL30.glBindVertexArray(vaoID);
		Loader.bindIndicicesBuffer(Renderer.CUBE_INDEX);
		GL30.glBindVertexArray(0);
		rID = createEmptyVbo(MAX_POINTS*3);
		cID = createEmptyVbo(MAX_POINTS*4);
		nID = createEmptyVbo(MAX_POINTS*3);
		gID = createEmptyVbo(MAX_POINTS*2);
		int cubeID = createEmptyVbo(17*3);
		loader.addVbo(cubeID);
		rBuf = BufferUtils.createFloatBuffer(MAX_POINTS*3);
		cBuf = BufferUtils.createFloatBuffer(MAX_POINTS*4);
		nBuf = BufferUtils.createFloatBuffer(MAX_POINTS*3);
		gBuf = BufferUtils.createFloatBuffer(MAX_POINTS*2);
		addInstancedAttribute(rID,0,3);
		addInstancedAttribute(cID,1,4);
		addInstancedAttribute(nID,2,3);
		addInstancedAttribute(gID,3,2);
		addCubeVbo(cubeID,4,3);

	}
	/**
	 * Creates an Empty VBO
	 * @param floatCount
	 * @return
	 */
	private int createEmptyVbo(int floatCount){
		int vbo = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, floatCount*4, GL15.GL_STREAM_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		Loader.addVbo(vbo);
		return vbo;
	}
	/**
	 * Renders Model as Instanced Points (like a particle system)
	 * @param vbo
	 * @param attribute
	 * @param dataSize
	 */
	private void addInstancedAttribute(int vbo, int attribute, int dataSize){
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL30.glBindVertexArray(vaoID);
		GL20.glVertexAttribPointer(attribute, dataSize, GL11.GL_FLOAT, false,0, 0);
		GL33.glVertexAttribDivisor(attribute, 1);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
	}

	private void addCubeVbo(int vbo, int attribute, int dataSize){
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL30.glBindVertexArray(vaoID);
		FloatBuffer buffer = BufferUtils.createFloatBuffer(ModelRenderer.CUBE.length);
		buffer.put(ModelRenderer.CUBE);
		buffer.flip();

		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(attribute, dataSize, GL11.GL_FLOAT, false,0,0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);



	}
	/**
	 * Updates values in a single VBO buffer(should only be called by updateVbos!)
	 * @param vbo
	 * @param data
	 * @param buffer
	 */
	private void updateOneVbo(int vbo, float[] data, FloatBuffer buffer){
		buffer.clear();
		buffer.put(data);
		buffer.flip();
		GL30.glBindVertexArray(vaoID);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer.capacity() * 4, GL15.GL_STREAM_DRAW);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
	}
	/**
	 * Updates values in all Vbos based on Verts list
	 */
	private void updateVbos(){
		float[] rF = new float[currentFrame.verts.size()*3];
		float[] cF = new float[currentFrame.verts.size()*4];
		float[] nF = new float[currentFrame.verts.size()*3];
		float[] gF = new float[currentFrame.verts.size()*2];
		for(int i=0;i<currentFrame.verts.size();i++){
			rF[i*3] = currentFrame.verts.get(i).position.x;
			rF[i*3+1] = currentFrame.verts.get(i).position.y;
			rF[i*3+2] = currentFrame.verts.get(i).position.z;

			cF[i*4] = currentFrame.verts.get(i).color.x;
			cF[i*4+1] = currentFrame.verts.get(i).color.y;
			cF[i*4+2] = currentFrame.verts.get(i).color.z;
			cF[i*4+3] = currentFrame.verts.get(i).color.w;

			nF[i*3] = currentFrame.verts.get(i).normal.x;
			nF[i*3+1] = currentFrame.verts.get(i).normal.y;
			nF[i*3+2] = currentFrame.verts.get(i).normal.z;

			gF[i*2] = currentFrame.verts.get(i).specular;
			gF[i*2+1] = currentFrame.verts.get(i).glow;
		}
		updateOneVbo(rID,rF,rBuf);
		updateOneVbo(cID,cF,cBuf);
		updateOneVbo(nID,nF,nBuf);
		updateOneVbo(gID,gF,gBuf);

	}

	/**
	 * quits modelEditor display.
     */
    public void quit(){
        exit = true;
    }

	/**
	 * writes to files
	 */
    private void write(){
		try {
			writeAnimDat();

			//Write each frame file
			for (Frame f : frames) {
				BufferedWriter Edit = new BufferedWriter(new FileWriter(new File("res/models/" + fileName + "/" + f.name + ".edt"), false));
				BufferedWriter Entity = new BufferedWriter(new FileWriter(new File("res/models/" + fileName + "/" + f.name + ".ent"), false));

				Edit.write("# " + "res/models/" + fileName + "/" + f.name + ".edt");
				Entity.write("# " + "res/models/" + fileName + "/" + f.name + ".ent");


				Edit.newLine();
				Entity.newLine();


				Edit.write("#Position x,y,z, Normal x,y,z, Color x,y,z,write, Specular, Glow");
				Entity.write("#Position x,y,z, Normal x,y,z, Color x,y,z,write, Specular, Glow");


				for (Vert vert : f.verts) {

					Edit.newLine();


					String vertData = vert.position.x + "," + vert.position.y + "," + vert.position.z + ","
							+ vert.normal.x + "," + vert.normal.y + "," + vert.normal.z + ","
							+ vert.color.x + "," + vert.color.y + "," + vert.color.z + "," + vert.color.w + "," + vert.specular + "," + vert.glow;

					Edit.write(vertData);

					int[] pos = new int[]{(int) (32 * vert.position.x), (int) (32 * vert.position.y), (int) (32 * vert.position.z)};

					if ((f.octVerts.get(new int[]{pos[0] + 1, pos[1], pos[2]}) == null)
							|| (f.octVerts.get(new int[]{pos[0] - 1, pos[1], pos[2]}) == null)
							|| (f.octVerts.get(new int[]{pos[0], pos[1] + 1, pos[2]}) == null)
							|| (f.octVerts.get(new int[]{pos[0], pos[1] - 1, pos[2]}) == null)
							|| (f.octVerts.get(new int[]{pos[0], pos[1], pos[2] + 1}) == null)
							|| (f.octVerts.get(new int[]{pos[0], pos[1], pos[2] - 1}) == null)) {

						Entity.newLine();
						Entity.write(vertData);

					}

				}
				Edit.close();
				Entity.close();

			}
		}catch (IOException e) {
			System.err.println("could not write file!");
			e.printStackTrace();
		}


    }

    /**
	 * writes the animation.data file, which stores all animation data.
	 * @throws IOException
	 */
    private void writeAnimDat() throws IOException {
		File folder = new File("res/models/"+fileName);
		folder.mkdirs();

		for(File file: folder.listFiles()) {
			if (!file.isDirectory()) {
				file.delete();
			}
		}

		BufferedWriter Data = new BufferedWriter(new FileWriter("res/models/"+fileName+"/animation.dat", false));
		Data.write("# " + "res/models/"+fileName+"/animation.dat");
		Data.newLine();

		Data.write("#Frame Count: "+frames.size());
		Data.newLine();

		Data.write("Frames:");
		Data.newLine();

		String frameString = "";
		for(int i=0;i<frames.size();i++){
			if(i!= 0){
				frameString +=",";
			}
			frameString += frames.get(i).name;
		}
		Data.write(frameString);
		Data.newLine();
		if(animations.isEmpty()){
			Data.write("#No Animations");
		}else {
			Data.write("Animations:");
			Data.newLine();

			Data.write("#AnimationName, end param, frame1, time 1, frame2, time2,...");
			Data.newLine();

			for (FrameAnimation a : animations) {
				String animationData = "";
				animationData += (a.name);
				for (int i = 0; i < a.frames.size(); i++) {
					animationData += ("," + a.frames.get(i).name);
					animationData += ("," + a.lengths.get(i));
				}
				Data.write(animationData);
				Data.newLine();
			}
		}

		Data.close();
	}

    /**
	 * reads all files associated with a model
     */
    private void read(){

    	//reading animation.dat
		File fileData = new File("res/models/" + fileName + "/" + "animation.dat");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileData));
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith("#")) {
					if (line.equals("Frames:")) {

						//reads frames
						line = reader.readLine();
						while(line.startsWith("#")){
							line = reader.readLine();
						}

						String[] s = line.split(",");
						frames.clear();
						animations.clear();
						for(String frame:s){

							readFile(frame);
						}
						currentFrame = frames.get(0);

					}
					if (line.startsWith("Animations:")) {


						while((line = reader.readLine())  != null) {

							if (!line.startsWith("#")) {
								String[] animationData = line.split(",");

								addAnimation(animationData);
							}
						}

					}
					if(!animations.isEmpty()){
						currentAnim = animations.get(0);
					}
				}
			}
			updateVbos();
		} catch (FileNotFoundException e) {
			System.err.println("Can not open file!");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Can not read file!");
			e.printStackTrace();
		}

	}

	/**
	 * creates an Animation from animationData
	 * @param animationData
     */
	private void addAnimation(String[] animationData){
		FrameAnimation animation = new FrameAnimation(new String(animationData[0]));
		for(int i=1;i<animationData.length;i+=2){
			Frame selectedFrame = null;
			for(Frame frame : frames){
				if(animationData[i].equals(frame.name)){
					selectedFrame = frame;
					break;
				}
			}
			float time = Float.parseFloat(animationData[i+1]);
			animation.frames.add(selectedFrame);
			animation.lengths.add(time);
			//System.out.println(selectedFrame.name);
		}
		animations.add(animation);

	}

	/**
	 * reads a single .edt file
	 * @param frameName
     */
	private void readFile(String frameName){
		try {

			Frame frame = new Frame(frameName);
			frames.add(frame);
			BufferedReader reader = new BufferedReader(new FileReader("res/models/"+fileName+"/"+frameName+".edt"));

			String line;

			line = reader.readLine();

			if(!(line == null) && !(line == "")){

				String[] Data;

				while ((line = reader.readLine()) != null)
				{
					if(!line.startsWith("#")){
						Data = line.split(",");
						Vert vert = new Vert();
						vert.position = new Vector3f(Float.parseFloat(Data[0]),Float.parseFloat(Data[1]),Float.parseFloat(Data[2]));
						vert.normal = new Vector3f(Float.parseFloat(Data[3]),Float.parseFloat(Data[4]),Float.parseFloat(Data[5]));
						vert.color = new Vector4f(Float.parseFloat(Data[6]),Float.parseFloat(Data[7]),Float.parseFloat(Data[8]),Float.parseFloat(Data[9]));
						vert.specular = Float.parseFloat(Data[10]);
						vert.glow = Float.parseFloat(Data[11]);
						frame.verts.add(vert);
						int[] pos = new int[]{(int)(32*vert.position.x),(int)(32*vert.position.y),(int)(32*vert.position.z)};
						frame.octVerts.add(vert, pos);
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.err.println("Can not open file!");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Can not read file!");
			e.printStackTrace();
		}

	}

	/**
	 * calulates time between frames
     */
	private static void calcDeltaTime(){
		//lastFrameTime = Sys.getTime()*1000/Sys.getTimerResolution();
		long currentFrameTime = Sys.getTime()*1000/Sys.getTimerResolution();
		delta = (currentFrameTime - lastFrameTime)/1000f;
		lastFrameTime = currentFrameTime;

	}

}
