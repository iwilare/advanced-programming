/*
 * MIT License
 *
 * Copyright (c) 2020 Andrea Laretto
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.regex.Pattern;

/**
 * Main class implementing the deserialization logic.
 *
 * @author Andrea Laretto
 */
public class XMLDeserializer {
    /**
     *
     * Main deserialization function that reads the elements in fileName.
     *
     * @param fileName The file name from which the deserialization is performed.
     * @return A list of Objects deserialized from the file specified.
     * @throws DeserializationException if one or more classes read is non-deserializable.
     * @throws IOException in case a I/O exception in the reading stream occurs.
     * @throws IllegalAccessException if the security settings don't allow accessing private fields.
     * @throws NoSuchMethodException  if no default constructor can be found in the class.
     * @throws InstantiationException if an exception occurs while trying to instantiate the class.
     * @throws ClassNotFoundException if the class specified by the XML stream doesn't exist.
     */
    public static List<Object> deserialize(String fileName) throws DeserializationException, IOException, IllegalAccessException, NoSuchMethodException, InstantiationException, ClassNotFoundException {
        // Open the source file, using a Scanner for parsing.
        Scanner s = new Scanner(new BufferedReader(new FileReader(fileName)));

        // Parse the standard XML 1.0 preamble.
        parsePreamble(s);

        // Sequentially combine the deserialization of all the elements in the file.
        List<Object> result = new ArrayList<>();
        while(s.hasNext())
            result.add(deserialize(s));

        // Flush and close the stream.
        s.close();

        return result;
    }

