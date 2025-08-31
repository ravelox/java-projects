/*
$Log$
Revision 1.1  2003/10/10 10:16:49  dkelly
Initial revision

Revision 1.1.1.1  2003/06/02 10:13:11  dkelly


Revision 1.1  99/07/23  14:52:17  14:52:17  dkelly (Dave Kelly)
Initial revision

*/

import java.util.*;
import java.text.*;
/**
 * Add a time zone at the Greenwich, England, meridian which
 * implements daylight time during summer.  Northern hemisphere only.  
 * This "GMT0BST" zone is usually called "British Summer Time", 
 * abbreviated "BST", during summer so these are the formatter's defaults.<br>
 * Use code like this when your program initializes:<pre><code>
 *   BSumTime bst = new BSumTime();
 *   TimeZone.setDefault( bst );
 *   SimpleDateFormat dtf = bst.getDateTimeFormatter();
 * </code></pre>
 * You can now display a date & time string for BST using the default
 * DateFormat.LONG styles with code like this:<pre><code>
 *   Date now = new Date();  // current date & time
 *   String dateString = dtf.format( now );
 * </code></pre>
 * This gives (today):  "10 April 1999 21:41:28 BST".<br>
 * NOTE:  Beginning with Java release 1.1.7 the "BST" time zone is supported.
 * The TimeZone ID is "Europe/London" and is available if your Locale is en_GB.
 * Also the "IST" time zone is supported for Ireland and is available if your
 * Locale is en_IE.  In either case you can use this class for clients 1.1.6
 * and above, possibly 1.1.x and above.  (Please let me know what doesn't work.)<br>
 * Furnished as freeware with no warranty whatsoever--use at your own risk!
 * However, if you have suggestions, find bugs or whatever, please drop me
 * a note.  Feel free to modify or distribute this as you wish, but please
 * take credit for your own modifications.  Compiles to 2195 bytes.<br>
 * For more about time and time zones, see Peter van der Linden's 
 * <A HREF="http://www.afu.com"> Java Programmers FAQ</A>. 
 * @author <A HREF="mailto:adahlman@jps.net">Tony Dahlman</A>
 * @version 1.1 - April 1999
 */
public class BSumTime extends SimpleTimeZone {
   private String id;

   /**
    * The full constructor allows you to choose the time zone ID that
    * Java uses internally.  It probably should not conflict with the 
    * existing Java time zone ID's.  For a full discussion of how the
    * start and end dates/times were chosen see the info supplied by
    * the <A HREF="http://www.rog.nmm.ac.uk/leaflets/summer/summer.html">
    * The Greenwich National Observatory</A>.
    */
   public BSumTime(String id) {
      super(  0,             // raw offset same as GMT
              id,            // use zone ID passed in
              Calendar.MARCH,-1,Calendar.SUNDAY,1*60*60*1000,
                             // last Sunday in March 1 am -> 2 am
              Calendar.OCTOBER,-1,Calendar.SUNDAY,2*60*60*1000 
                             // last Sunday in Oct 2 am -> 1 am
            );
      this.id = id;
   }

   /**
    * The "default" constructor will use "UK/London" as the internal Java
    * time zone ID.
    */
   public BSumTime() {
      this("UK/London");
   }

   /**
    * Get a formatter for parse()ing or format()ing strings containing
    * both date and time, using the specified date and time display modes.
    * @param zoneName - set the time zone name, as in "British Summer Time".
    * @param zoneAbbr - set the time zone abbreviation, as in "BST".
    * @param dateDisplay - one of the DateFormat display modes, such as
    * DateFormat.FULL or DateFormat.SHORT.
    * @param timeDisplay - one of the DateFormat display modes, such as
    * DateFormat.FULL or DateFormat.SHORT.
    * @return an instance of SimpleDateFormat to use for parse()ing or
    * format()ing.
    */
   public SimpleDateFormat getDateTimeFormatter(String zoneName, 
                      String zoneAbbr, int dateDisplay, int timeDisplay) {
      SimpleDateFormat df = (SimpleDateFormat)DateFormat.getDateTimeInstance(
                         dateDisplay, timeDisplay);
      df = setSymbols( df, zoneName, zoneAbbr);
      df.setTimeZone(this);
      return df;
   }

