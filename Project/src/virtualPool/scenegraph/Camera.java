package virtualPool.scenegraph;

import javax.media.opengl.GL;

public abstract class Camera extends Node
{
	public float centerX, centerY, centerZ;
	public float rotX, rotY;
	public float pivotDistance;
	
	public Camera()
	{
		centerX = centerY = 0;
		centerZ = 10.0f;
		
		rotX = 45.0f;
		rotY = 0.0f;
		pivotDistance = 0.0f;
	}
	
	public void setPosition(float centerX, float centerY, float centerZ)
	{
		this.centerX = centerX;
		this.centerY = centerY;
		this.centerZ = centerZ;
	}
	
	public void setRotation(float rotX, float rotY)
	{
		this.rotX = rotX;
		this.rotY = rotY;
	}

	public void align(GL gl)
	{
		gl.glTranslatef(0.0f, 0.0f, -pivotDistance);
		gl.glRotatef(rotX, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(rotY, 0.0f, 1.0f, 0.0f);
		gl.glTranslatef(-centerX, -centerY, -centerZ);
	}
}
