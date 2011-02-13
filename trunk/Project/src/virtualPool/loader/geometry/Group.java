package virtualPool.loader.geometry;

import java.util.ArrayList;
import java.util.List;

public class Group
{
	public String name;
	public List<Mesh> meshes;	
	
	public Group(String name)
	{
		this.name = name;
		this.meshes = new ArrayList<Mesh>();
	}
	
	public Group()
	{
		this("default");
	}
}
