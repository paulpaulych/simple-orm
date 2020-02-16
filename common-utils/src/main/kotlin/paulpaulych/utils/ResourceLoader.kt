package paulpaulych.utils

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class ResourceLoader {

    companion object{

        fun loadText(fname: String): String{
            val fis = javaClass.classLoader.getResourceAsStream(fname)
            return BufferedReader(InputStreamReader(fis)).readText()
        }

        fun loadLines(fname: String): List<String> {
            val fis = javaClass.classLoader.getResourceAsStream(fname)
            return BufferedReader(InputStreamReader(fis)).readLines()
        }

    }

}