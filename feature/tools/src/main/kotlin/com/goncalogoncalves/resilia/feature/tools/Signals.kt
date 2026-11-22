package com.goncalogoncalves.resilia.feature.tools

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.sin

/**
 * SOS in Morse (··· ––– ···) as a list of (lightOn, durationMs).
 *
 * The unit is 250 ms, so the fastest change is one 250 ms flash followed by 250 ms dark:
 * 2 flashes per second, below the W3C limit of 3 per second (WCAG 2.2 SC 2.3.1, decision
 * §2 of marco-5-fontes.md). Standard Morse ratios: dot 1, dash 3, gap inside a letter 1,
 * between letters 3, after the word 7.
 */
internal object SosPattern {
    const val UNIT_MS = 250L

    val sequence: List<Pair<Boolean, Long>> = buildList {
        fun letter(symbols: String) {
            symbols.forEachIndexed { i, c ->
                add(true to (if (c == '.') UNIT_MS else 3 * UNIT_MS))
                if (i < symbols.lastIndex) add(false to UNIT_MS)
            }
        }
        letter("...")
        add(false to 3 * UNIT_MS)
        letter("---")
        add(false to 3 * UNIT_MS)
        letter("...")
        add(false to 7 * UNIT_MS)
    }

    /** Highest number of light-on starts in any 1-second window of the looping pattern. */
    fun maxFlashesPerSecond(): Int {
        val starts = mutableListOf<Long>()
        var t = 0L
        repeat(2) { // two loops cover the wrap-around
            sequence.forEach { (on, ms) ->
                if (on) starts += t
                t += ms
            }
        }
        return starts.maxOf { s -> starts.count { it in s until s + 1000 } }
    }

    /** Plays the pattern until the coroutine is cancelled. */
    suspend fun play(setLight: (Boolean) -> Unit) {
        try {
            while (currentCoroutineContext().isActive) {
                for ((on, ms) in sequence) {
                    setLight(on)
                    delay(ms)
                }
            }
        } finally {
            setLight(false)
        }
    }
}

/**
 * Whistle: a high tone (3.15 kHz) for 0.5 s, then 0.5 s of silence, generated on the fly
 * (no audio files, no permission). Plays on the alarm stream so it is loud. Stops when the
 * coroutine is cancelled.
 */
internal object Whistle {
    private const val RATE = 44_100
    private const val FREQUENCY = 3_150.0
    private const val ON_MS = 500
    private const val OFF_MS = 500

    private val beep: ShortArray by lazy {
        val n = RATE * ON_MS / 1000
        val fade = RATE / 100 // 10 ms fade in/out: no clicks
        ShortArray(n) { i ->
            val envelope = minOf(1.0, i / fade.toDouble(), (n - i) / fade.toDouble())
            (sin(2 * PI * FREQUENCY * i / RATE) * envelope * Short.MAX_VALUE * 0.9).toInt().toShort()
        }
    }

    suspend fun play() {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(beep.size * 2)
            .build()
        try {
            track.write(beep, 0, beep.size)
            while (currentCoroutineContext().isActive) {
                runCatching { track.stop() }
                track.reloadStaticData()
                track.play()
                delay((ON_MS + OFF_MS).toLong())
            }
        } finally {
            runCatching { track.stop() }
            track.release()
        }
    }
}
