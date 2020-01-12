import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D.Double;
import java.awt.geom.Line2D;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class MapEditor extends JFrame{

	private MapReaderWriter mapReaderWriter = new MapReaderWriter();
	public Map map;
	public MapPanel mapPanel;
	public JMenuBar menuBar;
	private boolean isSaved = true;

	// constructor
	public MapEditor(){
		this.setTitle("map editor"); // set title
		this.initMapPanel(); // init map panel
		this.initMainMenu(); // init menu bar

		this.setSize(1200,800);	// set size 
		this.setLocationRelativeTo(null); // set location
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); // set close operation
		this.setVisible(true); // set visible to be true
	}

	// main method
	public static void main(String[] args) {
		new MapEditor();
	}

	// init map panel
	public void initMapPanel(){
		// remove panel if exits
		if(this.mapPanel != null){
			this.remove(this.mapPanel);
		}
		// construct map and panel object
		this.map = new MapImpl();
		this.mapPanel = new MapPanel();
		// add listener to map
		this.map.addListener(this.mapPanel);
		// set map for panel
		this.mapPanel.setMap(this.map);
		// add panel to frame
		this.add(mapPanel);
	}

	// init menu bar
	public void initMainMenu(){
		// construct menubar
		this.menuBar = new JMenuBar();
		// file menu
		JMenu fileMenu = new JMenu("File");
		FileListener fileListener = new FileListener(this);

		// open...
		JMenuItem openItem = new JMenuItem("Open...");
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openItem.addActionListener(fileListener);
		// save...
		JMenuItem saveItem = new JMenuItem("Save as...");
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveItem.addActionListener(fileListener);
		// append...
		JMenuItem appendItem = new JMenuItem("Append...");
		appendItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		appendItem.addActionListener(fileListener);
		// quit...
		JMenuItem quitItem = new JMenuItem("Quit...");
		quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		quitItem.addActionListener(new QuitListener(this));
		// add item to file menu
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(appendItem);
		fileMenu.add(quitItem);
		// add file menu to menubar
		menuBar.add(fileMenu);

		// edit menu
		JMenu editMenu = new JMenu("Edit");
		// new place
		JMenuItem newPlaceItem = new JMenuItem("New place");
		newPlaceItem.addActionListener(new NewPlaceListener(this));
		// new road
		JMenuItem newRoadItem = new JMenuItem("New road");
		newRoadItem.addActionListener(new NewRoadListener(this));
		// set start
		JMenuItem setStartItem = new JMenuItem("Set start");
		setStartItem.addActionListener(new SetPlaceListener(this));
		// unset start
		JMenuItem unsetStartItem = new JMenuItem("Unset start");
		unsetStartItem.addActionListener(new SetPlaceListener(this));
		// set end
		JMenuItem setEndItem = new JMenuItem("Set end");
		setEndItem.addActionListener(new SetPlaceListener(this));
		// unset end
		JMenuItem unsetEndItem = new JMenuItem("Unset end");
		unsetEndItem.addActionListener(new SetPlaceListener(this));
		// delete
		JMenuItem deleteItem = new JMenuItem("Delete");

		// add items to edit menu
		editMenu.add(newPlaceItem);
		editMenu.add(newRoadItem);
		editMenu.add(setStartItem);
		editMenu.add(unsetStartItem);
		editMenu.add(setEndItem);
		editMenu.add(unsetEndItem);
		editMenu.add(deleteItem);

		// add editmenu to menu bar
		menuBar.add(editMenu);
		// set menu bar for frame
		this.setJMenuBar(menuBar);
	}

	// open file
	public void openFile(File file){
		// System.out.println("open...");
		try{
			initMapPanel();
			InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
			BufferedReader br = new BufferedReader(reader);
			this.mapReaderWriter.read(br,map);
			this.isSaved = false;
		}catch (Exception e){
			e.printStackTrace();
			popDialog(e.getMessage());
		}
	}

	// save file
	public void saveFile(File file){
		// System.out.println("save...");
		try{
			FileWriter fw = new FileWriter(file,true);
			this.mapReaderWriter.write(fw,this.map);
			this.isSaved = true;
		}catch (Exception e){
			popDialog(e.getMessage());
		}
	}

	// append file
	public void appendFile(File file){
		// System.out.println("append...");
		try{
			InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
			BufferedReader br = new BufferedReader(reader);
			this.mapReaderWriter.read(br,this.map);
			this.isSaved = false;
		}catch (Exception e){
			popDialog(e.getMessage());
		}
	}

	// pop warning dialog to show error message
	private void popDialog(String message){
		JOptionPane.showMessageDialog(this, message, "warning", JOptionPane.WARNING_MESSAGE);
	}

	// return true if map is saved, else false.
	public boolean isSaved(){
		return this.isSaved;
	}

	// Panel class
	class MapPanel extends JPanel implements MapListener{
		public Set<Place> places = new HashSet<>();
		public Set<Road> roads = new HashSet<>();
		public Set<PlaceIcon> placeIcons = new HashSet<>();
		public Set<RoadIcon> roadIcons = new HashSet<>();
		private Map map;

		public Line2D line = null;
		public boolean selectMode = false;
		public Place firstPlace = null;
		public Place secondPlace = null;
		public String newRoadName;
		public int newRoadLength;


		// constructor
		public MapPanel(){
			this.setLayout(null);
			MyMouseListener myMouseListener = new MyMouseListener();
			this.addMouseListener(myMouseListener);
			this.addMouseMotionListener(myMouseListener);
		}

		// set map for panel
		public void setMap(Map map){
			this.map = map;
		}

		// set first chosen place for panel
		public void setFirstPlace(Place p){
			this.firstPlace = p;
		}

		// set second chosen place for panel
		public void setSecondPlace(Place p){
			this.secondPlace = p;
		}

		// method to create a new road
		public void newRoad(){
			MapEditor mapEditor = (MapEditor)this.getRootPane().getParent();
			try{
				mapEditor.isSaved = false;
				this.map.newRoad(this.firstPlace, this.secondPlace, this.newRoadName, this.newRoadLength);
				this.firstPlace = null;
				this.secondPlace = null;
				this.newRoadName = null;
				this.newRoadLength = 0;
			}catch (IllegalArgumentException ex){
				mapEditor.popDialog(ex.getMessage());
			}
		}

		// method to add a place icon on panel
		public void addPlaceIcon(Place place){
    		this.places.add(place);
    		PlaceIcon placeIcon = new PlaceIcon(place, 50, 50);
    		this.add(placeIcon);
    		this.placeIcons.add(placeIcon);
			this.revalidate();
			this.repaint();
    	}

		// method to add a road icon on panel
    	public void addRoadIcon(Road road){
    		this.roads.add(road);
    		RoadIcon roadIcon =  new RoadIcon(road);
    		this.add(roadIcon);
    		this.roadIcons.add(roadIcon);
    		this.revalidate();
    		this.repaint();
    	}

		//Called whenever the number of places in the map has changed
		@Override
    	public void placesChanged(){
    		// System.out.println("places changed");
    		Set<Place> places = this.map.getPlaces();
    		Set<Place> changedPlaces = new HashSet<>();
    		Set<Place> newPlaces = new HashSet<>();
    		for(Place pFromMap : places){
    			boolean newPlaceFlag = true;
    			for(Place pFromMapEdit : this.places){
    				if(pFromMap.getName().equals(pFromMapEdit.getName())){
    					newPlaceFlag = false;
    					if(!pFromMap.equals(pFromMapEdit)){
    						changedPlaces.add(pFromMap);
    					}
    				}
    			}
    			if(newPlaceFlag){
    				// add a new place
    				newPlaces.add(pFromMap);
    			}
    		}
    		for(Place p : newPlaces){
    			addPlaceIcon(p);
    		}
    		//changed place
    		for(Place p : changedPlaces){

    		}
    	}

    	//Called whenever the number of roads in the map has changed
    	@Override
    	public void roadsChanged(){
    		Set<Road> roadsFromMap = this.map.getRoads();
    		Set<Road> changedRoads = new HashSet<>();
    		Set<Road> newRoads = new HashSet<>();
    		for(Road rFromMap : roadsFromMap){
    			boolean newRoadFlag = true;
    			for(Road rFromMapEdit : this.roads){
    				if(rFromMap.roadName().equals(rFromMapEdit.roadName()) &&
    					rFromMap.firstPlace().getName().equals(rFromMapEdit.firstPlace().getName()) &&
    					rFromMap.secondPlace().getName().equals(rFromMapEdit.secondPlace().getName())){
    					newRoadFlag = false;
    					if(!rFromMap.equals(rFromMapEdit)){
    						changedRoads.add(rFromMap);
    					}
    				}
    			}
    			if(newRoadFlag){
    				// add a new place
    				newRoads.add(rFromMap);
    			}
    		}

    		for(Road r : newRoads){
    			addRoadIcon(r);
    		}
    		//changed place
    		for(Road r : changedRoads){

    		}
    	}

    	//Called whenever something about the map has changed
    	//(other than places and roads)
    	@Override
    	public void otherChanged(){
    		// System.out.println("other changed");
    		int distance = this.map.getTripDistance();
    		if(distance == -1){
    			System.out.println("no route from start to end!");
    		}else if(distance == -2){
    			System.out.println("no start place defined!");
    		}else if(distance == -3){
    			System.out.println("no end place defined!");
    		}else{
    			System.out.println("the shortest distance is " + distance);
    		}

    		for(PlaceIcon placeIcon : this.placeIcons){
    			placeIcon.repaint();
    		}
    		for(RoadIcon roadIcon : this.roadIcons){
    			roadIcon.repaint();
    		}
    	}
	}

	// Place icon class
	class PlaceIcon extends JComponent implements PlaceListener{
		public Place place;
		public int width;
		public int height;
		public boolean isSelected;

		// constructor
		public PlaceIcon(Place place, int width, int height){
			this.place = place;
			this.width = width;
			this.height = height;
			this.setBounds(place.getX(),place.getY(),width,height);
			MyMouseListener mouseListener = new MyMouseListener();
			this.addMouseListener(mouseListener);
			this.addMouseMotionListener(mouseListener);
			place.addListener(this);
		}

		// return place which is attached to icon
		public Place getPlace(){
			return this.place;
		}

		// paint place icon
		@Override
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			this.setBounds(place.getX(),place.getY(),width,height);
			Graphics2D g2d = (Graphics2D)g.create();
			if(this.place.isStartPlace()){
				g2d.setColor(Color.RED);
				g2d.fill(new Rectangle(0,0,width,height));
				g2d.setColor(Color.WHITE);
				g2d.drawString(this.place.getName(), 0, height / 2);
			}else if(this.place.isEndPlace()){
				g2d.setColor(Color.BLUE);
				g2d.fill(new Rectangle(0,0,width,height));
				g2d.setColor(Color.WHITE);
				g2d.drawString(this.place.getName(), 0, height / 2);
			}else{
				g2d.setColor(Color.BLACK);
				g2d.draw(new Rectangle(0,0,width,height));
				g2d.drawString(this.place.getName(), 0, height / 2);
			}

			if(isSelected){
				g2d.setStroke(new BasicStroke(3.0f));
				g2d.setColor(Color.GREEN);
				g2d.draw(new Rectangle(0,0,width,height));
			}
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(width, height);
		}

		@Override
		public Point getLocation(Point rv){
			return new Point(this.place.getX(),this.place.getY());
		}

		//Called whenever the visible state of a place has changed
		@Override
    	public void placeChanged(){
    		// System.out.println("place changed");
    		this.repaint();
    	}
	}

	// Road icon class
	class RoadIcon extends JComponent implements RoadListener{
		private Road road;
		private int x1;
		private int y1;
		private int x2;
		private int y2;

		private int leftX;
		private int upY;
		private int width;
		private int height;

		// constructor
		public RoadIcon(Road road){
			this.road = road;
			this.setComponentBounds();
			this.setLine();
			this.road.addListener(this);
		}

		// set road line
		public void setLine(){
			int firstX = this.road.firstPlace().getX();
			int firstY = this.road.firstPlace().getY();
			int secondX = this.road.secondPlace().getX();
			int secondY = this.road.secondPlace().getY();
			this.x1 = firstX == this.leftX ? 0 : width;
			this.y1 = firstY == this.upY ? 0 : height;
			this.x2 = secondX == this.leftX ? 0 : width;
			this.y2 = secondY == this.upY ? 0 : height;
		}

		// set component bounds
		public void setComponentBounds(){
			int firstX = this.road.firstPlace().getX();
			int firstY = this.road.firstPlace().getY();
			int secondX = this.road.secondPlace().getX();
			int secondY = this.road.secondPlace().getY();
			int x = firstX < secondX ? firstX : secondX;
			int y = firstY < secondY ? firstY : secondY;
			int width = firstX < secondX ? (secondX - firstX) : (firstX - secondX);
			int height = firstY < secondY ? (secondY - firstY) : (firstY - secondY);

			this.leftX = x;
			this.upY = y;
			this.width = width;
			this.height = height;
			this.setBounds(x,y,width + 10,height + 10);
		}

		// paint road icon
		@Override
		public void paintComponent(Graphics g){
			// draw line
			super.paintComponent(g);
			this.setComponentBounds();
			this.setLine();
			Graphics2D g2d = (Graphics2D)g.create();
			Line2D line = new Line2D.Double(x1,y1,x2,y2);
			g2d.drawString(this.road.roadName() + this.road.length(), (x1 + x2)/2, (y1 + y2)/2);
			if(road.isChosen()){
				g2d.setColor(Color.RED);
			}else{
				g2d.setColor(Color.BLACK);
			}
			g2d.draw(line);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(width, height);
		}

		@Override
		public Point getLocation(Point rv){
			return new Point(leftX,upY);
		}

		@Override
		public void roadChanged(){
			// System.out.println("road changed");
			this.repaint();
		}
	}

	// file listener class
	class FileListener implements ActionListener{
		private MapEditor mapEditor;

		// constructor
		public FileListener(MapEditor mapEditor){
			this.mapEditor = mapEditor;
		}

		@Override
		public void actionPerformed(ActionEvent e){
			String mode = e.getActionCommand();
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int retval;
			if(mode.equals("Open...")){
				retval = fileChooser.showOpenDialog(this.mapEditor);
				if(retval == JFileChooser.APPROVE_OPTION){
					File file = fileChooser.getSelectedFile();
					this.mapEditor.openFile(file);
				}
			}else if(mode.equals("Save as...")){
				File file = new File("untitled.map");
				fileChooser.setSelectedFile(file);
				retval = fileChooser.showSaveDialog(this.mapEditor);
				if(retval == JFileChooser.APPROVE_OPTION){
					file = fileChooser.getSelectedFile();
					this.mapEditor.saveFile(file);
				}
			}else if(mode.equals("Append...")){
				retval = fileChooser.showOpenDialog(this.mapEditor);
				if(retval == JFileChooser.APPROVE_OPTION){
					File file = fileChooser.getSelectedFile();
					this.mapEditor.appendFile(file);
				}
			}
		}
	}

	// quit listener class
	class QuitListener implements ActionListener{

		private MapEditor mapEditor;

		public QuitListener(MapEditor mapEditor){
			this.mapEditor = mapEditor;
		}

		@Override
		public void actionPerformed(ActionEvent e){
			if(this.mapEditor.isSaved()){
				System.exit(0);
			}else{
				int choice = JOptionPane.showConfirmDialog(null, "map not saved, continue to exit?", "quit", JOptionPane.YES_NO_OPTION);
				if(choice == 0){
					System.exit(0);
				}
			}
		}
	}

	// set place class
	class SetPlaceListener implements ActionListener{
		private MapEditor mapEditor;

		public SetPlaceListener(MapEditor mapEditor){
			this.mapEditor = mapEditor;
		}

		@Override
		public void actionPerformed(ActionEvent e){
			String mode = e.getActionCommand();
			Set<PlaceIcon> chosenPlaceIcons = new HashSet<>();
			PlaceIcon chosenPlaceIcon = null;
			for(PlaceIcon placeIcon : this.mapEditor.mapPanel.placeIcons){
				if(placeIcon.isSelected){
					chosenPlaceIcons.add(placeIcon);
					chosenPlaceIcon = placeIcon;
				}
			}
			if(mode.equals("Unset start")){
				this.mapEditor.map.setStartPlace(null);
			}else if(mode.equals("Unset end")){
				this.mapEditor.map.setEndPlace(null);
			}else{
				if(chosenPlaceIcons.size() > 1){
					this.mapEditor.popDialog("only one place can be selected");
				}else if(chosenPlaceIcons.size() == 0){
					this.mapEditor.popDialog("no place selected");
				}else{
					if(mode.equals("Set start")){
						chosenPlaceIcon.isSelected = false;
						this.mapEditor.map.setStartPlace(chosenPlaceIcon.getPlace());
					}else if(mode.equals("Set end")){
						chosenPlaceIcon.isSelected = false;
						this.mapEditor.map.setEndPlace(chosenPlaceIcon.getPlace());
					}
				}
			}
		}
	}

	// new place class
	class NewPlaceListener implements ActionListener{
		private MapEditor mapEditor;

		public NewPlaceListener(MapEditor mapEditor){
			this.mapEditor = mapEditor;
		}

		@Override
		public void actionPerformed(ActionEvent e){
			String inputContent = JOptionPane.showInputDialog(this.mapEditor, "Place name:", "untitled");
			if(inputContent != null){
				try{
					this.mapEditor.isSaved = false;
					this.mapEditor.map.newPlace(inputContent, this.mapEditor.mapPanel.getWidth() / 2, this.mapEditor.mapPanel.getHeight() / 2);
				}catch (IllegalArgumentException ex){
					this.mapEditor.popDialog(ex.getMessage());
				}
			}
		}
	}

	// new road class
	class NewRoadListener implements ActionListener{
		private MapEditor mapEditor;
		private String roadName;
		private int roadLength;

		public NewRoadListener(MapEditor mapEditor){
			this.mapEditor = mapEditor;
		}

		@Override
		public void actionPerformed(ActionEvent e){
			JDialog dialog = new JDialog(this.mapEditor, "new road", true);
			dialog.setSize(300,200);
			dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			dialog.setLocationRelativeTo(null);
			JLabel labelForName = new JLabel("road name:");
			JTextField textFieldForName = new JTextField(8);
			JLabel labelForLength = new JLabel("road length:");
			JTextField textFieldForLength = new JTextField(8);
			JButton okButton = new JButton("Ok");
			okButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e){
					Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
					if(!textFieldForName.getText().equals("") && !textFieldForLength.getText().equals("") &&
						pattern.matcher(textFieldForLength.getText()).matches()){
						roadName = textFieldForName.getText();
						roadLength = Integer.parseInt(textFieldForLength.getText());
						dialog.dispose();
						selectPlace();
					}
					dialog.dispose();
				}
			});
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e){
					// System.out.println("cancel");
					dialog.dispose();
				}
			});
			JPanel panel = new JPanel(new GridLayout(3,3));
			panel.add(labelForName);
			panel.add(textFieldForName);
			panel.add(labelForLength);
			panel.add(textFieldForLength);
			panel.add(okButton);
			panel.add(cancelButton);
			dialog.setContentPane(panel);
			dialog.setVisible(true);
		}

		public void selectPlace(){
			this.mapEditor.mapPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
			this.mapEditor.mapPanel.selectMode = true;
			this.mapEditor.mapPanel.newRoadName = roadName;
			this.mapEditor.mapPanel.newRoadLength = roadLength;
		}
	}

	// mouse listener
	class MyMouseListener implements MouseListener, MouseMotionListener{
		private int x = 0;
		private int y = 0;
		private SelectBox selectBox = null;

		public MyMouseListener(){}

		@Override
		public void mouseClicked(MouseEvent e){
			// System.out.println("mouse clicked");
			Component component = e.getComponent();
			if(component instanceof PlaceIcon){
				PlaceIcon placeIcon = (PlaceIcon)component;
				placeIcon.isSelected = !placeIcon.isSelected;
				placeIcon.repaint();
				MapPanel mapPanel = (MapPanel)placeIcon.getParent();
				if(mapPanel.selectMode){
					//select mode
					// set
					if(mapPanel.firstPlace == null){
						mapPanel.setFirstPlace(placeIcon.getPlace());
					}else{
						mapPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						mapPanel.setSecondPlace(placeIcon.getPlace());
						mapPanel.selectMode = false;
						mapPanel.newRoad();
						for(PlaceIcon p : mapPanel.placeIcons){
							p.isSelected = false;
							p.repaint();
						}
					}
				}
			}else if(component instanceof MapPanel){
				MapPanel mapPanel = (MapPanel)component;
				for(PlaceIcon placeIcon : mapPanel.placeIcons){
					placeIcon.isSelected = false;
					if(mapPanel.selectMode && placeIcon.getPlace().equals(mapPanel.firstPlace)){
						placeIcon.isSelected = true;
					}
					placeIcon.repaint();
				}
				
			}
		}

		@Override
		public void mouseEntered(MouseEvent e){
			// System.out.println("mouse entered");
		}

		@Override
		public void mouseExited(MouseEvent e){
			// System.out.println("mouse exit");
		}

		@Override
		public void mousePressed(MouseEvent e){
			// System.out.println("mouse press");
			this.x = e.getX();
			this.y = e.getY();
			Component component = e.getComponent();
			if(component instanceof MapPanel){
				MapPanel mapPanel = (MapPanel) e.getComponent();
				this.selectBox = new SelectBox(new Rectangle(x,y,0,0));
				mapPanel.add(this.selectBox);
				this.selectBox.repaint();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e){
			// System.out.println("mouse release");
			Component component = e.getComponent();
			if(component instanceof MapPanel){
				MapPanel mapPanel = (MapPanel) component;
				mapPanel.remove(this.selectBox);
				mapPanel.repaint();
			}
		}

		@Override
		public void mouseMoved(MouseEvent e){
			// System.out.println("mouse moved");
			Component component = e.getComponent();
			if(component instanceof MapPanel){
				MapPanel mapPanel = (MapPanel) component;
				if(mapPanel.selectMode){
					// select
					if(mapPanel.firstPlace != null){
						Graphics2D g2d = (Graphics2D)mapPanel.getGraphics().create();
						if(mapPanel.line == null){
							mapPanel.line = new Line2D.Double(mapPanel.firstPlace.getX(),mapPanel.firstPlace.getY(),e.getX(),e.getY());
						}else{
							g2d.setColor(mapPanel.getBackground());
							g2d.draw(mapPanel.line);
							mapPanel.line.setLine(mapPanel.firstPlace.getX(),mapPanel.firstPlace.getY(),e.getX(),e.getY());
							for(PlaceIcon placeIcon : mapPanel.placeIcons){
								placeIcon.repaint();
							}
							for(RoadIcon roadIcon : mapPanel.roadIcons){
								roadIcon.repaint();
							}
						}
						g2d.setColor(Color.BLACK);
						g2d.draw(mapPanel.line);
					}
				}
			}
		}

		@Override
		public void mouseDragged(MouseEvent e){
			// System.out.println("mouse dragged");
			Component component = e.getComponent();
			if(component instanceof PlaceIcon){
				PlaceIcon placeIcon = (PlaceIcon) component;
				int dx = e.getX() - this.x;
				int dy = e.getY() - this.y;
				placeIcon.getPlace().moveBy(dx,dy);
			}else if(component instanceof MapPanel){
				MapPanel mapPanel = (MapPanel) component;
				this.selectBox.updateSize(e);
				for(PlaceIcon placeIcon : mapPanel.placeIcons){
					Rectangle selectBoxBound = this.selectBox.getBounds(new Rectangle());
					Rectangle placeIconBound = placeIcon.getBounds(new Rectangle());
					if(selectBoxBound.intersects(placeIconBound)){
						placeIcon.isSelected = true;
					}else{
						placeIcon.isSelected = false;
					}
					placeIcon.repaint();
				}
			}
		}
	}

	// selectBox
	class SelectBox extends JComponent{
		private int fixedX;
		private int fixedY;
		private int x;
		private int y;
		private int width;
		private int height;

		public SelectBox(Rectangle rect){
			this.fixedX = rect.x;
			this.fixedY = rect.y;
			this.x = rect.x;
			this.y = rect.y;
			this.width = rect.width;
			this.height = rect.height;
			this.setBounds(x,y,width,height);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(width, height);
		}

		@Override
		public Point getLocation(Point rv){
			int resultX = fixedX < x ? fixedX : x;
			int resultY = fixedY < y ? fixedY : y;
			return new Point(resultX, resultY);
		}

		@Override
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setColor(Color.BLACK);
			g2d.draw(new Rectangle(0,0,width,height));
		}

		public void updateSize(MouseEvent e){
			int curX = e.getX();
			int curY = e.getY();
			int leftX;
			int upY;
			if(curX < this.fixedX){
				leftX = curX;
				this.x = curX;
				this.width = this.fixedX - curX;
			}else{
				leftX = this.fixedX;
				this.width = curX - this.fixedX;
			}

			if(curY < this.fixedY){
				upY = curY;
				this.y = curY;
				this.height = this.fixedY - curY;
			}else{
				upY = this.fixedY;
				this.height = curY - this.fixedY;
			}
			this.setBounds(leftX, upY, this.width, this.height);
			repaint();
		}
	}


}