import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * A concrete instantiation of the MapReduce framework for the counting words
 * application. The type parameters given to the framework are as follows:
 * @param <K1> String, a filename.
 * @param <V1> List<String>, a list of all the lines in the filename.
 * @param <K2> String, a word.
 * @param <V2> Integer, a per-line number of occurrences of the word.
 * @param <R>  Integer, the global number of occurrences of the word in all files.
 * @author Andrea Laretto
 */
class CountingWords extends MapReduce<String, List<String>,
                                      String, Integer, Integer> {

    /**
      * The input path where files will be read from and the output file.
      */
    Path path;
    File output;

    public CountingWords(Path path, File output) {
        this.path = path;
        this.output = output;
    }

    /**
     * The read function required by the MapReduce framework.
     * Simply provides the initial Stream to be working on, by reading
     * it from the path given in the constructor.
     * @return The initial stream given to the framework.
     */
    @Override
    protected Stream<Pair<String, List<String>>> read() {
        try {
            // Use the helper Reader class
            Reader reader = new Reader(path);
            return reader.read();
        } catch(IOException e) {
            e.printStackTrace();
            return Stream.empty();
        }
    }

    /**
     * The map function required by the MapReduce framework.
     * As specified by the framework, this function is allowed to map not only
     * values, but keys as well.
     * @param s A stream of pairs <filename, list of lines>.
     * @return A stream of pairs <word, occourrences per line>.
     */
    @Override
    protected Stream<Pair<String, Integer>> map(Stream<Pair<String, List<String>>> s) {
        return s.map(Pair::getValue)   // Ignore the filename and only get lines.
                .flatMap(List::stream) // FlatMap all the lines of all files together.
                .map(String::trim)     // Now, preprocess and split each and all the lines.
                .map(w -> w.replaceAll("[^a-zA-Z0-9]+", " "))
                .map(w -> w.split(" "))
                .map(Arrays::asList)   // Each element of the Stream is now a List of words.
                .map(List::stream)     // Convert the List into a Stream, without flatMapping it yet.
                                       // FlatMapping here would collect up ALL
                                       // the words regardless of their membership
                                       // to lines, which is not what the exercise requires.
                .flatMap(words ->
                         words.map(String::toLowerCase)
                              .filter(w -> w.length() > 3)
                              // Collect all identical words inside the line, and sum up their count.
                              // This is similar to the usual collect used in the MapReduce
                              // framework, but we can instead irectly use a HashMap to
                              // collect all the per-line results.
                              // This function essentially exploits a similar mechanism
                              // as the one described in the "group" function and
                              // the Collectors::groupingBy method of the
                              // MapReduce framework, by collecting results into a Map.
                              // Its main components are described as follows:
                              //     - the first function provides a "keyMapper"
                              //       (identical to the string itself, since words are keys)
                              //     - the second function is a "valueMapper": in this
                              //       case, each word counts as one.
                              //     - a "mergeFunction" function that combines
                              //       values and keys into the map.
                              // By looking into the documentation and implementation
                              // details, we can see that it internally uses a HashMap to
                              // collect the words.
                             .collect(Collectors.toMap(Function.identity(),
                                                       word -> 1,
                                                       Integer::sum))
                             .entrySet()
                             .stream()
                             .map(MapReduce::entryToPair));
                             // Now that we have converted the per-line words
                             // along with their frequencies back into a stream,
                             // we can now flatMap them back into the main stream.
    }

    /**
     * The reduce function required by the MapReduce framework.
     * @param s A stream of pairs <word, list of per-line occurrences>.
     * @return A stream of pairs <word, total occurrences>.
     */
    @Override
    protected Stream<Pair<String, Integer>> reduce(Stream<Pair<String, List<Integer>>> s) {
        // Our stream has been collected by the group function inside the
        // framework. So, each elements of the stream is a pair containing a word
        // and a list of occurrences in all the lines where it appears.
        // Simply sum up all the number of line occurrences inside the List.
        // Quite conveniently, we can simply reuse Java Streams to calculate
        // the sum of all the integers in a list, by transforming it into a stream
        // and then essentially applying a foldl operation.
        return s.map(p -> new Pair<>(p.getKey(),
                                     p.getValue().stream().reduce(0, Integer::sum)));
    }

    /**
     * Comparison function required for the MapReduce framework.
     * @param k1 The first string to compare
     * @param k2 The second string to compare
     * @return A value <0, ==0, >0 according to the Comparable interface specs.
     */
    @Override
    protected int compare(String k1, String k2) {
        return k1.compareTo(k2);
    }

    /**
     * Output writing function required by the MapReduce framework as final step.
     * Simply write the output to the path specified by the path
     * specified as instance member of the class.
     * @param s The stream to write as output
     */
    @Override
    protected void write(Stream<Pair<String, Integer>> stream) {
        try {
            // Simply use the helper writer function.
            Writer.write(output, stream);
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Main function showing the functionalities of the CountingWords class.
     */
    public static void main(String[] args) {
        System.out.println("Please insert the directory path where the *.txt files are stored (default: \"./Books\"): ");
        Scanner s = new Scanner(System.in);
        String input = s.nextLine();
        Path path = Paths.get(input.isEmpty() ? "Books" : input);
        String outputFilename = "word-count.csv";
        File outputFile = new File(outputFilename);
        CountingWords cw = new CountingWords(path, outputFile);
        cw.mapReduce();
        System.out.println("Output has been written to file \"" + outputFilename + "\".");
    }
}
