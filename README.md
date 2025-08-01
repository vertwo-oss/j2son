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




*[It does use the Properties hack for Unicode decoding.]*
