package virtualPool.loader.geometry;

import java.awt.Color;

public class Material
{
    public String strName;
    public String strFile;

    public Color ambientColor;
    public Color specularColor;
    public Color diffuseColor;
    public Color emissiveColor = Color.BLACK;
    public float shininess;
    public float transparency;
    public int textureId;
    public float uTile;
    public float vTile;
    public float uOffset;
    public float vOffset;
}