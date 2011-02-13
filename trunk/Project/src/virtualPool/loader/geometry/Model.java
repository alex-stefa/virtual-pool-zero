package virtualPool.loader.geometry;

import java.util.ArrayList;
import java.util.List;


public class Model
{
	public List<Material> materials = new ArrayList<Material>();
	public List<Group> groups = new ArrayList<Group>();
	public List<Vec4> vertices = new ArrayList<Vec4>();
	public List<Vec4> normals = new ArrayList<Vec4>();
	public List<TexCoord> texCoords = new ArrayList<TexCoord>();
	
	public String modelFilename;
	public String textureBasePath;

	public boolean renderModel = true;
	public boolean centerModel = false;
	public boolean renderModelBounds = false;
	public boolean unitizeSize = false;
	public boolean useTexture = true;
	public boolean renderAsWireframe = false;
	public boolean useLighting = true;

	public Bounds bounds = null;

	public Model(String source, String textureBasePath)
	{
		this.modelFilename = source;
		this.textureBasePath = textureBasePath;
	}

	public void updateBounds()
	{
		bounds = new Bounds();
		for (Vec4 v : vertices) bounds.calc(v);
	}
	
	public void center()
	{
		centerAt(0, 0, 0);
		updateBounds();
	}
	
	public void centerAt(float posX, float posY, float posZ)
	{
		Vec4 center = bounds.getCenterPoint();
		
		for (Vec4 v : vertices)
		{
			v.x += posX - center.x;
			v.y += posY - center.y;
			v.z += posZ - center.z;
		}
		updateBounds();
	}
	
	public void scaleX(float maxSize)
	{
		float rs = maxSize / bounds.getSize().x;
		
		for (Vec4 v : vertices)
		{
			v.x *= rs;
			v.y *= rs;
			v.z *= rs;
		}
		updateBounds();
	}

	public void scaleY(float maxSize)
	{
		float rs = maxSize / bounds.getSize().y;
		
		for (Vec4 v : vertices)
		{
			v.x *= rs;
			v.y *= rs;
			v.z *= rs;
		}
		updateBounds();
	}
	
	public void scaleZ(float maxSize)
	{
		float rs = maxSize / bounds.getSize().z;
		
		for (Vec4 v : vertices)
		{
			v.x *= rs;
			v.y *= rs;
			v.z *= rs;
		}
		updateBounds();
	}
	
	public void scaleDiag(float maxSize)
	{
		float rs = maxSize / (2 * bounds.getRadius());
		
		for (Vec4 v : vertices)
		{
			v.x *= rs;
			v.y *= rs;
			v.z *= rs;
		}		
		updateBounds();
	}
}
