package pt.ulisboa.tecnico.cmov.librarist.utils

object Constants {
    //API
    const val API_BASE = "http://100.68.28.175:5000/"

    // Object tables
    const val LIBRARY_TABLE = "library_table"
    const val BOOK_TABLE = "book_table"
    const val NOTIFICATIONS_TABLE = "notifications_table"
    const val RATINGS_TABLE = "ratings_table"

    const val DATABASE_NAME = "librarist_database"
    const val ITEMS_PER_PAGE = 4

    object Routes {
        // Detail keys
        const val BOOK_DETAIL_ID = "bookId"
        const val LIBRARY_DETAIL_ID = "libraryId"

        // Detail routes
        const val BOOK_DETAIL_ROUTE = "book"
        const val LIBRARY_DETAIL_ROUTE = "library"
    }

    object Graph {
        const val ROOT = "root_graph"
    }
}