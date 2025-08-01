/*
 * Copyright (c) 2013-2025 - Troy Wu
 *
 * The MIT License (MIT)
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.v2.j2son;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.*;


/**
 * A JSON Parser, which reads the entire input and creates a JSON object.
 *
 * @see Json
 */
public class JsonParser
{
    public static final boolean DEBUG_INPUT = false;
    public static final boolean DEBUG_JSON  = false;
    public static final boolean DEBUG_WS    = false;
    public static final boolean DEBUG_OBJ   = false;
    public static final boolean DEBUG_ARR   = false;
    public static final boolean DEBUG_STR   = false;
    public static final boolean DEBUG_NUM   = false;

    public static final String ENCODING = "UTF-8";

    private InputStreamReader isr;

    private boolean isBareLiteral = false;


    static Json parse( final String json ) throws IOException, InvalidJsonException
    {
        JsonParser parser = new JsonParser();
        return parser.parseJson( json );
    }


    private Json parseJson( final String json ) throws IOException, InvalidJsonException
    {
        final ByteArrayInputStream bais = new ByteArrayInputStream( json.getBytes() );

        isr = new InputStreamReader( bais, ENCODING );

        int c;

        try
        {
            c = consumeWhitespace();
        }
        catch( InvalidJsonException jsex )
        {
            // This isn't an error; it's just empty JSON, which is valid.
            //return null;
            throw jsex;
        }

        Json j = this.parseValue( c );

        try
        {
            this.consumeWhitespace();
        }
        catch( InvalidJsonException jsex )
        {
            // This isn't an error; it's just whitespace at the end of the JSON, which is valid.
        }

        return j;
    }


    private int consumeWhitespace() throws IOException, InvalidJsonException
    {
        int c = isr.read();
        return consumeWhitespace( c );
    }


    private int consumeWhitespace( int c ) throws IOException, InvalidJsonException
    {
        do
        {
            if( DEBUG_WS ) System.out.println( "            consuming whitespace [" + (c) + "]" );

            if( -1 == c ) throw new InvalidJsonException( "JSON terminated in whitespace" );

            switch( c )
            {
                case 0: // NUL
                case 9: // tab
                case 10: // LF
                case 13: // CR
                case 32: // SP
                    c = isr.read();
                    break;

                default:
                    if( DEBUG_WS ) System.out.println( "              returning non-whitespace [" + (c) + "]" );
                    return c;
            }
        } while( true );
    }


    /**
     * Implementing subclasses -MUST- be constructable before being initialized by
     * JSON.  It is recognized that this leaves the object potentially in an invalid
     * state, so the ctor() must have a mechanism to alert caller to invalid state; e.g.,
     * <p>
     * DataHouse hd = new DataHouse();   // DataHouse object is invalid, but detectably invalid.
     * hd.fromJSON(jsonString);          // DataHouse is valid after fromJSON().
     * <p>
     * Similarly:
     * <p>
     * DataMember md = new DataMember(); // DataMember object is detectably invalid.
     * md = md.readJSON(reader);         // DataMember is valid after this call.
     *
     * @param c character (pre-read from caller)
     * @throws IOException Thrown if JSON is malformed.
     * @noinspection CharsetObjectCanBeUsed
     */
    public Json parseValue( int c ) throws IOException, InvalidJsonException
    {
        Json j; // = null;

        switch( c )
        {
            case '{':
                j = parseObject();
                break;

            case '[':
                j = parseArray();
                break;

            case '"':
                j = parseString();
                break;

            // If not a "bare literal" (number/t/f/null), then WTF??
            default:
                isBareLiteral = true; // NOTE - Setting global state.
                j = parseLiteral( c );
                break;
        }

        return j;

    }


