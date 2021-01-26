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

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Main class implementing the serialization logic.
 *
 * @author Andrea Laretto
 */
public class XMLSerializer {
    public static void serialize(Object[] arr, String fileName) throws IOException, IllegalAccessException, SerializationException {
        // Open the destination file, using a try-with-resources
        // instantiating a BufferedWriter for efficiency.
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            // Write the standard XML 1.0 preamble to the file.
            writePreamble(bw);

            // Sequentially combine the serialization of all the elements in the array.
            for(Object e : arr)
                serialize(e, bw);
        }
    }
    // Straightforward helper functions to deal with XML serialization.
    private static void writePreamble(Writer w) throws IOException {
        w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    }
    private static void writeOpeningTag(Writer w, String tag) throws IOException {
        w.write("<");
        w.write(tag);
        w.write(">");
    }
    private static void writeOpeningTag(Writer w, String tag, String attributeName, String attributeValue) throws IOException {
        w.write("<");
        w.write(tag);
        w.write(" ");
        w.write(attributeName);
        w.write("=\"");
        w.write(attributeValue);
        w.write("\">");
    }
    private static void writeClosingTag(Writer w, String tag) throws IOException {
        w.write("</");
        w.write(tag);
        w.write(">");
    }
    private static void writeIndentation(Writer w) throws IOException {
        w.write("\t");
    }
    private static void writeNewline(Writer w) throws IOException {
        w.write("\n");
    }
    /**
     * Serialize the given object using the given Writer interface.
     * We do not assume that our destination is a BufferedWriter so that it can
     * be extended with other concrete implementations in the future.
     * @param e The object to serialize.
     * @param w The Writer to which the serialization stream is written to.
     * @throws IOException if an error occurs while interacting with the Writer.
     * @throws SerializationException if an invalid annotation is present.
     * @throws IllegalAccessException if the security settings don't allow accessing private fields.
     */
    public static void serialize(Object e, Writer w) throws IOException, SerializationException, IllegalAccessException {
        // Using the generic wildcard argument <?> available on Class
        // allows us to get rid of a unchecked/unsafe operation warning.
        Class<?> c = e.getClass();
        // If the element possessed the XMLable annotation,
        if(c.isAnnotationPresent(XMLable.class)) {
            // Get the fully qualified name of the class; the FQN is necessary
            // and strictly required later for deserialization, since we need to
            // include the package information to uniquely look up the class name.
            String className = c.getCanonicalName();

            writeOpeningTag(w, className);

            writeNewline(w);

            // Sequentially try to serialize all the declared fields in the object.
            for(Field f : c.getDeclaredFields())
                serializeField(e, f, w);

            writeClosingTag(w, className);

            writeNewline(w);
        } // else, simply ignore it.
    }
    /**
     * Serialize the given field using the value present in the given object.
     * @param e The object from which the field is taken from.
     * @param f The current field object.
     * @param w The Writer to which the serialization stream is written to.
     * @throws IOException if an error occurs while interacting with the Writer.
     * @throws SerializationException if an invalid annotation is present.
     * @throws IllegalAccessException if the security settings don't allow accessing private fields.
     */
    public static void serializeField(Object e, Field f, Writer w) throws IOException, SerializationException, IllegalAccessException {
        // Check that the XMLfield annotation is present;
        XMLfield ann = f.getAnnotation(XMLfield.class);
        if(ann == null)
            return; // else, simply don't serialize this field.

        // If the field type is not a primitive nor a String, then the annotation
        // is invalid and we throw a SerializationException.
        if(!isValidSerializableType(f.getType()))
            throw new SerializationException("Invalid not serializable type \"" + f.getType() + "\" in the field.");

        // Get the annotation elements, and check for default value;
        // if the default value is used, use the class field name.
        String fieldName = ann.name().equals("") ? f.getName() : ann.name();
        String fieldType = ann.type();

        // Check that the type declared in the annotation is serializable.
	    // (i.e.: a primitive type or a String)
        if(!isValidAnnotationType(fieldType))
            throw new SerializationException("Invalid not serializable type \"" + fieldType + "\" in the annotation.");

	    // Furthermore, ensure that the type declared by the annotation actually
	    // corresponds with the one declared as field type. If this were not the case,
	    // it would raise an IllegalArgumentException during deserialization.
	    // (this would happen even if a casting between types were possible.)
	    if(!f.getType().getSimpleName().equals(fieldType))
                throw new SerializationException("The type declared by the annotation \"" + fieldType + "\" doesn't match the field type \"" + f.getType().getSimpleName() + "\".");

        // For readability, indent the attributes of the objects.
        writeIndentation(w);

        // Write the field tag indicating the attribute, using the type
        // declared in the annotation.
        writeOpeningTag(w, fieldName, "type", fieldType);

        // Save the accessibility for later, so that it can be restored;
        // The method Field::isAccessible is deprecated, and this is the suggested way.
        boolean isAccessible = f.canAccess(e);

        // Suppress Java access checking for this field in case it's private.
        // If this is not done, an IllegalAccessException will be raised here.
        f.setAccessible(true);

        // Get and write the concrete value of the field on the current object.
        // WARNING: If the element is a String and it contains a "<" or ">" character
        //          it will essentially corrupt the entire XML document. Unfortunately,
        //          no out-of-the-box escaping mechanism specific for XML
        //          exists and is available in the Java library, so for
        //          this exercise we have to avoid this problem in order
        //          to not overcomplicate the solution.
        w.write(f.get(e).toString());

        // Restore the original field accessibility.
        f.setAccessible(isAccessible);

        writeClosingTag(w, fieldName);

        writeNewline(w);
    }
    // Simple helper function that incapsulates the validation of types in class fields.
    private static boolean isValidSerializableType(Class c) {
        return c.isPrimitive() || c.equals(String.class);
    }
    // Simple helper function that incapsulates the validation of types in annotations.
    private static boolean isValidAnnotationType(String typeName) {
        Set<String> validTypes = new HashSet<>(Arrays.asList(
            "String", "int", "double", "float", "short", "long", "byte", "char", "boolean"));
        return validTypes.contains(typeName);
    }
}

// A simple wrapper on Exception for our custom serialization exceptions.
class SerializationException extends Exception {
    SerializationException(String message) {
        super(message);
    }
}
