package com.molloo

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object EmptySerializer : KSerializer<Any?> {
    override fun serialize(encoder: Encoder, value: Any?) {

    }

    override fun deserialize(decoder: Decoder): Any? {
        return null
    }

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Any")
}