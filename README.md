# Heuristic-Battleship-Solitaire
Uses simulated annealing to try and solve a Battleship Solitaire puzzle. An example of this game can be found [here](https://lukerissacher.com/battleships).

## Concept
The game board is a N by N grid with ships hidden on it.  
The board is described by three vectors:
1. A set of K integers specifying the lengths of K ships
2. A set of N integers specifying how many cells in each of the N rows contain a hidden
part of a ship
3. A set of N integers specifying how many cells in each of the N columns contain a
hidden part of a ship
Here is an example:
Ships = {4 3 3 2 2 2 1 1 1 1}
Xhits = {1 2 1 3 2 2 3 1 5 0}
Yhits = {3 2 2 4 2 1 1 2 3 0}
The player must deduce from these facts exactly which cells are empty and which have part of a ship.

## Usage
Takes the name of a text file as a command line argument.  The text file contains three lines of numbers separated spaces.  The first line is for the Ships, the second is Xhits, and the third is Yhits.  The above example would be displayed as:
```
4 3 3 2 2 2 1 1 1 1
1 2 1 3 2 2 3 1 5 0
3 2 2 4 2 1 1 2 3 0
```
The program also takes an integer as a command line argument.  This specifies the maximum number of solutions the program can evaluate before it has to stop and output the best result it has come up with. 

```bash
$ java BattleShip <filename> <number of solutions attempts>
```

## Output
Once the search is completed, the best solution is outputted to standard output followed by a SCORE specifying the number of errors in the hit lists of the best solution.

Example input:
```
4 3 3 2 2 2 1 1 1 1
1 2 1 3 2 2 3 1 5 0
3 2 2 4 2 1 1 2 3 0
```
Example output:
```
$ java BattleShip BS-10-10-10.txt 40
Best Solution:
  1 2 1 3 2 2 3 1 5 0  
3 ~ X ~ ~ ~ X X ~ ~ ~  
2 ~ X ~ ~ ~ ~ ~ ~ X ~ 
2 ~ ~ ~ ~ ~ ~ ~ ~ X ~
4 ~ ~ X X X ~ ~ ~ X ~
2 ~ ~ ~ ~ ~ ~ X ~ X ~
1 ~ X ~ ~ ~ ~ ~ ~ ~ ~
1 ~ ~ ~ ~ ~ X ~ ~ ~ ~
2 X ~ ~ X ~ ~ ~ ~ ~ ~
3 ~ ~ ~ X ~ ~ X X X ~
0 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
SCORE: 4
```
