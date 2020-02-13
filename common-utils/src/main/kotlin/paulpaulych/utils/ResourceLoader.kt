package paulpaulych.utils

import java.io.File
import java.io.InputStreamReader

class ResourceLoader {

    companion object{

        fun loadText(fname: String): String{
            return InputStreamReader(File(fname).inputStream()).readText()
        }

        fun loadLines(fname: String): List<String> {
            return InputStreamReader(File(fname).inputStream()).readLines()
        }

    }

}