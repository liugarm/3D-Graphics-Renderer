public class EdgeList {

	private float[] coordinates = new float[4];

	public EdgeList(float x, float z) {
		coordinates[0] = x;
		coordinates[1] = z;
	}

	public void add(float x, float z) {
		if (x < coordinates[0]) {
			coordinates[2] = coordinates[0];
			coordinates[3] = coordinates[1];
			coordinates[0] = x;
			coordinates[1] = z;
		} else {
			coordinates[2] = x;
			coordinates[3] = z;

		}
	}
	
	public float getLeftX() {
		return coordinates[0];
	}

	public float getLeftZ() {
		return coordinates[1];
	}

	public float getRightX() {
		return coordinates[2];
	}

	public float getRightZ() {
		return coordinates[3];
	}

}
