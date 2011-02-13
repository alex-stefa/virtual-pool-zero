package virtualPool.scenegraph;

import javax.media.opengl.GL;


public class Material extends Node
{
	public float diffuse[];
	public float ambient[];
	public float specular[];
	public float emissive[];
	public float shininess;

	public Material()
	{
		diffuse = new float[4];
		ambient = new float[4];
		specular = new float[4];
		emissive = new float[4];
		
		for (int i = 0; i < 3; i++)
		{
			diffuse[i] = ambient[i] = specular[i] = 0.0f;
			emissive[i] = 1.0f;
		}
		diffuse[3] = ambient[3] = specular[3] = 1.0f;
		
		shininess = 50.0f;

		saveAttributes = true;
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

	@Override
	public void render(GL gl)
	{
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, diffuse, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, ambient, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, specular, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_EMISSION, emissive, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shininess);
	}

}
