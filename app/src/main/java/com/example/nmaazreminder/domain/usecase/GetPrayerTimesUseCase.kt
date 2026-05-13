package com.example.nmaazreminder.domain.usecase

import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.data.DateComponents
import com.example.nmaazreminder.data.local.PrayerSettings
import java.util.*
import javax.inject.Inject

class GetPrayerTimesUseCase @Inject constructor() {

    fun execute(settings: PrayerSettings, date: Date = Date()): PrayerTimes {
        val coordinates = Coordinates(settings.latitude, settings.longitude)
        val dateComponents = DateComponents.from(date)

        // Map the integer from our Database to the Adhan Library Methods
        val params = when (settings.calculationMethod) {
            1 -> CalculationMethod.KARACHI.parameters
            2 -> CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
            else -> CalculationMethod.NORTH_AMERICA.parameters
        }
        // hanfi or shafi k liye
        params.madhab = if (settings.asrMethod == 1) Madhab.HANAFI else Madhab.SHAFI

        return PrayerTimes(coordinates, dateComponents, params)
    }
}