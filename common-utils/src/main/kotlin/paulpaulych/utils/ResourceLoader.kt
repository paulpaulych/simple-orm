package paulpaulych.utils

import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

class ResourceLoader {

    companion object{

        fun loadText(fname: String): String{
            return BufferedReader(InputStreamReader(fis(fname))).readText()
        }

        fun loadLines(fname: String): List<String> {
            return BufferedReader(InputStreamReader(fis(fname))).readLines()
        }

        private fun fis(fname: String): InputStream {
            return javaClass.classLoader.getResourceAsStream(fname)
                    ?:error("resource '$fname' not found")
        }

    }

}
