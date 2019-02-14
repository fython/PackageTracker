package com.spreada.utils.chinese

import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.HashMap
import java.util.HashSet
import java.util.Properties

class ZHConverter private constructor(propertyFile: String) {

    private val charMap = Properties()
    private val conflictingSets = HashSet<Any>()

    init {
        val inputStream: InputStream? = javaClass.getResourceAsStream(propertyFile)
        if (inputStream != null) {
            var reader: BufferedReader? = null
            try {
                reader = BufferedReader(InputStreamReader(inputStream))
                charMap.load(reader)
            } catch (e: FileNotFoundException) {
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            } finally {
                try {
                    reader?.close()
                    inputStream.close()
                } catch (e: IOException) {
                }
            }
        }
        initializeHelper()
    }

    private fun initializeHelper() {
        val stringPossibilities = HashMap<String, Int>()
        var iter: Iterator<*> = charMap.keys.iterator()
        while (iter.hasNext()) {
            val key = iter.next() as String
            if (key.isNotEmpty()) {
                for (i in 0 until key.length) {
                    val keySubstring = key.substring(0, i + 1)
                    if (stringPossibilities.containsKey(keySubstring)) {
                        val integer = stringPossibilities[keySubstring] as Int
                        stringPossibilities[keySubstring] = integer + 1
                    } else {
                        stringPossibilities[keySubstring] = 1
                    }

                }
            }
        }

        iter = stringPossibilities.keys.iterator()
        while (iter.hasNext()) {
            val key = iter.next()
            if (stringPossibilities[key]!!.toInt() > 1) {
                conflictingSets.add(key)
            }
        }
    }

    fun convert(inStr: String): String {
        val outString = StringBuilder()
        val stackString = StringBuilder()

        for (i in 0 until inStr.length) {
            val c = inStr[i]
            val key = "" + c
            stackString.append(key)
            when {
                conflictingSets.contains(stackString.toString()) -> {
                }
                charMap.containsKey(stackString.toString()) -> {
                    outString.append(charMap[stackString.toString()])
                    stackString.setLength(0)
                }
                else -> {
                    val sequence = stackString.subSequence(0, stackString.length - 1)
                    stackString.delete(0, stackString.length - 1)
                    flushStack(outString, StringBuilder(sequence))
                }
            }
        }

        flushStack(outString, stackString)

        return outString.toString()
    }

    private fun flushStack(outString: StringBuilder, stackString: StringBuilder) {
        while (stackString.isNotEmpty()) {
            if (charMap.containsKey(stackString.toString())) {
                outString.append(charMap[stackString.toString()])
                stackString.setLength(0)
            } else {
                outString.append("" + stackString[0])
                stackString.delete(0, 1)
            }
        }
    }

    internal fun parseOneChar(c: String): String {
        return if (charMap.containsKey(c)) charMap[c] as String else c
    }

    companion object {

        const val TRADITIONAL = 0
        const val SIMPLIFIED = 1
        private const val NUM_OF_CONVERTERS = 2
        private val converters = arrayOfNulls<ZHConverter>(NUM_OF_CONVERTERS)
        private val propertyFiles = arrayOfNulls<String>(2)

        init {
            propertyFiles[TRADITIONAL] = "zh2Hant.properties"
            propertyFiles[SIMPLIFIED] = "zh2Hans.properties"
        }

        /**
         *
         * @param converterType 0 for traditional and 1 for simplified
         * @return
         */
        fun getInstance(converterType: Int): ZHConverter? {
            if (converterType in 0..(NUM_OF_CONVERTERS - 1)) {
                if (converters[converterType] == null) {
                    synchronized(ZHConverter::class.java) {
                        if (converters[converterType] == null) {
                            converters[converterType] = ZHConverter(propertyFiles[converterType]!!)
                        }
                    }
                }
                return converters[converterType]
            } else {
                return null
            }
        }

        fun convert(text: String, converterType: Int): String {
            val instance = getInstance(converterType)
            return instance!!.convert(text)
        }

    }

}
