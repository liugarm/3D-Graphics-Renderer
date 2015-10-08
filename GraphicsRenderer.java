import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GraphicsRenderer {

	//Window areas
	private JFrame frame;
	private JComponent drawing;
	private JPanel buttonPanel;
	private JLabel info;

	//Boolean to check if a file has been read in or not
	private boolean hasFile = false;

	//Polygons list
	private List<Polygon> polygons = new ArrayList<Polygon>();

	//Light
	private List<Light> lights = new ArrayList<Light>();
	private int selectedLightNo = -1;

	private Pixel polygonCentrePoint;
	private Pixel screenCentrePoint = new Pixel(350, 300, 0);
	private Pixel offset;

	//Ambient Lighting
	private float ambient = 0.1f;

	//Colour Sliders
	private JSlider red;
	private JSlider green;
	private JSlider blue;

	//Keys
	private boolean upPressed = false;
	private boolean downPressed = false;
	private boolean leftPressed = false;
	private boolean rightPressed = false;
	private boolean wPressed = false;
	private boolean aPressed = false;
	private boolean sPressed = false;
	private boolean dPressed = false;

	//Bounding box of model
	private int boundingX;
	private int boundingY;
	private int boundingZ;

	//buffer
	private static Color[][] screen = new Color[700][600];
	private static float[][] zBuffer = new float[700][600];

	//rotating amount
	private double rotateAmountX = 0.0;
	private double rotateAmountY = 0.0;

	public GraphicsRenderer() {
		setupWindow();
	}

	@SuppressWarnings("unchecked")
	public void setupWindow() {
		//********************************************************
		// Setup the graphics window
		//********************************************************
		frame = new JFrame("3D Graphics Renderer");
		frame.setSize(700, 700);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setFocusable(true);

		//********************************************************
		// Setup the drawing window
		//********************************************************
		drawing = new JComponent() {
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				redraw(g);
			}
		};

		frame.add(drawing, BorderLayout.CENTER);
		drawing.setBorder(BorderFactory.createLoweredBevelBorder());
		drawing.setFocusable(true);

		//********************************************************
		// Buttons Panel
		//********************************************************
		buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.setPreferredSize(new Dimension(700, 90));
		buttonPanel.setFocusable(false);
		frame.add(buttonPanel, BorderLayout.NORTH);

		JPanel buttonPanelTop = new JPanel();
		buttonPanelTop.setPreferredSize(new Dimension(700, 35));
		buttonPanelTop.setFocusable(false);
		buttonPanel.add(buttonPanelTop, BorderLayout.NORTH);

		JPanel buttonPanelBottom = new JPanel(new BorderLayout());
		buttonPanelBottom.setPreferredSize(new Dimension(700, 50));
		buttonPanelBottom.setFocusable(false);
		buttonPanel.add(buttonPanelBottom, BorderLayout.SOUTH);

		//********************************************************
		// Buttons to change RGB on lights
		//********************************************************

		red = new JSlider(JSlider.HORIZONTAL, 0, 255, 0);
		red.setFocusable(false);
		buttonPanelBottom.add(red, BorderLayout.WEST);
		red.setMajorTickSpacing(10);
		red.setMinorTickSpacing(1);
		red.setPaintTicks(true);

		//Create the label table
		Hashtable labelTable = new Hashtable();
		labelTable.put(new Integer(0), new JLabel("0"));
		labelTable.put(new Integer(255 / 2), new JLabel("Red"));
		labelTable.put(new Integer(255), new JLabel("255"));
		red.setLabelTable(labelTable);
		red.setPaintLabels(true);

		red.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (hasFile && !source.getValueIsAdjusting()) {
					int value = (int) source.getValue();
					
					lights.get(selectedLightNo).setRed(value);

					//Find centre
					polygonCentrePoint = findCentre();
					offset = findOffsets();

					drawing.repaint();
				}
			}

		});

		green = new JSlider(JSlider.HORIZONTAL, 0, 255, 0);
		green.setFocusable(false);
		buttonPanelBottom.add(green, BorderLayout.CENTER);
		green.setMajorTickSpacing(10);
		green.setMinorTickSpacing(1);
		green.setPaintTicks(true);

		//Create the label table
		Hashtable labelTable2 = new Hashtable();
		labelTable2.put(new Integer(0), new JLabel("0"));
		labelTable2.put(new Integer(255 / 2), new JLabel("Green"));
		labelTable2.put(new Integer(255), new JLabel("255"));
		green.setLabelTable(labelTable2);
		green.setPaintLabels(true);

		green.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (hasFile && !source.getValueIsAdjusting()) {
					int value = (int) source.getValue();
					
					lights.get(selectedLightNo).setGreen(value);

					//Find centre
					polygonCentrePoint = findCentre();
					offset = findOffsets();

					drawing.repaint();
				}
			}

		});

		blue = new JSlider(JSlider.HORIZONTAL, 0, 255, 0);
		blue.setFocusable(false);
		buttonPanelBottom.add(blue, BorderLayout.EAST);
		blue.setMajorTickSpacing(10);
		blue.setMinorTickSpacing(1);
		blue.setPaintTicks(true);

		//Create the label table
		Hashtable labelTable3 = new Hashtable();
		labelTable3.put(new Integer(0), new JLabel("0"));
		labelTable3.put(new Integer(255 / 2), new JLabel("Blue"));
		labelTable3.put(new Integer(255), new JLabel("255"));
		blue.setLabelTable(labelTable3);
		blue.setPaintLabels(true);

		blue.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (hasFile && !source.getValueIsAdjusting()) {
					int value = (int) source.getValue();
					
					lights.get(selectedLightNo).setBlue(value);

					//Find centre
					polygonCentrePoint = findCentre();
					offset = findOffsets();

					drawing.repaint();
				}
			}

		});

		//********************************************************
		// Buttons to Select/Add/Remove lights
		//********************************************************
		JButton selectLight = new JButton("Select Light");
		selectLight.setFocusable(false);
		buttonPanelTop.add(selectLight, BorderLayout.SOUTH);
		selectLight.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (hasFile) {
					if (selectedLightNo + 1 < lights.size()) {
						selectedLightNo++;
					} else {
						selectedLightNo = 0;
					}
					
					red.setValue(lights.get(selectedLightNo).getRed());
					green.setValue(lights.get(selectedLightNo).getGreen());
					blue.setValue(lights.get(selectedLightNo).getBlue());

					info.setText(" Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 100.0) / 100.0 + "      Ambient: " + Math.round(ambient * 100.0) / 100.0 + "      No. of Lights: " + lights.size() + "      Light Selected: " + (selectedLightNo + 1) + "      Light Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 10.0) / 10.0 + "      Viewing Dir: (" + Math.round(rotateAmountX * 10.0) / 10.0 + "," + Math.round(rotateAmountY * 10.0) / 10.0 + ")");
				}
			}

		});

		JButton addLight = new JButton("Add Light");
		addLight.setFocusable(false);
		buttonPanelTop.add(addLight, BorderLayout.SOUTH);
		addLight.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (lights.size() > 0) {
					//add a light
					Vector3D newLight = newLightRotation(lights.get(0).getLight());
					lights.add(new Light(newLight));
					selectedLightNo++;
					info.setText(" Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 100.0) / 100.0 + "      Ambient: " + Math.round(ambient * 100.0) / 100.0 + "      No. of Lights: " + lights.size() + "      Light Selected: " + (selectedLightNo + 1) + "      Light Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 10.0) / 10.0 + "      Viewing Dir: (" + Math.round(rotateAmountX * 10.0) / 10.0 + "," + Math.round(rotateAmountY * 10.0) / 10.0 + ")");

					red.setValue(lights.get(selectedLightNo).getRed());
					green.setValue(lights.get(selectedLightNo).getGreen());
					blue.setValue(lights.get(selectedLightNo).getBlue());
					
					//Find centre
					polygonCentrePoint = findCentre();
					offset = findOffsets();

					drawing.repaint();
				}
			}

		});

		JButton removeLight = new JButton("Remove Light");
		removeLight.setFocusable(false);
		buttonPanelTop.add(removeLight, BorderLayout.SOUTH);
		removeLight.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (lights.size() > 1) {
					lights.remove(lights.size() - 1);
					if (selectedLightNo >= lights.size()) {
						selectedLightNo--;
						
						red.setValue(lights.get(selectedLightNo).getRed());
						green.setValue(lights.get(selectedLightNo).getGreen());
						blue.setValue(lights.get(selectedLightNo).getBlue());

					}
					info.setText(" Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 100.0) / 100.0 + "      Ambient: " + Math.round(ambient * 100.0) / 100.0 + "      No. of Lights: " + lights.size() + "      Light Selected: " + (selectedLightNo + 1) + "      Light Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 10.0) / 10.0 + "      Viewing Dir: (" + Math.round(rotateAmountX * 10.0) / 10.0 + "," + Math.round(rotateAmountY * 10.0) / 10.0 + ")");

					//Find centre
					polygonCentrePoint = findCentre();
					offset = findOffsets();

					drawing.repaint();
				}
			}

		});

		JButton reset = new JButton("Reset");
		reset.setFocusable(false);
		buttonPanelTop.add(reset, BorderLayout.SOUTH);
		reset.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ambient = 0.1f;
				lights.get(selectedLightNo).setIntensity(1.0f);

				red.setValue(0);
				green.setValue(0);
				blue.setValue(0);

				if (hasFile) {
					for (Light l : lights) {
						l.setIntensity(1.0f);
						l.setRed(0);
						l.setGreen(0);
						l.setBlue(0);
					}

					info.setText(" Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 100.0) / 100.0 + "      Ambient: " + Math.round(ambient * 100.0) / 100.0 + "      No. of Lights: " + lights.size() + "      Light Selected: " + (selectedLightNo + 1) + "      Light Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 10.0) / 10.0 + "      Viewing Dir: (" + Math.round(rotateAmountX * 10.0) / 10.0 + "," + Math.round(rotateAmountY * 10.0) / 10.0 + ")");

					//Find centre
					polygonCentrePoint = findCentre();
					offset = findOffsets();

					drawing.repaint();
				}
			}

		});

		/* ********************* Bottom Panel Information ********************* */

		//Displaying the information
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setFocusable(false);
		bottomPanel.setPreferredSize(new Dimension(700, 35));
		frame.add(bottomPanel, BorderLayout.SOUTH);

		info = new JLabel();
		info.setFocusable(false);
		info.setText(" Intensity: " + "0.0" + "      Ambient: " + Math.round(ambient * 100.0) / 100.0 + "      No. of Lights: " + lights.size() + "      Light Selected: " + (selectedLightNo + 1) + "      Light Intensity: 0" + "      Viewing Dir: (" + Math.round(rotateAmountX * 10.0) / 10.0 + "," + Math.round(rotateAmountY * 10.0) / 10.0 + ")");
		bottomPanel.add(info, BorderLayout.NORTH);

		//Keys
		JLabel keys = new JLabel();
		keys.setFocusable(false);
		keys.setText(" Rotate: Arrow Keys   Ambient: +/-   Intensity: [/]   Light Direction: W/A/S/D");
		bottomPanel.add(keys, BorderLayout.SOUTH);

		/* ********************* Open File Button ********************* */
		JButton open = new JButton("Open");
		open.setFocusable(false);
		buttonPanelTop.add(open, BorderLayout.NORTH);

		open.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//Ask the user to select a file
				JFileChooser read = new JFileChooser();
				read.setFileSelectionMode(JFileChooser.FILES_ONLY);
				read.setAcceptAllFileFilterUsed(false);
				read.setDialogTitle("Select File");
				int returnVal = read.showOpenDialog(null);

				File file = read.getSelectedFile();

				//*****************************************
				// Reading File
				//*****************************************
				if (returnVal == JFileChooser.APPROVE_OPTION && file.exists()) {
					//Resetting the information
					polygons.clear();
					lights.clear();
					hasFile = false;
					selectedLightNo = -1;
					rotateAmountX = 0.0;
					rotateAmountY = 0.0;
					red.setValue(0);
					green.setValue(0);
					blue.setValue(0);

					try {
						BufferedReader scan = new BufferedReader(new FileReader(file));

						int lineNum = 0;

						while (true) {
							String line = scan.readLine();

							if (line == null) {
								break;
							}

							String lineTrimmed = line.trim();
							String[] sArray = lineTrimmed.split("\\s", -1);

							if (lineNum == 0) {
								Vector3D light = new Vector3D(Float.parseFloat(sArray[0]), Float.parseFloat(sArray[1]), Float.parseFloat(sArray[2]));
								lights.add(new Light(light));
							} else {
								Vector3D point = new Vector3D(Float.parseFloat(sArray[0]), Float.parseFloat(sArray[1]), Float.parseFloat(sArray[2]));
								Vector3D point2 = new Vector3D(Float.parseFloat(sArray[3]), Float.parseFloat(sArray[4]), Float.parseFloat(sArray[5]));
								Vector3D point3 = new Vector3D(Float.parseFloat(sArray[6]), Float.parseFloat(sArray[7]), Float.parseFloat(sArray[8]));

								Polygon polygon = new Polygon(point, point2, point3, Integer.parseInt(sArray[9]), Integer.parseInt(sArray[10]), Integer.parseInt(sArray[11]));

								polygons.add(polygon);
							}

							lineNum++;
						}

					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}

					hasFile = true;

					//Rotate the model 360 degrees and check if it can fit into the window all the way round. If not, keep rescaling
					//until it fits the screen properly.
					for (int i = 0; i < 360; i += 40) {
						//Find centre
						polygonCentrePoint = findCentre();
						offset = findOffsets();

						rotatePolygonsX((float) Math.toRadians(i));

						if (boundingX > 700 || boundingY > 600) {
							rescalePolygons();
						}
					}

					//Find centre
					polygonCentrePoint = findCentre();
					offset = findOffsets();

					if (boundingX > 700 || boundingY > 600) {
						rescalePolygons();
					}

					polygonCentrePoint = findCentre();
					offset = findOffsets();

					selectedLightNo = 0;

					info.setText(" Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 100.0) / 100.0 + "      Ambient: " + Math.round(ambient * 100.0) / 100.0 + "      No. of Lights: " + lights.size() + "      Light Selected: " + (selectedLightNo + 1) + "      Light Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 10.0) / 10.0 + "      Viewing Dir: (" + Math.round(rotateAmountX * 10.0) / 10.0 + "," + Math.round(rotateAmountY * 10.0) / 10.0 + ")");
					drawing.repaint();
				}
			}

		});

		/* ********************* Save Image ********************* */
		JButton save = new JButton("Save");
		save.setFocusable(false);
		buttonPanelTop.add(save, BorderLayout.NORTH);

		save.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (hasFile) {
					renderZBuffer();
					saveImage(screen, "Image.png");
				}
			}

		});

		/* ********************* Exit Button ********************* */
		JButton exit = new JButton("Exit");
		exit.setFocusable(false);
		buttonPanelTop.add(exit, BorderLayout.NORTH);

		exit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}

		});

		//********************************************************
		// Set the window to be visible so it appears
		//********************************************************
		frame.setVisible(true);

		//*******************************************************
		// Key Listener
		//*******************************************************
		frame.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();

				switch (keyCode) {
				case KeyEvent.VK_UP:
					upPressed = true;
					break;
				case KeyEvent.VK_DOWN:
					downPressed = true;
					break;
				case KeyEvent.VK_LEFT:
					leftPressed = true;
					break;
				case KeyEvent.VK_RIGHT:
					rightPressed = true;
					break;
				case KeyEvent.VK_W:
					wPressed = true;
					break;
				case KeyEvent.VK_S:
					sPressed = true;
					break;
				case KeyEvent.VK_A:
					aPressed = true;
					break;
				case KeyEvent.VK_D:
					dPressed = true;
					break;
				case KeyEvent.VK_CLOSE_BRACKET:
					lights.get(selectedLightNo).setIntensity(lights.get(selectedLightNo).getIntensity() + 0.05f);
					info.setText(" Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 100.0) / 100.0 + "      Ambient: " + Math.round(ambient * 100.0) / 100.0 + "      No. of Lights: " + lights.size() + "      Light Selected: " + (selectedLightNo + 1) + "      Light Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 10.0) / 10.0 + "      Viewing Dir: (" + Math.round(rotateAmountX * 10.0) / 10.0 + "," + Math.round(rotateAmountY * 10.0) / 10.0 + ")");
					drawing.repaint();
					break;
				case KeyEvent.VK_OPEN_BRACKET:
					lights.get(selectedLightNo).setIntensity(lights.get(selectedLightNo).getIntensity() - 0.05f);
					info.setText(" Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 100.0) / 100.0 + "      Ambient: " + Math.round(ambient * 100.0) / 100.0 + "      No. of Lights: " + lights.size() + "      Light Selected: " + (selectedLightNo + 1) + "      Light Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 10.0) / 10.0 + "      Viewing Dir: (" + Math.round(rotateAmountX * 10.0) / 10.0 + "," + Math.round(rotateAmountY * 10.0) / 10.0 + ")");
					drawing.repaint();
					break;
				case KeyEvent.VK_EQUALS:
					ambient += 0.05;
					info.setText(" Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 100.0) / 100.0 + "      Ambient: " + Math.round(ambient * 100.0) / 100.0 + "      No. of Lights: " + lights.size() + "      Light Selected: " + (selectedLightNo + 1) + "      Light Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 10.0) / 10.0 + "      Viewing Dir: (" + Math.round(rotateAmountX * 10.0) / 10.0 + "," + Math.round(rotateAmountY * 10.0) / 10.0 + ")");
					drawing.repaint();
					break;
				case KeyEvent.VK_MINUS:
					ambient -= 0.05;
					info.setText(" Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 100.0) / 100.0 + "      Ambient: " + Math.round(ambient * 100.0) / 100.0 + "      No. of Lights: " + lights.size() + "      Light Selected: " + (selectedLightNo + 1) + "      Light Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 10.0) / 10.0 + "      Viewing Dir: (" + Math.round(rotateAmountX * 10.0) / 10.0 + "," + Math.round(rotateAmountY * 10.0) / 10.0 + ")");
					drawing.repaint();
					break;
				}

				keysPressed();
			}

			@Override
			public void keyReleased(KeyEvent e) {
				int keyCode = e.getKeyCode();
				switch (keyCode) {
				case KeyEvent.VK_UP:
					upPressed = false;
					break;
				case KeyEvent.VK_DOWN:
					downPressed = false;
					break;
				case KeyEvent.VK_LEFT:
					leftPressed = false;
					break;
				case KeyEvent.VK_RIGHT:
					rightPressed = false;
					break;
				case KeyEvent.VK_W:
					wPressed = false;
					break;
				case KeyEvent.VK_S:
					sPressed = false;
					break;
				case KeyEvent.VK_A:
					aPressed = false;
					break;
				case KeyEvent.VK_D:
					dPressed = false;
					break;
				}

				info.setText(" Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 100.0) / 100.0 + "      Ambient: " + Math.round(ambient * 100.0) / 100.0 + "      No. of Lights: " + lights.size() + "      Light Selected: " + (selectedLightNo + 1) + "      Light Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 10.0) / 10.0 + "      Viewing Dir: (" + Math.round(rotateAmountX * 10.0) / 10.0 + "," + Math.round(rotateAmountY * 10.0) / 10.0 + ")");

			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

		});
	}

	public void redraw(Graphics g) {
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, 700, 600);

		for (Polygon p : polygons) {
			p.recentre(offset);
		}
		renderZBuffer();

		for (Polygon p : polygons) {
			p.hiddenPolygon();
			if (!p.getHidden()) {
				p.draw(g, ambient, lights.get(selectedLightNo).getIntensity(), screen);
			}
		}
	}

	public Pixel findCentre() {
		int lowestX = Integer.MAX_VALUE;
		int lowestY = Integer.MAX_VALUE;
		int highestX = Integer.MIN_VALUE;
		int highestY = Integer.MIN_VALUE;
		int lowestZ = Integer.MAX_VALUE;
		int highestZ = Integer.MAX_VALUE;

		int centreX = 0;
		int centreY = 0;
		int centreZ = 0;

		for (Polygon p : polygons) {
			if (p.getPointOne().x < lowestX) {
				lowestX = (int) p.getPointOne().x;
			}
			if (p.getPointOne().x > highestX) {
				highestX = (int) p.getPointOne().x;
			}
			if (p.getPointOne().y < lowestY) {
				lowestY = (int) p.getPointOne().y;
			}
			if (p.getPointOne().y > highestY) {
				highestY = (int) p.getPointOne().y;
			}
			if (p.getPointOne().z < lowestZ) {
				lowestZ = (int) p.getPointOne().z;
			}
			if (p.getPointOne().z > highestZ) {
				highestZ = (int) p.getPointOne().z;
			}

			if (p.getPointTwo().x < lowestX) {
				lowestX = (int) p.getPointTwo().x;
			}
			if (p.getPointTwo().x > highestX) {
				highestX = (int) p.getPointTwo().x;
			}
			if (p.getPointTwo().y < lowestY) {
				lowestY = (int) p.getPointTwo().y;
			}
			if (p.getPointTwo().y > highestY) {
				highestY = (int) p.getPointTwo().y;
			}
			if (p.getPointTwo().z < lowestZ) {
				lowestZ = (int) p.getPointTwo().z;
			}
			if (p.getPointTwo().z > highestZ) {
				highestZ = (int) p.getPointTwo().z;
			}

			if (p.getPointThree().x < lowestX) {
				lowestX = (int) p.getPointThree().x;
			}
			if (p.getPointThree().x > highestX) {
				highestX = (int) p.getPointThree().x;
			}
			if (p.getPointThree().y < lowestY) {
				lowestY = (int) p.getPointThree().y;
			}
			if (p.getPointThree().y > highestY) {
				highestY = (int) p.getPointThree().y;
			}
			if (p.getPointThree().z < lowestZ) {
				lowestZ = (int) p.getPointThree().z;
			}
			if (p.getPointThree().z > highestZ) {
				highestZ = (int) p.getPointThree().z;
			}
		}

		centreX = (highestX + lowestX) / 2;
		centreY = (highestY + lowestY) / 2;
		centreZ = (highestZ + lowestZ) / 2;

		Pixel centrePoint = new Pixel(centreX, centreY, centreZ);

		//Find the bounding box e.g highest-lowest values and see if it's bigger than window size (700x600)
		boundingX = highestX - lowestX;
		boundingY = highestY - lowestY;
		boundingZ = highestZ - lowestZ;

		return centrePoint;
	}

	public Pixel findOffsets() {
		int offsetX = 0;
		int offsetY = 0;
		int offsetZ = 0;

		offsetX = screenCentrePoint.getX() - polygonCentrePoint.getX();
		offsetY = screenCentrePoint.getY() - polygonCentrePoint.getY();
		offsetZ = screenCentrePoint.getZ() - polygonCentrePoint.getZ();

		Pixel offset = new Pixel(offsetX, offsetY, offsetZ);

		return offset;
	}

	public void rotatePolygonsX(float value) {
		for (Polygon p : polygons) {
			p.rotateX(value);
		}
		rotateAllLightX(value);
	}

	public void rotatePolygonsY(float value) {
		for (Polygon p : polygons) {
			p.rotateY(value);
		}
		rotateAllLightY(value);
	}

	public Vector3D newLightRotation(Vector3D l) {
		Transform rotateX = Transform.newXRotation(2.0f);
		Transform rotateY = Transform.newYRotation(2.0f);

		Vector3D newLight = l;
		newLight = rotateX.multiply(newLight);
		newLight = rotateY.multiply(newLight);

		return newLight;
	}

	public void rotateAllLightX(float value) {
		Transform rotate = Transform.newXRotation(value);

		for (Light l : lights) {
			l.setLight(rotate.multiply(l.getLight()));
		}
	}

	public void rotateAllLightY(float value) {
		Transform rotate = Transform.newYRotation(value);

		for (Light l : lights) {
			l.setLight(rotate.multiply(l.getLight()));
		}
	}

	public void rotateLightX(float value) {
		Transform rotate = Transform.newXRotation(value);
		lights.get(selectedLightNo).setLight(rotate.multiply(lights.get(selectedLightNo).getLight()));
	}

	public void rotateLightY(float value) {
		Transform rotate = Transform.newYRotation(value);
		lights.get(selectedLightNo).setLight(rotate.multiply(lights.get(selectedLightNo).getLight()));
	}

	public void rescalePolygons() {
		int average = (boundingX + boundingY) / 2;
		float ratio = ((float) 900) / average;

		for (Polygon p : polygons) {
			p.rescale(polygonCentrePoint, ratio / 2, ratio / 2, ratio / 2);
		}
	}

	private void renderZBuffer() {
		for (int i = 0; i < 700; i++) {
			for (int j = 0; j < 600; j++) {
				screen[i][j] = Color.GRAY;
				zBuffer[i][j] = Integer.MAX_VALUE;
			}
		}

		for (int i = 0; i < polygons.size(); i++) {
			Polygon p = polygons.get(i);
			p.hiddenPolygon();

			if (!p.getHidden()) {

				p.shading(lights, ambient);

				EdgeList[] edgeList = p.edgeList();
				int minY = p.getMinY();
				Color c = p.getShading();

				for (int j = 0; j < edgeList.length; j++) {
					if (edgeList[j] != null) {
						int y = minY + j;
						int x = Math.round(edgeList[j].getLeftX());
						float z = edgeList[j].getLeftZ();
						float mz = (edgeList[j].getRightZ() - edgeList[j].getLeftZ()) / (edgeList[j].getRightX() - edgeList[j].getLeftX());

						while (x <= Math.round(edgeList[j].getRightX())) {
							if (x > 0 && y > 0 && x < 700 && y < 600 && z < zBuffer[x][y]) {
								if (x < 700 && y < 600 && x >= 0 && y >= 0) {
									zBuffer[x][y] = z;
									screen[x][y] = c;
								}
							}
							x++;
							z += mz;
						}
					}
				}
			}
		}
	}

	public void saveImage(Color[][] colours, String filename) {
		BufferedImage image = new BufferedImage(700, 600, BufferedImage.TYPE_INT_RGB);

		for (int i = 0; i < 700; i++) {
			for (int j = 0; j < 600; j++) {
				image.setRGB(i, j, colours[i][j].getRGB());
			}
		}

		try {
			ImageIO.write(image, "png", new File(filename));
		} catch (IOException e) {
			System.out.println("Error saving image: " + e);
		}
	}

	public void keysPressed() {
		if (upPressed) {
			rotatePolygonsX(-0.1f);
			rotateAmountY = rotateAmountY - Math.toDegrees(0.1f);
			System.out.println("Y :" + Math.toRadians(rotateAmountY));
		}
		if (downPressed) {
			rotatePolygonsX(0.1f);
			rotateAmountY = rotateAmountY + Math.toDegrees(0.1f);
			System.out.println("Y: " + Math.toRadians(rotateAmountY));
		}
		if (leftPressed) {
			rotatePolygonsY(0.1f);
			rotateAmountX = rotateAmountX + Math.toDegrees(0.1f);
			System.out.println("X: " + Math.toRadians(rotateAmountX));
		}
		if (rightPressed) {
			rotatePolygonsY(-0.1f);
			rotateAmountX = rotateAmountX - Math.toDegrees(0.1f);
			System.out.println("X: " + Math.toRadians(rotateAmountX));
		}
		if (wPressed) {
			rotateLightX(-0.1f);
		}
		if (sPressed) {
			rotateLightX(0.1f);
		}
		if (aPressed) {
			rotateLightY(0.1f);
		}
		if (dPressed) {
			rotateLightY(-0.1f);
		}

		info.setText(" Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 100.0) / 100.0 + "      Ambient: " + Math.round(ambient * 100.0) / 100.0 + "      No. of Lights: " + lights.size() + "      Light Selected: " + (selectedLightNo + 1) + "      Light Intensity: " + Math.round(lights.get(selectedLightNo).getIntensity() * 10.0) / 10.0 + "      Viewing Dir: (" + Math.round(rotateAmountX * 10.0) / 10.0 + "," + Math.round(rotateAmountY * 10.0) / 10.0 + ")");

		//Find centre
		polygonCentrePoint = findCentre();
		offset = findOffsets();

		drawing.repaint();
	}

}
