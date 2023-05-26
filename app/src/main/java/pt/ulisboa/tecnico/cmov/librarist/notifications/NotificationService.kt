package pt.ulisboa.tecnico.cmov.librarist.notifications

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import pt.ulisboa.tecnico.cmov.librarist.data.remote.LibraryApi
import pt.ulisboa.tecnico.cmov.librarist.model.Book


class NotificationService(
    private val libraryApi: LibraryApi,
    private val notificationsController: NotificationsController
) {

    suspend fun checkBookAvailability(bookTitle: String) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val response = async {
                    libraryApi.getBookByTitle(bookTitle)
                }.await()

                if (response.isSuccessful && response.body() != null) {
                    val book = response.body() as Book
                    book.available?.let {
                        showNotification(
                            "Book available",
                            book.name + "is available now",
                        )
                    }

                }
            }

        } catch (e: Exception) {
            Log.d("error", "Failed Notification Service call")
        }
    }

    private fun showNotification(title: String, message: String) {
        // Create and show the notification
        notificationsController.showNotification(title, message)
    }


}