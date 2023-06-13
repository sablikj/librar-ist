package pt.ulisboa.tecnico.cmov.librarist.data.paging

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import pt.ulisboa.tecnico.cmov.librarist.data.Repository
import pt.ulisboa.tecnico.cmov.librarist.data.remote.LibraryApi
import pt.ulisboa.tecnico.cmov.librarist.model.Book
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants.ITEMS_PER_PAGE
import pt.ulisboa.tecnico.cmov.librarist.utils.checkNetworkType

class BookPagingSource(
    private val libraryApi: LibraryApi,
    private val query: String,
    private val application: Application
) : PagingSource<Int, Book>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
        val currentPage = params.key ?: 1
        Log.d("paging", currentPage.toString())
        return try {
            val offset = (currentPage - 1) * ITEMS_PER_PAGE

            val (isWiFi, isMetered) = checkNetworkType(application.applicationContext)

            // Use the appropriate API based on the network connection
            val results = if (isWiFi && !isMetered) {
                libraryApi.searchBooks(query = query, offset = offset, limit = ITEMS_PER_PAGE).data
            } else if (!isWiFi && isMetered) {
                libraryApi.searchBooksMetered(query = query, offset = offset, limit = ITEMS_PER_PAGE).data
            } else {
                emptyList()
            }
            Log.d("paging", results.toString())

            if (results.isNotEmpty()) {
                LoadResult.Page(
                    data = results,
                    prevKey = if (currentPage == 1) null else currentPage - 1,
                    nextKey = currentPage + 1
                )
            } else {
                LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null
                )
            }
        } catch (e: Exception) {
            Log.d("paging", e.toString())
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Book>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}