
public class PolygonEdge {
	
	private Vector3D pointA;
	private Vector3D pointB;
	
	public PolygonEdge(Vector3D pointA, Vector3D pointB){
		this.pointA = pointA;
		this.pointB = pointB;
	}

	public Vector3D getPointA() {
		return pointA;
	}

	public Vector3D getPointB() {
		return pointB;
	}
}
