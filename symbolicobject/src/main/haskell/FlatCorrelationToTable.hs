module FlatCorrelationToTable where

import qualified FlatCorrelation as F
import qualified Table as T
import Table (Cell(..),content,cs,rs,changeWidth)
import Data.Maybe
import Control.Arrow
import ListUtils

data CorrelationCellData r d = NoData | NodeData r | RelData d deriving (Eq,Ord)

type CorrelationCell r d = Cell (CorrelationCellData r d)

type MultiRowFlatCorrelation d r = F.FlatCorrelation [d] [r]

extractLines :: F.FlatCorrelation r d -> ([(Maybe r,Int,Int)], [(Maybe d, Int, Int)])
extractLines = F.fold f >>> snd >>> unzip >>> id *** catMaybes
  where
    f d nc Nothing = (nc,[((d,nc,1),Nothing)])
    f d nc (Just (mr,(nc',a))) = (nc,[((d,nc,1),Just (mr,nc,nc' - nc))] ++ a)

filterEmpty :: [(Maybe [a],Int,Int)] -> [([a],Int,Int)]
filterEmpty = map f
  where
    f (Just x@(_:_),a,n) = (x,a,n)
    f (_,a,n) = ([],a,n)

completeH :: [([a],Int,Int)] -> [([a],Int)]
completeH = reverse . snd . foldl f (0,[])
  where
    f (curPos,curL) (d,start,len) 
        | curPos < start = (start + len,(d,len) : ([], start - curPos) : curL)
        | otherwise = (start + len, (d,len) : curL)
    

expand :: [([a],Int)] -> [[(Maybe a, Int)]]
expand = foldr f []
   where
    f (al,n) r  = complete (Nothing,n) [(Just a,n) | a <- al] r 

makeCells :: (a -> CorrelationCellData r d) -> [[(Maybe a,Int)]] -> [[CorrelationCell r d]]
makeCells f = map (map (\ (ma,n) -> T.C { content = maybe NoData f ma, rs = 1, cs =  n } ))

expandH = map (concatMap f)
  where
    f (T.C { content = c, rs = rs, cs = cs }) = T.C { content = c, rs = 1, cs = 1 } : replicate (cs - 1) (T.C { content = NoData , rs = 1, cs = 1}) 

flatCorrelationToTable = extractLines >>> (f NodeData >>> expandH >>> dupWidth) *** (f RelData  >>> dupWidth >>> (map (T.C NoData 1 1 :)) ) >>> uncurry (++) >>> T.T
  where
    dupWidth = map (map (changeWidth (*2)))
    f h = filterEmpty >>> completeH >>> expand >>> makeCells h
{-
fcToTable :: MultiRowFlatCorrelation r d -> ([CellData r d],[CellData r d])
fcToTable = F.fold f
  where
    f Nothing Nothing = error "Empty correlation"
    f (Just []) Nothing = ([NC Nothing],[])
    f (Just l
-}    
