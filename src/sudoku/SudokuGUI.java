package sudoku;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SudokuGUI extends JFrame{
	
	private static final long serialVersionUID = 1L;
	
	private static JLabel[][] cells = new JLabel[9][9]; // JLabel for each cell number in the sudoku (when its 0 it stores an empty string to be displayed)
	private static JButton solve = new JButton("Solve");
	private static JButton upload = new JButton("Upload");
	private static int[][] sudoku = new int[9][9]; // the main sudoku array that is taken from the file and will be updated as it is solved
	
	private static Sudoku sud = new Sudoku(); // instantiates the main sudoku class that holds the method for solving the puzzle along with backtrack method
	
	SudokuGUI() throws IOException{
		
		setLayout(new BorderLayout());
		// Set a panel of 9 by  9 grids representing the 9 by 9 sudoku board
		JPanel grid = new JPanel(new GridLayout(9, 9));
		grid.setPreferredSize(new Dimension(450, 450));
		Font fontNums = new Font("SansSerif", Font.BOLD, 30);
		
		// loops through every cell in the grid
		for (int i = 0; i< 9; i++){
			for (int j = 0; j< 9; j++) {
				JLabel cell = new JLabel("", JLabel.CENTER); // a cell initially empty
				cell.setFont(fontNums);
				cell.setOpaque(true);
				
				/* the sudoku value for each cell stored and the JLabel is given a light gray background if it is initially not 0 (empty). Otherwise a white
				 * background with the text set as how it was when 'cell' was initialised (an empty string) */
				int val = sudoku[i][j];
				if (val != 0) {
					cell.setBackground(Color.lightGray);
					cell.setText(String.valueOf(val));
				}
				else {
					cell.setBackground(Color.WHITE);
				}
				
				// To make each 3x3 box of the sudoku to have darker borders (denoted by 3, thinner borders is 1) so users can recognize this visually easier
				int top = (i % 3 == 0) ? 3 : 1; // Whenever a cell is at the top of every three rows, color top of that cell darker
				int left = (j % 3 == 0) ? 3 : 1; // Whenever a cell is at the most left of every three columns, color the left of that cell darker
				int bottom = (i == 8) ? 3 : 1; // bottom most cells colors the bottom side of the cell darker
				int right = (j == 8) ? 3 : 1; // right most cells colors the right side of the cell darker
				
				/* Used MatteBorder as it allows specific sides of each cell to have a specific thickness of color */ 
				cell.setBorder(new MatteBorder(top, left, bottom, right, Color.BLACK));
				
				// Finally add that cell to the cells array and add it to the main grid panel
				cells[i][j] = cell;
				grid.add(cell);
			}
		}
		
		Font font = new Font("SansSerif", Font.BOLD, 20);
		
		//Upload Panel (At top)
		JPanel uploadPanel = new JPanel();
		
		// Y Axis to add elements top to bottom
		uploadPanel.setLayout(new BoxLayout(uploadPanel, BoxLayout.Y_AXIS));
		
		upload.setFont(font);
		upload.addActionListener(e -> loadFile()); // Pressing the upload button will call the loadFile function to upload a file
		upload.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		// This is the label to let users know what type of file they should be uploading
		JLabel uploadlbl = new JLabel("Select a Sudoku file (Format: .txt or .csv, rows separated by new lines, columns separated by space, empty boxes represented as 0) ");
		uploadlbl.setFont(new Font("SansSerif", Font.ITALIC, 10));
		uploadlbl.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		// Adds both the label and the button to the panel
		uploadPanel.add(uploadlbl);
		uploadPanel.add(upload);
		
		
		
		// Metrics Panel (At bottom)
		JPanel metrics = new JPanel();
		metrics.setLayout(new BoxLayout(metrics, BoxLayout.Y_AXIS));
		
		// Each metric labels including 'result' which will only show if the sudoku is invalid
		JLabel backtracksLbl = new JLabel("Backtracks: ");
		JLabel timeLbl = new JLabel("Total Time (ms): ");
		JLabel result = new JLabel("");
		
		backtracksLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
		timeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
		result.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		result.setFont(font);
		backtracksLbl.setFont(font);
		timeLbl.setFont(font);
		
		// Adds result and all metrics to the panel
		metrics.add(result);
		metrics.add(backtracksLbl);
		metrics.add(timeLbl);
		
		
		
		// Solve button panel (At bottom)
		JPanel solvePnl = new JPanel();
		
		solve.setFont(font);
		
		/* Solve button will call the solveSudoku function sending the initial sudoku as a parameter. The function returns
		 * a boolean. If it is unsuccessful, the result label will show that the sudoku is invalid. Also calls getBacktracks and
		 * getTotalTime methods from the Sudoku class (where the logic is) to get the metrics and sets the metrics labels to the data received */
		solve.addActionListener(e -> {
			new Thread(() -> {
			
			boolean res = solveSudoku(sudoku);
			
				if (!res) {
					
					result.setText("Invalid Sudoku");
					result.setForeground(Color.RED);
					
				}
				else
					result.setText(""); // To reset in case result text was already showing
			
			int backtracks = sud.getBacktracks();
			backtracksLbl.setText("Backtracks: "+ backtracks);
			
			long totalTime = sud.getTotalTime();
			timeLbl.setText("Total Time (ms): "+ totalTime);
			
			revalidate();
			repaint();
			
			}).start();
		});
		
		/* Solve button is initially disabled until a sudoku file is uploaded */
		solve.setEnabled(false);
		
		solvePnl.add(solve);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		
		// Adds both the solve button and the metrics into the bottom panel
		bottomPanel.add(solvePnl);
		bottomPanel.add(metrics);
		
		// Adds all panels to the main window
		add(uploadPanel, BorderLayout.NORTH);
		add(grid, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
		
        setVisible(true);
        
        grid.revalidate();
        grid.repaint();
		
        // When window is closed, system exits
		addWindowListener(
				new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						System.exit(0);
					}
				});	
	}
	
	/* solveSudoku function takes parameter sudoku 2d array storing the initial sudoku before solve.
	 * It returns a boolean indicating if the solve was successful or not */
	private boolean solveSudoku(int[][] sudoku){
		// Indicated that solve can't happen during an already solving process
		solve.setText("Solving...");
		solve.setEnabled(false);
		/* Calls the solver on the Sudoku class. This will solve it and update its values based on if it was solvable or not. By sending the
		 * JLabel cells, the Sudoku class will update the GUI every step throughout the solving process so it can be visualised. */
		if (sud.solver(sudoku, cells)){
			/* Once everything has been updated, the solve button text is set to say "Solved" and it is also disabled
			 * as there is no use to solve it again when it is already solved */
			solve.setText("Solved");
			solve.setEnabled(false);
			return true;
		}
		return false;
	}
	
	/* loadFile() function uses JFileChooser for users to pick a file in the GUI */
	private void loadFile() {
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Select a Sudoku file (.txt or .csv)");
		
		/* 'result' should store Approve_Option which is returned by file chooser open dialog which is when user has selected a file,
		 * if not nothing happens */
		int result = fc.showOpenDialog(this);
		if(result == JFileChooser.APPROVE_OPTION) {
			// selected file stored in 'file'
			File file = fc.getSelectedFile();
			
			// tries to read the file using BufferReader
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				int i = 0;
				String line;
				
				/* Due to the format of the sudoku file expected (rows separated by new lines, columns separated by space), I first go through each separate
				 * line (row) and use line.split for spaces to stored those values for that particular row in a rowValues array. A for loop loops through each
				 * of those values in that line (row) to store them in the sudoku array for each column j after parsing them as integers. After the loop, i increments
				 * for the next row and the next line is read of the file and process repeats. The while loop ends when all the lines are read resulting in the sudoku
				 * text file successfully transferred into the 2D sudoku integer array */
				while((line = br.readLine()) != null) {
			
					String[] rowValues = line.split(" ");
					for(int j = 0; j < 9; j++) {
						sudoku[i][j] = Integer.parseInt(rowValues[j]);
					}
					i++;
				}
				br.close();
				
				/* Loops through each number in sudoku and changes the cells JLabel to match */
				for (i = 0; i< 9; i++){
					for (int j = 0; j< 9; j++) {
						
						if(sudoku[i][j] != 0) {
							cells[i][j].setBackground(Color.lightGray);
							cells[i][j].setText(String.valueOf(sudoku[i][j]));
						}
						else {
							cells[i][j].setBackground(Color.WHITE);
							cells[i][j].setText("");
						}
						
					}
				}
				/* In case the solve button already had a puzzle solved, it set texts back to Solve
				 * Also enables the button */
				solve.setText("Solve");
				solve.setEnabled(true);
			}
			// Catches an error and reminds user that the file should be in the right format
			catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "Invalid file, " + ex.getMessage() + "\nEnsure that the file is in the correct format (.txt or .csv)");
			}
			
		}
		
	}
	
	
}