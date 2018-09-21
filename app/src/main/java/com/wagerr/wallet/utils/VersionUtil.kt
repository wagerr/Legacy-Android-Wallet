package com.wagerr.wallet.utils

import java.util.*
//https://stackoverflow.com/questions/11501192/comparing-version-number-strings-major-minor-revision-beta
private fun formatVersionString(strArr: Array<String>): Array<String> {
    //remove trailing 0s
    val list = mutableListOf<String>()
    var foundChar = false
    for (i in strArr.indices.reversed()) {
        val curChar = strArr[i]
        if (curChar == "0" && !foundChar) {
            continue
        } else {
            list.add(strArr[i])
            foundChar = true
        }

    }
    Collections.reverse(list)
    return list.toTypedArray()
}

private fun getPreReleaseBuildStr(buildStr: String?): String? {
    //removing build metadata
    if (buildStr == null) {
        return null
    }
    val a = buildStr.split("\\+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    return if (a.size > 0) {
        a[0]
    } else {
        null
    }
}

private fun compareVersionString(str1: String, str2: String): Int {
    var ret = 0
    val verStr1 = formatVersionString(str1.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
    val verStr2 = formatVersionString(str2.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())

    var i = 0
    // set index to first non-equal ordinal or length of shortest version string
    while (i < verStr1.size && i < verStr2.size && verStr1[i] == verStr2[i]) {
        i++
    }

    // compare first non-equal ordinal number
    if (i < verStr1.size && i < verStr2.size) {
        var diff = 0
        try {
            if (verStr1[i] == null || verStr1[i].trim { it <= ' ' }.length == 0) {
                verStr1[i] = "0"
            }
            if (verStr2[i] == null || verStr2[i].trim { it <= ' ' }.length == 0) {
                verStr2[i] = "0"
            }
            diff = Integer.valueOf(verStr1[i]).compareTo(Integer.valueOf(verStr2[i]))
        } catch (e: NumberFormatException) {
            diff = verStr1[i].compareTo(verStr2[i])
        } finally {
            ret = Integer.signum(diff)
        }
    } else {
        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"

        ret = Integer.signum(verStr1.size - verStr2.size)
    }

    return ret
}

/**
 * Compares two version strings.
 * follow this link for more info http://semver.org/
 *
 * Use this instead of String.compareTo() for a non-lexicographical
 * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
 *
 * Ex:--
 * //1.0.0-alpha < 1.0.0-alpha.1 < 1.0.0-alpha.beta < 1.0.0-beta < 1.0.0-beta.2 < 1.0.0-beta.11 < 1.0.0-rc.1 < 1.0.0 < 2.0.0.6
 *
 * @param str1 a string of ordinal numbers separated by decimal points.
 * @param str2 a string of ordinal numbers separated by decimal points.
 * @return The result is a negative integer if str1 is _numerically_ less than str2.
 * The result is a positive integer if str1 is _numerically_ greater than str2.
 * The result is zero if the strings are _numerically_ equal.
 */

fun versionCompare(str1: String, str2: String): Int {

    var ret = 0
    val val1 = str1.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val val2 = str2.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

    var preReleaseVer1: String? = null
    var preReleaseVer2: String? = null
    if (val1.size > 1) {
        preReleaseVer1 = getPreReleaseBuildStr(val1[1])
    }
    if (val2.size > 1) {
        preReleaseVer2 = getPreReleaseBuildStr(val2[1])
    }

    ret = compareVersionString(val1[0], val2[0])

    if (ret == 0) {
        //if both version are equal then compare with pre_release String
        if (preReleaseVer1 == null && preReleaseVer2 == null) {
            ret = 0
        } else if (preReleaseVer1 == null && preReleaseVer2 != null) {
            //1.0.0 > 1.0.0-beta
            ret = 1
        } else if (preReleaseVer1 != null && preReleaseVer2 == null) {
            //1.0.0-beta < 1.0.0
            ret = -1
        } else {
            //both hasve pre release string
            ret = compareVersionString(preReleaseVer1!!, preReleaseVer2!!)
        }
    }

    return ret
}