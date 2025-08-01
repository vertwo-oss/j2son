# JSON Parser in Java, From Scratch

No external libraries.  Vanilla Java 11.

I had no idea this was some colossal landmine.

https://seriot.ch/projects/parsing_json.html

## It's slow.

But it appears to work.

## It's a bit too permissive.

On the tests which **should** fail, it succeeds.  Especially on the trailing garbage tests.  But, it works.


*[It does use the Properties hack for Unicode decoding.]*
