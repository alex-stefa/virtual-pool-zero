package virtualPool.scenegraph;

import javax.media.opengl.GL;

public class SpotLight extends Light
{
	public float direction[];
	public float spot_exp, spot_cutoff;

	public SpotLight(int id)
	{
		super(id);
		
		this.setDirectional(true);
		
		direction = new float[]{ 0.0f, -1.0f, 0.0f };
		spot_exp = 30.0f;
		spot_cutoff = 180.0f;
	}
	
	public void setDirection(float x, float y, float z)
	{
		direction[0] = x;
		direction[1] = y;
		direction[2] = z;
	}
	
	@Override
	public void render(GL gl)
	{
		gl.glLightfv(id, GL.GL_SPOT_DIRECTION, direction, 0);
		gl.glLightf(id, GL.GL_SPOT_EXPONENT, spot_exp);
		gl.glLightf(id, GL.GL_SPOT_CUTOFF, spot_cutoff);
		
		super.render(gl);
	}
}
