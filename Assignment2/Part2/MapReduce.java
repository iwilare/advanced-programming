import java.util.*;
import java.util.Map.Entry;
import java.util.stream.*;

/**
 * Main abstract application logic for the MapReduce framework.
 *
 * Logically, a MapReduce framework operates as follows:
 *   1. Take a stream of <key, value> data.
 *   2. Apply a function to all the elements of the data, possibly changing the key.
 *   3. Group all the mapped elements into lists according to the value of their mapped key.
 *      For this step, it is required for the type of the mapped key to be
 *      comparable, so that elements with identical key can be collected together.
 *   4. Finally, apply to each element a reduce function that collapses a list of elements into a single one.
 *   5. Output the results with a custom output function.
 *
 * @author Andrea Laretto
 * @param <K1> Key   type of the initial stream.
 * @param <V1> Value type of the initial stream.
 * @param <K2> Key   type of the mapped  stream and the final stream.
 *             It is required to be comparable in order to collect identical results.
 * @param <V2> Value type of the mapped  stream.
 * @param <R>  Value type of the final   stream.
 */
public abstract class MapReduce<K1, V1, K2, V2, R> {
    // First, we define the abstract primitive operations that will be
    // provided (i.e.: overridden) by the concrete implementation.
    // We also declare them as protected so that they are visible to
    // the subclasses, but not visible from outside of the package/class.
    // (note that private abstract methods are in any case invalid)

    // An auxiliary `read` function that provides the initial stream.
    protected abstract Stream<Pair<K1, V1>> read();

    // A `map` function that maps both the key and the element over the initial stream.
    protected abstract Stream<Pair<K2, V2>> map(Stream<Pair<K1, V1>> s);

    // A `reduce` function that reduces a pair of key and list of mapped elements,
    // with the list being appropriately collected by grouping together equal keys.
    protected abstract Stream<Pair<K2, R>> reduce(Stream<Pair<K2, List<V2>>> ps);

    // An auxiliary `write` function that effectfully processes the result as output.
    protected abstract void write(Stream<Pair<K2, R>> s);

    // An helper function that explicitly implements the Comparable interface
    // for the type K2. The requirement for the compare method is due to the fact
    // that we need to be able to collect elements with identical key into the
    // same list. However, it is more convenient to explictly provide the comparison
    // function as abstract method in the class here, instead of having the type
    // K2 implement the Comparable interface in the type parameters.
    // As a practical example, suppose that K2 is a String. If we required a
    // different comparison mechanism than the usual one for strings, we would
    // need to create a wrapper class for String that simply overrides Comparable
    // in the appropriate way. This wrapping object, required for each and all keys,
    // might imply a non-trivial overhead in large streams, even though it is just needed to
    // implement the comparison logic. Therefore, we simply include the comparison
    // mechanism here as "parameter" for the MapReduce framework, the functional way.
    protected abstract int compare(K2 a, K2 b);

    // We can now define the central function to collect and group
    // the stream according to the key and the abstract function compare.
    private Stream<Pair<K2, List<V2>>> group(Stream<Pair<K2, V2>> s) {
        // We can exploit the groupingBy function to collect all the values
        // provided by the input stream into a TreeMap.
        // After looking inside the implementation of groupingBy, we can notice
        // that the standard Collectors::groupingBy taking one classifier is, in fact,
        // a two-layer wrapper over the following default implementation:
        /*
            public static <T, K> Collector<T, ?, Map<K, List<T>>>
                groupingBy(Function<? super T, ? extends K> classifier) {
                    return groupingBy(classifier, toList());
                }
        */
        // Which defaults to collecting the results into a List.
        // In turn, this internal function is a wrapper for the following definition:
        /*
            public static <T, K, A, D> Collector<T, ?, Map<K, D>>
                groupingBy(Function<? super T, ? extends K> classifier,
                           Collector<? super T, A, D> downstream) {
                return groupingBy(classifier, HashMap::new, downstream);
            }
        */
        // This implementation detail explains why there is no variation of
        // groupingBy that fits out specific need of using a Comparable-like
        // `compare` function, acting on an hypotetical comparison model.
        // Indeed, a HashMap is used by default to collect the values, which operates using
        // the internal Java hash function and does not make use of a compare function.
        // We can substitute a HashMap with a custom TreeMap that we can now
        // instruct to use our custom comparison function, using this::compare.
        // Finally, we need to apply Collectors::mapping to keep only values in the list
        // and discard the key, which we finally combine it with the standard
        // List collector. After this, we extract the sorted values from the TreeMap.
        return s.collect(Collectors.groupingBy(Pair::getKey,
                                               () -> new TreeMap<>(this::compare),
                                               Collectors.mapping(Pair::getValue, Collectors.toList())))
                // Get an ordered stream (according to compare) of all the Entries in the Map.
                .entrySet()
                .stream()
                // Simply convert the each Entry of the Map back into a Pair.
                .map(MapReduce::entryToPair);
    }

    // Helper function to convert Map.Entry into a standard Pair.
    static protected <K, V> Pair<K, V> entryToPair(Entry<K, V> e) {
        return new Pair<>(e.getKey(), e.getValue());
    }

    // The mapReduce method is the core functionality provided by the framekwork.
    // Using the template method mechanism we now provide the main functionality/logic
    // of the class, thus this method should not be overridden. We therefore
    // declare it as a final method. We maintain the protected visibility, in case
    // the concrete class wants to keep it private and only provide it in a wrapped fashion.
    protected final void mapReduce() {
        // Simply concatenate all the stream processing steps in a single body.
        write(reduce(group(map(read()))));
    }
}
