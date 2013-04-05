module FlatCorrelation where

import Prelude hiding (map)
import qualified Data.List as L
import qualified Correlation as C
import Control.Monad

data FlatCorrelation d r = FCR { 
    value :: Maybe d, 
    col :: Int, 
    rel :: Maybe (FlatRel d r) } deriving (Eq,Ord,Show)

data FlatRel d r = FRel { relData :: Maybe r, next :: FlatCorrelation d r } deriving (Eq,Ord,Show)

fold :: (Maybe d -> Int -> Maybe (Maybe r, a) -> a) -> FlatCorrelation d r -> a
fold f (FCR v n d) = f v n (fmap g d)
  where
    g (FRel rd n) = (rd,fold f n)

map :: (d -> d') -> (r -> r') -> FlatCorrelation d r -> FlatCorrelation d' r'
map h k = fold f
  where
    f x n l = FCR (fmap h x) n (fmap j l)
    j (rd,x) = FRel (fmap k rd) x

firstRest :: (a -> b) -> (a -> b) -> [a] -> [b]
firstRest _ _ [] = []
firstRest f g (x:xs) = f x : L.map g xs

flatten :: C.Correlation d r -> [FlatCorrelation d r]
flatten = C.fold f
  where
    f d nc [] = [FCR (Just d) nc Nothing]
    f d nc l = concat $ firstRest (add (Just d)) (add Nothing) l    
      where
        add v (rd,n) = firstRest (add' v (Just rd)) (add' Nothing Nothing) n
          where
            add' v rd c = FCR v nc $ Just (FRel rd c)

joinFRel :: FlatCorrelation d (Maybe r) -> FlatCorrelation d r 
joinFRel = fold f 
  where
    f x n l = FCR x n (fmap j l)
    j (rd,x) = FRel (join rd) x
