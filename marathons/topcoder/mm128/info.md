https://www.topcoder.com/challenges/8287c805-1f7e-40c8-9d5d-63a86759f72b

TCO21 Regional Marathon - Lightning Marathon Match 128

July 28 - August 5, 2021

12/70 provisional and final rank

## Approach (copy-pasted from Topcoder challenge discussion)

I didn't manage to discover the spiral pattern during competition, but I did use a "ring" pattern, where I filled in cells of each color from the border of the grid inward. I first made an initial target grid by brute-force searching over all permutations of colors to fill in that order, until 2 seconds had passed.

Then I did hill climbing, where I repeatedly swapped two cells of different colors in the target board that would change the connected components of both colors of the cells without making the board have more than C total components, and see if that reduced the total number of moves needed for the solution.

[image of sample run of seed 2](https://imgur.com/a/EWyU05u)

To make the solution changing the starting grid into the target grid, I split the target grid into rings. Then, for each ring, outside to inside, I greedily solved the next cell in the ring that took the least amount of moves.

Example test case results:
1: 6
2: 3706
3: 64
4: 1093
5: 2391
6: 2887
7: 169
8: 53
9: 462
10: 865