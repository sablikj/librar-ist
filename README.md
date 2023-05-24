# Librar IST

* Android app for managing and crowd-sourcing free libraries. Uses [Android JetPack Compose](https://developer.android.com/jetpack) with Material 3.
* Created as uni project for the LPRO course.

## Mandatory features
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
* UI Adaptability: Rotation (5%)



## Used libraries
* Android Room
* Retrofit
* CameraX
* Dagger - Hilt
* Coil
* Bar Code Scanner
* Maps SDK
* [Gson](https://github.com/google/gson)


## Authors
* [Jan Sáblík](https://github.com/sablikj)
* [David Bilnica](https://github.com/dbilnica)
* [Tina Petkova](https://github.com/tina5kova)