   /**
    * Get a formatter for parse()ing or format()ing strings containing
    * both date and time, using default parameters.
    * @return an instance of SimpleDateFormat to use for parse()ing or
    * format()ing.
    */
   public SimpleDateFormat getDateTimeFormatter() {
      return getDateTimeFormatter("British Summer Time", "BST", 
                             DateFormat.LONG, DateFormat.LONG);
   }

   /**
    * Get a formatter for parse()ing or format()ing strings containing
    * just the date, using the specified date display mode.
    * @param zoneName - set the time zone name, as in "British Summer Time".
    * @param zoneAbbr - set the time zone abbreviation, as in "BST".
    * @param dateDisplay - one of the DateFormat display modes, such as
    * DateFormat.FULL or DateFormat.SHORT.
    * @return an instance of SimpleDateFormat to use for parse()ing or
    * format()ing.
    */
   public SimpleDateFormat getDateFormatter(String zoneName,
                               String zoneAbbr, int dateDisplay) {
      SimpleDateFormat df = (SimpleDateFormat)DateFormat.getDateInstance(
                                                   dateDisplay);
      df = setSymbols( df, zoneName, zoneAbbr);
      df.setTimeZone(this);
      return df;
   }

   /**
    * Get a formatter for parse()ing or format()ing strings containing
    * just the date, using default parameters.
    * @return an instance of SimpleDateFormat to use for parse()ing or
    * format()ing.
    */
   public SimpleDateFormat getDateFormatter() {
      return getDateFormatter("British Summer Time", "BST",DateFormat.LONG);
   }

   /**
    * Get a formatter for parse()ing or format()ing strings containing
    * just the time, using the specified time display mode.
    * @param zoneName - set the time zone name, as in "British Summer Time".
    * @param zoneAbbr - set the time zone abbreviation, as in "BST".
    * @param timeDisplay - one of the DateFormat display modes, such as
    * DateFormat.FULL or DateFormat.SHORT.
    * @return an instance of SimpleDateFormat to use for parse()ing or
    * format()ing.
    */
   public SimpleDateFormat getTimeFormatter(String zoneName,
                             String zoneAbbr, int timeDisplay) {
      SimpleDateFormat df = (SimpleDateFormat)DateFormat.getTimeInstance(
                                                           timeDisplay);
      df = setSymbols( df, zoneName, zoneAbbr);
      return df;
   }

   /**
    * Get a formatter for parse()ing or format()ing strings containing
    * just the time, using default parameters.
    * @return an instance of SimpleDateFormat to use for parse()ing or
    * format()ing.
    */
   public SimpleDateFormat getTimeFormatter() {
      return getTimeFormatter("British Summer Time", "BST", DateFormat.LONG);
   }

   /** 
    * Add the new time zone ID, its name and displayed abbreviation to the
    * internal list of time zone ID and formatting data.  This method is only
    * used by the various getXXXFormatter(...) methods in this class or a subclass.
    * @return a partly formed instance of SimpleDateFormat with the new time zone
    * ID, name, and displayed abbreviation added to the DateFormatSymbols list.
    * I'm not sure versions before JDK 1.1.6 had this functionality so we just
    * change the TimeZone ID to the first 3 letters of zoneAbbr if a method isn't
    * found, and return a formatter with a 3-letter ID in that case.
    */
   protected SimpleDateFormat setSymbols( SimpleDateFormat df, String zoneName, String zoneAbbr ) {
      try {
         DateFormatSymbols dfs = df.getDateFormatSymbols();
         String[][] zones = dfs.getZoneStrings();
         int len = zones.length;
         String[][] newZones = new String[len + 1][5];
         System.arraycopy(zones, 0, newZones, 0, len);
         String[] temp = { id, "Greenwich Mean Time", "GMT",
                           zoneName, zoneAbbr };
         newZones[len] = temp;
         dfs.setZoneStrings( newZones );
   
         df.setDateFormatSymbols( dfs );
         df.setTimeZone(this);
         return df;
      } catch ( NoSuchMethodError e ) {
         id = zoneAbbr.substring(0,3);
         setID(id);
         df.setTimeZone(this);
         return df;
      } /* endcatch */
   }
}
