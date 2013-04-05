import Data.List
import Control.Arrow
import qualified Data.Set as S
import qualified Data.Map as M
import Data.Maybe

data Tree a = Tree a (Forest a) deriving (Eq,Ord,Show)
type Forest a = [Tree a]

value :: Tree a -> a 
value (Tree v _) = v

children :: Tree a -> [Tree a]
children (Tree _ l) = l

extracts :: Forest a -> [(a,Forest a, Forest a, Forest a)]
extracts [] = []
extracts (n@(Tree x f):l) = (x,[],f,l) : map (\(x',ll,lc,lr) -> (x',n : ll,lc,lr)) (extracts l)

type ITree a = Tree (Int,a)
type IForest a = Forest (Int, a)

enumerate :: Int -> Forest a -> (Int,IForest a)
enumerate = mapAccumL $ \n (Tree v l) ->
  let (n',l') = enumerate (n+1) l
    in (n', Tree (n,v) l')

foldForest :: (a -> [b] -> b) -> Forest a -> [b]
foldForest h = map (foldTree h)

foldTree :: (a -> [b] -> b) -> Tree a -> b
foldTree h t = h (value t) (foldForest h (children t))


data LTree a =   LNode (LForest a) | LLeaf deriving (Eq,Ord,Show)
type LForest a = [(a,LTree a)]

type IT = Tree Int
type IF = Forest Int

collect :: IForest a -> (M.Map Int a, IF)
collect = merge . foldForest f  
  where
    f (i,x) = merge >>> (M.insert i x *** Tree i)
    merge = unzip >>> first M.unions

ltreeFold :: a -> ([(b,a)] -> a) -> LTree b -> a
ltreeFold c f LLeaf = c
ltreeFold c f (LNode l) = ltreeForest c f l

ltreeForest :: a -> ([(b,a)] -> a) -> LForest b -> a
ltreeForest c f l = f [(i,ltreeFold c f t) | (i,t) <- l]

ltreeProduct :: LTree a -> LTree a -> LTree a
ltreeProduct x y = ltreeFold y LNode x


data WrapPos a = Root | FirstBelow a | FirstAfter a deriving (Eq,Ord,Show)

data Operation a = Pop a | Wrap a (WrapPos a) [Int] | Replace a a deriving (Eq,Ord,Show)

genCost eq (Pop _) = 1.0
genCost eq (Wrap _ _ _) = 1.0
genCost eq (Replace x y) | eq x y = 0.0
                         | otherwise = 1.0

type PF = LForest [Operation Int]
type PT = LTree [Operation Int]

lastMaybe [] = Nothing
lastMaybe l = Just (last l)

single [x] = True
single _ = False

problem :: Forest a -> Forest a -> (IF,IF,M.Map Int a, M.Map Int a, PT)
problem of1 of2 = (f1,f2,m1,m2,problemTree Nothing f1 f2)
  where
    (i,if1) = enumerate 0 of1
    (_,if2) = enumerate i of2
    (m1,f1) = collect if1
    (m2,f2) = collect if2

    problemTree :: Maybe Int -> IF -> IF -> PT
    problemTree ctx f g = case problemForest ctx f g of
      [] -> LLeaf
      l -> LNode l

    wrapPos Nothing  []  = Root
    wrapPos (Just r) []  = FirstBelow r
    wrapPos _        l   = FirstAfter (value (last l))

    problemForest :: Maybe Int -> IF -> IF -> PF
    problemForest _ [] [] = []
--    problemForest [] g  = [([Insert g],LLeaf)]
--    problemForest f  [] = [([Delete f],LLeaf)]
    problemForest mctx f g = (pops :: PF) ++ (wraps :: PF) ++ (dividesOrReplaces :: PF)
      where
          ef = extracts f
          eg = extracts g
          pops =  [([Pop v], problemTree mctx (ll ++ lc ++ lr) g) | (v,ll,lc,lr) <- ef ]
          wraps = [([Wrap v (wrapPos mctx ll) (map value lc)], problemTree mctx f (ll ++ lc ++ lr)) | (v,ll,lc,lr) <- eg ]
          dividesOrReplaces = if not (single f && single g) then concat [ ff | 
             (v1,ll1,lc1,lr1) <- ef,
             (v2,ll2,lc2,lr2) <- eg,
             let pt1 = problemTree mctx [Tree v1 lc1] [Tree v2 lc2],
             let pt2 = problemTree mctx (ll1 ++ lr1) (ll2 ++ lr2),
             let LNode(ff) = ltreeProduct pt1 pt2
             ] else [ ([Replace v1 v2], problemTree (Just v1) (ll1 ++ lc1 ++ lr1) (ll2 ++ lc2 ++  lr2)) |
                       (v1,ll1,lc1,lr1) <- ef,
                       (v2,ll2,lc2,lr2) <- eg ]


