// WindowInventory.java by Matt Fritz
// November 29, 2009
// Handles the "Stuff" (Inventory) window

package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicScrollBarUI;

import roomviewer.RoomViewerGrid;

import util.AppletResourceLoader;
import util.InventoryInfo;
import util.InventoryItem;
import util.RatingSystem;
import util.StaticAppletData;

public class WindowInventory extends JPanel
{
	Font textFont;
	Font textFontBold;
	
	private RoomViewerGrid gridObject;
	
	private int x = 0;
	private int y = 0;
	
	private int width = 439;
	private int height = 397;
	private ImageIcon windowImage = AppletResourceLoader.getImageFromJar("img/ui/inventory.png");
	private ImageIcon inventorySquare = AppletResourceLoader.getImageFromJar("img/ui/inventory_square.png");
	private ImageIcon inventorySquareHighlight = AppletResourceLoader.getImageFromJar("img/ui/inventory_square_highlight.png");
	private ImageIcon inventoryCardImage = AppletResourceLoader.getImageFromJar("img/furniture/card_template.png");
	private ImageIcon wearImageOff = AppletResourceLoader.getImageFromJar("img/ui/inventory_wear_off.png");
	private ImageIcon wearImageOn = AppletResourceLoader.getImageFromJar("img/ui/inventory_wear_on.png");
	private ImageIcon takeOffImageOff = AppletResourceLoader.getImageFromJar("img/ui/inventory_take_off_off.png");
	private ImageIcon takeOffImageOn = AppletResourceLoader.getImageFromJar("img/ui/inventory_take_off_on.png");
	private ImageIcon sellImageOff = AppletResourceLoader.getImageFromJar("img/ui/inventory_sell_off.png");
	private ImageIcon sellImageOn = AppletResourceLoader.getImageFromJar("img/ui/inventory_sell_on.png");
	
	// structure to hold a player's inventory items
	private ArrayList<InventoryItem> inventoryItems = new ArrayList<InventoryItem>();
	private int furnitureCount = 0;
	private int pinCount = 0;
	private int posterCount = 0;
	
	private JLabel inventoryNameLabel = new JLabel("");
	private JLabel inventorySquareHighlightLabel = new JLabel(inventorySquareHighlight);
	private JPanel inventoryPinsPanel = new JPanel();
	private JLabel inventoryCardDisplayLabel = new JLabel(inventoryCardImage);
	private JScrollPane inventoryPinsScrollPane;
	
	private JPanel pinsWornPanel = new JPanel();
	private JLabel pinsWornHighlightLabel = new JLabel(inventorySquareHighlight);
	private InventoryInfo pinsWorn[];
	
	private JLabel wearPinButton = new JLabel(wearImageOff);
	private JLabel takeOffButton = new JLabel(takeOffImageOff);
	private JLabel sellButton = new JLabel(sellImageOff);
	
	private InventoryPinSquare selectedPin = null;
	
	private JPanel inventoryFurniturePanel = new JPanel();
	private JScrollPane inventoryFurnitureScrollPane;
	private ImageIcon furnitureWindowImage = AppletResourceLoader.getImageFromJar("img/ui/furniture.png");
	
	private final int INVENTORY_PANEL_WIDTH = 262;
	
	private final int ITEMS_PER_ROW = 6;
	private final int INVENTORY_SQUARE_SPACING = 2;
	private final int PIN_INV_OFFSET_LEFT = 24;
	private final int PIN_INV_OFFSET_TOP = 98;
	
	private final int MAX_WORN_PINS = 8;
	private int originalPinRows[] = new int[MAX_WORN_PINS];
	private int originalPinCols[] = new int[MAX_WORN_PINS];
	private int originalPinIndices[] = new int[MAX_WORN_PINS];
	
	private JLabel creditsLabel = new JLabel("");
	private ImageIcon creditsWindowImage = AppletResourceLoader.getImageFromJar("img/ui/credits.png");
	
