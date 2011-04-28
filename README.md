This utility will download an entire Flickr photoset to a specified location on your local hard disk.

## REQUIREMENTS ##

1. Java JDK 1.4+
2. Maven 2.2+


## SETUP ##

Obtain a Flickr API key.

To get an API key, see [http://www.flickr.com/services/api/misc.overview.html](http://www.flickr.com/services/api/misc.overview.html)


## Example usage ##

This program is designed to download entire Flickr photo sets at a time.

Size options = tiny|thumb|small|large|original

mvn exec:java -DtargetDir=/home/mypics -DphotoSetId=72157617533326814 -DsizeCode=tiny -Dapikey=XXX


## NOTES ##

The set id is found by navigating to the photo set on the Flickr site and extracting it from the URL. 

For example :

1. Start here : [http://www.flickr.com/photos/afwaibel/](http://www.flickr.com/photos/afwaibel/)
2. Then click on Dunlap
3. This will take you here : [http://www.flickr.com/photos/afwaibel/sets/72157594149030301/](http://www.flickr.com/photos/afwaibel/sets/72157594149030301/)

The 72.... number is the set id.

The sizes tiny|thumb|small|large|original correspond to the descriptions here : [http://www.flickr.com/services/api/misc.urls.html](http://www.flickr.com/services/api/misc.urls.html)

General Flickr API docs : [http://www.flickr.com/services/api/](http://www.flickr.com/services/api/)