showLTree olimit = showLTree' 0 ""
  where
    over = case olimit of
             Nothing -> \ _ -> False
             Just l -> \n -> n >= l
    showLTree' level ind (LNode branches) 
      | over level = "(...)"
      | otherwise = "." ++ concat [ "\n" ++ ind ++ "|\n" ++ ind' ++ "-" ++ sl ++  showLTree' (level + 1) ind'' t | 
                                              (l,t) <- branches, 
                                              let sl = show l, 
                                              let ind' = ind ++ "|",
                                              let ind'' = ind' ++ " "
                                              ]
    showLTree' _ _ LLeaf = "$"                                              

costLTree :: (Operation a -> Double) -> LTree [Operation a] -> LTree (Double,[Operation a])
costLTree cost = ltreeFold LLeaf f
  where 
    f l = LNode [((sum (map cost ops) + best t,ops),t)  | (ops,t) <- l ]
    best (LNode for) = minimum (1E40 : map (fst . fst) for)
    best LLeaf = 0.0

type CostL a = (Double, [Operation a])
type CostT a = LTree (CostL a)

pruneBest :: LTree (Double, [Operation a]) -> LTree (Double, [Operation a])
pruneBest = ltreeFold LLeaf f
  where f :: [(CostL a,CostT a)] -> CostT a  
        f [] = LLeaf
        f l = let m = minimum $ map (fst . fst) l in LNode [(x,y) | (x@(c,_),y) <- l, c == m]

mybest LLeaf = []
mybest (LNode l) = let m = minimum $ map (fst . fst) l in [(x,y) | (x@(c,_),y) <- l, c == m]

ltreePaths = ltreeFold [[]] f
  where f l = [ x : p | (x,s) <- l, p <- s ]

instance Functor LTree where
  fmap f (LNode l) = LNode [(f lab,fmap f t) | (lab,t) <- l]
  fmap _ LLeaf = LLeaf
{-
problemForest :: Forest a -> Forest a -> LForest a
problemForest [] [] = [([],LLeaf)]
problemForest [] g  = [([Insert g],LLeaf)]
problemForest f  [] = [([Delete f],LLeaf)]
problemForest f  g  = 
  let
    deletes = [([Pop i v], | (i,((id,v),f)



data Operation a = Remove (ITree a) | Insert (ITree a) | eplace Int Int | Insert Int | Delete Int

data Assumption a = Replace a a | Insert a | Delete a deriving (Eq,Ord,Show)

type Problem a = (Forest a, Forest a)

type ProblemTree a = LTree (Problem a) [Assumption a]



subProblems :: Problem a -> [SubProblem a]
subProblems (Problem []   []  ) = [ SubProblem []          Nothing ]
subProblems (Problem l    []  ) = [ SubProblem [Removed v] (Just (Problem x [])) | (v,x) <- extracts l ]
subProblems (Problem []   l   ) = [ SubProblem [New v]     (Just (Problem [] x)) | (v,x) <- extracts l ]
subProblems (Problem [tl] [tr]) =  



-}

forest1 = [Tree "a" [Tree "0" [], Tree "b" [], Tree "d" []]]
forest2 = [Tree "a" [Tree "0" [], Tree "e" [], Tree "b" [], Tree "d" []]]

(if1,if2,m1,m2,pt) = problem forest1 forest2

myeq x y = M.lookup x m1 == M.lookup y m2

mycost = genCost myeq

ct = costLTree mycost pt

bt = pruneBest ct

disp t n = mapM_ putStrLn (take 30 (drop (30 * n) ll))
  where
    ll = lines $ (showLTree Nothing t)

minCost (LNode l) = fst $ head l

ot = fmap snd bt

pl = ltreePaths ot
ps = S.fromList (map S.fromList pl)