	private JLabel backgroundLabel = new JLabel(windowImage);
	
	private WindowInventory messagesWindow;
	private Rectangle titleRectangle = new Rectangle(52, 10, 366, 35);
	private Rectangle tabPinsRectangle = new Rectangle(173, 46, 38, 22);
	private Rectangle tabFurnitureRectangle = new Rectangle(86, 46, 78, 21);
	private Rectangle tabCreditRectangle = new Rectangle(35, 45, 47, 21);
	private Rectangle exitRectangle = new Rectangle(407, 9, 15, 16);
	
	private Rectangle wearPinRectangle = new Rectangle(362, 233, 51, 19);
	private Rectangle takeOffRectangle = new Rectangle(288, 352, 105, 19);
	private Rectangle sellRectangle = new Rectangle(309, 232, 51, 20);
	
	private long firstClick = 0; // first click in milliseconds
	private final long DOUBLE_CLICK_TIME = 500; // time in milliseconds for a double-click
	
	public WindowInventory(Font textFont, Font textFontBold, int x, int y)
	{
		this.textFont = textFont;
		this.textFontBold = textFontBold;
		this.x = x;
		this.y = y;
		
		loadWindowInventory();
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
	}
	
	public void update(Graphics g)
	{
		paintComponent(g);
	}
	
