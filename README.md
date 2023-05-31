# Librar IST

* Android app for managing and crowd-sourcing free libraries. Uses [Android JetPack Compose](https://developer.android.com/jetpack) with Material 3.
* Created as uni project for the LPRO course.

## Mandatory features
### TODO
* **(Map tab)** Search bar to lookup and center the map on a given address
* **(Book detail)** A button to enable/ disable notifications of when the book becomes available in one of the user’s favorite libraries
* **(Book detail)**	A list of libraries where the book is available indicating how far away they are and sorted by this distance
* **(Book detail)**	Tapping a library brings up its information panel 
* **(Search tab)** The book search screen allows users to see the full list of books ever donated to any library managed from within the App and to filter it down with a text search
* When the user is actively viewing a library or book, ensure that any new content shows up quickly. If the user disengages from the application, use more efficient messaging to save network resources, even if at the expense of increased latency
* When a user searches for books using a search filter, search results can be downloaded only as scrolling requires - paging lib
* Show placeholder images when user is on the metered connection
* Application should be aware of its location and should automatically open free library information panels when close to the library (e.g. within 100m)
* When user is on the WIFI, preload the library data for libraries within a 10km radius and their respective books


### Complete
* Two main screens: 
  * a map of available free libraries.
  * a book search screen.
* The map can be dragged around to show more libraries.
* The user should be able to center the map on their current location at the press of a button. 
*	Free libraries should show up on the map with markers. 
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
* UI Adaptability: Localization (5%)
* UI Adaptability: Rotation (5%)
* Securing communication (5%) or something else
### Complete
* UI Adaptability: Light/Dark Theme (5%) (just pick app colors)


## Used libraries
* Android Room
* Retrofit
* CameraX
* Dagger - Hilt
* Coil
* [Bar Code Scanner](https://developers.google.com/ml-kit/vision/barcode-scanning/code-scanner)
* [Maps SDK](https://developers.google.com/maps/documentation/android-sdk/overview)
* [Gson](https://github.com/google/gson)


## Authors
* [Jan Sáblík](https://github.com/sablikj)
* [David Bilnica](https://github.com/dbilnica)
* [Tina Petkova](https://github.com/tina5kova)
