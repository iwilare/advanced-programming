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

import java.lang.annotation.*;
        
/**
 * Class for the XMLfield annotation.
 *
 * @author Andrea Laretto
 */

// We indicate that this annotation should be applied to fields.
@Target({ElementType.FIELD})
// This annotation must be present at runtime, since XMLSerializer will also run at runtime.
@Retention(RetentionPolicy.RUNTIME)
public @interface XMLfield {
    // The names of these functions are also the same names that the 
    // XMLfield annotation will require and/or have as default.
    String type();
    
    // We use the empty string to signal the fact that it has not been supplied.
    // Note that using null here is not possible and Java disallows this,
    // since "the default element must be a constant expression", and null
    // is not such an expression, as it can be read in the Java documentation[1].
    // [1]: https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.28
    
    String name() default "";
}
