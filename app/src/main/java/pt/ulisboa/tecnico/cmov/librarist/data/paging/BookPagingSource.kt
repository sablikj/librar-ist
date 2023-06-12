package pt.ulisboa.tecnico.cmov.librarist.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import pt.ulisboa.tecnico.cmov.librarist.data.remote.LibraryApi
import pt.ulisboa.tecnico.cmov.librarist.model.Book
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants.ITEMS_PER_PAGE

class BookPagingSource(
    private val libraryApi: LibraryApi,
    private val query: String
) : PagingSource<Int, Book>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
        val currentPage = params.key ?: 1
        Log.d("paging", currentPage.toString())
        return try {
            val offset = (currentPage - 1) * ITEMS_PER_PAGE
            val results = libraryApi.searchBooks(query = query, offset = offset, limit = ITEMS_PER_PAGE).data

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
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Book>): Int? {
        return state.anchorPosition
    }
}