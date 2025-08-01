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
package com.crinqle.j2son;


import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import static dev.v2.mob.a.JsonParser.DEBUG_JSON_WALK;
//import static bingo.ngo.mobile.a.Base.*;


/**
 * An in-memory, random-access, representation of a JSON object, created by
 * JsonParser.
 *
 * @see JsonParser
 */
public class Json
{
    public static final boolean DEBUG_IO        = true;
    public static final boolean DEBUG_VERBOSE   = false;
    public static final boolean DEBUG_SEARCH    = false;
    public static final boolean DEBUG_JSON_WALK = false;


    private static final String TEXT_COLOR_WHITE  = "\033[1;37m";
    private static final String TEXT_COLOR_RED    = "\033[0;31m";
    private static final String TEXT_COLOR_GREEN  = "\033[0;32m";
    private static final String TEXT_COLOR_YELLOW = "\033[1;33m";
    private static final String TEXT_COLOR_BLUE   = "\033[0;34m";
    private static final String TEXT_COLOR_CYAN   = "\033[0;36m";
    private static final String TEXT_COLOR_ORANGE = "\033[0;33m";

    private static final String TEXT_COLOR_SUFFIX = "\033[0m"; // NOTE -- This closes the ANSI escape sequence!

    private static final String JSON_ERROR = TEXT_COLOR_RED + "JSON error" + TEXT_COLOR_SUFFIX;

    public static final int JSON_INVALID = -9;
    public static final int JSON_NULL    = -1;
    public static final int JSON_FALSE   = 0;
    public static final int JSON_TRUE    = 1;
    public static final int JSON_BOOLEAN = 2;
    public static final int JSON_LONG    = 3;
    public static final int JSON_DOUBLE  = 4;
    public static final int JSON_STRING  = 5;
    public static final int JSON_ARRAY   = 8;
    public static final int JSON_OBJECT  = 9;

    public static final String JSON_LIT_NULL  = "null";
    public static final String JSON_LIT_TRUE  = "true";
    public static final String JSON_LIT_FALSE = "false";

    protected final String key;
    protected final Object val;

    protected final int type;

    private int lookahead;


    Json( final Json j, final int lookahead )
    {
        this.key  = j.key;
        this.val  = j.val;
        this.type = j.type;

        this.lookahead = lookahead;
    }


    public Json( final Object val )
    {
        this.key = null;
        this.val = val;

        this.type = determineType();
    }


    /**
     * Package level; only JsonParser should be able to make one of these.
     *
     * @param val
     * @param c
     */
    Json( final Object val, final int c )
    {
        this.key       = null;
        this.val       = val;
        this.lookahead = c;

        this.type = determineType();
    }


    public Json( final String key, final Object val )
    {
        if( null == key ) throw new InvalidParameterException( "JSON key cannot be null" );

        this.key = key;
        this.val = val;

        this.type = determineType();
    }


    public Json( final String key, final Json val, int c )
    {
        if( null == key ) throw new InvalidParameterException( "JSON key cannot be null" );

        this.key       = key;
        this.val       = val;
        this.lookahead = c;

        this.type = determineType();
    }


    /**
     * Package level; only JsonParser should be able to make one of these.
     *
     * @return
     */
    int lookahead() {return this.lookahead;}


    private int determineType()
    {
        if( val instanceof Long )
            return JSON_LONG;
        else if( val instanceof Double )
            return JSON_DOUBLE;
        else if( val instanceof String )
            return JSON_STRING;
        else if( val instanceof Boolean )
            return JSON_BOOLEAN;
        else if( val instanceof List )
            return JSON_ARRAY;
        else if( val instanceof Map )
            return JSON_OBJECT;
        else if( null == val )
            return JSON_NULL;
        else
            return JSON_INVALID;
    }


    public boolean isNull()
    {
        return null == val;
    }

    public boolean isBoolean()
    {
        return JSON_BOOLEAN == type;
    }

