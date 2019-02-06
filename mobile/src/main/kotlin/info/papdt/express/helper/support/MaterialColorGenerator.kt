package info.papdt.express.helper.support

import android.content.Context
import info.papdt.express.helper.model.MaterialPalette
import kotlin.random.Random

object MaterialColorGenerator {

    lateinit var Red: MaterialPalette
    lateinit var Pink: MaterialPalette
    lateinit var Purple: MaterialPalette
    lateinit var DeepPurple: MaterialPalette
    lateinit var Indigo: MaterialPalette
    lateinit var Blue: MaterialPalette
    lateinit var LightBlue: MaterialPalette
    lateinit var Cyan: MaterialPalette
    lateinit var Teal: MaterialPalette
    lateinit var Green: MaterialPalette
    lateinit var LightGreen: MaterialPalette
    lateinit var Lime: MaterialPalette
    lateinit var Yellow: MaterialPalette
    lateinit var Amber: MaterialPalette
    lateinit var Orange: MaterialPalette
    lateinit var DeepOrange: MaterialPalette
    lateinit var Brown: MaterialPalette
    lateinit var BlueGrey: MaterialPalette

    val AllPalettes: List<MaterialPalette> by lazy {
        listOf(Red, Pink, Purple, DeepPurple, Indigo, Blue, LightBlue,
                Cyan, Teal, Green, LightGreen, Lime, Amber, Orange,
                DeepOrange, Brown, BlueGrey)
    }

    private val random: Random = Random(System.currentTimeMillis())

    private val RedProducer = PaletteProducer("red",
            "50", "100", "200", "300", "400", "500",
            "600", "700", "800", "900", "A100", "A200", "A400", "A700")
    private val PinkProducer = PaletteProducer("pink",
            "50", "100", "200", "300", "400", "500",
            "600", "700", "800", "900", "A100", "A200", "A400", "A700")
    private val PurpleProducer = PaletteProducer("purple",
            "50", "100", "200", "300", "400", "500",
            "600", "700", "800", "900", "A100", "A200", "A400", "A700")
    private val DeepPurpleProducer = PaletteProducer("deep_purple",
            "50", "100", "200", "300", "400", "500",
            "600", "700", "800", "900", "A100", "A200", "A400", "A700")
    private val IndigoProducer = PaletteProducer("indigo",
            "50", "100", "200", "300", "400", "500",
            "600", "700", "800", "900", "A100", "A200", "A400", "A700")
    private val BlueProducer = PaletteProducer("blue",
            "50", "100", "200", "300", "400", "500",
            "600", "700", "800", "900", "A100", "A200", "A400", "A700")
    private val LightBlueProducer = PaletteProducer("light_blue",
            "50", "100", "200", "300", "400", "500",
            "600", "700", "800", "900", "A100", "A200", "A400", "A700")
    private val CyanProducer = PaletteProducer("cyan",
            "50", "100", "200", "300", "400", "500",
            "600", "700", "800", "900", "A100", "A200", "A400", "A700")
    private val TealProducer = PaletteProducer("teal",
            "50", "100", "200", "300", "400", "500",
            "600", "700", "800", "900", "A100", "A200", "A400", "A700")
    private val GreenProducer = PaletteProducer("green",
            "50", "100", "200", "300", "400", "500",
            "600", "700", "800", "900", "A100", "A200", "A400", "A700")
    private val LightGreenProducer = PaletteProducer("light_green",
            "50", "100", "200", "300", "400", "500",
            "600", "700", "800", "900", "A100", "A200", "A400", "A700")
    private val LimeProducer = PaletteProducer("lime",
            "50", "100", "200", "300", "400", "500",
            "600", "700", "800", "900", "A100", "A200", "A400", "A700")
    private val YellowProducer = PaletteProducer("yellow",
            "50", "100", "200", "300", "400", "500",
            "600", "700", "800", "900", "A100", "A200", "A400", "A700")
    private val AmberProducer = PaletteProducer("amber",
            "50", "100", "200", "300", "400", "500",
            "600", "700", "800", "900", "A100", "A200", "A400", "A700")
    private val OrangeProducer = PaletteProducer("orange",
            "50", "100", "200", "300", "400", "500",
            "600", "700", "800", "900", "A100", "A200", "A400", "A700")
    private val DeepOrangeProducer = PaletteProducer("deep_orange",
            "50", "100", "200", "300", "400", "500",
            "600", "700", "800", "900", "A100", "A200", "A400", "A700")
    private val BrownProducer = PaletteProducer("brown",
            "50", "100", "200", "300", "400", "500",
            "600", "700", "800", "900")
    private val BlueGreyProducer = PaletteProducer("blue_grey",
            "50", "100", "200", "300", "400", "500",
            "600", "700", "800", "900")

    fun init(context: Context) {
        Red = RedProducer.make(context)
        Pink = PinkProducer.make(context)
        Purple = PurpleProducer.make(context)
        DeepPurple = DeepPurpleProducer.make(context)
        Indigo = IndigoProducer.make(context)
        Blue = BlueProducer.make(context)
        LightBlue = LightBlueProducer.make(context)
        Cyan = CyanProducer.make(context)
        Teal = TealProducer.make(context)
        Green = GreenProducer.make(context)
        LightGreen = LightGreenProducer.make(context)
        Lime = LimeProducer.make(context)
        Yellow = YellowProducer.make(context)
        Amber = AmberProducer.make(context)
        Orange = OrangeProducer.make(context)
        DeepOrange = DeepOrangeProducer.make(context)
        Brown = BrownProducer.make(context)
        BlueGrey = BlueGreyProducer.make(context)
    }

    fun getPalette(key: Any): MaterialPalette {
        return AllPalettes[Math.abs(key.hashCode()) % AllPalettes.size]
    }

    private class PaletteProducer(
            val colorName: String,
            vararg val colorKeys: String
    ) {

        fun make(context: Context): MaterialPalette {
            val res = context.resources
            val ids = colorKeys.map {
                it to res.getIdentifier("${colorName}_$it",
                        "color", context.packageName)
            }.toTypedArray()
            return MaterialPalette.makeFromResources(context, colorName, *ids)
        }

    }

}