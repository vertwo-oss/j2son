# JSON Parser in Java, From Scratch

No external libraries.  Vanilla Java 11.

I had no idea this was some colossal landmine, but once I got into it, I was inspired by this quote from Hacker News (link below):

> *"Well, first and most obviously, if you are thinking of rolling your own JSON parser, stop and seek medical attention."*

Then:

> *"Takeaways: Don't parse JSON yourself"*

And, finally, as if I wasn't already taking this as a personal challenge:

> *"Or do you really think you could wrap your head all around those banana peels, and put together a robust, production-ready parser in a weekend?"*

Sorry, Hacker News, that's **exactly** what I've done.

## Is it really that bad to parse JSON?

https://seriot.ch/projects/parsing_json.html

## It's slow.

But it appears to work.

It can parse a 280 kB file in ~0.2 sec:

```
-rw-r--r--@ 1 troy  staff  285152 Jun 19 ... test.json

real  0m0.159s
user  0m0.270s
sys   0m0.031s
```

Not great.

Compared with PHP's `json_decode()`:

```
real  0m0.090s
user  0m0.044s
sys   0m0.019s
```

So, 80% slower.  Like I said, not great.  Could be, though, that I output the whole file and the whole parse.

When I don't do all that stuff to STDOUT:

```
real  0m0.138s
user  0m0.143s
sys   0m0.025s
```

Just 55% slower.

## It's a bit too permissive.

On the tests which **should** fail, it succeeds.  Especially on the trailing garbage tests.  But, it works.

But, I'm not the only one this happens to:

> *Several other parsers (Obj-C TouchJSON, PHP, R rjson, Rust json-rust, Bash JSON.sh, C jsmn and Lua dkjson) will also erroneously parse [1.]. One may wonder if, at least in some cases, this bug may have spread from JSON_Checker because parser developers and testers used it as a reference, as advised on json.org.*

## It's pretty easy to use, though.

When you have a JSON string, and you want to use this to parse it:

```
String input = ...;
final Json js = Json.parse( input );
```

Using it is pretty basic:

```
js.getString();   // If this Json object is a string.
js.getArray();    // Returns a List< Json >, when the JSON value is an array.
js.getMap();      // Returns a Map< String, Json >, when the JSON value is an object.
```

If you want to see what's actually in the object, call `dump()`:

```
js.dump();
```

Which will recursively dump the contents:

```
>>> ---- y_array_with_several_null.json ----
>>> >[1,null,null,null,2]<
<<< [1, (null), (null), (null), 2]
================================================
   Datum type: JSON_[array] System.out.println(8)
        value: [1, (null), (null), (null), 2]
------------------------------------------------
          key: null
------------------------------------------------
    isBoolean? false
       isLong? false
     isDouble? false
     isString? false
------------------------------------------------
    ================================================
       Datum type: JSON_LONG System.out.println(3)
            value: 1
    ------------------------------------------------
              key: null
    ------------------------------------------------
        isBoolean? false
           isLong? true
         isDouble? false
         isString? false
    ------------------------------------------------
    ================================================
       Datum type: JSON_NULL System.out.println(-1)
            value: null
    ------------------------------------------------
              key: null
    ------------------------------------------------
        isBoolean? false
           isLong? false
         isDouble? false
         isString? false
    ------------------------------------------------
    ================================================
       Datum type: JSON_NULL System.out.println(-1)
            value: null
    ------------------------------------------------
              key: null
    ------------------------------------------------
        isBoolean? false
           isLong? false
         isDouble? false
         isString? false
    ------------------------------------------------
    ================================================
       Datum type: JSON_NULL System.out.println(-1)
            value: null
    ------------------------------------------------
              key: null
    ------------------------------------------------
        isBoolean? false
           isLong? false
         isDouble? false
         isString? false
    ------------------------------------------------
    ================================================
       Datum type: JSON_LONG System.out.println(3)
            value: 2
    ------------------------------------------------
              key: null
    ------------------------------------------------
        isBoolean? false
           isLong? true
         isDouble? false
         isString? false
    ------------------------------------------------
```

## Test Suite

If you wanna run the "Test Suite", I assume that the build outputs are parallel to the `src` directory, in a directory called `out`, and that the class files are found in in `out/production/j2son`.  IntelliJ users should find this build location familiar.  Obviously, adapt this to whatever you need, but you'll have to change the test-runner file and driver script.

In the `src` directory (and not in `out`, the build ouputs directory), you can run:

```
$ make
```

to run all the tests, or if you just wanna run a group of them, use these targets:

```
$ make y
...
$ make n
...
$ make i
...
```

To see the test suite I pulled from:

https://github.com/nst/JSONTestSuite

Which is (again) discussed here:

https://seriot.ch/projects/parsing_json.html

and also here, on Hacker News:

https://news.ycombinator.com/item?id=12796556

## Miscellany

This library *DOES* use the Properties hack for Unicode escape sequence decoding.

https://stackoverflow.com/questions/13700333/convert-escaped-unicode-character-back-to-actual-character

## License: MIT

```
/*
 * Copyright (c) 2013-2025 - Troy Wu
 *
 * The MIT License (MIT)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
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
 ```

## Have fun!