    // Helper function to skip spaces.
    private static void skipSpace(Scanner s) throws IOException {
        s.skip(Pattern.compile("[ \t\n]*"));
    }
    // Helper function to parse whitespace surrounding string literals.
    private static void token(Scanner s, String t) throws IOException {
        skipSpace(s);
        s.skip(Pattern.quote(t));
    }
    // Helper function to parse a simple identifier in XML.
    private static String identifier(Scanner s) throws IOException {
        skipSpace(s);
        s.skip(Pattern.compile("[a-zA-Z0-9_.]+"));
        return s.match().group();
    }
    // Read a string literal until the closing tag is found.
    private static String parseContent(Scanner s) {
        // As mentioned in the Serializer, we assume for simplicity that
        // Strings do not contain any XML structure-breaking "<" or ">" elements.
        s.skip(Pattern.compile("[^<]*")); // Get until the start of the next tag.
        return s.match().group();
    }
    // Parsing function to eat the XML preamble.
    private static void parsePreamble(Scanner s) throws IOException {
        token(s, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    }
    /**
     * Deserialize a single object from the input Scanner.
     * Throws the same general exceptions as the main method indicates.
     * @param s The Scanner from which to read the tokens.
     * @return null if the serialized class is not serializable,
     *              or an internal reflection error occured.
     */
    public static Object deserialize(Scanner s) throws IOException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException, DeserializationException {
        // Parse the initial identifier.
        token(s, "<");
        String tag = identifier(s);
        token(s, ">");

        // Get the class expressed by the initial element tag, which is required
        // to be the fully qualified name for the class. That specific
        // choice was necessary so that it can be looked up in this point here.
        Class<?> c = Class.forName(tag);

        // The XMLable annotation must be present on the class to be deserializable.
        if(!c.isAnnotationPresent(XMLable.class))
            return null;

        // Furthermore, this class should have a default constructor that takes
        // no arguments and returns an instance of the class, so we get and invoke it.
        Object o;
        try {
            o = c.getDeclaredConstructor().newInstance();
        } catch(NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            // If the method doesn't exist or running it creates an exception,
            // we have to return null and avoid the serialization of this object.
            return null;
        }

        // Check that all the field respect the constraints and then
        // parse them in order from the XML.
        for(Field f : c.getDeclaredFields()) {
            // Check if the field violates the following constraints:
            //    - Has to be non-static (we use Modifier to get this information)
            //    - Has to be of primitive or String type
            //    - Has to be annotated with XMLfield
            if(Modifier.isStatic(f.getModifiers())
               || !f.getType().isPrimitive() && !f.getType().equals(String.class)
               || !f.isAnnotationPresent(XMLfield.class))
                return null;
            // Else, start reading the field and insert it into the object.
            deserializeField(s, c, f, o);
        }

        token(s, "</");
        token(s, tag); // The closing and initial tag must be the same.
        token(s, ">");

        skipSpace(s);

        return o;
    }
    /**
     * Deserialize the indicated field and insert its value inside the given object.
     *
     * @param s The Scanner from which to read the input tokens.
     * @param c The Class of the object being deserialized.
     * @param f The Field object being deserialized.
     * @param o The target Object whose field will be set.
     */
    public static void deserializeField(Scanner s, Class c, Field f, Object o) throws IOException, DeserializationException, IllegalAccessException, IllegalArgumentException {
        // Start parsing the field XML.

        token(s, "<");
        String fieldName = identifier(s);
        skipSpace(s);

        // Parse the type attribute.
        token(s, "type");
        token(s, "=");
        token(s, "\"");
        String type = identifier(s);
        token(s, "\"");
        token(s, ">");

        // Get the annotation elements and the name used in the XML field serialization.
        XMLfield annotation = f.getAnnotation(XMLfield.class);
        String annotationName = annotation.name();
        String annotationType = annotation.type();
        String serializationName = annotationName.equals("") ? f.getName() : annotationName;

        // If the field read here doesn't coincide with the
        // serialization-declared one, then this object is badly formed.
        if(!serializationName.equals(fieldName))
            throw new DeserializationException("XML field name doesn't correspond with serialization name.");

        // Check that the type in the XML corresponds with the declared one in the annotation.
        if(!annotationType.equals(type))
            throw new DeserializationException("XML type doesn't correspond with serialization type.");

	    // Ensure that the type declared in the XML attribute corresponds with the one
        // effectively declared in the class. If this were not the case, it would raise an
        // IllegalArgumentException while trying to inject a value with the wrong type to the object field.
	    // (this would happen even if a casting between types were possible.)
	    if(!f.getType().getSimpleName().equals(type))
            throw new DeserializationException("The type declared by the annotation \"" + type + "\" doesn't match the field type \"" + f.getType().getSimpleName() + "\".");

        // Read the entire content of a tag from the XML stream.
        String content = parseContent(s);

        // Parse the content of the tag according to the type.
        Object value = parsePrimitive(type, content);

        if(value == null)
            throw new DeserializationException("An error occured while serializing a field.");

        skipSpace(s);

        token(s, "</");
        token(s, fieldName); // The closing and initial tag must be the same.
        token(s, ">");

        // Inject the field and its content inside the object.
        injectField(f, o, value);
    }
    /**
     * Inject the value expressed by content inside the object e with the fieldName specified.
     * As usual, we need to temporarily disable the privacy of the class.

     * @param f The Field object being deserialized.
     * @param o The target Object whose field will be set.
     * @param v The value being set as field value.
     */
    private static void injectField(Field f, Object o, Object value) throws IllegalArgumentException, IllegalAccessException {
        // As before, we need to change the privacy setting in order to access private fields.

        // Save the accessibility for later, so that it can be restored;
        // This time we can't use the Field::canAccess method, since there is
        // not yet any object to check it for. We can use the Field::getModifiers
        // method (which returns a decoded int) and then check if it's private
        // using the Modifier::isPrivate decoder.
        boolean isPrivate = Modifier.isPrivate(f.getModifiers());

        // Suppress Java access checking for this field in case it's private.
        // If this is not done, an IllegalAccessException will be raised here.
        f.setAccessible(true);

        // Forcibly set the field of the object to the content provided.
        f.set(o, value);

        // Restore the original field accessibility.
        f.setAccessible(isPrivate);
    }
    /**
     * Simple procedure to parse one of the primitive types.
     * Unfortunately, I have discovered the hard way (i.e.: through some hours of
     * reading and debugging internal Java Scanner code) that "22</tag>" and the
     * "<" character cannot be read using the classic Scanner::nextInt method,
     * so we need to get and parse the string manually. This has also been
     * tested and confirmed in a Java program created from scratch.
     * @param type  The type, as a string.
     * @param value The value to be parsed.
     * @return null if the type is invalid.
     */
    private static Object parsePrimitive(String type, String s) {
        // Switch expression! :-)
        return switch(type) {
            case "int"     -> Integer.parseInt(s);
            case "double"  -> Double.parseDouble(s);
            case "float"   -> Float.parseFloat(s);
            case "short"   -> Short.parseShort(s);
            case "long"    -> Long.parseLong(s);
            case "byte"    -> Byte.parseByte(s);
            case "char"    -> s.charAt(0); // Simply get the first character.
            case "boolean" -> Boolean.parseBoolean(s);
            case "String"  -> s; // Return the entire tag content.
            default        -> null;
        };
    }
}

// A simple wrapper on Exception for our custom deserialization exceptions.
class DeserializationException extends Exception {
    DeserializationException(String message) {
        super(message);
    }
}
