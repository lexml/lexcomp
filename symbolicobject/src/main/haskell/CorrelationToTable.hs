module CorrelationToTable where

import Correlation as C
import FlatCorrelation as F
import FlatCorrelationToTable as FT
import Table as T
import Control.Arrow
import Data.List as L

correlationToTable :: [Correlation [r] [d]] -> Table (FT.CorrelationCellData r d)
correlationToTable =  L.concatMap (F.flatten >>> L.map FT.flatCorrelationToTable) >>> vcats


