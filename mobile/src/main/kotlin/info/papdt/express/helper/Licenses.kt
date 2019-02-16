package info.papdt.express.helper

import kotlinx.html.*
import kotlinx.html.dom.*

object Licenses {

    private val APACHE_LICENSE_FOOTER = """
        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
        """.trimIndent()

    private val MIT_LICENSE_FOOTER = """
        MIT License

        Permission is hereby granted, free of charge, to any person obtaining a copy
        of this software and associated documentation files (the "Software"), to deal
        in the Software without restriction, including without limitation the rights
        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        copies of the Software, and to permit persons to whom the Software is
        furnished to do so, subject to the following conditions:

        The above copyright notice and this permission notice shall be included in all
        copies or substantial portions of the Software.

        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
        SOFTWARE.
        """.trimIndent()

    private const val AUTHOR_NAME_GOOGLE = "Google Inc."
    private const val AUTHOR_NAME_ME = "Fung Gwo (fython)"
    private const val AUTHOR_NAME_RIKKA = "RikkaW"
    private const val AUTHOR_NAME_DRAKEET = "Drakeet"
    private const val AUTHOR_NAME_AOSP = "The Android Open Source Project"

    val html: String = document {
        append.html {
            head {
                style {
                    unsafe {
                        raw("""
                            body {
                            margin: 1em;
                            }
                            pre {
                            background: #DDD;
                            padding: 1em;
                            white-space: pre-wrap;
                            }""")
                    }
                }
            }
            body {
                h2 {
                    text("Notices for Additional Libraries")
                }

                p {
                    text("This app contains several libraries that are released under the terms of the following licenses.")
                }

                noticeForFiles(
                        "OkHttp",
                        APACHE_LICENSE_FOOTER
                )

                noticeForFiles(
                        "Gson",
                        copyright(2008, AUTHOR_NAME_GOOGLE) + APACHE_LICENSE_FOOTER
                )

                noticeForFiles(
                        "AlipayZeroSdk",
                        copyright(2016, AUTHOR_NAME_ME) + APACHE_LICENSE_FOOTER
                )

                noticeForFiles(
                        "Kotlinyan",
                        copyright(2017, AUTHOR_NAME_ME) + MIT_LICENSE_FOOTER
                )

                noticeForFiles(
                        "MaterialStepperView",
                        copyright(2017, AUTHOR_NAME_ME) + MIT_LICENSE_FOOTER
                )

                noticeForFiles(
                        "MaterialPreference",
                        copyright(2016, AUTHOR_NAME_RIKKA) + APACHE_LICENSE_FOOTER
                )

                noticeForFiles(
                        "CircleImageView",
                        copyright(2014, 2016, "Henning Dodenhof") +
                                APACHE_LICENSE_FOOTER
                )

                noticeForFiles(
                        "Android Support Libraries (AndroidX)",
                        copyright(2013, 2019, AUTHOR_NAME_AOSP) +
                                APACHE_LICENSE_FOOTER
                )

                noticeForFiles(
                        "SmartRefreshLayout",
                        copyright(2017, "scwang90") + APACHE_LICENSE_FOOTER
                )

                noticeForFiles(
                        "MultiType",
                        copyright(2017, AUTHOR_NAME_DRAKEET) + APACHE_LICENSE_FOOTER
                )

                noticeForFiles(
                        "Spanny",
                        copyright(2015, "Pavlovsky Ivan") + APACHE_LICENSE_FOOTER
                )

                noticeForFiles(
                        "barcodescanner",
                        copyright(2014, "Dushyanth Maguluru") + APACHE_LICENSE_FOOTER
                )

                noticeForFiles(
                        "ZHConverter",
                        """
                            LGPL

                            More details see: https://code.google.com/archive/p/java-zhconverter
                        """.trimIndent()
                )

                noticeForFiles(
                        "TinyPinyin",
                        Licenses.copyright(2017, "promeG") + APACHE_LICENSE_FOOTER
                )
            }
        }
    }.serialize()

    private fun copyright(year: Int, orgName: String): String {
        return "Copyright (c) $year $orgName\n\n"
    }

    private fun copyright(fromYear: Int, toYear: Int, orgName: String): String {
        return "Copyright (c) $fromYear - $toYear $orgName\n\n"
    }

    private fun BODY.noticeForFiles(title: String, content: String) {
        h3 {
            text("Notices for files")
        }

        ul {
            li {
                text(title)
            }
        }
        pre {
            text(content)
        }
    }

}