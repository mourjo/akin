# Problem
Movies are collected from different data providers. Data providers don't always use the same information to describe 
the same movie. For example, the list of actors may be incomplete. The director may be missing. 
The movie length may change slightly. The release year may vary according to the region.

A movie has the following attributes: 
- id : unique in the provided list 
- year: release year
- length: running duration in minutes
- genre: zero, one or many categories
- actors: zero, one or many actors
- directors: zero, one or many directors

For two entries describing the same movie, it's possible to have the following differences:
- id: each id is unique
- year: 1 year more or less at maximum 
- length: 5% more or less at maximum, 2 * |x - y|/(x + y) <= 0.05 where x and y is the provided movie length
- genre: different order, or missing one or many genres 
- actors: different order, or missing one or many actors
- directors: different order, or missing one or many directors

Given a list of movies where each movie is described by two different providers, the task is to identify the duplications.

Take the two following lines for example, they both refer to the same movie but have different information:
```
id	year	length	genre	directors	actors
f7b5b8cb-293b-4ced-ba19-e5b781935489    2007    60      Horror  Jamie Sharps    Alan Gilman,Heather Hamilton,Lisa Lovett,Reggie Provencher,Jareth Ryan
a86cbd11-5eed-41e6-9da7-42bf3eb1c99a    2008    63      Horror  \N      Jareth Ryan,Reggie Provencher,Alan Gilman,Heather Hamilton,Lisa Lovett
```
## Input
The file `movies.tsv` is provided. It contains a list of movies.
The first line contains movie attribute name. Then we have the movie list.
Each field is separated by a tab **\t** and line is separated by **\n**.
Unknown field is presented as **\N**. For example: 
```
id	year	length	genre	directors	actors
tt2355936	2013	89	Drama	Lina Chamie	Lucas Zamberlan,Gregório Mussatti Cesare,Dira Paes,Julia Weiss,Antônia Ricca,Marco Ricca
tt0226204	1999	95	Drama,Sci-Fi	Kazuya Konaka	Timothy Breese Miller,J. David Brimmer,Jessica Calvello,Shannon Conley
tt1226780	2008	92	Horror	Shawn Cain	Chuck Williams,Neil D'Monte,Larry Laverty,Stefano Capone
tt0109288	1994	92	Action,Comedy,Crime	Mike Binder	Damon Wayans,David Alan Grier,Robin Givens,Christopher Lawford
tt0444519	2006	90	Comedy,Sci-Fi	Eric Lartigau	Kad Merad,Olivier Baroux,Marina Foïs,Guillaume Canet
tt1847669	2011	103	Documentary	Thunska Pansittivorakul	\N
tt5127394	2015	107	Animation,Fantasy,Sci-Fi	Paris Tosen	\N
```

## Output
Provide a text file with the list of matchings. Separate field by tab **\t** and lines by **\n**. **One movie id can only be used in one matching.**
```
f7b5b8cb-293b-4ced-ba19-e5b781935489    a86cbd11-5eed-41e6-9da7-42bf3eb1c99a 
```

# Submission
The solution should have **zero** third-party dependencies, except for your tests. 
Both matching result and source code should be submitted. **Please don't publish your solution on the internet**.

# Scoring
When we receive your submission, we'll calculate the right matchings by comparing the provided output and the reference. Suppose that we have M matchings in the reference and 
N matchings in your output. Among the N matchings, K matchings are correct. Two metrics are calculated:
- Recall: **K/M** correct matchings over reference
- Precision: **K/N** correct matchings over actual result

# Notes
You will be assessed on the following points (with highest priority first):
- A working and optimized solution, with the highest possible score
- A clear explaination of your choices
- A decent code quality, with some coverage

# Clarifications
- Each movie in the list contains ONE SINGLE duplicate. You should not try to match 3 or more movies together
- The test is manually graded, not automatically. We will be reading the code and we will value how optimized your approach is, even if the score is low because of an unimportant mistake
- This is a fuzzy matching problem, meaning that there is no perfect solution. Sometimes, two duplicates will have exactly the same actors, sometimes they will have no actor in common. It's up to your algorithm to decide if it has enough information to make a match

Good luck!
