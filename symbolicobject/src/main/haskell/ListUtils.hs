module ListUtils where

zipAll :: (a -> c) -> (a -> b -> c) -> (b -> c) -> [a] -> [b] -> [c]
zipAll _ _ _ [] [] = []
zipAll left both right l1 l2 = zipWith f (comp l1) (comp l2)
  where
    comp l = take len (map Just l ++ repeat Nothing)
    len = length l1 `max` length l2
    f (Just x) (Just y) = both x y
    f (Just x) _ = left x
    f _ (Just y) = right y

catListList :: [a] -> [[a]] -> [[a]]
catListList = zipAll (: []) ((:)) id

complete :: a -> [a] -> [[a]] -> [[a]]
complete blank = zipAll (\x -> [x]) ((:)) ((blank :))


