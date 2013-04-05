{-# LANGUAGE FlexibleInstances #-}
module Main where

import Correlation
import System.IO
import CorrelationToTable
import FlatCorrelationToTable
import Table
import Control.Arrow
import Data.List as L

readCorrelation :: String -> Correlation [String] [String]
readCorrelation = read

instance Show (CorrelationCellData String String) where
  show NoData = ""
  show (NodeData x) = x
  show (RelData x) = x

test :: [Correlation [String] [String]] -> IO ()
test c = do
  let tableText = show (correlationToTable c)
  writeFile "/tmp/test.html" $
    "<html><head><title>teste</title></head><body>\n"
    ++ tableText
    ++ "\n</body></html>"
    

main = do
  contents <- getContents
  let cs = L.map readCorrelation $ lines contents
      table = correlationToTable cs
  putStrLn "<html><head><title>teste</title></head><body>"
  print table
  putStrLn "</body></html>"


ex1 = CR {  
    value = ["Art.1. Este é um exemplo ...", "(comentário do art.1)"]
  , col = 0 
  , rels = [
       Rel { relData = ["diff (A)"], next = CR {
          value = ["Art. 2. Este é outro exemplo ..."],
          col = 1,
          rels = []
       } }
     , Rel { relData = ["diff (B)"], next = CR {
          value = ["Art. 3. Este é mais um exemplo ..."],
          col = 2,
          rels = []
       } }
     , Rel { relData = ["diff (C)"], next = CR {
          value = ["Art. 4. Este é another example ..."],
          col = 1,
          rels = [
            Rel { relData = ["diff (D)", "(coment. diff D)"], next = CR {
                value = ["Art 5. Just another ...", "(just another longer)","(comment)"],
                col = 2,
                rels = []
              }
            }
          ]
       } }

    ] }

ex2 = CR {  
    value = ["Art.1. Este é um exemplo ...", "(comentário do art.1)"]
  , col = 1  
  , rels = [
     Rel { relData = ["diff (A)"], next = CR {
          value = ["Art. 2. Este é outro exemplo ..."],
          col = 3,
          rels = []
       } }
    ] }

