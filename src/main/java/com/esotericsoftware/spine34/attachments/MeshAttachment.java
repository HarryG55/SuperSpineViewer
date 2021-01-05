package com.esotericsoftware.spine34.attachments;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.NumberUtils;
import com.esotericsoftware.spine34.Bone;
import com.esotericsoftware.spine34.Skeleton;
import com.esotericsoftware.spine34.Slot;

/**
 * Attachment that displays a texture region.
 */
public class MeshAttachment extends VertexAttachment {
    private final Color color = new Color(1, 1, 1, 1);
    private TextureRegion region;
    private String path;
    private float[] regionUVs, worldVertices;
    private short[] triangles;
    private int hullLength;
    private MeshAttachment parentMesh;
    private boolean inheritDeform;

    // Nonessential.
    private short[] edges;
    private float width, height;

    public MeshAttachment(String name) {
        super(name);
    }

    public TextureRegion getRegion() {
        if (region == null) throw new IllegalStateException("Region has not been set: " + this);
        return region;
    }

    public void setRegion(TextureRegion region) {
        if (region == null) throw new IllegalArgumentException("region cannot be null.");
        this.region = region;
    }

    public void updateUVs() {
        float[] regionUVs = this.regionUVs;
        int verticesLength = regionUVs.length;
        int worldVerticesLength = (verticesLength >> 1) * 5;
        if (worldVertices == null || worldVertices.length != worldVerticesLength)
            worldVertices = new float[worldVerticesLength];

        float u, v, width, height;
        if (region == null) {
            u = v = 0;
            width = height = 1;
        } else {
            u = region.getU();
            v = region.getV();
            width = region.getU2() - u;
            height = region.getV2() - v;
        }
        if (region instanceof AtlasRegion && ((AtlasRegion) region).rotate) {
            for (int i = 0, w = 3; i < verticesLength; i += 2, w += 5) {
                worldVertices[w] = u + regionUVs[i + 1] * width;
                worldVertices[w + 1] = v + height - regionUVs[i] * height;
            }
        } else {
            for (int i = 0, w = 3; i < verticesLength; i += 2, w += 5) {
                worldVertices[w] = u + regionUVs[i] * width;
                worldVertices[w + 1] = v + regionUVs[i + 1] * height;
            }
        }
    }

    /**
     * @return The updated world vertices.
     */
    public float[] updateWorldVertices(Slot slot, boolean premultipliedAlpha) {
        Skeleton skeleton = slot.getSkeleton();
        Color skeletonColor = skeleton.getColor(), slotColor = slot.getColor(), meshColor = color;
        float alpha = skeletonColor.a * slotColor.a * meshColor.a * 255;
        float multiplier = premultipliedAlpha ? alpha : 255;
        float color = NumberUtils.intToFloatColor( //
                ((int) alpha << 24) //
                        | ((int) (skeletonColor.b * slotColor.b * meshColor.b * multiplier) << 16) //
                        | ((int) (skeletonColor.g * slotColor.g * meshColor.g * multiplier) << 8) //
                        | (int) (skeletonColor.r * slotColor.r * meshColor.r * multiplier));

        float x = skeleton.getX(), y = skeleton.getY();
        FloatArray deformArray = slot.getAttachmentVertices();
        float[] vertices = this.vertices, worldVertices = this.worldVertices;
        int[] bones = this.bones;
        if (bones == null) {
            int verticesLength = vertices.length;
            if (deformArray.size > 0) vertices = deformArray.items;
            Bone bone = slot.getBone();
            x += bone.getWorldX();
            y += bone.getWorldY();
            float a = bone.getA(), b = bone.getB(), c = bone.getC(), d = bone.getD();
            for (int v = 0, w = 0; v < verticesLength; v += 2, w += 5) {
                float vx = vertices[v], vy = vertices[v + 1];
                worldVertices[w] = vx * a + vy * b + x;
                worldVertices[w + 1] = vx * c + vy * d + y;
                worldVertices[w + 2] = color;
            }
            return worldVertices;
        }
        Object[] skeletonBones = skeleton.getBones().items;
        if (deformArray.size == 0) {
            for (int w = 0, v = 0, b = 0, n = bones.length; v < n; w += 5) {
                float wx = x, wy = y;
                int nn = bones[v++] + v;
                for (; v < nn; v++, b += 3) {
                    Bone bone = (Bone) skeletonBones[bones[v]];
                    float vx = vertices[b], vy = vertices[b + 1], weight = vertices[b + 2];
                    wx += (vx * bone.getA() + vy * bone.getB() + bone.getWorldX()) * weight;
                    wy += (vx * bone.getC() + vy * bone.getD() + bone.getWorldY()) * weight;
                }
                worldVertices[w] = wx;
                worldVertices[w + 1] = wy;
                worldVertices[w + 2] = color;
            }
        } else {
            float[] deform = deformArray.items;
            for (int w = 0, v = 0, b = 0, f = 0, n = bones.length; v < n; w += 5) {
                float wx = x, wy = y;
                int nn = bones[v++] + v;
                for (; v < nn; v++, b += 3, f += 2) {
                    Bone bone = (Bone) skeletonBones[bones[v]];
                    float vx = vertices[b] + deform[f], vy = vertices[b + 1] + deform[f + 1], weight = vertices[b + 2];
                    wx += (vx * bone.getA() + vy * bone.getB() + bone.getWorldX()) * weight;
                    wy += (vx * bone.getC() + vy * bone.getD() + bone.getWorldY()) * weight;
                }
                worldVertices[w] = wx;
                worldVertices[w + 1] = wy;
                worldVertices[w + 2] = color;
            }
        }
        return worldVertices;
    }

    public boolean applyDeform(VertexAttachment sourceAttachment) {
        return this == sourceAttachment || (inheritDeform && parentMesh == sourceAttachment);
    }

    public float[] getWorldVertices() {
        return worldVertices;
    }

    public short[] getTriangles() {
        return triangles;
    }

    /**
     * Vertex number triplets which describe the mesh's triangulation.
     */
    public void setTriangles(short[] triangles) {
        this.triangles = triangles;
    }

    public float[] getRegionUVs() {
        return regionUVs;
    }

    /**
     * Sets the texture coordinates for the region. The values are u,v pairs for each vertex.
     */
    public void setRegionUVs(float[] regionUVs) {
        this.regionUVs = regionUVs;
    }

    public Color getColor() {
        return color;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getHullLength() {
        return hullLength;
    }

    public void setHullLength(int hullLength) {
        this.hullLength = hullLength;
    }

    public short[] getEdges() {
        return edges;
    }

    public void setEdges(short[] edges) {
        this.edges = edges;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    /**
     * Returns the source mesh if this is a linked mesh, else returns null.
     */
    public MeshAttachment getParentMesh() {
        return parentMesh;
    }

    /**
     * @param parentMesh May be null.
     */
    public void setParentMesh(MeshAttachment parentMesh) {
        this.parentMesh = parentMesh;
        if (parentMesh != null) {
            bones = parentMesh.bones;
            vertices = parentMesh.vertices;
            regionUVs = parentMesh.regionUVs;
            triangles = parentMesh.triangles;
            hullLength = parentMesh.hullLength;
            edges = parentMesh.edges;
            width = parentMesh.width;
            height = parentMesh.height;
        }
    }

    public boolean getInheritDeform() {
        return inheritDeform;
    }

    public void setInheritDeform(boolean inheritDeform) {
        this.inheritDeform = inheritDeform;
    }
}
