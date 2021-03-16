package modelEdit;

import java.awt.*;
import java.util.List;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import main.Camera;
import main.Light;
import main.ShaderProgram;
import render.Fbo;
import render.Loader;
import render.Renderer;

public class ModelRenderer extends Renderer{

	ShaderProgram cursorShader;
	ShaderProgram editorShader;
    ShaderProgram lineShader;
	
	Loader loader;

    private static final float LENGTH = 6/32f;

	int xVAO, yVAO, zVAO;

	ModelRenderer(Camera camera, Loader loader, Canvas canvas){
		super(camera,loader,canvas);
		this.loader = loader;
		cursorShader = new ShaderProgram("src/shaders/cursorVert","src/shaders/shaderFrag",new int[] {},new String[] {});
		editorShader = new ShaderProgram("src/shaders/editorVert","src/shaders/shaderFrag",new int[] {0,1,2,3,4},new String[] {"pos","color","norm","glow","cube"});
        lineShader = new ShaderProgram("src/shaders/lineVert","src/shaders/shaderFrag",new int[] {0},new String[] {"vert"});
		Fbo.unbindCurrentFrameBuffer();

        xVAO = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(xVAO);
        loader.loadToVBO(0,new float[]{0,0,0,LENGTH,0,0},3);
        loader.addVao(xVAO);

        yVAO = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(yVAO);
        loader.loadToVBO(0,new float[]{0,0,0,0,LENGTH,0},3);
        loader.addVao(yVAO);

        zVAO = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(zVAO);
        loader.loadToVBO(0,new float[]{0,0,0,0,0,LENGTH},3);
        loader.addVao(zVAO);

        GL30.glBindVertexArray(0);
	}
	
	/**
	 * Used for Model Editor. Does not render Entities, but does render Texts.
	 * @param camera
	 * @param light
	 * @param vaoID
	 * @param shineDamper
	 * @param reflectivity
	 * @param vertSize
	 */
	public void renderModelEdit(Camera camera, Light light,int vaoID, float shineDamper,float reflectivity,
			int vertSize, Vert cursor1, Vert cursor2, Vert cursor3, List<Vert> selected, List<Vert> brush, Vector4f color){

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClearColor(color.x, color.y, color.z, color.w);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
		
		drawEditor(camera, light, vaoID, vertSize);
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		drawCursors(camera, cursor1,cursor2,cursor3,selected,brush,vaoID);

		drawLines(camera,cursor1);
		
		if((mode & GUI_BIT)==GUI_BIT) {renderGUIs(); }
		if((mode & TEXT_BIT)==TEXT_BIT) {renderFonts(); }
		
		loader.clearLists();
		
		Display.sync(FPS_CAP);
		Display.update();
	}

	/**
	 * draws axis lines coming from cursor
	 * @param camera
	 * @param cursor
     */
	private void drawLines(Camera camera,Vert cursor){
		lineShader.start();

		drawLine(xVAO, cursor.position,new Vector4f(1,0,0,1),camera);
		drawLine(yVAO, cursor.position,new Vector4f(0,1,0,1),camera);
		drawLine(zVAO, cursor.position,new Vector4f(0,0,1,1),camera);

		lineShader.stop();
	}

	/**
	 * Draws Cursors
	 * @param camera
	 * @param cursor1
	 * @param cursor2
	 * @param cursor3
	 * @param selected
	 * @param brush
     * @param vaoID
     */
	private void drawCursors(Camera camera, Vert cursor1, Vert cursor2, Vert cursor3, List<Vert> selected, List<Vert> brush, int vaoID){
		cursorShader.start();
		cursorShader.setUniform(Camera.projection, "proj");
		cursorShader.setUniform(camera.getViewMatrix(), "view");


		GL30.glBindVertexArray(vaoID);

		drawCursor(cursor1,Vert.cursor1Color);
		drawCursor(cursor2,Vert.cursor2Color);
		drawCursor(cursor3,Vert.cursor3Color);

		for(Vert vert : selected){
			drawCursor(vert,Vert.sellectedColor);
		}
		for(Vert vert : brush){
			drawCursor(vert,vert.color);
		}

		GL30.glBindVertexArray(0);


		GL11.glDisable(GL11.GL_BLEND);

		cursorShader.stop();
	}

	/**
	 * draws model
	 * @param camera
	 * @param light
	 * @param vaoID
	 * @param vertSize
     */
	private void drawEditor(Camera camera, Light light, int vaoID, int vertSize){
		editorShader.start();
		editorShader.setUniform(Camera.projection, "proj");
		editorShader.setUniform(camera.getViewMatrix(), "view");
		editorShader.setUniform(light.position, "lightPos");
		editorShader.setUniform(light.color, "lightColor");

		GL30.glBindVertexArray(vaoID);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glEnableVertexAttribArray(3);
		GL20.glEnableVertexAttribArray(4);

		editorShader.setUniform(false, "isFlip");
		//GL31.glDrawArraysInstanced(GL11.GL_TRIANGLE_STRIP, 0, 17,vertSize);
		GL31.glDrawElementsInstanced(GL11.GL_TRIANGLE_STRIP, 15,GL11.GL_UNSIGNED_INT,0,vertSize);

		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(3);
		GL20.glDisableVertexAttribArray(4);
		GL30.glBindVertexArray(0);

		editorShader.stop();
	}

	/**
	 * draws a single line
	 * @param vaoID
	 * @param position
	 * @param color
     * @param camera
     */
	private void drawLine(int vaoID, Vector3f position, Vector4f color, Camera camera){

        GL30.glBindVertexArray(vaoID);
        GL20.glEnableVertexAttribArray(0);

        lineShader.setUniform(Camera.projection, "proj");
        lineShader.setUniform(camera.getViewMatrix(), "view");
        lineShader.setUniform(position,"pos");
        lineShader.setUniform(color,"color");

        GL11.glDrawArrays(GL11.GL_LINES, 0, 2);

        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
    }

	/**
	 * draws a single point on screen, using the cursor shader.
	 * these verts are larger, and change alot, hense why they are rendered
	 * as uniforms instead of having a vao
	 * @param vert
	 * @param color
	 */
	private void drawCursor(Vert vert, Vector4f color){
		cursorShader.setUniform(vert.position,"pos");
		cursorShader.setUniform(color,"color");
		GL11.glDrawArrays(GL11.GL_POINTS, 0, 1);
	}

}
