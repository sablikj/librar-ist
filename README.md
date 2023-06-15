# Librar IST

* Android app for managing and crowd-sourcing free libraries. Uses [Android JetPack Compose](https://developer.android.com/jetpack) with Material 3 design.
* Created as uni project for the CMU course.

## Mandatory features
### TODO
* When the user is actively viewing a library or book, ensure that any new content shows up quickly. If the user disengages from the application, use more efficient messaging to save network resources, even if at the expense of increased latency

### Complete
* Two main screens: 
  * a map of available free libraries.
  * a book search screen.
* The map can be dragged around to show more libraries.
* Show placeholder images when user is on the metered connection
* Application is aware of its location and automaticallys open free library information panels when close to the library (within 100m)
* The book search screen allows users to see the full list of books ever donated to any library managed from within the App and to filter it down with a text search
* When a user searches for books using a search filter, search results can be downloaded only as scrolling requires
* The user should be able to center the map on their current location at the press of a button. 
* A button to enable/ disable notifications of when the book becomes available in one of the user’s favorite libraries
* A list of libraries where the book is available indicating how far away they are and sorted by this distance
* When user is on the WIFI, the library data for libraries within a 10km radius and their respective books are preloaded
* Map has search bar to lookup and center the map on a given address
*	Free libraries should show up on the map with markers. 
*	Tapping a library brings up its information panel
*	The user’s favorite libraries should be highlighted with a different marker. 
*	Tapping a marker goes to the respective library’s information panel, which should include the following: 
    *	The library’s name  
    *	location (shown on a map)
    *	photo
    *	A button to help the user navigate to the library. 
    *	Button to add/remove the library from the user’s favorites. 
    *	A button to check in / donate a book (scan barcode).
        *	If the code is unknown, create a new book with a title and cover photo (taken from the camera).
    *	A button to check out a book (scan barcode). 
    *	The list of books currently available at the library. 
        * Tapping a book opens a panel with more information about the book. 	
* Allow the user to add a new library, with the following information: 
    * A name for the library. 
    * A location (either picked from a map, searched by address, or using current location) 
    * A photo (taken from the phone camera) 

## Additional features
### TODO
* User Ratings (10%)
### Complete
* UI Adaptability: Light/Dark Theme (5%)
* UI Adaptability: Localization (5%)
* Social Sharing To Other Apps (5%)

## Prerequisities to run
* Google Maps API key
  * Should be saved in the local.properties file as GOOGLE_MAPS_API_KEY 

## Used libraries
* Android Room
* Retrofit
* CameraX
* Dagger - Hilt
* Coil
* [Bar Code Scanner](https://developers.google.com/ml-kit/vision/barcode-scanning/code-scanner)
* [Maps SDK](https://developers.google.com/maps/documentation/android-sdk/overview)
* [Places SDK](https://developers.google.com/maps/documentation/places/android-sdk/overview)
* [Gson](https://github.com/google/gson)


## Authors
* [Jan Sáblík](https://github.com/sablikj)
* [David Bilnica](https://github.com/dbilnica)
* [Tina Petkova](https://github.com/tina5kova)
