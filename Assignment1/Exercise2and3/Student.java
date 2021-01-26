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

/**
 * An example class with all the relevant cases for the XMLfield annotator.
 *
 * @author Andrea Laretto
 */

// Add the root class annotation, indicating that this class is XML-serializable.
@XMLable
public class Student {

    // Serializable fields.

    // Testing both for String and for primitive types.

    @XMLfield(type="String")
    private String firstName;

    // Test both cases of using a custom XML element name or using the default one.

    @XMLfield(type="String", name="surname")
    private String lastName;

    @XMLfield(type="int")
    private int age;

    @XMLfield(type="int", name="id")
    private int identifier;

    // Also test different visibilities and other more complex types.

    @XMLfield(type="float")
    public float nameRatio;

    @XMLfield(type="char", name="lastNameChar")
    protected char initial;

    // Empty constructor required for deserialization.

    public Student() {}

    // Below is just regular boilerplate code for a simple Java class.

    public Student(String firstName, String lastName, int age, int identifier) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.identifier = identifier;
        this.nameRatio = (float)firstName.length() / lastName.length();
        this.initial = lastName.charAt(0);
    }

    @Override
    public String toString() {
        return getFirstName() + ", " + getLastName()+ ", " + getAge() + ", " + getIdentifier() + ", " + getNameRatio() + ", " + getInitial();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public float getNameRatio() {
        return nameRatio;
    }

    public void setNameRatio(float nameRatio) {
        this.nameRatio = nameRatio;
    }

    public char getInitial() {
        return initial;
    }

    public void setInitial(char Initial) {
        this.initial = initial;
    }
}
