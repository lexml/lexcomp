{-# OPTIONS_GHC -XMultiParamTypeClasses #-} 
module TreeEditDistance where

import Data.Tree as T
import qualified Data.Set as S
import Problem
import Data.List

data TEDProblem a = TEDP { old :: T.Forest a, new :: T.Forest a } deriving (Eq,Ord,Show)

data TEDProposition a = P_New a | P_Ommited a | P_Replaced a a deriving (Eq,Ord,Show)

theory t = TT { pos = S.singleton t, neg = S.empty }

propCostToTheory :: Num cost => (TEDProposition a -> cost) -> TEDTheory a -> cost
propCostToTheory prop = sum . map prop . S.toAscList . pos

data TEDTheory a = TT { pos :: S.Set (TEDProposition a), neg :: S.Set (TEDProposition a) } deriving (Eq,Ord,Show)

instance Ord a => Theory (TEDTheory a) where
  true = TT { pos = S.empty, neg = S.empty }  
  merge th1 th2 = TT { pos = pos th1 `S.union` pos th2, neg = neg th1 `S.union` neg th2 }
  negate t | S.size (pos t) == 1 && S.null (neg t) = TT { pos = S.empty, neg = pos t }
           | otherwise = true
  isConsistent (TT { pos = p, neg = n }) = S.null (p `S.intersection` n)

splits1 l = [(x,y,z') | (x,z) <- zip (inits l) (tails l), not (null z), let (y:z') = z ]

extracts :: T.Forest a -> [(a,T.Forest a,T.Forest a,T.Forest a)]
extracts fl = [(v,l,c,r) | (l,T.Node v c,r) <- splits1 fl]

instance Ord a => Ord (Tree a) where
  compare (Node lv ll) (Node rv rl) = case compare lv rv of
    EQ -> compare ll rl
    x -> x

moreThanOne (_:_:_) = True
moreThanOne _ = False

justOne [_] = True
justOne _ = False

instance Ord a => Problem (TEDProblem a) (TEDTheory a) where
  reduce (TEDP { old = old, new = new }) = case (old,new) of
    ([],[]) ->  [(true,[])]
    _       -> 
      [(theory (P_Ommited v), [TEDP {old = ll ++ c ++ rr, new = new}]) | (v,ll,c,rr) <- extracts old] ++
      [(theory (P_New v), [TEDP {old = old, new = ll ++ c ++ rr}]) | (v,ll,c,rr) <- extracts new] ++
      [(true,[TEDP { old = [tl], new = [tr] }, TEDP { old = ll ++ lr, new = rl ++ rr }]) | 
          moreThanOne old || moreThanOne new,
          (ll,tl,lr) <- splits1 old, (rl,tr,rr) <- splits1 new ] ++
      [(theory (P_Replaced ov nv), [TEDP { old = oi, new = ni}]) |
          justOne old && justOne new,
          let Node ov oi = head $ old,
          let Node nv ni = head $ new ]
     
