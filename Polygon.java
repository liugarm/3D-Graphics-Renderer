import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

public class Polygon {

	private Vector3D pointOne;
	private Vector3D pointTwo;
	private Vector3D pointThree;
	private int red;
	private int green;
	private int blue;
	private Color shadeColour;

	private Bounds bounds;
	private boolean hidden;

	public Polygon(Vector3D pointOne, Vector3D pointTwo, Vector3D pointThree, int red, int green, int blue) {

		this.pointOne = pointOne;
		this.pointTwo = pointTwo;
		this.pointThree = pointThree;
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public Vector3D getPointOne() {
		return pointOne;
	}

	public Vector3D getPointTwo() {
		return pointTwo;
	}

	public Vector3D getPointThree() {
		return pointThree;
	}

	public void draw(Graphics g, float ambient, float incident, Color[][] screen) {
		Graphics2D g2 = (Graphics2D) g;
		java.awt.Polygon polygon = new java.awt.Polygon();

		polygon.addPoint((int) pointOne.x, (int) pointOne.y);
		polygon.addPoint((int) pointTwo.x, (int) pointTwo.y);
		polygon.addPoint((int) pointThree.x, (int) pointThree.y);

		Rectangle rect = polygon.getBounds();

		//Iterating through pixels and finding out which ones to colour in
		for (int i = (int) rect.getX(); i < (rect.getX() + rect.getWidth()); i++) {
			for (int j = (int) rect.getY(); j < (rect.getY() + rect.getHeight()); j++) {
				if (i < 700 && j < 600 && i >= 0 && j >= 0 && polygon.contains(i, j) && screen[i][j] != null && !screen[i][j].equals(Color.GRAY)) {
					g2.setColor(screen[i][j]);
					g2.fillRect(i, j, 1, 1);
				}
			}
		}
	}

	public void shading(List<Light> lights, float ambientLight) {
		float reflect = ambientLight;
		float lightIntensity = 0.0f;
		Color reflectivity = new Color(red, green, blue);

		float reflectRed = 0;
		float reflectGreen = 0;
		float reflectBlue = 0;

		Vector3D vectorA = pointTwo.minus(pointOne);
		Vector3D vectorB = pointThree.minus(pointTwo);
		Vector3D surfaceNormal = vectorA.crossProduct(vectorB);
		Vector3D normalUnitVector = surfaceNormal.unitVector();

		for (Light light : lights) {
			//Light unit vector
			Vector3D lightUnitVector = light.getLight().unitVector();

			if (normalUnitVector.dotProduct(lightUnitVector) > 0) {
				lightIntensity += (light.getIntensity() * normalUnitVector.dotProduct(lightUnitVector));
				
				reflectRed += light.getRed();
				reflectGreen += light.getGreen();
				reflectBlue += light.getBlue();
			}
		}

		reflect = ambientLight + lightIntensity;

		int r = colourRange((int) (reflectivity.getRed() * reflect + reflectRed));
		int g = colourRange((int) (reflectivity.getGreen() * reflect + reflectGreen));
		int b = colourRange((int) (reflectivity.getBlue() * reflect + reflectBlue));

		shadeColour = new Color(r, g, b);
	}

	private int colourRange(int x) {
		if (x <= 0)
			x = 0;

		if (x >= 255)
			x = 255;

		return x;
	}

	public void rotateX(float dragX) {
		Transform rotate = Transform.newXRotation(dragX);
		pointOne = rotate.multiply(pointOne);
		pointTwo = rotate.multiply(pointTwo);
		pointThree = rotate.multiply(pointThree);
	}

	public void rotateY(float dragY) {
		Transform rotate = Transform.newYRotation(dragY);
		pointOne = rotate.multiply(pointOne);
		pointTwo = rotate.multiply(pointTwo);
		pointThree = rotate.multiply(pointThree);
	}

	public void rescale(Pixel polygonCentrePoint, float scaleX, float scaleY, float scaleZ) {
		Transform scale = Transform.newScale(scaleX, scaleY, scaleZ);

		pointOne = scale.multiply(pointOne);
		pointTwo = scale.multiply(pointTwo);
		pointThree = scale.multiply(pointThree);
	}

	public void recentre(Pixel offset) {
		pointOne.x += offset.getX();
		pointOne.y += offset.getY();

		pointTwo.x += offset.getX();
		pointTwo.y += offset.getY();

		pointThree.x += offset.getX();
		pointThree.y += offset.getY();
	}

	private Bounds bounds() {
		float minX = Math.min(Math.min(pointOne.x, pointTwo.x), pointThree.x);
		float minY = Math.min(Math.min(pointOne.y, pointTwo.y), pointThree.y);
		float maxX = Math.max(Math.max(pointOne.x, pointTwo.x), pointThree.x);
		float maxY = Math.max(Math.max(pointOne.y, pointTwo.y), pointThree.y);
		bounds = new Bounds(minX, minY, (maxX - minX), (maxY - minY));
		return bounds;
	}

	public EdgeList[] edgeList() {
		Vector3D[] vertices = new Vector3D[3];
		vertices[0] = pointOne;
		vertices[1] = pointTwo;
		vertices[2] = pointThree;

		bounds();
		
		int minY = Math.round(bounds.getY());
		int maxY = Math.round(bounds.getHeight());
		
		EdgeList[] e = new EdgeList[maxY + 1];

		for (int i = 0; i < 3; i++) {
			Vector3D va = vertices[i];
			Vector3D vb = vertices[(i + 1) % 3];

			if (va.y > vb.y) {
				vb = va;
				va = vertices[(i + 1) % 3];
			}

			float mx = (vb.x - va.x) / (vb.y - va.y);
			float mz = (vb.z - va.z) / (vb.y - va.y);
			float x = va.x;
			float z = va.z;

			int j = Math.round(va.y) - minY;
			int maxj = Math.round(vb.y) - minY;

			while (j < maxj) {
				if (e[j] == null) {
					e[j] = new EdgeList(x, z);
				} else {
					e[j].add(x, z);
				}

				j++;
				x += mx;
				z += mz;
			}

		}

		return e;
	}

	public void hiddenPolygon() {
		//Hide hidden polygons
		Vector3D vectorA = pointTwo.minus(pointOne);
		Vector3D vectorB = pointThree.minus(pointTwo);
		Vector3D normal = vectorA.crossProduct(vectorB);

		if (normal.z < 0) {
			hidden = false;
		} else {
			hidden = true;
		}
	}

	public boolean getHidden() {
		return hidden;
	}

	public Color getShading() {
		return shadeColour;
	}

	public int getMinY() {
		return Math.round(bounds.getY());
	}
}
