import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import java.util.stream.*;

/**
 * A concrete instantiation of the MapReduce framework for the inverted index
 * application.
 *
 * The idea behind the approach taken here can be summarized as follows:
 *    - Start with filename and a list of all lines. (input)
 *    - For each filename, create its inversed index. (map)
 *    - Having all the inverted indexes, combine them
 *      by each word and merge all their occurrences.
 *      Do this by expading the list of occurrences and by
 *      repeating the word each time it appears in the text,
 *      as it is described by the expected CSV format (reduce)
 *    - Orderly output the result for each word. (output)
 *
 * The type parameters given to the framework are as follows:
 * @param <K1> String, a filename.
 * @param <V1> List<String>, a list of all the lines in the filename.
 * @param <K2> String, a word.
 * @param <V2> Pair<String, Integer>, a word occurrence in the given filename and line number.
 * @param <R>  Pair<String, Integer>, as above.
 * @author Andrea Laretto
 */
class InvertedIndex extends MapReduce<String, List<String>,
                                      String, Pair<String, Integer>,
                                      Pair<String, Integer>> {
    /**
      * The input path where files will be read from and the output file.
      */
    Path path;
    File output;

    public InvertedIndex(Path path, File output) {
        this.path = path;
        this.output = output;
    }

    /**
     * The read function required by the MapReduce framework.
     * Simply provides the initial Stream to be working on.
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
     * This function essentially maps each filename to a stream
     * representing its inverted index.
     * @param s A stream of pairs <filename, list of lines>.
     * @return A stream of pairs <word, <filename, line number>>.
     */
    @Override
    protected Stream<Pair<String, Pair<String, Integer>>> map(Stream<Pair<String, List<String>>> s) {
        return
                // Pair each line with its corresponding filename,
                // and enumerate them according to the filename.
                // Quite unfortunately, there is no "zip" function (in the Haskell sense)
                // for Java Streams, so this must be done manually.
                // Wrap a simple Integer value as counter with the AtomicInteger class.
                // Then, flatten all of them in the stream.
                s.flatMap(p -> {
                   AtomicInteger lineNumber = new AtomicInteger(0);
                   return p.getValue()
                           .stream()
                           .sequential() // Logically ensure that the elements are processed in order
                           .map(line -> new Pair<>(line, new Pair<>(p.getKey(), lineNumber.getAndIncrement())));})
                // Each stream element is in the form <line, <filename, line number>>,
                // with the identifying data for the line as a nested pair.
                // As done in the word count problem, further expand all lines into
                // triples <word, <filename, line number>> by tokenizing each line
                // into a stream of its words. Simply flatMap through the key while
                // we break it into words, and then pair each word with the relevant
                // line data <filename, line number>.
                 .flatMap(p -> Arrays.asList(p.getKey()
                                             .trim()
                                             .replaceAll("[^a-zA-Z0-9]+", " ")
                                             .split(" "))
                                     .stream()
                                     .map(String::toLowerCase)
                                     .filter(w -> w.length() > 3)
                                     .map(w -> new Pair<>(w, p.getValue())));
    }

    /**
     * The reduce function required by the MapReduce framework.
     * @param s A stream of pairs <word, List<filename, line number>>.
     * @return  A stream of pairs <word,     <filename, line number>>.
     */
    @Override
    protected Stream<Pair<String, Pair<String, Integer>>> reduce(Stream<Pair<String, List<Pair<String, Integer>>>> s) {
        // Simply expand all the entries for each word into multiple
        // entries, repeating the word for each of them so that
        // it mirrors the required output.
        return s.flatMap(occs -> occs.getValue()
                                     .stream()
                                     .map(p -> new Pair<>(occs.getKey(), p)));
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
     * Simply write the output to the path specified by the InvertedIndex.path
     * path specified as instance member of the class.
     * @param s The stream to write as output
     */
    @Override
    protected void write(Stream<Pair<String, Pair<String, Integer>>> stream) {
        try {
            // Simply use the helper writer function.
            Writer.writeInvertedIndex(output, stream);
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Main function showing the functionalities of the InvertedIndex class.
     */
    public static void main(String[] args) {
        System.out.println("Please insert the directory path where the *.txt files are stored (default: \"./Books\"): ");
        Scanner s = new Scanner(System.in);
        String input = s.nextLine();
        Path path = Paths.get(input.isEmpty() ? "Books" : input);
        String outputFilename = "inverted-index.csv";
        File outputFile = new File(outputFilename);
        InvertedIndex cw = new InvertedIndex(path, outputFile);
        cw.mapReduce();
        System.out.println("Output has been written to file \"" + outputFilename + "\".");
    }
}