    private Json parseObject() throws IOException, InvalidJsonException
    {
        Map< String, Json > map = new HashMap<>();

        if( DEBUG_OBJ ) System.out.println( "    Starting object..." );

        int c = -1;
        try
        {
            c = consumeWhitespace();
        }
        catch( InvalidJsonException jex )
        {
            // NOTE - Nothing wrong with this.  Empty object.
        }

        if( '}' == c )// Empty object.
        {
            return new Json( map );
        }

        int k = 0;
        while( true )
        {
            if( DEBUG_OBJ ) System.out.println( "        Reading map element [" + k + "] (with lookahead c: [" + ((char) c) + "]: (" + c + "))" );

            final Json member = parseMember( c );

            if( DEBUG_OBJ ) member.dump();

            c = member.lookahead();

            if( DEBUG_OBJ ) System.out.println( "        lookahead c: [" + ((char) c) + "] after map element consumed." );

            map.put( member.key(), member );

            if( '}' == c )
            {
                break; // End of the object.
            }
            else if( ',' == c )
            {
                // This is a multi-element object.
                c = consumeWhitespace();
                ++k;
                continue;
            }
            else
            {
                // WTF is going on here?
                throw new InvalidJsonException( "Invalid character (not comma or end-curly) in map." );
            }
        }

        return new Json( map );
    }

    private Json parseMember( int c ) throws IOException, InvalidJsonException
    {
        // c = consumeWhitespace();

        if( '"' != c )
        {
            throw new InvalidJsonException( "No key (no st arting double-quote) for object member." );
        }
        Json   j   = parseString();
        String key = j.getString();

        c = consumeWhitespace();
        if( ':' != c )
        {
            throw new InvalidJsonException( "No ':' for object member." );
        }

        c = consumeWhitespace();
        j = parseValue( c );

        c = consumeWhitespace( j.lookahead() );

        return new Json( key, j, c );
    }


    private Json parseArray() throws IOException, InvalidJsonException
    {
        List< Json > array = new ArrayList<>();

        if( DEBUG_ARR ) System.out.println( "    Starting array..." );

        int c = -1;
        try
        {
            c = consumeWhitespace();
        }
        catch( InvalidJsonException jex )
        {
            // NOTE - Nothing wrong with this.  Empty array.
        }

        if( ']' == c )// Empty array.
        {
            return new Json( array );
        }
        else if( -1 == c ) // Unterminated array.
        {
            throw new InvalidJsonException( "Unterminated array." );
        }

        int k = 0;
        while( true )
        {
            if( DEBUG_ARR ) System.out.println( "        Reading array element [" + k + "] (with lookahead c: [" + ((char) c) + "])" );

            final Json element = parseElement( c );

            if( DEBUG_ARR ) element.dump();

            c = element.lookahead();

            if( DEBUG_ARR ) System.out.println( "        lookahead c: [" + ((char) c) + "] after array element consumed." );

            array.add( element );

            if( ']' == c )
            {
                break; // End of the array.
            }
            else if( ',' == c )
            {
                // This is a multi-element array.
                c = consumeWhitespace();
                ++k;
                continue;
            }
            else
            {
                // WTF is going on here?
                throw new InvalidJsonException( "Invalid character (not comma or end-bracket) in array." );
            }
        }

        return new Json( array );
    }


    private Json parseElement( int c ) throws IOException, InvalidJsonException
    {
        c = consumeWhitespace( c );

        if( DEBUG_ARR ) System.out.println( "Starting on value (prob in array)..." );

        Json j = parseValue( c );

        if( DEBUG_INPUT ) j.dump();

        c = consumeWhitespace( j.lookahead() );

        return new Json( j, c );
    }


