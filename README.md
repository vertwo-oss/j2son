# JSON Parser in Java, From Scratch

No external libraries.  Vanilla Java 11.

I had no idea this was some colossal landmine.

https://seriot.ch/projects/parsing_json.html

## It's slow.

But it appears to work.

It can parse a 280 kB file in ~0.2 sec:

```
-rw-r--r--@ 1 troy  staff  285152 Jun 19 ... test.json

real	0m0.198s
user	0m0.228s
sys	0m0.029s
```

Not great.  Pretty horrifying, I'm sure.

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
js.getMap();      // Returns a Map< String, Json >, whent he JSON value is an object.
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



*[It does use the Properties hack for Unicode decoding.]*
