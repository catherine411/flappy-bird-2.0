//Catherine Hsu 
//ISU - Flappy Bird 
//Date: January 27, 2020
//Description: Flappy Bird is an arcade-style single-person game where the player is in control of a bird icon that is moving persistently to the right (as shown by a parallax background). 
//The bird will automatically descend and pressing the spacebar will allow the bird to jump and ascend. The player’s objective is to navigate the bird through an endless series of paired pipes 
//with equally sized gaps. These pipes are placed at random heights and colliding with the pipe or the ground will end the gameplay while each successful pass through a pair of pipes will award 
//the player with one point. The player’s high score will be saved and the game over screen will prompt the player to play again. A practice mode is also avalable for a easier level of difficulty, 
//but the score earned in the practice mode will not be recorded as a high score. 


import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.*;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;

//main Flappy Bird working class
public class FlappyBird extends JPanel implements Runnable, KeyListener, ActionListener{
	static JFrame frame;
	int FPS = 125;	
	Thread thread;
	int screenWidth = 500;
	int screenHeight = 630;


	BufferedImage bg, ground;
	Rectangle [] pipesRect = new Rectangle[4]; 		//to track position of pipes
	Rectangle birdRect;		//to track position of bird
	int speed = 1;
	int referenceX = 0;  //reference point
	int trackImage = 0;
	boolean jump, gameEnd, pipes, practiceMode;
	boolean airborne = true;
	double jumpSpeed = 2;
	double gravity = 0.2;
	double yVel = 0;
	Clip lose, point, bgMusic;
	Image pipe1, pipe2, bird, transitionOne, transitionTwo, nightBg, title;
	Image [] birdFall;
	JMenuItem practiceOnOption, practiceOffOption;		
	int width,pipe1PositionY, pipe2PositionY, pipe3PositionY, pipe4PositionY, pipe1PositionX, pipe2PositionX, birdPositionY, trackScore, currentHighest, highScore, jumpNum;
	private final Font COURIER_75 = new Font("Courier", Font.BOLD, 75); 
	private final Font COURIER_30 = new Font("Courier", Font.BOLD, 30); 