    public boolean isLong()
    {
        return JSON_LONG == type;
    }

    public boolean isDouble()
    {
        return JSON_DOUBLE == type;
    }

    public boolean isString()
    {
        return JSON_STRING == type;
    }

    public boolean isArray()
    {
        return JSON_ARRAY == type;
    }

    public boolean isObject()
    {
        return JSON_OBJECT == type;
    }

    public int getType()
    {
        return type;
    }

    public int type()
    {
        return getType();
    }


    public boolean getBoolean()
    {
        if( JsonParser.DEBUG_JSON ) dump();
        return isBoolean() ? (Boolean) val : false;
    }

    public int getInt()
    {
        if( JsonParser.DEBUG_JSON ) dump();
        return isLong() ? (Integer) val : -1;
    }

    public long getLong()
    {
        if( JsonParser.DEBUG_JSON ) dump();
        return isLong() ? (Long) val : -1L;
    }

    public double getDouble()
    {
        if( JsonParser.DEBUG_JSON ) dump();
        return isDouble() ? (Double) val : -1.0d;
    }

    public List getArray()
    {
        if( JsonParser.DEBUG_JSON ) dump();
        return isArray() ? (List) val : null;
    }

    public Map getObject()
    {
        if( JsonParser.DEBUG_JSON ) dump();
        return isObject() ? (Map) val : null;
    }


    public String getString()
    {
        return toString();
    }

    public String key()
    {
        return this.key;
    }

    public String getKey()
    {
        return key();
    }

    public Object getValue()
    {
        return val;
    }

    public Object value()
    {
        return getValue();
    }

    public Object val()
    {
        return getValue();
    }

    public boolean hasKey()
    {
        return null != this.key;
    }


    private String toKey( final boolean useKey )
    {
        return useKey ? (hasKey() ? ("\"" + key.trim() + "\" : ") : "") : "";
    }


    public String asJSON()
    {
        return asJSON( true );
    }


    public String asJSON( final boolean useKey )
    {
        if( val instanceof Long )
            return (toKey( useKey ) + val);
        else if( val instanceof Double )
            return (toKey( useKey ) + val);
        else if( val instanceof String )
            return (toKey( useKey ) + "\"" + val + "\"");
        else if( val instanceof Boolean )
        {
            final String content = ((Boolean) val) ? JSON_LIT_TRUE : JSON_LIT_FALSE;
            return (toKey( useKey ) + content);
        }
        else if( val instanceof List )
        {
            final List          list = (List) val;
            int                 i    = 0;
            final StringBuilder sb   = new StringBuilder();
            for( final Object o : list )
            {
                if( 0 < i++ ) sb.append( "," );

                if( o instanceof Json )
                {
                    final Json   j       = (Json) o;
                    final String content = j.asJSON( false );
                    sb.append( content );
                }
            }
            return toKey( useKey ) + "[" + sb.toString() + "]";
        }
        else if( val instanceof Map )
        {
            final Map           map = (Map) val;
            int                 i   = 0;
            final StringBuilder sb  = new StringBuilder();

            final Set keys = map.keySet();
            for( final Object k : keys )
            {
                if( 0 < i++ ) sb.append( "," );
                final Object v = map.get( k );

                if( v instanceof Json )
                {
                    final Json   j       = (Json) v;
                    final String content = j.asJSON();

                    if( DEBUG_VERBOSE ) System.out.println( "    ==> content: " + content );

                    sb.append( content );
                }
            }

            final String mapAsJSON = toKey( useKey ) + "{" + sb.toString() + "}";

            if( DEBUG_VERBOSE ) System.out.println( "        --> map: " + mapAsJSON );

            return mapAsJSON;
        }
        else if( null == val )
            return (toKey( useKey ) + JSON_LIT_NULL);
        else
            return "";
    }


    @SuppressWarnings( "unchecked" )
    public Map< String, Json > getMap()
    {
        return isObject() ? (Map< String, Json >) val : null;
    }

