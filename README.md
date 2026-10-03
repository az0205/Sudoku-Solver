# Sudoku Solver with AI Heuristics

A Java Sudoku solver with a live Swing GUI that visualises the backtracking search as it runs, showing each cell being filled and unfilled in real time, along with the total backtracks and time taken to reach a solution.

## Overview

This solver treats Sudoku as a Constraint Satisfaction Problem (CSP) and solves it using backtracking search, rather than brute force. Three heuristics work together to minimise the number of backtracks needed:

- **Minimum Remaining Values (MRV)** — always selects the empty cell with the fewest legal values remaining, tackling the most constrained cells first.
- **Least Constraining Value (LCV)** — when filling a cell, tries values in the order that leaves the most options open for neighbouring cells, rather than trying 1–9 in a fixed order.
- **Forward Checking** — after every assignment, checks whether any affected row, column, or 3x3 box has been left with a cell that has zero legal values remaining. If so, it backtracks immediately instead of continuing down a doomed path.

The solver also visualises its own process: as it searches, the GUI updates live, so you can watch cells get filled, backtracked, and refilled as the algorithm works toward a solution.

## Features

- Upload a Sudoku puzzle from a `.txt` or `.csv` file
- Live visual grid with a classic 3x3 box layout and bold dividers
- Real-time animation of the backtracking search as it solves
- Displays total backtracks and total solve time (ms) after each run
- Flags invalid/unsolvable puzzles clearly in the UI

## Tech Stack

- **Language:** Java
- **GUI:** Java Swing

## Getting Started

### Prerequisites
- Java JDK 8 or higher

### Running the Solver
```bash
git clone https://github.com/az0205/Sudoku-Solver.git
cd Sudoku-Solver
javac -d bin src/sudoku/*.java
java -cp bin sudoku.Run
```

### Using the App
1. Launch the app, a 9x9 grid window will open.
2. Click **Upload** and select a puzzle file (`.txt` or `.csv`).
   - Format: 9 rows separated by new lines, values in each row separated by spaces, empty cells represented as `0`.
3. Click **Solve** and watch the backtracking search run live on the grid.
4. Once finished, the total backtracks and solve time (in ms) are displayed below the grid. If the puzzle has no valid solution, the UI will flag it as invalid.

## What I Learned

Building this project gave me hands-on experience applying classic AI search techniques, backtracking, constraint propagation, and heuristic ordering, to a real constraint satisfaction problem, and a concrete sense of how much heuristic choices like MRV and LCV reduce the search space compared to naive backtracking. Building the live visualisation also meant reasoning about how to interleave a recursive algorithm with GUI updates on a separate thread without blocking the interface.