	public FlappyBird() {
		
		//audio files (sound effects and background music)
		try {
			AudioInputStream sound = AudioSystem.getAudioInputStream(new File ("splat.wav"));
			lose = AudioSystem.getClip();
			lose.open(sound);
			sound = AudioSystem.getAudioInputStream(new File ("point.wav"));
			point = AudioSystem.getClip();
			point.open(sound);
			sound = AudioSystem.getAudioInputStream(new File ("bgMusic.wav"));
			bgMusic = AudioSystem.getClip();
			bgMusic.open(sound);
		}
		catch(Exception e) {}

		
		//sets up JPanel
		setPreferredSize(new Dimension(screenWidth, screenHeight));
		setVisible(true);

		// Set up the Menu
		// Set up the Game MenuItems
		// Set up Extra Features MenuItems 
		JMenuItem exitOption, aboutOption, newOption, instructionsOption, bgDayOption, bgNightOption, bgDayNightOption;
		aboutOption = new JMenuItem ("About");
		newOption = new JMenuItem ("New");
		exitOption = new JMenuItem ("Exit");
		instructionsOption = new JMenuItem ("Instructions");
		practiceOnOption = new JMenuItem("On");
		practiceOffOption = new JMenuItem("Off");
		bgDayOption = new JMenuItem("Day Background");
		bgNightOption = new JMenuItem("Night Background");
		
		// Set up the Game Menu and extra features menu
		
		// Add each MenuItem to the Game Menu and Extra Features menu
		JMenu gameMenu, featuresMenu, backgroundsMenu, practiceMenu;
		gameMenu = new JMenu ("Game");
		gameMenu.add (newOption);
		gameMenu.add (aboutOption);
		gameMenu.addSeparator ();
		gameMenu.add (exitOption);

		// Add each MenuItem to Extra Features menu
		featuresMenu = new JMenu ("Extra Features");
		practiceMenu = new JMenu("Practice Mode");		
		practiceMenu.add(practiceOnOption);		//submenu items
		practiceMenu.add(practiceOffOption);
		featuresMenu.add (practiceMenu);
		backgroundsMenu = new JMenu ("Backgrounds");
		backgroundsMenu.add(bgDayOption);		//submenu items
		backgroundsMenu.add(bgNightOption);
		featuresMenu.add(backgroundsMenu);

		JMenuBar mainMenu = new JMenuBar ();
		mainMenu.add (gameMenu);
		mainMenu.add(featuresMenu);
		
		// Set the menu bar for this frame to mainMenu
		frame.setJMenuBar (mainMenu);

		// Use a media tracker to make sure all of the images are
		// loaded before we continue with the program
		MediaTracker tracker = new MediaTracker (this);
		bird = Toolkit.getDefaultToolkit().getImage("bird.png");
		tracker.addImage (bird, 0);
		pipe1 = Toolkit.getDefaultToolkit().getImage("pipe.png");
		tracker.addImage (pipe1, 1);
		pipe2 = Toolkit.getDefaultToolkit().getImage("pipeDown.png");
		tracker.addImage (pipe2, 2);
		title = Toolkit.getDefaultToolkit().getImage("flappy-bird-logo.png");
		tracker.addImage (title, 3);
		
		birdFall = new Image [9];

		//  Wait until all of the images are loaded
		try
		{
			tracker.waitForAll ();
		}
		catch (InterruptedException e)
		{}

		// Start a new game and then make the window visible
		newGame ();

		aboutOption.setActionCommand ("About");
		aboutOption.addActionListener (this);
		newOption.setActionCommand ("New");
		newOption.addActionListener (this);
		exitOption.setActionCommand ("Exit");
		exitOption.addActionListener (this);
		instructionsOption.setActionCommand("Instructions");
		instructionsOption.addActionListener(this);
		practiceOnOption.setActionCommand("PracticeOn");
		practiceOnOption.addActionListener(this);
		practiceOffOption.setActionCommand("PracticeOff");
		practiceOffOption.addActionListener(this);
		bgDayOption.setActionCommand("Day");
		bgDayOption.addActionListener(this);
		bgNightOption.setActionCommand("Night");
		bgNightOption.addActionListener(this);
		thread = new Thread(this);
		thread.start();

		//starting the thread

		setFocusable (true); // Need this to set the focus to the panel in order to add the keyListener
		addKeyListener (this);
	} // Constructor

	
	// 	Description: To handle normal menu items
	//	Parameters: event - the event name that indicates which menu item has been selected
	//	Returns: Void
	public void actionPerformed (ActionEvent event)	{

		String eventName = event.getActionCommand();
		if (eventName.equals("New"))	//brings player to a title screen/new game
			newGame();
		else if (eventName.equals ("Exit"))		//exits the game
		{
			System.exit (0);
		}
		else if (eventName.equals ("About")) {		//about the game - name and version #
			JOptionPane.showMessageDialog (frame, (Object) "By: Catherine Hsu \nFlappy Bird: Version 1.0", "About", JOptionPane.INFORMATION_MESSAGE);			
		}
		else if (eventName.equals ("PracticeOn")) {		//turns on practice mode - ONLY accessible when the player is at the title screen
			practiceMode = true;
			FPS = 100;
		}
		else if (eventName.equals ("PracticeOff")) {		//turns off practice mode - ONLY accessible when the player is at the title screen
			practiceMode = false;
			FPS = 125;
		}
		else if (eventName.equals ("Day")) {		//Day background option
			try {
				bg = ImageIO.read(new File("flappyBirdBackground.png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (eventName.equals ("Night")) {		//Night background option
			try {
				bg = ImageIO.read(new File("flappyBirdBackgroundNight.png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	

	// Description: Prompts the initialize method as well as paint component and update methods every FPS
	// Parameters: None
	// Returns: Void
	@Override
	public void run() {
		initialize();
		while(true) {
			//main game loop
			update();

			this.repaint();		//prompts pain component to redraw window
			try {
				Thread.sleep(1000/FPS);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	// Description: Sets up the defaults of the game when the program first runs
	// Parameters: None
	// Returns: Void
	public void initialize() {
		//setups before the game starts running
		pipesPosition();
		birdRect = new Rectangle(220, birdPositionY, 55, 55);	//an invisible rectangle to keep track of bird position/collisions: x-position of bird stays fixed while y-position moves up/down
		practiceMode = false;	//practice mode is off by default
		bgMusic.start();
		bgMusic.loop(Clip.LOOP_CONTINUOUSLY);
		try {
			bg = ImageIO.read(new File("flappyBirdBackground.png"));		//background stays fixed while the ground moves to the left continuously, day background by default
			ground = ImageIO.read(new File("ground.png"));
			width = bg.getWidth();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Description: Updates the position of images, rectangles, background, and updates interactions (ie collisions) every frame
	// Parameters: none
	// Returns: void
	public void update() {
		if (gameEnd == false) {
			referenceX -= speed;		//keeps the ground moving continuously in the background
			if(referenceX < -width)
				referenceX = 0;
			else if(referenceX > width)
				referenceX = 0;

			if (pipes == true) {		//when the game starts, pipes move continuously to the left at same speed as pipes
				pipe1PositionX -= speed;
				pipe2PositionX -= speed;

				for (int i = 0; i < pipesRect.length; i++) {	//checks for any collisions with any of the 4 pipes on the screen
					pipesPosition();
					checkCollision(pipesRect[i]); 
				}

				if (pipe1PositionX == -80) {		//updates pipes horizontal position
					pipesPosition();
					pipe1PositionX = 500;
				}
				else if (pipe2PositionX == -80) {
					pipesPosition();
					pipe2PositionX = 500;
				}
			}

		}
		birdPosition();	//keeps track of bird's vertical position and keeps it within bounds
		keepInBound();
	}

	
	// Description: Resets the default settings after every new game 
	// Parameters: none
	// Returns: none
	public void newGame () { //settings for when the game starts	
		jumpNum = 0;	//jumpNum keeps track of number of times bird has jumped/how many times space has been pressed per round
		jump = false;
		gameEnd = false;
		practiceOnOption.setEnabled(true);		//player can choose to turn on or off the practice mode at the start screen
		practiceOffOption.setEnabled(true);
		trackScore = 0;		//the score starts at 0 each round
		pipes = false;		//pipes are not prompted to move to the left at the title screen
		birdPositionY = 250;
		speed = 1;	
		jumpSpeed = 2;
		repaint();
	}

	
	// Description: draws all graphics, including invisible rectangles to keep track of positions/collisions
	// Parameters: g - graphics parameter required for paintComponent method
	// Returns: void
	public void paintComponent(Graphics g) {		
		super.paintComponent(g);
		//draw stuff
		Graphics2D g2 = (Graphics2D)g;

		g.drawImage(bg, 0, 0, null);	//draws background
		

		if (pipes == true) {
			g.drawImage (pipe1, pipe1PositionX, pipe1PositionY, 80, 420, this);	// draws a total of 4 pipes in pairs (up and down) equal distant from each other
			g.drawImage (pipe2, pipe1PositionX, pipe2PositionY, 80, 420, this); 
			g.drawImage (pipe1, pipe2PositionX, pipe3PositionY, 80, 420, this);
			g.drawImage (pipe2, pipe2PositionX, pipe4PositionY, 80, 420, this);
		}

		if (jumpNum == 0) {		//displays "press space to start", disappears after player presses space
			setFont(COURIER_30);
			g.setColor(Color.WHITE);
			String pressSpace = "Press Space to Start";
			g.drawString(pressSpace, 75, 150);
			g.drawImage(title, 50, 10, 400, 100, null);
		}

		if (practiceMode == true) {		//practice icon in top left when practice mode is selected
			g.setColor(new Color(0, 180, 0));	
			g.fillRect(0, 0, 110, 25);
			g.setFont(new Font("COURIER", Font.BOLD, 20)); 
			g.setColor(Color.WHITE);
			g.drawString("PRACTICE", 10, 20);
			g.setColor(Color.BLACK);	
			g2.setStroke(new BasicStroke (2));	
			g2.drawRect(0, 0, 110, 25);
		}

		String score = trackScore/4 + "";
		if (jumpNum > 0 && gameEnd == false){	//displays score on the top center after the player presses space 
			g.setFont(COURIER_75); 
			g.setColor(Color.WHITE);
			int xPos = (int) ((this.getWidth() - this.getFontMetrics(COURIER_75).getStringBounds(score, g).getWidth()) / 2); 
			g.drawString(score, xPos, 100);
		}

		g.drawImage(ground, referenceX, screenHeight - 70, null);		//draws ground (separate from background)
		g.drawImage(ground, referenceX + width, screenHeight - 70, null);
		g.drawImage(ground, referenceX - width, screenHeight - 70, null);

		if (gameEnd == false)
			g.drawImage(bird, 220, birdPositionY, 55, 55, this);	//bird pointing forwards

		if (gameEnd == true) {	
			birdAnimations();
			g.drawImage(birdFall[trackImage - 1], 220, birdPositionY, 55, 55, this);	//rotates bird quickly so that it points down when plummeting to the ground
		}

		int xRect = 150; 
		int yRect = 145;

		if (gameEnd == true && birdPositionY == screenHeight - 125 && practiceMode == false) {		//end screen (for practice mode OFF) with the round's score and the highest all time score 
			int xPos = (int) ((this.getWidth() - this.getFontMetrics(COURIER_30).getStringBounds(score, g).getWidth()) / 2); 
			g.setColor(new Color(255, 222, 105));
			g.fillRect(xRect, yRect, 200, 250);

			g.setFont(COURIER_30);
			g.setColor(new Color(255, 118, 38));
			g.drawString("Score", xPos - 30, yRect + 45);
			g.drawString("Best", xPos - 20, yRect + 150);
			g.setFont(new Font("COURIER", Font.BOLD, 60)); 
			g.setColor(Color.BLACK);
			g.drawString(score, xPos - 5, yRect + 105);
			g.drawString(highScore + "", xPos - 5, yRect + 210);
			g.setFont(new Font("COURIER", Font.BOLD, 25)); 
			g.drawString("Press ENTER to Play Again",60, 435);

			g2.setStroke(new BasicStroke (5));
			g2.drawRect(xRect, yRect, 200, 250);
		}
		else if(gameEnd == true && birdPositionY == screenHeight - 125 && practiceMode == true) {		//end screen for practice mode ON with the round's score ONLY (scores in practice mode will not be saved as highest score)
			int xPos = (int) ((this.getWidth() - this.getFontMetrics(COURIER_30).getStringBounds(score, g).getWidth()) / 2); 
			g.setColor(new Color(255, 222, 105));
			g.fillRect(200, yRect + 5, 110, 125);

			g.setFont(COURIER_30);
			g.setColor(new Color(255, 118, 38));
			g.drawString("Score", 208, yRect + 45);
			g.setFont(new Font("COURIER", Font.BOLD, 60)); 
			g.setColor(Color.BLACK);
			g.drawString(score, xPos -8, yRect + 100);
			g.setFont(new Font("COURIER", Font.BOLD, 25)); 
			g2.setStroke(new BasicStroke (5));
			g2.drawRect(200, yRect + 5, 110, 125);

			g.drawString("Press ENTER to Practice Again", 30, 435);

		}
	}

	// Description: Determines the x and y position of the pipes at every frame 
	// Parameters: none
	// Returns: void
	public void pipesPosition() {
		if (pipes == true) {
			int [] pipeArray1 = {250, 275, 300, 325, 350, 375, 400, 425, 450};	//possible top pipes y-positions
			int [] pipeArray2 = {-330, -305, -280, -255, -230, -205, -180, -155, -130}; //bottom pipes y-positions
			int randNum1 = (int)(Math.random() * 9);
			int randNum2 = (int)(Math.random() * 9);
			int practiceAdjust;

			if (practiceMode == true) 
				practiceAdjust = 15;		//bigger pipe openings for practice mode
			else 
				practiceAdjust = 0;

			if (jumpNum == 1) {
				pipe1PositionX = 500;		//x position of pipes
				pipe2PositionX = 790;
			}

			if (pipe1PositionX == -80 || pipe2PositionX == 790) {		//y position of pipes
				pipe1PositionY = pipeArray1[randNum1] + practiceAdjust;
				pipe2PositionY = pipeArray2[randNum1] - practiceAdjust;
			}
			if (pipe2PositionX == -80 || pipe2PositionX == 790) {
				pipe3PositionY = pipeArray1[randNum2] + practiceAdjust;
				pipe4PositionY = pipeArray2[randNum2] - practiceAdjust;
			}

			pipesRect[0] = new Rectangle(pipe1PositionX, pipe1PositionY, 80, 420);	//invisible rectangles outlining each of the 4 pipes to keep track of collisions/location
			pipesRect[1] = new Rectangle(pipe1PositionX, pipe2PositionY, 80, 420);
			pipesRect[2] = new Rectangle(pipe2PositionX, pipe3PositionY, 80, 420);
			pipesRect[3] = new Rectangle(pipe2PositionX, pipe4PositionY, 80, 420);
		}
	}

	// Description: Determines and updates the y position of the bird every frame
	// Parameters: none
	// Returns: void
	public void birdPosition () {
		if (jumpNum > 0) {
			if (airborne == true) {
				yVel -= gravity;	//when space is not pressed during the game and bird free falls 
			}
			if(jump == true) {
				yVel = jumpSpeed;		//constant jump increments for bird when space is pressed
				airborne = true;
			}
			birdPositionY -= yVel;		//updates y position of bird

			birdRect.setLocation(birdRect.x, birdPositionY);	
		}
	}
	
	// Description: Checks if a collision occurs between the bird and a pipe or the ground during the game and keeps track of scores 
	//				if the player passes through each pair of pipes
	// Parameters: pipes - the array of invisible rectangles outlining the pipes that keeps track of the pipes position
	// Returns: void
	public void checkCollision(Rectangle pipes) {
		//check if bird touched the pipes 
		if(birdRect.intersects(pipes) || birdPositionY + birdRect.width >= screenHeight - 70) {
			//stops bird and background from moving
			speed = 0;
			jumpSpeed = 0;	
			airborne = true;	
			gameEnd = true; 
			practiceOnOption.setEnabled(false); 	//player cannot turn on or off the practice mode at this time
			practiceOffOption.setEnabled(false);
			lose.setFramePosition(0);		//splat sound effect after the bird falls
			lose.start();

			if (practiceMode == false) {		//keeps track of high score (when NOT in practice mode)
				try {
					highScore();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			}
		}
		else if (pipe1PositionX == birdRect.x + 25 || pipe2PositionX == birdRect.x + 25) {		//keeps track of the round's score
			trackScore++;	//adds 4 each time, divide by 4 for actual score
			point.setFramePosition(0);		//point gain sound effect
			point.start();
		}
	}

	// Description: reads the high score to a text file and updates the high score to the file if the current round's score is higher than the previous high score
	// Parameters: none
	// Returns: void
	public void highScore() throws FileNotFoundException, IOException {
		if (gameEnd == true) {
			Scanner in = new Scanner (new File ("highScore.txt"));		//reads from high score text file
			while (in.hasNextInt()) 
				currentHighest = in.nextInt();		
			in.close();

			PrintWriter out = new PrintWriter (new FileWriter("highScore.txt")); 	//compares previous high score to current round score and updates if necessary
			if (trackScore/4 > currentHighest)
				highScore = trackScore/4;
			else {
				highScore = currentHighest;
			}
			out.println(highScore);
			out.close();

		}
	}

	// Description: rotates the bird quickly facing the ground when the bird collides with the pipes or with the ground 
	// Parameters: none
	// Returns: void
	public void birdAnimations () {
		if (trackImage < 9) {		//loops through a set of 9 images of the bird rotating clockwise to the ground
			String imageFileName = "bird" + trackImage + ".png";
			birdFall [trackImage]= Toolkit.getDefaultToolkit().getImage(imageFileName);
			trackImage++;
		}
	}


	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_SPACE && gameEnd == false) {		//when space key is pressed, bird jumps by constant increment 
			jumpNum++;
			pipes = true;
			jump = true;
		}
		if (key == KeyEvent.VK_ENTER && gameEnd == true) {		//when enter key is pressed at the end screen, it allows the player to play again
			newGame();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_SPACE) {		//bird free falls when space is released during the game
			jump = false;
		}
	}

	// Description: sets up the boundaries that the bird can stay within and keeps it within the window (the bird cannot have a y position smaller than the ground)
	// Parameters: none
	// Returns: void
	void keepInBound() {  
		if(birdPositionY < 0) {
			yVel = 0;
		}
		else if(birdPositionY + birdRect.width > screenHeight - 55) {	//keeps the bird from going further down into the ground
			birdPositionY = screenHeight - 125;
			airborne = false;
			yVel = 0;
		}
		birdPositionY -= yVel;
	}
	
	// Description: main method to create the window
	// Returns: void
	public static void main(String[] args) {
		//The following lines create your window

		frame = new JFrame ("Flappy Bird!");
		FlappyBird myPanel = new FlappyBird ();
		frame.add(myPanel);
		frame.addKeyListener(myPanel);
		frame.setVisible(true);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
	}//main method
}//FlappyBirdWorking class
