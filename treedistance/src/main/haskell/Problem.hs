{-# OPTIONS_GHC -XMultiParamTypeClasses -XFunctionalDependencies #-}
module Problem where

import Prelude hiding (negate)

import Control.Arrow
import Control.Monad
import qualified Data.Set as S
import qualified Data.Map as M

class Theory th where 
  true :: th
  merge :: th -> th -> th
  negate :: th -> th
  isConsistent :: th -> Bool

class Theory th => Problem prob th where
  reduce :: prob -> [(th, [prob])]

solve :: (Ord th, Ord cost, Theory th, Problem prob th, Ord prob) => (th -> cost) -> prob -> S.Set th
solve cost = maybe S.empty snd . solve' true Nothing . (: [])
  where        
    Nothing        `plus` t             = Just (cost t,S.singleton t)
    Just (bc,sols) `plus` t | c < bc    = Just (c, S.singleton t)
                            | otherwise = Just (bc, S.insert t sols)
      where c = cost t
    
    solve' assumptions sofar [] = sofar `plus` assumptions 
    solve' assumptions sofar (p:pl) 
        | not (isConsistent assumptions) = sofar
        | otherwise            = case reduce p of
              [] -> sofar
              alts -> snd $ foldl solve'' (assumptions,sofar) $ fmap (second (++ pl)) $ alts
  
    solve'' (assumptions, sofar) (t,pl) 
        | c <= maybe c fst sofar = (assumptions'', solve' assumptions' sofar pl)
        | otherwise              = (assumptions',sofar)
      where
        assumptions'  = assumptions `merge` t
        c             = cost assumptions'
        assumptions'' = assumptions `merge` negate t
