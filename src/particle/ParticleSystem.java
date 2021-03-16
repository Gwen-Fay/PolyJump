package particle;

import java.nio.FloatBuffer;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import main.Main;
import render.Loader;
import render.Renderer;

public class ParticleSystem {

	private float pps, averageSpeed, gravityComplient, averageLifeLength, averageScale;

	private float speedError, lifeError, scaleError = 0;
	private Vector3f direction;
	private float directionDeviation = 0;
	
	private Particle[] particles;
	
	private float[] pos, col;
	private static Random random = new Random();
	private Vector3f velocity;
	
	private int vaoID;
	private int posID, colorID;
	private static FloatBuffer posBuf, colorBuf;
	
	public boolean isAdditive = false;
	
	public Vector3f position = new Vector3f(0,0,0);
	
	private int particleCount = 0;
	float delta = 0;
	
	/**
	 * 
	 * @param pps
	 * @param speed
	 * @param gravityComplient
	 * @param lifeLength
	 * @param scale
	 * @param particleCount
	 */
	public ParticleSystem(float pps, float speed, float gravityComplient, float lifeLength, float scale, int particleCount, Vector3f position, Vector4f[] color) {
		this.pps = pps;
		this.averageSpeed = speed;
		this.gravityComplient = gravityComplient;
		this.averageLifeLength = lifeLength;
		this.averageScale = scale;
		this.position = position;
		
		vaoID = GL30.glGenVertexArrays();
		Loader.addVao(vaoID);
		posID = createEmptyVbo(particleCount*3);
		colorID = createEmptyVbo(particleCount*4);
		posBuf = BufferUtils.createFloatBuffer(particleCount*3);
		colorBuf = BufferUtils.createFloatBuffer(particleCount*4);

		GL30.glBindVertexArray(vaoID);
		Loader.bindIndicicesBuffer(Renderer.CUBE_INDEX);
		addInstancedAttribute(posID,0,3);
		addInstancedAttribute(colorID,1,4);
		Loader.loadToVBO(2,Renderer.CUBE,3);
		GL30.glBindVertexArray(0);
		
		particles = new Particle[particleCount];
		for(int i=0;i<particleCount;i++){
			particles[i] = new Particle(color);
			emitParticle(i);
			particles[i].isAlive = false;
		}
		
		pos = new float[particleCount * 3];
		col = new float[particleCount * 4];
	}

