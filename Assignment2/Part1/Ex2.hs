module Ex2 where

import Ex1 ( ListBag, getListBag, empty, insert )

-- Simply reconvert the multiset into a list,
-- then apply the standard foldr operation for lists
-- while completely ignoring the multiplicties and
-- selecting just the element with fst.

instance Foldable ListBag where
    foldr f z = foldr (f . fst) z . getListBag

-- First, reconvert the multiset into a standard list.
-- Then, starting from the empty ListBag, use `insert`
-- to progressively combine the results of the function.
-- The inserting function will simply be reapplied to the tuple,
-- but also applying f to the value and then leaving the multiplicity as it is.
-- Using `insert` is necessary in order to preserve the well-formedness of the
-- multiset, since the function `f` might not be injective.
-- As an extreme case, consider `const 3`, which always returns the
-- same value no matter the argument. Using `insert`, we ensure that
-- equal values are accumulated with increasing multiplicity.
-- Note how this implicitly relies on the fact that a list
-- of equal elements [a, a, a, ...] of size k maps to a list of
-- elements [f a, f a, f a, ...], with cardinality still equal to `k`,
-- without having to insert again each and every element k times
-- (this is what would happen if we used the toList and fromList functions).
-- We essentially exploit the fact that the function `f` is a pure
-- function with referential transparency to avoid having to compute it
-- (and insert its values) multiple times for the same elements.
-- Fortunately, this is Haskell, and we don't need to worry about this.

mapLB :: Eq b => (a -> b) -> ListBag a -> ListBag b
mapLB f = foldr (uncurry (insert . f)) empty . getListBag

-- "Why it is not possible to define an instance of Functor
-- for ListBag by providing mapLB as the implementation of fmap?"
--
-- In order to maintain the well-formedness of ListBag, we
-- necessarily need to constraint the internal element type of
-- the final bag `b` to be subject to the (Eq b) constraint, in order
-- to check and remove duplicate elements. This is both reflected in the
-- type for `insert`, and transitively in the type of `mapLB` itself.
-- However, defining an instance of Functor for ListBag requires `mapLB`
-- to be independent on the particular type of the multiset, without
-- being able to assume an (Eq b) constraint on the internal type of the type constructor.
-- The type of `fmap` reflects this fact by having no constraint nor assumption on
-- the type being applied to the Functor for which it is defined.
-- Haskell notices the requirement for the (Eq b) constraint of the
-- mapLB function, and even suggests this possible fix:
{-
instance Functor ListBag where
  fmap = mapLB
         ^^^^^

No instance for (Eq b) arising from a use of ‘mapLB’
  Possible fix:
    add (Eq b) to the context of
      the type signature for:
        fmap :: forall a b. (a -> b) -> ListBag a -> ListBag b
-}
-- This would require changing the definition of fmap itself, inside
-- the Functor class. If we hypotesized a typeclass FunctorEq with such
-- a constraint in an hypotetical fmapEq, this would be indeed possible.
-- Alternatively, it could be possible to present a `mapLB` without equality
-- constraint that, however, does not produce well-formed ListBags.

-- A similar problem can be found in the documentation for the Data.Set
-- library for Haskell, where the Ord requirement prevents the type
-- to have a well-formed Functor instance. (a simple function called `map` is provided.)
