package com.example.util

import java.security.SecureRandom

object SeedPhraseGenerator {
    private val WORD_LIST = listOf(
        "calyx", "sepal", "bloom", "petal", "flora", "haven", "shield", "signal",
        "cipher", "forest", "emerald", "sage", "slate", "moss", "breeze", "dawn",
        "solstice", "lunar", "orbit", "wave", "pulse", "rhythm", "serene", "canopy",
        "zenith", "crystal", "amber", "willow", "meadow", "harbor", "anchor", "crest",
        "echo", "prism", "shadow", "summit", "timber", "vortex", "whisper", "horizon",
        "aurora", "balsam", "cedar", "dune", "elm", "fern", "grove", "heather",
        "iris", "juniper", "kelp", "laurel", "maple", "nectar", "oak", "pine",
        "quartz", "redwood", "spruce", "tulip", "umber", "violet", "wisteria", "yarrow",
        "clover", "frond", "stem", "sprout", "botanic", "vital", "matrix", "nexus",
        "equinox", "aspect", "vector", "beacon", "solace", "refuge", "vault", "sanctuary",
        "quiet", "gentle", "radiant", "luminous", "purity", "tranquil", "steady", "harmony",
        "clarity", "subtle", "tactile", "element", "essence", "balance", "cycle", "stream"
    )

    fun generate24Words(): String {
        val random = SecureRandom()
        val words = mutableListOf<String>()
        for (i in 0 until 24) {
            val randomIndex = random.nextInt(WORD_LIST.size)
            words.add(WORD_LIST[randomIndex])
        }
        return words.joinToString(" ")
    }

    fun hashPin(pin: String): String {
        if (pin.isEmpty()) return ""
        val bytes = pin.toByteArray(Charsets.UTF_8)
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
