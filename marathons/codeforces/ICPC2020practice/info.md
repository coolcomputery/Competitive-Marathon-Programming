https://codeforces.com/contest/1376/standings

June 25-27, 2020

A1: sort a list of numbers (easy)

B1-4: find the largest independent set of a graph

## Approach (copy-pasted from https://codeforces.com/blog/entry/79078#comment-651485)

I did simulated annealing:

Repeatedly add an un-chosen vertex to our independent set; remove all neighbors of that vertex from our set; let the score change be D: if D>=0, we definitely keep this change: otherwise, we might still keep this change with probability e^(D/temp), where temp is our "temperature". Temperature has a certain value at the start of the SA (manually controlled; usually I did 0.5, 0.25, or 1.0 for long SA's) and decays linearly to 0 at the end of the SA.

I ran multiple SA for various lengths of time (ex. 100 sec, 200 sec, 1000 sec, 7 hrs). In the beginning I always started with no vertices chosen. Later I took previous solutions and ran SA on them.