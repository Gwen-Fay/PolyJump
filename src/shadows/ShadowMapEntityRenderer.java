package shadows;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Matrix4f;

import entity.PointEntity;
import entity.PolyEntity;
import main.ShaderProgram;

public class ShadowMapEntityRenderer {

	private Matrix4f projectionViewMatrix;
	private ShaderProgram shader;

	/**
	 * @param shader
	 *            - the simple shader program being used for the shadow render
	 *            pass.
	 * @param projectionViewMatrix
	 *            - the orthographic projection matrix multiplied by the light's
	 *            "view" matrix.
	 */
	protected ShadowMapEntityRenderer(ShaderProgram shader, Matrix4f projectionViewMatrix) {
		this.shader = shader;
		this.projectionViewMatrix = projectionViewMatrix;
	}

	/**
	 * Renders entieis to the shadow map. Each model is first bound and then all
	 * of the entities using that model are rendered to the shadow map.
	 * 
	 * @param entities
	 *            - the entities to be rendered to the shadow map.
	 */
	protected void renderEntities(Map<Integer, List<PointEntity>> entities) {


		GL20.glEnableVertexAttribArray(4);

		for (int vaoID : entities.keySet()) {
			bindModel(vaoID);
			for (PointEntity entity : entities.get(vaoID)) {
				prepareInstance(entity);

				GL31.glDrawArraysInstanced(GL11.GL_POINTS, 0,1,entity.getVertCount());
			}
		}
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(4);
		GL30.glBindVertexArray(0);
	}
	
	/**
	 * Renders entieis to the shadow map. Each model is first bound and then all
	 * of the entities using that model are rendered to the shadow map.
	 * 

	 *            - the entities to be rendered to the shadow map.
	 */
	protected void renderEnvironments(Map<Integer, List<PolyEntity>> environments) {

		for (int vaoID : environments.keySet()) {
			bindModel(vaoID);
			for (PolyEntity env : environments.get(vaoID)) {
				prepareInstance(env);
				
				if(env.isTransparent){
					GL11.glDisable(GL11.GL_CULL_FACE);
				}
				
				GL13.glActiveTexture(GL13.GL_TEXTURE0);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, env.getTextureID());
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
				
				GL11.glDrawElements(GL11.GL_TRIANGLES, env.getVertCount(),GL11.GL_UNSIGNED_INT,0);
				
				if(env.isTransparent){
					GL11.glEnable(GL11.GL_CULL_FACE);
					GL11.glCullFace(GL11.GL_BACK);
				}
				
			}
		}
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
	}

	/**
	 * Binds a raw model before rendering. Only the attribute 0 is enabled here
	 * because that is where the positions are stored in the VAO, and only the
	 * positions are required in the vertex shader.
	 *
	 *            - the model to be bound.
	 */
	private void bindModel(int vaoID) {
		GL30.glBindVertexArray(vaoID);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);

	}

	/**
	 * Prepares an entity to be rendered. The model matrix is created in the
	 * usual way and then multiplied with the projection and view matrix (often
	 * in the past we've done this in the vertex shader) to create the
	 * mvp-matrix. This is then loaded to the vertex shader as a uniform.
	 * 
	 * @param entity
	 *            - the entity to be prepared for rendering.
	 */
	private void prepareInstance(PointEntity entity) {
		Matrix4f modelMatrix = entity.getWorldMatrix();
		Matrix4f mvpMatrix = Matrix4f.mul(projectionViewMatrix, modelMatrix, null);
		
		shader.setUniform(mvpMatrix,"mvpMatrix");
	}
	
	private void prepareInstance(PolyEntity env) {
		Matrix4f modelMatrix = env.getWorldMatrix();
		Matrix4f mvpMatrix = Matrix4f.mul(projectionViewMatrix, modelMatrix, null);
		shader.setUniform(mvpMatrix,"mvpMatrix");
	}

}
