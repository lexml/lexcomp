module TEDExample1 where

import TreeEditDistance
import Problem
import Data.Tree
import Control.Arrow
import qualified Data.Set as S
import qualified Data.Map as M
import qualified Data.Foldable as F

type MyTree = Tree String

tree1 = Node "A" [Node "B" [], Node "C" []]

tree2 = Node "A" [Node "B" [], Node "D" []]

tree3 = Node "A" [             Node "B" [Node "C" [], Node "D" []], Node "E" []]

tree5 = Node "A" [Node "B" [], Node "B" [Node "C" [], Node "D" []], Node "E" []]

tree4 = Node "A" [Node "B" [], Node "B" [Node "C" [], Node "H" []], Node "E" []]

cost :: TEDTheory String -> Double
cost = propCostToTheory $ \p -> case p of
          P_New _ -> 1.0
          P_Ommited _ -> 1.0
          P_Replaced v1 v2 | v1 == v2 -> 0.0
                           | otherwise -> 3.0

treeFold :: ([a] -> b -> a) -> Tree b -> a
treeFold f (Node v l) = f (fmap (treeFold f) l) v


enumerate = treeFold h 
  where
    h cs v n = (foldl h' (n,[]) >>> \(n',l) -> ((n' + 1 ,Node (n',v) (reverse l)))) cs 
      where 
        h' (n,l) g = let (n',t) = g n in (n',t : l)

index base t = let (n,t') = enumerate t base 
                   m = F.foldl (\m (x,y) -> M.insert x y m) M.empty t'
                   t'' = fmap fst t'
                   in (t'',m,n)

test' t1 t2 = test cost t1' t2'
  where
    (t1',m1,n') = index 0 t1
    (t2',m2,_) = index n' t2
    cost :: TEDTheory String -> Double
    cost = propCostToTheory $ \p -> case p of
          P_New _ -> 1.0
          P_Ommited _ -> 1.0
          P_Replaced v1 v2 | M.lookup v1 m1 == M.lookup v2 m2 -> 0.0
                           | otherwise -> 3.0



test c t1 t2 = let
  notEqual (P_Replaced x y) = x /= y
  notEqual _ = True
  res = (solve c >>> S.map (pos >>> S.filter notEqual) >>> S.toAscList) (TEDP [t1] [t2])
    in print res



