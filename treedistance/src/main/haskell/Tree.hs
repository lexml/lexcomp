import Data.List
import qualified Data.Set as S
import qualified Data.Map as M
import Control.Arrow


data Tree a = Tree a (Forest a) deriving (Eq,Ord,Show)

type Forest a = [Tree a]

value :: Tree a -> a
value (Tree v _) = v

subForest :: Tree a -> [Tree a]
subForest (Tree _ f) = f

node :: a -> [Tree a] -> Tree a
node = Tree

leaf :: a -> Tree a
leaf = flip node []


extracts :: Forest a -> [(a,Forest a, Forest a, Forest a)]
extracts [] = []
extracts (n:l) = (value n,[],subForest n,l) : [ (x', n : ll, lc, lr) | (x',ll,lc,lr) <- extracts l ] 

instance Functor Tree where
  fmap f = fold (node . f)

treeOut :: Tree a -> (a,Forest a)
treeOut = value &&& subForest

treeIn :: (a,Forest a) -> Tree a
treeIn = uncurry node

fold :: (a -> [b] -> b) -> Tree a -> b
fold h = f
  where 
    f = treeOut >>> second (fmap f) >>> uncurry h

test = (inits && tails)  
