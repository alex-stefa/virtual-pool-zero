package virtualPool.scenegraph;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLUquadric;


public class Sphere extends Geometry
{
	public GLUquadric quad;
	public double radius;

	public Sphere(double radius)
	{
		quad = glu.gluNewQuadric();
		this.radius = radius;
	}

	@Override
	public void render(GL gl)
	{
		glu.gluSphere(quad, radius, 50, 50);
	}
}