    /**
     * Valid literal values expected here:
     * <p>
     * -1.2
     * -0.2
     * 0.2
     * 1.2
     * 1.2e13
     * 1.2e-13
     * <p>
     * false
     * true
     * null
     *
     * @param c
     * @return
     * @throws IOException
     * @throws InvalidJsonException
     */
    private Json parseLiteral( int c ) throws IOException, InvalidJsonException
    {
        if( -1 == c ) throw new InvalidJsonException( "JSON aborted at the start of a literal." );

        if( '-' == c )
        {
            return parseNumber( true, isr.read() );
        }
        else if( 'f' == c )
        {
            int d = isr.read();
            int e = isr.read();
            int f = isr.read();
            int g = isr.read();

            if( 'a' != d || 'l' != e || 's' != f || 'e' != g )
            {
                throw new InvalidJsonException( "(false) literal started, but unfinished." );
            }

            return new Json( false );
        }
        else if( 't' == c )
        {
            int d = isr.read();
            int e = isr.read();
            int f = isr.read();

            if( 'r' != d || 'u' != e || 'e' != f )
            {
                throw new InvalidJsonException( "(true) literal started, but unfinished." );
            }

            return new Json( true );
        }
        else if( 'n' == c )
        {
            int d = isr.read();
            int e = isr.read();
            int f = isr.read();

            if( 'u' != d || 'l' != e || 'l' != f )
            {
                throw new InvalidJsonException( "(null) literal started, but unfinished." );
            }

            return new Json( null );
        }
        else
        {
            return parseNumber( false, c );
        }
    }


    private Json parseNumber( boolean isNegative, int c ) throws IOException, InvalidJsonException
    {
        if( -1 == c ) throw new InvalidJsonException( "JSON aborted at the magnitude (after sign) of a number." );

        StringBuilder buf = new StringBuilder();
        if( isNegative ) buf.append( "-" );

        Json j;

        if( '0' == c ) // Fractional (e.g., 0.123) or the ZERO literal (0)
        {
            c = isr.read();

            if( '.' == c )
            {
                buf.append( "0." );

                j = parseNumberPostfix( buf, isr.read(), true, false );
            }
            else if( 'e' == c || 'E' == c )
            {
                j = parseNumberPostfix( buf, c, false, true );

                return new Json( Long.valueOf( 0 ), j.lookahead() );
            }
            else
            {
                // NOTE - Zero can just end the number, without anything follow.

                return new Json( Long.valueOf( 0 ), c );
            }
        }
        else // Not fractional, so must be non-zero digit ("normal" number)
        {
            switch( c )
            {
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    buf.append( (char) c );
                    break;

                default:
                    throw new InvalidJsonException( "Bad start to number (" + c + ")" );
            }

            j = parseNumberPostfix( buf, isr.read(), false, true );
        }

        return j;
    }


    private Json parseNumberPostfix( StringBuilder buf, int c, boolean isInFraction, boolean hasNumericAlready ) throws IOException, InvalidJsonException
    {
        boolean hasNumericPart      = hasNumericAlready;
        boolean isInExponent        = false;
        boolean isInExponentNumeric = false;

        do
        {
            if( !isBareLiteral && -1 == c ) throw new InvalidJsonException( "JSON aborted in number." );

            switch( c )
            {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    hasNumericPart = true;
                    buf.append( (char) c );
                    break;

                case '.':
                    if( isInFraction ) throw new InvalidJsonException( "Two periods inside one number." );
                    if( isInExponent ) throw new InvalidJsonException( "Periods inside exponent." );
                    isInFraction = true;
                    buf.append( (char) c );
                    break;

                case 'E':
                case 'e':
                    if( !hasNumericPart ) throw new InvalidJsonException( "No numeric before exponent." );
                    if( isInExponent ) throw new InvalidJsonException( "Two exponents inside one number." );
                    isInExponent = true;
                    buf.append( (char) c );
                    c = isr.read();
                    // Check if this is a sign.  If so, add to buf.  If not, check if digit.
                    switch( c )
                    {
                        case '+': // We ignore + signs in floats.
                            break;

                        case '-':
                            if( isInExponentNumeric )
                                throw new InvalidJsonException( "More than one sign (or misplaced sign) in exponent." );

                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            buf.append( (char) c );
                            break;

                        default:
                            throw new InvalidJsonException( "Unexpected character [" + c + "] in exponent." );
                    }
                    isInExponentNumeric = true;
                    break;

                default:
                    String s = buf.toString();
                    if( isInFraction || isInExponent )
                    {
                        try
                        {
                            // This is a double.
                            if( DEBUG_NUM ) System.out.println( "  ==> Converting [" + s + "]..." );
                            final double dbl = Double.parseDouble( s );

                            if( DEBUG_NUM ) System.out.println( "      Converted [" + s + "] to (" + dbl + ")" );
                            return new Json( dbl, c );
                        }
                        catch( NumberFormatException nfe )
                        {
                            return new Json( 0, c );
                        }
                    }
                    else
                    {
                        try
                        {
                            // This is an integer.
                            if( DEBUG_NUM ) System.out.println( "  ==> Converting [" + s + "]..." );
                            final long l = Long.parseLong( s );

                            if( DEBUG_NUM ) System.out.println( "      Converted [" + s + "] to (" + l + ")" );
                            return new Json( l, c );
                        }
                        catch( NumberFormatException nfe )
                        {
                            return new Json( 0, c );
                        }
                    }
            }

            // Read next character.  Don't forget this, just because it's do-while.
            c = isr.read();

        } while( true );
    }


