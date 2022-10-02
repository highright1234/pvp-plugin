package io.github.highright1234.harasample.config

import io.github.monun.tap.config.Config

@Suppress("MagicNumber")
object SampleConfig : ConfigClass {

    @Config
    var message = "응애"

    @Config
    var loveLetterDelay = 2000L

    @Config
    var loveLetterRandomDelay = 750L
}