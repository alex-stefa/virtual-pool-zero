package virtualPool.scenegraph;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

abstract public class Node
{
	public static GLU glu = new GLU();

	private Node LeftChild;
	private Node RightSibling;

	protected boolean saveAttributes = false;

	public Node()
	{
		setLeftChild(null);
		setRightSibling(null);
	}

	public void addChild(Node child)
	{
		child.setRightSibling(getLeftChild());
		setLeftChild(child);
	}

	abstract public void render(GL gl);

	public void traverse(GL gl)
	{

		if (saveAttributes)
		{
			gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
		}
		gl.glPushMatrix();
		render(gl);

		if (getLeftChild() != null)
		{
			getLeftChild().traverse(gl);
		}
		gl.glPopMatrix();
		if (saveAttributes)
		{
			gl.glPopAttrib();
		}
		if (getRightSibling() != null)
		{
			getRightSibling().traverse(gl);
		}
	}

	public void setLeftChild(Node leftChild)
	{
		LeftChild = leftChild;
	}

	public Node getLeftChild()
	{
		return LeftChild;
	}

	public void setRightSibling(Node rightSibling)
	{
		RightSibling = rightSibling;
	}

	public Node getRightSibling()
	{
		return RightSibling;
	}

}
