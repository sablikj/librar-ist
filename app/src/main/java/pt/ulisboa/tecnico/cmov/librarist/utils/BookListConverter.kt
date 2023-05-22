package pt.ulisboa.tecnico.cmov.librarist.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pt.ulisboa.tecnico.cmov.librarist.model.book.Book

class BookListConverter {
    @TypeConverter
    fun fromBookList(books: List<Book>?): String? {
        if (books == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<Book>>() {}.type
        return gson.toJson(books, type)
    }

    @TypeConverter
    fun toBookList(booksString: String?): List<Book>? {
        if (booksString == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<Book>>() {}.type
        return gson.fromJson(booksString, type)
    }
}