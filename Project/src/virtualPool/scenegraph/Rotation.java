package virtualPool.scenegraph;

import javax.media.opengl.GL;


public class Rotation extends Transformation
{
	public float angle, ax, ay, az;

	public Rotation(float angle, float ax, float ay, float az)
	{
		this.angle = angle;
		this.ax = ax;
		this.ay = ay;
		this.az = az;
	}

	@Override
	public void render(GL gl)
	{
		gl.glRotatef(angle, ax, ay, az);
	}

}
