module Correlation where

import Prelude hiding (map)
import qualified Data.List as L
import Control.Monad

data Correlation d r = CR { value :: d, col :: Int, rels :: [Rel d r] } deriving (Eq,Ord,Show, Read)

data Rel d r = Rel { relData :: r, next :: Correlation d r } deriving (Eq,Ord,Show,Read)

fold :: (d -> Int -> [(r,a)] -> a) -> Correlation d r -> a
fold f (CR d n l) = f d n [(r,fold f x) | Rel r x <- l ]

map :: (d -> d') -> (r -> r') -> Correlation d r -> Correlation d' r'
map g h = fold f
  where 
    f d n l = CR (g d) n [ Rel (h r) t | (r,t) <- l ]
             