    /**
     * @return Json
     */
    private Json parseString() throws IOException, InvalidJsonException
    {
        StringBuilder buf = new StringBuilder();

        while( true )
        {
            int c = isr.read();

            if( DEBUG_STR ) System.out.println( "        > Reading [" + c + "]" );

            if( -1 == c )
            {
                throw new InvalidJsonException( "JSON aborted in the middle of string." );
            }
            else if( '\\' == c )
            {
                int d = isr.read();
                if( -1 == d ) throw new InvalidJsonException( "JSON aborted in the middle of string." );

                switch( d )
                {
                    case '"':
                    case '\\':
                    case '/':
                        buf.append( (char) d );
                        break;

                    case 'b':
                        buf.append( "\b" );
                        break;
                    case 'f':
                        buf.append( "\f" );
                        break;
                    case 'n':
                        buf.append( "\n" );
                        break;
                    case 'r':
                        buf.append( "\r" );
                        break;
                    case 't':
                        buf.append( "\t" );
                        break;

                    case 'u':
                    {
                        if( DEBUG_STR ) System.out.println( "  Encountered Unicode escape..." );

                        int e = isr.read();
                        int f = isr.read();
                        int g = isr.read();
                        int h = isr.read();

                        if( isHexDigit( e ) && isHexDigit( f ) && isHexDigit( g ) && isHexDigit( h ) )
                        {
                            // Valid unicode escape sequence.
                            StringBuilder sb = new StringBuilder( "\\u" );
                            sb.append( (char) e ).append( (char) f ).append( (char) g ).append( (char) h );
                            if( DEBUG_STR ) System.out.println( "    ==> Converting [" + sb + "]..." );

                            Properties p = new Properties();
                            p.load( new StringReader( "key = " + sb ) );
                            final String uc = p.getProperty( "key" );

                            if( DEBUG_STR ) System.out.println( "        Converted  [" + sb + "] to \"" + uc + "\"" );

                            buf.append( uc );
                        }
                        else
                        {
                            // Invalid unicode escape sequence.
                            throw new InvalidJsonException( "Invalid Unicode escape sequence." );
                        }
                        break;
                    }

                    default:
                        throw new InvalidJsonException( "Invalid JSON string escape sequence [\\" + d + "]" );
                }
            }
            else if( '"' == c )
            {
                if( DEBUG_STR ) System.out.println( "--End of string." );
                break; // Termination of a string.
            }
            else if( '\n' == c )
            {
                throw new InvalidJsonException( "Unescaped newline found in string." );
            }
            else if( '\t' == c )
            {
                throw new InvalidJsonException( "Unescaped tab found in string." );
            }
            else
            {
                buf.append( (char) c );
            }
        }

        final String s = buf.toString();

        if( DEBUG_STR ) System.out.println( "s: [" + s + "], len: " + s.length() );
        if( DEBUG_STR ) System.out.println( "  ==> string!" );

        return new Json( s );
    }


    private static boolean isHexDigit( int c )
    {
        switch( c )
        {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':

            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':

            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                return true;

            default:
                return false;
        }
    }
}
