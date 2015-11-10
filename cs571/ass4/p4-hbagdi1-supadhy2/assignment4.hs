--Solution Question 1 starts
slice:: Int->Int->[a]->[a]
slice i j (x:xs)
	| i>1  = slice (i-1) (j-1) xs
	| i==1 && j>0 = x: slice 1 (j-1) xs
	| i==1 && j==0  = []
--Solution Question 1 ENDS




--Solution Quest-2 starts
--helper function for deleting kth element
deletekth1:: Int->Int->[a]->[a]
deletekth1 x y [] = []
deletekth1 n k (x:xs)
	| k==1 =  deletekth1 n n xs
	| k>1 = x:deletekth1 n (k-1) xs

--function to delete kth element
delete k lt = deletekth1 k k lt
--Solution Quest-2 ENDS



--Solution Question Starts  Search for a key in binary tree
data Tree = Leaf Int | Node Tree Int Tree

occurs :: Int -> Tree -> Bool
occurs x (Leaf y) 
	 |x==y =True
	 | otherwise =False
occurs x (Node tr1 y1 tr2)
	|x==y1 = True
	| otherwise = (occurs x tr1) || (occurs x  tr2)
--Solution Question Ends  Search for a key in binary tree



--Solution Question 4 starts Sortlist according to the length of the sublist using quicksort
length1 ::[a]->Int
length1 [] =0
length1 (x:xs)
	 = 1+ length1 xs

sortlist :: [[a]]->[[a]]
sortlist [] =[]
sortlist (x:xs)
	= sortlist[y|y<-xs,length1 y <= length1 x] ++ [x]++ sortlist[y|y<-xs,length1 y> length1 x]
--Solution Question 4 - ENDS



--Solution Question 5  starts
flatten lt = [x| sublist<-lt,x<-sublist]
--Solution Question 5  ends

