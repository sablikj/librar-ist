package pt.ulisboa.tecnico.cmov.librarist.notifications

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pt.ulisboa.tecnico.cmov.librarist.data.Repository


class NotificationService(
    private val application: Application,
    private val repository: Repository,
    private val notificationsController: NotificationsController
) {
    private val pollingIntervalMillis = 5000L // 5 seconds

    fun startApiPolling(): Boolean {
        var result = false;
        var favs = repository.getFavouriteLibraryIds()
        try {
            CoroutineScope(Dispatchers.IO).launch {
                repository.getNotifications().let {
                    for (n in it) {
                        while (n.notifications) {
                            repository.getBookLib()?.let { bookLibs ->
                                for (book in bookLibs) {
                                    if (book.available && favs.contains(book.libraryId)) {
                                        val bookname = repository.getBook(application.applicationContext, book.barcode)?.name
                                        showNotification(
                                            "Book available",
                                            "$bookname is available now",
                                        )
                                        result = true
                                        return@launch
                                    }
                                }
                            }
                            delay(pollingIntervalMillis)
                        }
                    }
                }
            }

        } catch (e: Throwable) {
            // Handle network or request failure
            e.printStackTrace()
            Log.d("error", "")
        }
        return result;
    }

    private fun showNotification(title: String, message: String) {
        // Create and show the notification
        notificationsController.showNotification(title, message)
    }


}