    @SuppressWarnings( "unchecked" )
    public List< Json > getList()
    {
        return isArray() ? (List< Json >) val : null;
    }


    public Json find( final String keySpec )
    {
        if( DEBUG_SEARCH ) System.out.println( "keySpec: " + keySpec );

        // Search term is null or empty-string.
        if( null == keySpec || 0 == keySpec.length() ) return this;

        final String[] keys   = keySpec.split( "\\.", 2 );
        final String   prefix = keys[0];

        if( DEBUG_SEARCH ) System.out.println( "  prefix: " + prefix );

        final boolean isArraySpec = prefix.startsWith( "[" ) && prefix.endsWith( "]" );

        return isArraySpec ? findInArray( keys ) : findInObject( keys );
    }


    private Json findInObject( final String[] keys )
    {
        if( !isObject() ) return null;

        final String prefix = keys[0];

        final Map< String, Json > map = getMap();

        // If keySpec token is not one of the children, abort.
        if( !map.containsKey( prefix ) ) return null;

        final Json child = map.get( prefix );

        // If key exists, but there is no 'Json' value here; abort.
        if( null == child ) return null;

        if( DEBUG_JSON_WALK ) child.dump();

        // If we have no more keySpec, return the child.
        if( 1 == keys.length ) return child;

        final String suffix = keys[1];

        // Recursively walk the rest of the keySpec...
        return child.find( suffix );
    }


    private Json findInArray( final String[] keys )
    {
        if( !isArray() ) return null;

        final String prefix = keys[0];

        final int    endIndex    = prefix.length() - 1;
        final String indexString = prefix.substring( 1, endIndex );

        System.out.println( "indexString: " + indexString );

        try
        {
            final int          index    = Integer.parseInt( indexString );
            final List< Json > list     = getList();
            final int          maxIndex = list.size() - 1;

            // If the index is OOB for this array, abort.
            if( index < 0 || index > maxIndex ) return null;

            final Json child = list.get( index );

            if( DEBUG_JSON_WALK ) child.dump();

            if( 1 == keys.length ) return child;

            final String suffix = keys[1];

            return child.find( suffix );
        }
        catch( NumberFormatException e )
        {
            // If the index is not an integer, abort.
            return null;
        }
    }


    @Override
    public String toString()
    {
        return null == val ? "(null)" : val.toString();
        //return TEXT_COLOR_YELLOW + s + TEXT_COLOR_SUFFIX;
    }


    public void dump()
    {
        dump( 0 );
//        System.out.println( "================================================" );
//        System.out.println( "   Datum type: " + getTypeName() + " System.out.println(" + type + ")" );
//        System.out.println( "        value: " + getValue() );
//        System.out.println( "------------------------------------------------" );
//        System.out.println( "          key: " + key );
//        System.out.println( "------------------------------------------------" );
//        System.out.println( "    isBoolean? " + isBoolean() );
//        System.out.println( "       isLong? " + isLong() );
//        System.out.println( "     isDouble? " + isDouble() );
//        System.out.println( "     isString? " + isString() );
//        System.out.println( "------------------------------------------------" );
//
//        if ( isArray() ) {
//            for ( Object obj : getArray() ) {
//                if ( obj instanceof Json ) {
//                    Json j = (Json)obj;
//                    j.dump(4);
//                }
//            }
//        }
    }