	private int createEmptyVbo(int floatCount){
		int vbo = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, floatCount*4, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		Loader.addVbo(vbo);
		return vbo;
	}

	
	private void addInstancedAttribute(int vbo, int attribute, int dataSize){
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL20.glVertexAttribPointer(attribute, dataSize, GL11.GL_FLOAT, false,0, 0);
		GL33.glVertexAttribDivisor(attribute, 1);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
	
	/**
	 * @param direction - The average direction in which particles are emitted.
	 * @param deviation - A value between 0 and 1 indicating how far from the chosen direction particles can deviate.
	 */
	public void setDirection(Vector3f direction, float deviation) {
		this.direction = new Vector3f(direction);
		this.directionDeviation = (float) (deviation * Math.PI);
	}

	/**
	 * @param error
	 *            - A number between 0 and 1, where 0 means no error margin.
	 */
	public void setSpeedError(float error) {
		this.speedError = error * averageSpeed;
	}

	/**
	 * @param error
	 *            - A number between 0 and 1, where 0 means no error margin.
	 */
	public void setLifeError(float error) {
		this.lifeError = error * averageLifeLength;
	}

	/**
	 * @param error
	 *            - A number between 0 and 1, where 0 means no error margin.
	 */
	public void setScaleError(float error) {
		this.scaleError = error * averageScale;
	}

	public void generateParticles() {
		delta += Main.getDelta();
		float particlesToCreate = pps * delta;
		int count = (int) Math.floor(particlesToCreate);
		if(count>0){
			delta = 0;
		}
		int emited = 0;
		for (int i = 0; i < particles.length; i++) {
			particles[i].update();
			if(emited<count){
				if(!particles[i].isAlive){
					emitParticle(i);
					emited+=1;
				}else{
					
				}
			}
		}
		updateVbos();
	}

	private void emitParticle(int location) {
		velocity = null;
		if(direction!=null){
			velocity = generateRandomUnitVectorWithinCone(direction, directionDeviation);
		}else{
			velocity = generateRandomUnitVector();
		}
		velocity.normalise();
		velocity.scale(generateValue(averageSpeed, speedError));
		float scale = generateValue(averageScale, scaleError);
		float lifeLength = generateValue(averageLifeLength, lifeError);
		particles[location].setParticle(new Vector3f(position), velocity, gravityComplient, lifeLength, scale);
	}

	private float generateValue(float average, float errorMargin) {
		float offset = (random.nextFloat() - 0.5f) * 2f * errorMargin;
		return average + offset;
	}

	private static Vector3f generateRandomUnitVectorWithinCone(Vector3f coneDirection, float angle) {
		float cosAngle = (float) Math.cos(angle);
		float theta = (float) (random.nextFloat() * 2f * Math.PI);
		float z = cosAngle + (random.nextFloat() * (1 - cosAngle));
		float rootOneMinusZSquared = (float) Math.sqrt(1 - z * z);
		float x = (float) (rootOneMinusZSquared * Math.cos(theta));
		float y = (float) (rootOneMinusZSquared * Math.sin(theta));

		Vector4f direction = new Vector4f(x, y, z, 1);
		if (coneDirection.x != 0 || coneDirection.y != 0 || (coneDirection.z != 1 && coneDirection.z != -1)) {
			Vector3f rotateAxis = Vector3f.cross(coneDirection, new Vector3f(0, 0, 1), null);
			rotateAxis.normalise();
			float rotateAngle = (float) Math.acos(Vector3f.dot(coneDirection, new Vector3f(0, 0, 1)));
			Matrix4f rotationMatrix = new Matrix4f();
			rotationMatrix.rotate(-rotateAngle, rotateAxis);
			Matrix4f.transform(rotationMatrix, direction, direction);
		} else if (coneDirection.z == -1) {
			direction.z *= -1;
		}
		return new Vector3f(direction);
	}
	
	private Vector3f generateRandomUnitVector() {
		float theta = (float) (random.nextFloat() * 2f * Math.PI);
		float z = (random.nextFloat() * 2) - 1;
		float rootOneMinusZSquared = (float) Math.sqrt(1 - z * z);
		float x = (float) (rootOneMinusZSquared * Math.cos(theta));
		float y = (float) (rootOneMinusZSquared * Math.sin(theta));
		return new Vector3f(x, y, z);
	}
	
	public int getVaoID(){
		return vaoID;
	}
	
	private void updateVbos(){
		particleCount = 0;
		int skip = 0;
		for(int i=0;i<particles.length;i++){
			if(particles[i].isAlive){
				
				pos[(i-skip)*3] = particles[i].position.x;
				pos[(i-skip)*3+1] = particles[i].position.y;
				pos[(i-skip)*3+2] = particles[i].position.z;
				
				col[(i-skip)*4] = particles[i].color.x;
				col[(i-skip)*4+1] = particles[i].color.y;
				col[(i-skip)*4+2] = particles[i].color.z;
				col[(i-skip)*4+3] = particles[i].color.w;
				
				particleCount+=1;
				
			}else{
				skip+=1;
			}
			
			updateOneVbo(posID,pos,posBuf);
			updateOneVbo(colorID,col,colorBuf);
		}
	}
	 /*
		private static void updateVbos(){
			float[] rF = new float[verts.size()*3];
			float[] cF = new float[verts.size()*4];
			float[] nF = new float[verts.size()*3];
			for(int i=0;i<verts.size();i++){
				rF[i*3] = verts.get(i).position.x;
				rF[i*3+1] = verts.get(i).position.y;
				rF[i*3+2] = verts.get(i).position.z;
				
				cF[i*4] = verts.get(i).color.x;
				cF[i*4+1] = verts.get(i).color.y;
				cF[i*4+2] = verts.get(i).color.z;
				cF[i*4+3] = verts.get(i).color.w;
				
				nF[i*3] = verts.get(i).normal.x;
				nF[i*3+1] = verts.get(i).normal.y;
				nF[i*3+2] = verts.get(i).normal.z;
			}
			updateOneVbo(posID,rF,posBuf);
			updateOneVbo(colorID,cF,colorBuf);
			updateOneVbo(normID,nF,normBuf);
		}
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

	public int getCount() {
		// TODO Auto-generated method stub
		return particleCount;
	}
	

}
