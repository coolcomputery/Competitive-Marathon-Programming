https://www.topcoder.com/challenges/39f57908-803c-4b69-b208-4fc03717ab12
March 23-31, 2021
23/53 provisional, 21/53 final rank

## Approach (copy-pasted from Topcoder forum)
I found this problem interesting and very difficult, and for the entire match I was stuck with using rudimentary greedy solutions. My final solution is a very messy combination of the following strategies, each of which is only used for certain cases of (N,C) (deciding which strategy to use when was determined with empirical tests):

* creating the longest line of length >=5 in one move, and then outputting that move

* creating the longest line of length >=5 in two moves, and then outputting the first of those two moves

* extending the longest line of any length in one move, taking into account the "potential" of that line

	* I have two methods of measuring this "potential": the first is to pretend that empty spaces are the same color as the color of the line we are examining (the "target color"); the second is to take into account how many dots of the target color are currently on the board, not accounting for any dots that will appear in the future. Depending on (N,C), I use one or the other method.

* making the move that maximizes the length of the longest line that can be formed in the next move (whether through "fixing" broken lines or forming lines of length 6 or more)

	* this strategy sometimes formed lines of 8 or more, but I didn't go down this route of trying to make really long lines, since it seems that the extra number of random dots placed on the board during the process of making a really long line often outweighs the higher score gain from making such a line.

Sidenote: it is technically possible to remove 13 dots in a single move by having 12 same-colored dots be positioned as shown below, and then placing the 13th dot in the middle (although this seems like a bad strategy because of the reasons I stated above):

....1....
....1....
....1....
....1....
1111.1111