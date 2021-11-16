package levkaantonov.com.study.voiceview

import java.util.*

class WaveRepository {
    companion object {
        const val MAX_VOLUME = 100
        private const val WAVE_LENGTH = 200

        fun getWaveData(): Array<Int> {
            val data = Array(WAVE_LENGTH) { 0 }
            val random = Random()
            for (i in data.indices) {
                data[i] = random.nextInt(MAX_VOLUME + 1)
            }
            return data
        }
    }
}