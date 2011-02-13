package virtualPool.scenegraph;

import javax.media.opengl.GL;


public class Light extends Node
{
	public float diffuse[];
	public float ambient[];
	public float specular[];
	public float position[];
	public float attenuation[];

	public boolean is_on;

	protected int id;

	public Light(int id)
	{
		diffuse = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
		ambient = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
		specular = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
		position = new float[]{0.0f, 0.0f, 0.0f, 0.0f};
		attenuation = new float[]{1.0f, 0.0f, 0.0f};

		is_on = true;
		this.id = id;
	}

	public int getId()
	{
		return id;
	}
	
	public void setPosition(float x, float y, float z)
	{
		position[0] = x;
		position[1] = y;
		position[2] = z;
	}

	public void setAttenuation(float r, float g, float b)
	{
		attenuation[0] = r;
		attenuation[1] = g;
		attenuation[2] = b;
	}

	public void setDiffuse(float r, float g, float b)
	{
		diffuse[0] = r;
		diffuse[1] = g;
		diffuse[2] = b;
	}

	public void setAmbient(float r, float g, float b)
	{
		ambient[0] = r;
		ambient[1] = g;
		ambient[2] = b;
	}

	public void setSpecular(float r, float g, float b)
	{
		specular[0] = r;
		specular[1] = g;
		specular[2] = b;
	}

	public void setDirectional(boolean isDirectional)
	{
		position[3] = (isDirectional ? 0.0f : 1.0f);
	}

	@Override
	public void render(GL gl)
	{
		gl.glLightfv(id, GL.GL_DIFFUSE, diffuse, 0);
		gl.glLightfv(id, GL.GL_AMBIENT, ambient, 0);
		gl.glLightfv(id, GL.GL_SPECULAR, specular, 0);
		gl.glLightfv(id, GL.GL_POSITION, position, 0);

		gl.glLightf(id, GL.GL_CONSTANT_ATTENUATION, attenuation[0]);
		gl.glLightf(id, GL.GL_LINEAR_ATTENUATION, attenuation[1]);
		gl.glLightf(id, GL.GL_QUADRATIC_ATTENUATION, attenuation[2]);

		if (is_on)
			gl.glEnable(id);
		else
			gl.glDisable(id);
	}
}
