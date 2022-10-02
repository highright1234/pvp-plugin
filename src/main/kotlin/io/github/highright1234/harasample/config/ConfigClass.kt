package io.github.highright1234.harasample.config

import io.github.monun.tap.config.ConfigSupport
import java.io.File

interface ConfigClass {
    fun load(configFile: File) {
        ConfigSupport.compute(this, configFile)
    }
}