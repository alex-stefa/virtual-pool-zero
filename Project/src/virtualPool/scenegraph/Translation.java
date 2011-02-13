package virtualPool.scenegraph;

import javax.media.opengl.GL;


public class Translation extends Transformation
{
	public float dx, dy, dz;

	public Translation(float dx, float dy, float dz)
	{
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
	}

	@Override
	public void render(GL gl)
	{
		gl.glTranslatef(dx, dy, dz);
	}
}
