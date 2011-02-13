package virtualPool.loader.geometry;

public class Face
{
	public int vertIndex[];
	public int coordIndex[];
	public int normalIndex[];

	public Face(int numVertices)
	{
		vertIndex = new int[numVertices];
		coordIndex = new int[numVertices];
		normalIndex = new int[numVertices];
	}
}
