module Ex1 (
    -- We should not export the constructor nor the deconstructor in
    -- order hide the implementation details of the type from the user.
    -- However, as the list representation might be required in the next exercise,
    -- we can imagine that the getListBag function is not defined in the constructor
    -- and that it is just a converter for our multisets into a more convenient representation.
    -- For the same reason, we should wrap the constructor LB into a mkListBag constructor with both
    -- getListBag and mkListBag, named as it is custom in the standard Haskell naming convention.
    -- We still need to export the LB constructor in order for the test files to work out-of-the-box.
    ListBag(..),
    mkListBag,
    wf,
    empty,
    singleton,
    insert,
    fromList,
    isEmpty,
    mul,
    toList,
    sumBag,
) where

import Data.List  ( nub, replicate )
import Data.Maybe ( fromMaybe )

-- ListBag type constructor.
-- We could have also used a newtype definition here to
-- avoid the (laziness) overhead of the LB constructor.
-- Adding a getListBag destructor function still allows us
-- to pattern match on the LB constructor, while also enabling
-- us to write most functions in a point-free notation.
data ListBag a = LB { getListBag :: [(a, Int)] }
  deriving (Show, Eq)

-- Wrap the constructor LB inside an exportable function, in
-- order to hide the implementation details and ensure that
-- ListBag objects constructed are well-formed.
mkListBag :: Eq a => [(a, Int)] -> Maybe (ListBag a)
mkListBag xs | wf (LB xs) = Just (LB xs)
mkListBag _ = Nothing

-- In order to check for well-formedness, we proceed as follows:
-- First, get a list of all the elements ignoring multiplicities.
-- Then, use the (unfortunately) quadratic time function
--     nub :: Eq a => [a] -> [a]
-- from Data.List to filter out duplicates. If there are
-- no such duplicates by comparing it to the initial
-- list, the multiset is well-formed.
-- Finally, check that all the multiplicities are non-negative.
wf :: Eq a => ListBag a -> Bool
wf lb = nonNegativeMultiplicities lb && uniqueElements lb
  where uniqueElements = (\xs -> xs == nub xs) . map fst . getListBag
        nonNegativeMultiplicities = all ((>=0) . snd) . getListBag

-- The empty multiset coincides with the empty list.
empty :: ListBag a
empty = LB []

-- Simply create an element with multiplicity one.
singleton :: a -> ListBag a
singleton v = LB [(v, 1)]

-- Auxiliary function that simply inserts the given element `v`
-- with multiplicity `k` in the given bag. This function increases
-- the multiplicity of the given element by `k` if it's already present,
-- otherwise it insert it with multiplicity `k`. This function
-- preserves well-formedness.
insert :: Eq a => a -> Int -> ListBag a -> ListBag a
insert v k = LB . insert' . getListBag
  where insert' [] = [(v, k)]
        insert' ((v', x):xs)
          | v == v'   = (v, x + k):xs
          | otherwise = (v', x):insert' xs

-- Simply apply the `insert` function just defined to all the elements of the given
-- list, using 1 as multiplicity for all elements and starting from the empty list.
fromList :: Eq a => [a] -> ListBag a
fromList = foldr (flip insert 1) empty

-- Use the "is empty list" function `null` from the Prelude.
isEmpty :: ListBag a -> Bool
isEmpty = null . getListBag

-- Quite conveniently, the Prelude includes a function `lookup` with type
--     lookup :: forall a b. Eq a => a -> [(a, b)] -> Maybe b
-- We use lookup to search for the element and get its multiplicity,
-- returning 0 if it is absent.
mul :: Eq a => a -> ListBag a -> Int
mul v = fromMaybe 0 . lookup v . getListBag

-- Apply the `replicate` function imported from Data.List
--     replicate :: forall a. Int -> a -> [a]
-- to all the elements of the multiset and then `concat` the results.
-- Unfortunately, we need to swap the arguments of `replicate` in order
-- to match them with the order of tuples in the multiset, so have to use `flip`.
-- We then use `uncurry` to apply this function directly to the values in the tuple,
-- creating a list of the same element repeated k times. Finally, apply this
-- to all the tuples and concat the lists together.
toList :: ListBag b -> [b]
toList = concatMap (uncurry $ flip replicate) . getListBag

-- Apply `insert` to all the elements of the second multiset, using
-- the as starting point the first multiset. We again need to `uncurry`
-- in order to apply the tuple element as arguments to insert.
sumBag :: Eq a => ListBag a -> ListBag a -> ListBag a
sumBag a = foldr (uncurry insert) a . getListBag
