package virtualPool.scenegraph;

import javax.media.opengl.GL;


public class Scaling extends Transformation
{
	public float sx, sy, sz;

	public Scaling(float sx, float sy, float sz)
	{
		this.sx = sx;
		this.sy = sy;
		this.sz = sz;
	}

	@Override
	public void render(GL gl)
	{
		gl.glScalef(sx, sy, sz);
	}

}
