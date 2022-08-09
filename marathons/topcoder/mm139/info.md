https://www.topcoder.com/challenges/05bd815f-0e25-4268-a02d-7b8233d710dc/

TCO22 Regional Marathon - Lightning Marathon Match 139

August 5-8, 2022

26/72 provisional rank

## Approach

Mid-tier hill-climbing solution here, didn't manage to get it to run very many iterations per test case.

### Initial solution

Call each colored cell a "vertex", and a pair of vertices that are able to be connected to each other an "edge".

Call a path between two vertices "greedy" if it is a shortest path that achieves minimum penalty on the current grid and layout of paths; all greedy paths are found with Dijkstra's.

Order edges by descending V*(N-L) for each edge, where V is the product of the values of the endpoints of the edge, N is the side length of the grid, and L is the shortest possible length of a path between two vertices, found with BFS. (This is taken from the leaked C++ solution.)

Make the following two solutions, then choose the one with higher score:

* For each edge, if both of its endpoints have not already been connected to a path, add a greedy path; then do hill climbing over the order of the edges, where in each iteration we do 1+V/10 random swaps (integer division). (This is taken from the leaked C++ solution.)

* Using the initial order of the edges before hill-climbing, for each edge e, add a greedy path if both endpoints are free, except this time if there is nonzero penalty (from crossing paths), remove all crossing paths, then add a shortest path between the endpoints of e, then remake paths between the endpoints of the paths that were removed. Do not do hill-climbing for this solution.

### Hill climbing

Each iteration:

* Choose row and column intervals [r0,r1] and [c0,c1], respectively, where the length of each interval has an expected value of sqrt(N) and has a roughly geometric distribution, capped between 1 and N.

* Remove all paths that contain a cell with row number in [r0,r1] and column number in [c0,c1].

* Do one of the following two things, with equal probability:

	* Connect back together, in a random order, the same pairs of vertices that the removed paths connected.

	* Randomly match all vertex endpoints of the removed paths with other unmatched vertices. Calculate an upper bound on the highest possible score we can achieve with this new matching; if it is less than our previous solution's score, randomly match again; do at most 100 tries. Then make paths between these new pairs of vertices.

All paths made in this stage of the program are greedy.

### Final optimizations

* For each edge (in the original ordering of edges in the first stage of the program), form a greedy path if both its endpoints are free; if there are any crossing paths, remove them, remake the path for our current edge, then remake paths between pairs of vertices that the removed paths connected.


* 2-opt: for each pair of paths a--b, c--d, try replacing them with a--c, b--d or a--d, b--c and use the highest-scoring solution that results.

* Repeat the first step in this stage.

## Example test case scores + hill climbing reps

1: 309, reps=812484

2: 1503, reps=19974

3: 2634, reps=20489

4: 3305, reps=22060

5: 3261, reps=20408

6: 580, reps=62233

7: 1328, reps=41848

8: 1096, reps=73799

9: 2183, reps=31121

10: 2013, reps=65365