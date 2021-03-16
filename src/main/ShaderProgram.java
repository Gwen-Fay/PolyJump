package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 * object that creates and interacts with a GLSL shader
 * @author gwen
 *
 */
public class ShaderProgram {
	private int programID;
	private int vertShaderID;
	private int fragShaderID;
	
	private HashMap<String, Integer> hm = new HashMap<String, Integer>();
	
	private static FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
	
	/**
	 * constructor
	 * @param vertFile - file location of vertex shader
	 * @param fragFile - file location of fragment shader
	 * @param attribs - attribs used (in)
	 * @param names - name of attribs in shader
	 */
	public ShaderProgram(String vertFile,String fragFile, int[] attribs, String[] names){
		
		vertShaderID = loadShader(vertFile,GL20.GL_VERTEX_SHADER);
		fragShaderID = loadShader(fragFile,GL20.GL_FRAGMENT_SHADER);
		programID = GL20.glCreateProgram();
		GL20.glAttachShader(programID, vertShaderID);
		GL20.glAttachShader(programID, fragShaderID);
		for(int i=0;i<attribs.length;i++){
			GL20.glBindAttribLocation(programID, attribs[i], names[i]);
		}
		GL20.glLinkProgram(programID);
		GL20.glValidateProgram(programID);
		
	}
	
	/**
	 * sets float uniform
	 * @param data
	 * @param name
	 */
	public void setUniform(float data, String name){
		if(!hm.containsKey(name)){
			hm.put(name,GL20.glGetUniformLocation(programID, name));
		}
		
		GL20.glUniform1f(hm.get(name),data);
	}
	
	public void setUniform(int data, String name){
		if(!hm.containsKey(name)){
			hm.put(name,GL20.glGetUniformLocation(programID, name));
		}
		
		GL20.glUniform1i(hm.get(name),data);
	}
	
	/**
	 * sets vec2 uniform
	 * @param data
	 * @param name
	 */
	public void setUniform(Vector2f data, String name){
		if(!hm.containsKey(name)){
			hm.put(name,GL20.glGetUniformLocation(programID, name));
		}
		GL20.glUniform2f(hm.get(name),data.x,data.y);
	}
	
	/**
	 * sets vec3 uniform
	 * @param data
	 * @param name
	 */
	public void setUniform(Vector3f data, String name){
		if(!hm.containsKey(name)){
			hm.put(name,GL20.glGetUniformLocation(programID, name));
		}
		GL20.glUniform3f(hm.get(name),data.x,data.y,data.z);
	}
	
	/**
	 * sets vec4 uniform
	 * @param data
	 * @param name
	 */
	public void setUniform(Vector4f data, String name){
		if(!hm.containsKey(name)){
			hm.put(name,GL20.glGetUniformLocation(programID, name));
		}
		GL20.glUniform4f(hm.get(name),data.x,data.y,data.z,data.w);
	}
	
	/**
	 * sets mat4 uniform
	 * @param data
	 * @param name
	 */
	public void setUniform(Matrix4f data, String name){
		if(!hm.containsKey(name)){
			hm.put(name,GL20.glGetUniformLocation(programID, name));
		}
		 data.store(matrixBuffer);
		 matrixBuffer.flip();
		 GL20.glUniformMatrix4(hm.get(name),false,matrixBuffer);
	}
	/**
	 * sets bool uniform
	 * @param data
	 * @param name
	 */
	public void setUniform(boolean data, String name){
		if(!hm.containsKey(name)){
			hm.put(name,GL20.glGetUniformLocation(programID, name));
		}
		 if (data){
			 GL20.glUniform1f(hm.get(name),1);
		 }else{
			 GL20.glUniform1f(hm.get(name),0);
		 }
	}
	
	public void setUniform(List<Vector3f> data, int length, Vector3f empty,String name){
		for(int i=0;i<length;i++){
			String newName = name+"["+i+"]";
			
			if(!hm.containsKey(newName)){
				hm.put(newName,GL20.glGetUniformLocation(programID, newName));

			}
			if(i<data.size()){
				GL20.glUniform3f(hm.get(newName),data.get(i).x,data.get(i).y,data.get(i).z);
				
			}else{
				GL20.glUniform3f(hm.get(newName),empty.x,empty.y,empty.z);
			}
		}
		
	}
	
	/**
	 * starts shader for rendering
	 */
	public void start(){
		GL20.glUseProgram(programID);
	}
	
	/**
	 * stops shader
	 */
	public void stop(){
		GL20.glUseProgram(0);
	}
	
	/**
	 * deletes shader from memory
	 */
	public void cleanUp(){
		stop();
		GL20.glDetachShader(programID, vertShaderID);
		GL20.glDetachShader(programID, fragShaderID);
		GL20.glDeleteShader(vertShaderID);
		GL20.glDeleteShader(fragShaderID);
		GL20.glDeleteProgram(programID);
		hm.clear();
		
	}
	
	/**
	 * loads shader from file and returns shader's ID
	 * @param file
	 * @param type
	 * @return
	 */
	private static int loadShader(String file, int type){
		StringBuilder shaderSource = new StringBuilder();
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while((line = reader.readLine())!=null){
				shaderSource.append(line).append("\n");
			}
			reader.close();
		}catch(IOException e){
			System.err.println("ERR! Could not read file: "+ file );
			e.printStackTrace();
			System.exit(-1);
		}
		int shaderID = GL20.glCreateShader(type);
		GL20.glShaderSource(shaderID, shaderSource);
		GL20.glCompileShader(shaderID);
		if(GL20.glGetShaderi(shaderID,GL20.GL_COMPILE_STATUS)==GL11.GL_FALSE){
			System.err.println("ERR! could not compile shader! File: "+ file);
			System.err.println(GL20.glGetShaderInfoLog(shaderID, 500));
			System.exit(-1);
		}
		return shaderID;
		
	}
	
}
