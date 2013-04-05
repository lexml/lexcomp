module Table where

data Cell a = C { content :: a, rs :: Int, cs :: Int }
                deriving (Eq,Ord)

instance Show a => Show (Cell a) where
  show (C { content = c, rs = rs, cs = cs }) = 
    "<td colspan=\"" ++ show cs ++ "\" rowspan=\"" ++ show rs ++ "\">" ++ show c ++ "</td>"

foldCell :: (a -> Int -> Int -> b) -> Cell a -> b
foldCell f (C { content = c, rs = rs, cs = cs}) = f c rs cs

changeWidth :: (Int -> Int) -> Cell a -> Cell a
changeWidth f (C { content = c, rs = rs, cs = cs}) = C c rs (f cs)

data Table a = T { rows :: [[Cell a]] } 
              deriving (Eq,Ord)


showTable :: Show a => Table a -> String
showTable (T l) = "<table style=\"border: 1px solid black; border-collapse: collapse\" border=\"1\">\n" ++ unlines (map (("    " ++) . showRow) l) ++ "</table>"
    where
      showRow l = "<tr>" ++ concatMap show l ++ "</tr>"

instance Show a => Show (Table a) where
  show = showTable

mapCell :: (a -> b) -> Cell a -> Cell b
mapCell f (C { content = c, rs = rs, cs = cs }) = 
    C { content = f c, rs = rs, cs = cs }

mapTable :: (a -> b) -> Table a -> Table b
mapTable f = T . map (map (mapCell f)) . rows


instance Functor Cell where
  fmap = mapCell 

instance Functor Table where
  fmap = mapTable

foldlTable :: acc ->
              (acc -> accRow) ->
              (accRow -> Cell a -> accRow) ->
              (acc -> accRow -> acc) -> 
              Table a ->
              acc

foldlTable acc accToAccRow combRow combAcc =
  foldl (\a -> combAcc a . foldl combRow (accToAccRow a)) acc . rows

vcat (T l1) (T l2) = T (l1 ++ l2)

vcats = foldl vcat (T [])



{-
zipAll :: (a -> c) -> (a -> b -> c) -> (b -> c) -> [a] -> [b] -> [c]
zipAll _ _ _ [] [] = []
zipAll left both right l1 l2 = map f $ zip (map Just l1 ++ repeat Nothing) (map Just l2 ++ repeat Nothing)
  where
    f (Just x) (Just y) = both x y
    f (Just x) _ = left x
    f _ (Just y) = right y

hcat :: Table a -> Table a -> Table a
hcat (T t1) (T t2) = zipAll t1 t2
-}
