package info.papdt.express.helper.support

import java.util.Arrays
import java.util.Random

/**
 * @author amulya
 * @datetime 14 Oct 2014, 5:20 PM
 */
class ColorGenerator private constructor(private val mColors: List<Int>) {

    private val mRandom: Random = Random(System.currentTimeMillis())

    val randomColor: Int
        get() = mColors[mRandom.nextInt(mColors.size)]

    fun getColor(key: Any): Int {
        return mColors[Math.abs(key.hashCode()) % mColors.size]
    }

    companion object {

        var DEFAULT: ColorGenerator = create(Arrays.asList(
                -0xe9c9c,
                -0xa7aa7,
                -0x65bc2,
                -0x1b39d2,
                -0x98408c,
                -0xa65d42,
                -0xdf6c33,
                -0x529d59,
                -0x7fa87f
        ))

        var MATERIAL: ColorGenerator = create(Arrays.asList(
                -0x16e19d,
                -0x63d850,
                -0x98c549,
                -0xc0ae4b,
                -0xde690d,
                -0xfc560c,
                -0xff6978,
                -0xb350b0,
                -0xa8de,
                -0x6800,
                -0x86aab8,
                -0x9f8275
        ))

        fun create(colorList: List<Int>): ColorGenerator {
            return ColorGenerator(colorList)
        }
    }
}
