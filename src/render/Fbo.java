package render;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

public class Fbo {

	public static final int NEAR = 0,
							LIN = 1;
	
	private static List<Integer> frameBuffers = new ArrayList<Integer>();
	private static List<Integer> renderBuffers = new ArrayList<Integer>(); 
	
	private int fb;
	public int width, height;
	private Integer colorTex = null;
	private Integer depthTex = null;
	private Integer depthBuff = null;
	private Integer colorBuff = null;
	private boolean isMS = false;

	/**
	 * wrapper for gl Fbos
	 * @param width
	 * @param height
	 * @param isMS
     */
	public Fbo(int width, int height, boolean isMS){
		this.isMS = isMS;
		fb = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fb);
		GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
		this.width = width;
		this.height = height;
	}
	public int getWidth(){
		return width;
	}
	public int getHeight(){
		return height;
	}
	public int getFrameBuffer(){
		return fb;
	}
	public int getColorTex(){
		return colorTex;
	}
	public int getdepthTex(){
		return depthTex;
	}
	public int getdepthBuff(){
		return depthBuff;
	}

	/**
	 * bind this Fbo
     */
	public void bindMe(){
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fb);
		GL11.glViewport(0, 0, width, height);
	}
	
	public static void cleanUp(){
		for(int fb :frameBuffers){
			GL30.glDeleteFramebuffers(fb);
		}
		for(int rb :renderBuffers){
			GL30.glDeleteRenderbuffers(rb);
		}
	}

	/**
	 * unbind this Fbo
     */
	public static void unbindCurrentFrameBuffer(){
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL11.glViewport(0, 0, Renderer.WIDTH, Renderer.HEIGHT);
	}
	/*
	public static int createFrameBuffer(){
		int fb = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fb);
		GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
		frameBuffers.add(fb);
		return fb;
	}
	*/

	/**
	 * attach texture buffer
	 * @param filter
     */
	public void createTextureAttacment(int filter){
		bindMe();
		colorTex = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTex);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height,
				0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)null);
		if(filter == NEAR){
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		}else{
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		}
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
				colorTex, 0);
		Loader.addTexture(colorTex);
	}

	/**
	 * blits to Fbo
	 * @param outputFbo
     */
	public void resolveToFbo(Fbo outputFbo){
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,outputFbo.getFrameBuffer());
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,fb);
		GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, outputFbo.getWidth(), 
				outputFbo.getHeight(), GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
		unbindCurrentFrameBuffer();
	}

	/**
	 * blits to screen
     */
	public void resolveToScreen(){
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,0);
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,fb);
		GL11.glDrawBuffer(GL11.GL_BACK);
		GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, Renderer.WIDTH, 
				Renderer.HEIGHT, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
		unbindCurrentFrameBuffer();
	}

	/**
	 * creates multi sampled color attachment
     */
	public void createColorMSAttachment(){
		if(isMS){
			colorBuff = GL30.glGenRenderbuffers();
			GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, colorBuff);
			GL30.glRenderbufferStorageMultisample(GL30.GL_RENDERBUFFER,4,GL11.GL_RGBA8,width,height);
			GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
					GL30.GL_RENDERBUFFER, colorBuff);
			renderBuffers.add(colorBuff);
		}
	}

	/**
	 * creates depth texture attachment
     */
	public void createDepthTextureAttahcment(){
		bindMe();
		depthTex = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTex);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, width, height, 0,
				GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
				depthTex, 0);
		Loader.addTexture(depthTex);
	}

	/**
	 * creates depth attachment
     */
	public void createDepthAttachment(){
		bindMe();
		depthBuff = GL30.glGenRenderbuffers();
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuff);
		if(!isMS){
			GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER,GL14.GL_DEPTH_COMPONENT24,width,height);
		}else{
			GL30.glRenderbufferStorageMultisample(GL30.GL_RENDERBUFFER,4,GL14.GL_DEPTH_COMPONENT24,width,height);
		}
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
		GL30.GL_RENDERBUFFER, depthBuff);
		renderBuffers.add(depthBuff);
	}
	
	
	
}
