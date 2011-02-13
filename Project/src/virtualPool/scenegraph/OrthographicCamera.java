package virtualPool.scenegraph;

import javax.media.opengl.GL;


public class OrthographicCamera extends Camera
{

	float near, far, left, right, top, bottom;

	public OrthographicCamera(float l, float r, float t, float b, float n, float f)
	{
		left = l;
		right = r;
		top = t;
		bottom = b;
		near = n;
		far = f;
	}

	@Override
	public void render(GL gl)
	{
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(left, right, bottom, top, near, far);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		align(gl);
	}

}