	private void loadWindowInventory()
	{
		// turn off double-buffering and set the opacity to "false"
		// required for image transparency on the window
		setDoubleBuffered(false);
		setOpaque(false);
		
		this.setLayout(null);
		
		// ==========================================
		// PINS TAB
		// ==========================================
		
		// add the inventory name bar
		inventoryNameLabel.setBounds(45, 72, 345, 16);
		inventoryNameLabel.setBackground(new Color(40, 84, 146));
		inventoryNameLabel.setForeground(Color.white);
		inventoryNameLabel.setFont(textFont);
		inventoryNameLabel.setVerticalAlignment(JLabel.TOP);
		inventoryNameLabel.setHorizontalAlignment(JLabel.CENTER);
		add(inventoryNameLabel);
		
		// add the card display label
		inventoryCardDisplayLabel.setBounds(307, 98, 109, 133);
		add(inventoryCardDisplayLabel);
		
		// panel that holds the pin inventory
		inventoryPinsPanel.setLayout(null);
		inventoryPinsPanel.setBackground(new Color(40, 84, 146));
		
		// add the panel to the scroll pane
		inventoryPinsScrollPane = new JScrollPane(inventoryPinsPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		inventoryPinsScrollPane.getVerticalScrollBar().setUI(new MyScrollBarUI());
		inventoryPinsScrollPane.setBorder(null);
		inventoryPinsScrollPane.setBounds(PIN_INV_OFFSET_LEFT, PIN_INV_OFFSET_TOP, INVENTORY_PANEL_WIDTH + 18, 175);
		add(inventoryPinsScrollPane);
		
		// button that says "Wear"
		wearPinButton.setBounds(362, 233, 51, 19);
		add(wearPinButton);
		
		// button that says "Take Off"
		takeOffButton.setBounds(288, 352, 105, 19);
		add(takeOffButton);
		
		// button that says "Sell"
		sellButton.setBounds(sellRectangle);
		add(sellButton);
		
		// panel that holds the Pins Worn section
		pinsWornPanel.setLayout(null);
		pinsWornPanel.setBackground(new Color(0, 28, 86));
		pinsWornPanel.setBounds(42, 304, 365, 48);
		add(pinsWornPanel);
		
		// ==========================================
		// FURNITURE TAB
		// ==========================================
		
		// panel that holds the pin inventory
		inventoryFurniturePanel.setLayout(null);
		inventoryFurniturePanel.setBackground(new Color(40, 84, 146));
		
		// add the panel to the scroll pane
		inventoryFurnitureScrollPane = new JScrollPane(inventoryFurniturePanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		inventoryFurnitureScrollPane.getVerticalScrollBar().setUI(new MyScrollBarUI());
		inventoryFurnitureScrollPane.setBorder(null);
		inventoryFurnitureScrollPane.setBounds(PIN_INV_OFFSET_LEFT, PIN_INV_OFFSET_TOP, INVENTORY_PANEL_WIDTH + 18, 175);
		inventoryFurnitureScrollPane.setVisible(false);
		add(inventoryFurnitureScrollPane);
		
		// ==========================================
		// CREDIT TAB
		// ==========================================
		
		// credits label for the "Credit" tab
		creditsLabel.setBounds(175, 90, 107, 16);
		creditsLabel.setBackground(new Color(40, 84, 146));
		creditsLabel.setForeground(Color.white);
		creditsLabel.setFont(textFontBold);
		creditsLabel.setVerticalAlignment(JLabel.CENTER);
		creditsLabel.setHorizontalAlignment(JLabel.CENTER);
		creditsLabel.setVisible(false);
		add(creditsLabel);
		
		backgroundLabel.setBounds(0,0,width,height);
		add(backgroundLabel);
		
		this.setBounds(x,y,width,height); // set the bounds
		
		this.addMouseListener(new MouseListener()
		{
			public void mouseExited(MouseEvent e) {}
			public void mouseReleased(MouseEvent e)
			{
				repaint();
				
				if(tabPinsRectangle.contains(e.getPoint()))
				{
					// change the tab to the "Pins" tab
					changeTab("pins");
				}
				else if(tabFurnitureRectangle.contains(e.getPoint()))
				{
					// change the tab to the "Furniture" tab
					changeTab("furniture");
				}
				else if(tabCreditRectangle.contains(e.getPoint()))
				{
					// change the tab to the "Credit" tab
					changeTab("credits");
				}
				else if(exitRectangle.contains(e.getPoint()))
				{
					// close the window
					setVisible(false);
				}
				else if(wearPinRectangle.contains(e.getPoint()) && wearPinButton.getIcon().equals(wearImageOn))
				{
					// wear the currently selected pin
					movePinInventoryToWorn(selectedPin);
					wearPinButton.setIcon(wearImageOff);
				}
				else if(takeOffRectangle.contains(e.getPoint()) && takeOffButton.getIcon().equals(takeOffImageOn))
				{
					// take off the currently selected pin
					moveWornToPinInventory(selectedPin);
					takeOffButton.setIcon(takeOffImageOff);
				}
				else if(sellRectangle.contains(e.getPoint()) && sellButton.getIcon().equals(sellImageOn))
				{
					// TODO: Sell the pin
				}
			}
			public void mouseEntered(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseClicked(MouseEvent e)
			{
				System.out.println("X: " + e.getX() + "; Y: " + e.getY());
			}
		});
		
		this.addMouseMotionListener(new MouseMotionListener()
		{
			public void mouseMoved(MouseEvent e) {}
			public void mouseDragged(MouseEvent e)
			{
				// check if the mouse is inside the "title bar"
				if(titleRectangle.contains(e.getPoint()))
				{
					Point p = new Point(e.getXOnScreen(), e.getYOnScreen());
					SwingUtilities.convertPointFromScreen(p, gridObject);
					int mouseX = p.x - (getBounds().width / 2); //gridObject.getBounds().x + getBounds().x;//e.getXOnScreen() - gridObject.getBounds().x - (getBounds().width / 2);
					int mouseY = p.y - (titleRectangle.height / 2); //e.getYOnScreen() - gridObject.getBounds().y - 75;
					messagesWindow.setLocation(mouseX, mouseY);
					//repaint();
				}
			}
		});
		
		messagesWindow = this;
	}
	
	// change the tab of the Inventory window to something, well... else
	private void changeTab(String tab)
	{
		// hide all the components in the window
		hideComponents();
		
		if(tab.equals("pins"))
		{
			// change it to the "Pins" tab
			inventoryNameLabel.setVisible(true);
			inventoryCardDisplayLabel.setVisible(true);
			inventoryPinsPanel.setVisible(true);
			inventoryPinsScrollPane.setVisible(true);
			wearPinButton.setVisible(true);
			takeOffButton.setVisible(true);
			pinsWornPanel.setVisible(true);
			backgroundLabel.setIcon(windowImage);
		}
		else if(tab.equals("furniture"))
		{
			// change it to the "Furniture" tab
			inventoryNameLabel.setVisible(true);
			inventoryCardDisplayLabel.setVisible(true);
			inventoryFurniturePanel.setVisible(true);
			inventoryFurnitureScrollPane.setVisible(true);
			backgroundLabel.setIcon(furnitureWindowImage);
		}
		else if(tab.equals("credits"))
		{
			// change it to the "Credit" tab
			creditsLabel.setVisible(true);
			creditsLabel.setText("" + gridObject.getMyCharacter().getCredits());
			backgroundLabel.setIcon(creditsWindowImage);
		}
		
		// set the window size and show it again
		backgroundLabel.setSize(backgroundLabel.getIcon().getIconWidth(), backgroundLabel.getIcon().getIconHeight());
		backgroundLabel.setVisible(true);
	}
	
	// hide all of the components in the window
	private void hideComponents()
	{
		for(Component c : this.getComponents())
		{
			c.setVisible(false);
		}
	}
	
	// set a player's inventory
	public void setInventory(ArrayList<InventoryItem> inventoryItems)
	{
		furnitureCount = 0;
		pinCount = 0;
		posterCount = 0;
		this.inventoryItems = inventoryItems;
		
		int row = 0;
		int col = 0;
		
		int pinPanelHeight = 0;
		int furniPanelHeight = 0;
		
		// add the highlight square
		inventorySquareHighlightLabel.setBounds(0, 0, 42, 42);
		inventorySquareHighlightLabel.setVisible(false);
		inventoryPinsPanel.add(inventorySquareHighlightLabel);
		
		// add the inventory squares
		for(int i = 0; i < inventoryItems.size(); i++)
		{
			// get the inventory item
			InventoryItem invItem = inventoryItems.get(i);

			// check and make sure this content is allowable first
			//if(RatingSystem.isContentAllowed(StaticAppletData.getInvInfo(invItem.getId()).getRatingIndex(), gridObject.getMyCharacter().getContentRatingIndex()))
			//{
				if(invItem.getType() == InventoryItem.FURNITURE)
				{
					col = furnitureCount % ITEMS_PER_ROW; // figure out the column
					row = (int)(furnitureCount / ITEMS_PER_ROW); // figure out the row
					
					// add a furniture item
					addFurnitureToInventoryPanel(row, col, invItem.getId());
					
					// figure out the furniture panel height
					furniPanelHeight = (INVENTORY_SQUARE_SPACING * row) + (42 * row) + 42;
					
					// increase the furniture count
					furnitureCount++;
				}
				else if(invItem.getType() == InventoryItem.PIN)
				{
					col = pinCount % ITEMS_PER_ROW; // figure out the column
					row = (int)(pinCount / ITEMS_PER_ROW); // figure out the row
					
					// add the pin and its backing to the panel
					addPinToInventoryPanel(row, col, invItem.getId());
					
					// figure out the pin panel height
					pinPanelHeight = (INVENTORY_SQUARE_SPACING * row) + (42 * row) + 42;
					
					// increase the pin count
					pinCount++;
				}
				else if(invItem.getType() == InventoryItem.POSTER)
				{
					col = posterCount % ITEMS_PER_ROW; // figure out the column
					row = (int)(posterCount / ITEMS_PER_ROW); // figure out the row
					
					// add a poster item
					
					// increase the poster count
					posterCount++;
				}
			//}
		}
		
		// set the size of the pins panel
		inventoryPinsPanel.setPreferredSize(new Dimension(INVENTORY_PANEL_WIDTH, pinPanelHeight));
		inventoryPinsPanel.setBounds(PIN_INV_OFFSET_LEFT, PIN_INV_OFFSET_TOP, INVENTORY_PANEL_WIDTH, pinPanelHeight);
		
		// set the size of the furniture panel
		inventoryFurniturePanel.setPreferredSize(new Dimension(INVENTORY_PANEL_WIDTH, furniPanelHeight));
		inventoryFurniturePanel.setBounds(PIN_INV_OFFSET_LEFT, PIN_INV_OFFSET_TOP, INVENTORY_PANEL_WIDTH, furniPanelHeight);
		
		// repaint the window
		repaint();
	}
	
	// add a furniture item to the Inventory furniture section
	private void addFurnitureToInventoryPanel(int row, int col, String id)
	{	
		// add a pin
		final InventoryPinSquare invPin = new InventoryPinSquare(row, col, id);
		invPin.setName(id);
		invPin.setIcon(invPin.getIconImage());
		invPin.setBounds((INVENTORY_SQUARE_SPACING * col) + (42 * col), (INVENTORY_SQUARE_SPACING * row) + (42 * row), 42, 42);
		invPin.setHorizontalAlignment(JLabel.CENTER);
		invPin.addMouseListener(new MouseListener()
		{
			public void mouseExited(MouseEvent e)
			{
				firstClick = 0;
			}
			public void mouseReleased(MouseEvent e)
			{
			}
			public void mouseEntered(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseClicked(MouseEvent e)
			{
				// move the inventory highlight square here
				//inventorySquareHighlightLabel.setVisible(true);
				//inventorySquareHighlightLabel.setLocation(e.getComponent().getLocation());
				
				InventoryPinSquare invSquare = (InventoryPinSquare)e.getComponent();
				
				// update the inventory name bar
				inventoryNameLabel.setText(invSquare.getPinName());
				
				// update the card display label
				inventoryCardDisplayLabel.setIcon(invSquare.getCardImage());
				
				// highlight the "Wear" button
				//wearPinButton.setIcon(wearImageOn);
				//takeOffButton.setIcon(takeOffImageOff);
				//selectedPin = invSquare;
				
				// check for double-click on this panel
				if(firstClick > 0)
				{
					// double-click, so move this pin to the Pins Worn section
					if((System.currentTimeMillis() - firstClick) <= DOUBLE_CLICK_TIME)
					{
						//movePinInventoryToWorn(invPin);
					}
					firstClick = 0;
				}
				else
				{
					firstClick = System.currentTimeMillis();
				}
			}
		});
		inventoryFurniturePanel.add(invPin);
		
		// add the furniture backing
		JLabel invSquare = new JLabel(inventorySquare);
		invSquare.setHorizontalAlignment(JLabel.CENTER);
		invSquare.setBounds((INVENTORY_SQUARE_SPACING * col) + (42 * col), (INVENTORY_SQUARE_SPACING * row) + (42 * row), 42, 42);
		inventoryFurniturePanel.add(invSquare);
	}
	
	// add a pin to the Inventory pins section
	private void addPinToInventoryPanel(int row, int col, String id)
	{	
		// add a pin
		final InventoryPinSquare invPin = new InventoryPinSquare(row, col, id);
		invPin.setName(id);
		invPin.setIcon(invPin.getImage());
		invPin.setBounds((INVENTORY_SQUARE_SPACING * col) + (42 * col), (INVENTORY_SQUARE_SPACING * row) + (42 * row), 42, 42);
		invPin.setHorizontalAlignment(JLabel.CENTER);
		invPin.addMouseListener(new MouseListener()
		{
			public void mouseExited(MouseEvent e)
			{
				firstClick = 0;
			}
			public void mouseReleased(MouseEvent e)
			{
			}
			public void mouseEntered(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseClicked(MouseEvent e)
			{
				// move the inventory highlight square here
				pinsWornHighlightLabel.setVisible(false);
				inventorySquareHighlightLabel.setVisible(true);
				inventorySquareHighlightLabel.setLocation(e.getComponent().getLocation());
				
				InventoryPinSquare invSquare = (InventoryPinSquare)e.getComponent();
				
				// update the inventory name bar
				inventoryNameLabel.setText(invSquare.getPinName());
				
				// update the card display label
				inventoryCardDisplayLabel.setIcon(invSquare.getCardImage());
				
				// highlight the "Wear" button
				wearPinButton.setIcon(wearImageOn);
				takeOffButton.setIcon(takeOffImageOff);
				selectedPin = invSquare;
				
				// check for double-click on this panel
				if(firstClick > 0)
				{
					// double-click, so move this pin to the Pins Worn section
					if((System.currentTimeMillis() - firstClick) <= DOUBLE_CLICK_TIME)
					{
						movePinInventoryToWorn(invPin);
					}
					firstClick = 0;
				}
				else
				{
					firstClick = System.currentTimeMillis();
				}
			}
		});
		inventoryPinsPanel.add(invPin);
		
		// add the pin backing
		JLabel invSquare = new JLabel(inventorySquare);
		invSquare.setHorizontalAlignment(JLabel.CENTER);
		invSquare.setBounds((INVENTORY_SQUARE_SPACING * col) + (42 * col), (INVENTORY_SQUARE_SPACING * row) + (42 * row), 42, 42);
		inventoryPinsPanel.add(invSquare);
	}
	
	// add a pin to the Pins Worn section of the Inventory window
	private void addPinToWornPinsPanel(int row, int col, int wornPinNum, String id)
	{
		final InventoryPinSquare wornPin = new InventoryPinSquare(row, col, id);
		wornPin.setIcon(wornPin.getImage());
		wornPin.setBounds((INVENTORY_SQUARE_SPACING * wornPinNum) + (42 * wornPinNum), 0, 42, 42);
		wornPin.setHorizontalAlignment(JLabel.CENTER);
		wornPin.addMouseListener(new MouseListener()
		{
			public void mouseExited(MouseEvent e)
			{
				firstClick = 0;
			}
			public void mouseReleased(MouseEvent e)
			{
			}
			public void mouseEntered(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseClicked(MouseEvent e)
			{
				// move the inventory highlight square here
				pinsWornHighlightLabel.setVisible(true);
				inventorySquareHighlightLabel.setVisible(false);
				pinsWornHighlightLabel.setLocation(e.getComponent().getLocation());
				
				InventoryPinSquare wornSquare = (InventoryPinSquare)e.getComponent();
				
				// update the inventory name bar
				inventoryNameLabel.setText(wornSquare.getPinName());
				
				// update the card display label
				inventoryCardDisplayLabel.setIcon(wornSquare.getCardImage());
				
				// highlight the "Take Off" button
				wearPinButton.setIcon(wearImageOff);
				takeOffButton.setIcon(takeOffImageOn);
				selectedPin = wornSquare;
				
				// check for double-click on this panel
				if(firstClick > 0)
				{
					// double-click, so move this pin to the Pins Worn section
					if((System.currentTimeMillis() - firstClick) <= DOUBLE_CLICK_TIME)
					{
						moveWornToPinInventory(wornPin);
					}
					firstClick = 0;
				}
				else
				{
					firstClick = System.currentTimeMillis();
				}
			}
		});
		pinsWornPanel.add(wornPin);
	}
	
	// set the Pins Worn pins
	public void setPinsWorn(InventoryInfo pinsWorn[])
	{
		this.pinsWorn = pinsWorn;
		
		InventoryInfo tempPins[] = Arrays.copyOf(pinsWorn, pinsWorn.length);
		int nullPins = 0;
		
		// check all the components in the Inventory Pins panel and pop out those that are worn
		for(int k = 0; k < inventoryPinsPanel.getComponentCount(); k++)
		{
			Component c = inventoryPinsPanel.getComponent(k);
			for(int i = 0; i < tempPins.length; i++)
			{
				if(tempPins[i] != null && c.getName() != null)
				{
					if(c.getName().equals(tempPins[i].getID()))
					{
						InventoryPinSquare ips = (InventoryPinSquare)c;
						
						originalPinRows[i] = ips.getRow();
						originalPinCols[i] = ips.getCol();
						originalPinIndices[i] = k;
						
						inventoryPinsPanel.remove(c);
						tempPins[i] = null;
						nullPins++;
					}
				}
			}
			
			if(nullPins == tempPins.length - 1)
			{
				// no more worn pins to check since we found them all, so leave the loop
				break;
			}
		}
		
		displayPinsWorn();
	}
	
	// create the pins in the Pins Worn section (called after player is added to room)
	private void displayPinsWorn()
	{	
		// remove all the components in the Pins Worn panel
		pinsWornPanel.removeAll();
		
		pinsWornHighlightLabel.setBounds(0,0,42,42);
		pinsWornHighlightLabel.setVisible(false);
		pinsWornPanel.add(pinsWornHighlightLabel);
		
		for(int i = 0; i < pinsWorn.length; i++)
		{
			// make sure an actual pin exists in this slot
			if(pinsWorn[i] != null)
			{
				addPinToWornPinsPanel(originalPinRows[i], originalPinCols[i], i, pinsWorn[i].getID());
			}
		}
		
		// add the pin backings for the Pins Worn section
		for(int i = 0; i < MAX_WORN_PINS; i++)
		{
			JLabel invSquare = new JLabel(inventorySquare);
			invSquare.setHorizontalAlignment(JLabel.CENTER);
			invSquare.setBounds((INVENTORY_SQUARE_SPACING * i) + (42 * i), 0, 42, 42);
			pinsWornPanel.add(invSquare);
		}
		
		// set the current player's displayed pins
		gridObject.getMyCharacter().setDisplayedPins(pinsWorn);
		gridObject.sendUpdateCharacterMessage(gridObject.getMyCharacter());
	}
	
	// move a pin from the Inventory section to the Pins Worn section
	private void movePinInventoryToWorn(InventoryPinSquare pinSquare)
	{
		boolean foundOpenSlot = false;
		
		// find an empty slot for the pin
		for(int i = 0; i < pinsWorn.length; i++)
		{
			if(pinsWorn[i] == null || pinsWorn[i].getID().equals(""))
			{
				foundOpenSlot = true;
				pinsWorn[i] = StaticAppletData.getInvInfo(pinSquare.getPinID());
				
				originalPinRows[i] = pinSquare.getRow();
				originalPinCols[i] = pinSquare.getCol();
				
				// break out of the loop
				break;
			}
		}
		
		if(foundOpenSlot == true)
		{
			// remove it from the Inventory section
			inventoryPinsPanel.remove(pinSquare);
			
			// update the inventory name bar
			inventoryNameLabel.setText("");
			
			// update the card display label
			inventoryCardDisplayLabel.setIcon(null);
			
			inventorySquareHighlightLabel.setVisible(false);
			
			// an open slot was found, so re-create the worn pins
			displayPinsWorn();
		}
	}
	
	// move a pin from the Pins Worn section to the Inventory section
	private void moveWornToPinInventory(InventoryPinSquare pinSquare)
	{
		int x = (INVENTORY_SQUARE_SPACING * pinSquare.getCol()) + (42 * pinSquare.getCol());
		int y = (INVENTORY_SQUARE_SPACING * pinSquare.getRow()) + (42 * pinSquare.getRow());
		
		// get the pin backing component at the desired location
		Component c = inventoryPinsPanel.getComponentAt(x,y);
		
		// remove the pin backing
		inventoryPinsPanel.remove(c);
		
		// make sure it's actually gone and that there isn't something else behind it
		if(inventoryPinsPanel.getComponentAt(x,y) != null)
		{
			inventoryPinsPanel.remove(inventoryPinsPanel.getComponentAt(x,y));
		}
		
		// re-add the once-worn pin to the Inventory panel
		addPinToInventoryPanel(pinSquare.getRow(), pinSquare.getCol(), pinSquare.getPinID());
		
		// figure out the worn pin's index so we can remove it
		int index = pinSquare.getBounds().x / 42;
		pinsWorn[index] = new InventoryInfo("","","","","",0,0);
		pinsWornPanel.remove(index);
		
		// update the inventory name bar
		inventoryNameLabel.setText("");
		
		// update the card display label
		inventoryCardDisplayLabel.setIcon(null);
		
		pinsWornHighlightLabel.setVisible(false);
		
		displayPinsWorn();
	}
	
	// toggle the visibility of this window
	public void toggleVisibility()
	{
		setVisible(!isVisible());
	}
	
	public void setGridObject(RoomViewerGrid gridObject)
	{
		this.gridObject = gridObject;
	}

	class MyScrollBarUI extends BasicScrollBarUI
	{
		protected void configureScrollBarColors()
		{
			thumbColor = new Color(153, 204, 255);//Color.lightGray;
			//thumbDarkShadowColor = Color.darkGray;
			//thumbHighlightColor = Color.white;
			//thumbLightShadowColor = Color.lightGray;
			trackColor = new Color(0, 153, 204);//Color.gray;
			//trackHighlightColor = Color.gray;
		}

		protected JButton createDecreaseButton(int orientation)
		{
			JButton button = new BasicArrowButton(orientation);
			button.setBackground(new Color(153, 204, 255));
			button.setForeground(new Color(40, 88, 136));
			return button;
		}

		protected JButton createIncreaseButton(int orientation)
		{
			JButton button = new BasicArrowButton(orientation);
			button.setBackground(new Color(153, 204, 255));
			button.setForeground(new Color(40, 88, 136));
			return button;
		}
	}
}

// internal class that handles displaying the inventory items
class InventoryPinSquare extends JLabel
{
	private int row = 0;
	private int col = 0;
	private String pinName = "";
	private ImageIcon image = null;
	private ImageIcon cardImage = null;
	private ImageIcon iconImage = null;
	private String pinID = "";
	
	public InventoryPinSquare()
	{
		super();
	}
	
	public InventoryPinSquare(int row, int col, String pinID)
	{
		super();
		
		this.row = row;
		this.col = col;
		this.pinID = pinID;
		
		// get the pin information
		if(!StaticAppletData.getInvInfo(pinID).getID().equals(""))
		{
			this.pinName = StaticAppletData.getInvInfo(pinID).getName();
			this.image = AppletResourceLoader.getImageFromJar(StaticAppletData.getInvInfo(pinID).getPath());
			this.cardImage = AppletResourceLoader.getImageFromJar(StaticAppletData.getInvInfo(pinID).getCardPath());
			this.iconImage = AppletResourceLoader.getImageFromJar(StaticAppletData.getInvInfo(pinID).getIconPath());
		}
	}
	
	public void setPinName(String pinName) {
		this.pinName = pinName;
	}
	
	public String getPinName() {
		return pinName;
	}
	
	public void setImage(String path)
	{
		if(!path.equals(""))
		{
			this.image = AppletResourceLoader.getImageFromJar(path);
		}
		else
		{
			this.image = null;
		}
	}
	
	public ImageIcon getImage() {
		return image;
	}
	
	public ImageIcon getCardImage() {
		return cardImage;
	}
	
	public ImageIcon getIconImage() {
		return iconImage;
	}
	
	public String getPinID() {
		return pinID;
	}
	
	public int getRow() {
		return row;
	}
	
	public int getCol() {
		return col;
	}
}
