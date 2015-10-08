public class Light {
	
	private Vector3D light;
	private float intensity = 1.0f;
	private int red = 0;
	private int green = 0;
	private int blue = 0;
	
	public Light(Vector3D light){
		this.light = light;
	}
	
	public void setLight(Vector3D l){
		light = l;
	}
	
	public Vector3D getLight(){
		return light;
	}
	
	public void setIntensity(float value){
		intensity = value;
	}
	
	public float getIntensity(){
		return intensity;
	}

	public int getRed() {
		return red;
	}

	public void setRed(int red) {
		this.red = colourRange(red);
	}

	public int getGreen() {
		return green;
	}

	public void setGreen(int green) {
		this.green = colourRange(green);
	}

	public int getBlue() {
		return blue;
	}

	public void setBlue(int blue) {
		this.blue = colourRange(blue);
	}
	
	private int colourRange(int x) {
		if (x <= 0)
			x = 0;

		if (x >= 255)
			x = 255;

		return x;
	}

}
