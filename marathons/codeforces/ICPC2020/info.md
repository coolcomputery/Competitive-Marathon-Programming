https://codeforces.com/contest/1378/standings (18/241 rank)

June 27 - July 4, 2020

A1-3: given a graph, partition its vertices to maximize modularity+regularization (definitions given in problem statements)

approach: simulated annealing + agglom and greedy at end

initial solution: every vertex is in its own group

each iteration:

* choose a random vertex v and number d uniformly in [0,2];

* find vs={all vertices of distance <=d from v};

* choose a random group number gi (with some preference to group numbers that are adjacent to some vertex in vs; see code for more detail)

* set all vertices in vs to have group number gi

agglom: repeatedly merge two groups together in order to increase the score, until doing so no longer increases score

greedy: for each vertex, set it to the group number that yields the largest increase in score