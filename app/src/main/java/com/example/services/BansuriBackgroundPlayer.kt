package com.example.services

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.sin

object BansuriBackgroundPlayer {
    private var isPlaying = false
    private var audioJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun setEnabled(enabled: Boolean) {
        if (enabled) {
            start()
        } else {
            stop()
        }
    }

    @Synchronized
    fun start() {
        if (isPlaying) return
        isPlaying = true

        audioJob = scope.launch {
            val sampleRate = 22050
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(2048)

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            try {
                audioTrack.play()
            } catch (e: Exception) {
                isPlaying = false
                return@launch
            }

            val shortBuffer = ShortArray(bufferSize / 2)
            var phase1 = 0.0
            var phase2 = 0.0
            var phase3 = 0.0
            var lfoPhase = 0.0

            // Frequencies for peaceful Bansuri / Tanpura drone in C (Root C4, Fifth G4, Octave C5)
            val freq1 = 261.63 // C4
            val freq2 = 392.00 // G4
            val freq3 = 523.25 // C5

            try {
                while (isActive && isPlaying) {
                    for (i in shortBuffer.indices) {
                        // Gentle breathing vibrato / swell
                        val lfo = 0.7 + 0.3 * sin(lfoPhase)
                        lfoPhase += 2.0 * Math.PI * 0.1 / sampleRate // slow 0.1 Hz breathing swell

                        val s1 = sin(phase1) * 0.15
                        val s2 = sin(phase2) * 0.10
                        val s3 = sin(phase3) * 0.06

                        val sample = ((s1 + s2 + s3) * lfo * 0.12 * Short.MAX_VALUE).toInt().toShort()
                        shortBuffer[i] = sample

                        phase1 += 2.0 * Math.PI * freq1 / sampleRate
                        phase2 += 2.0 * Math.PI * freq2 / sampleRate
                        phase3 += 2.0 * Math.PI * freq3 / sampleRate

                        if (phase1 > 2.0 * Math.PI) phase1 -= 2.0 * Math.PI
                        if (phase2 > 2.0 * Math.PI) phase2 -= 2.0 * Math.PI
                        if (phase3 > 2.0 * Math.PI) phase3 -= 2.0 * Math.PI
                        if (lfoPhase > 2.0 * Math.PI) lfoPhase -= 2.0 * Math.PI
                    }
                    audioTrack.write(shortBuffer, 0, shortBuffer.size)
                }
            } catch (e: Exception) {
                // handle
            } finally {
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (e: Exception) {}
            }
        }
    }

    @Synchronized
    fun stop() {
        isPlaying = false
        audioJob?.cancel()
        audioJob = null
    }
}
