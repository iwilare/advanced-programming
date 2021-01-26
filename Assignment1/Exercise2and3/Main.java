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
import java.util.List;

/**
 * Main example class for both the Serialization and Deserialization exercises.
 *
 * @author Andrea Laretto
 */
public class Main {
    public static void main(String[] args) {
        String fileName = "students.xml";

        // Serialization

        Student[] students = {new Student("Andrea", "Laretto", 22, 1),
                              new Student("Alonzo", "Church",  33, 2),
                              new Student("Philip", "Wadler",  64, 3)};

        try {
            System.out.println("Starting serialization...");

            // Serialize the example array of Students to the "students.xml" file.

            XMLSerializer.serialize(students, fileName);
            System.out.println("Serialization completed, written in " + fileName + ".");
        } catch(IOException | IllegalAccessException | SerializationException e) {
            e.printStackTrace();
        }

        // Deserialization

        try {
            System.out.println("Starting deserialization...");
            List<Object> l = XMLDeserializer.deserialize(fileName);
            System.out.println("Deserialization results:");

            // Print the deserialization results.

            for(Object e : l) {
		        Student s = (Student)e;
	            System.out.println(s.toString());
            }
        } catch(DeserializationException | IOException | ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
