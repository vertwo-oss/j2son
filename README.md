# JSON Parser in Java, From Scratch

No external libraries.  Vanilla Java 11.

I had no idea this was some colossal landmine.

https://seriot.ch/projects/parsing_json.html

## It's slow.

But it appears to work.

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
js.getString(); // If this Json object is a string.
js.getArray(); // Returns a List< Json >, when the JSON value is an array.
js.getMap(); // Returns a Map< String, Json >, whent he JSON value is an object.
```

If you want to see what's actually in the object, call `dump()`:

```
js.dump();
```

Which will recursively dump the contents.



*[It does use the Properties hack for Unicode decoding.]*