    public void dump( final int prefixLength )
    {
        StringBuilder prefix = new StringBuilder();
        for( int i = 0; i < prefixLength; ++i ) prefix.append( " " );

        System.out.println( prefix + "================================================" );
        System.out.println( prefix + "   Datum type: " + getTypeName() + " System.out.println(" + type + ")" );
        System.out.println( prefix + "        value: " + getValue() );
        System.out.println( prefix + "------------------------------------------------" );
        System.out.println( prefix + "          key: " + key );
        System.out.println( prefix + "------------------------------------------------" );
        System.out.println( prefix + "    isBoolean? " + isBoolean() );
        System.out.println( prefix + "       isLong? " + isLong() );
        System.out.println( prefix + "     isDouble? " + isDouble() );
        System.out.println( prefix + "     isString? " + isString() );
        System.out.println( prefix + "------------------------------------------------" );

        if( isArray() )
        {
            for( Object obj : getArray() )
            {
                if( obj instanceof Json )
                {
                    Json j = (Json) obj;
                    j.dump( 4 + prefixLength );
                }
            }
        }
    }


    public void dumpShort()
    {
        System.out.println( "  " + getKey() + ": " + getValue() );
    }


    private String getTypeName()
    {
        switch( type )
        {
            case JSON_STRING:
                return "JSON_STRING";
            case JSON_BOOLEAN:
                return "JSON_BOOLEAN";
            case JSON_LONG:
                return "JSON_LONG";
            case JSON_DOUBLE:
                return "JSON_DOUBLE";
            case JSON_NULL:
                return "JSON_NULL";
            case JSON_ARRAY:
                return "JSON_[array]";
            case JSON_OBJECT:
                return "JSON_{object}";
            default:
                return "[unknown-type]";
        }
    }


    private static String readFile( String path, Charset encoding ) throws IOException
    {
        byte[] encoded = Files.readAllBytes( Paths.get( path ) );
        return new String( encoded, encoding );
    }


    private static String loadFile( String path ) throws IOException, SecurityException
    {
        String orig = new StringBuilder(path).toString();

        try
        {
            File f = new File( path );
            if( !f.exists() )
            {
                System.out.println( "Can't find file; checking parse-testing directory..." );
                path = "../../../tests/test_parsing/" + path;

                f = new File( path );
                if( !f.exists() )
                {
                    System.out.println( "Can't find file; checking transform-testing director..." );

                    path = "../../../tests/test_transform/" + path;

                    f = new File( path );
                    if( !f.exists() )
                    {
                        System.out.println( "Cannot find file; aborting." );
                        System.exit( 1 );
                    }
                    else
                    {
                        System.out.println( "Reading [" + path + "]..." );
                    }
                }
                else
                {
                    System.out.println( "Reading [" + path + "]..." );
                }
            }
            else
            {
                System.out.println( "Reading [" + path + "]..." );
            }

            String json = readFile( path, StandardCharsets.UTF_8 );

            if( DEBUG_IO ) System.out.println( ">>> ---- " + TEXT_COLOR_GREEN + orig + TEXT_COLOR_SUFFIX + " ----" );
            if( DEBUG_IO ) System.out.println( ">>> >" + TEXT_COLOR_CYAN + json + TEXT_COLOR_SUFFIX + "<" );

            return json;
        }
        catch( IOException ioex )
        {
            System.err.println( "IO error: " + ioex.getMessage() );
            throw ioex;
        }
        catch( SecurityException secex )
        {
            System.err.println( "Cannot access file: " + secex.getMessage() );
            throw secex;
        }
    }


    public static void main( String[] args )
    {
        int argc = args.length;

        if( argc < 1 )
        {
            System.err.println( "Usage: java com.crinqle.j2son.Json file [file1 [file2 ... [filen]]]" );
            System.exit( 1 );
        }

        final String path = args[0];

        try
        {
            final String input = loadFile( path );
            final Json   j     = JsonParser.parse( input );

            //j.dump();
            System.out.println( "<<< " + TEXT_COLOR_YELLOW + j + TEXT_COLOR_SUFFIX );
        }
        catch( InvalidJsonException jex )
        {
            System.err.println( JSON_ERROR + ": " + jex.getMessage() );
            System.exit( 1 );
        }
        catch( IOException ioex )
        {
            System.err.println( "IO error: " + ioex.getMessage() );
            System.exit( 1 );
        }

        System.exit( 0 );
    }
}
