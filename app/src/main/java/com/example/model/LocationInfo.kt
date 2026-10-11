package com.example.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class LocationInfo(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val isDefault: Boolean = false,
    val parishNote: String? = null
)

object LocationDatabase {
    const val DEFAULT_LOCATION_ID = 106 // Živinice (Džemat Oskova)

    // Complete list of 116 locations aligned with vaktija.ba API indices (0 to 115)
    val LOCATIONS: List<LocationInfo> = listOf(
        LocationInfo(0, "Banovići", 44.4069, 18.5286),
        LocationInfo(1, "Banja Luka", 44.7722, 17.1910),
        LocationInfo(2, "Bihać", 44.8169, 15.8708),
        LocationInfo(3, "Bijeljina", 44.7587, 19.2144),
        LocationInfo(4, "Bileća", 42.8764, 18.4297),
        LocationInfo(5, "Bosanski Brod", 45.1381, 17.9864),
        LocationInfo(6, "Bosanska Dubica", 45.1800, 16.8086),
        LocationInfo(7, "Bosanska Gradiška", 45.1447, 17.2544),
        LocationInfo(8, "Bosansko Grahovo", 44.1794, 16.3639),
        LocationInfo(9, "Bosanska Krupa", 44.8825, 16.1528),
        LocationInfo(10, "Bosanski Novi", 45.0464, 16.3769),
        LocationInfo(11, "Bosanski Petrovac", 44.5550, 16.3694),
        LocationInfo(12, "Bosanski Šamac", 45.0597, 18.4681),
        LocationInfo(13, "Bratunac", 44.1844, 19.3308),
        LocationInfo(14, "Brčko", 44.8728, 18.8108),
        LocationInfo(15, "Breza", 44.0206, 18.2608),
        LocationInfo(16, "Bugojno", 44.0575, 17.4508),
        LocationInfo(17, "Busovača", 44.0983, 17.8817),
        LocationInfo(18, "Bužim", 45.0539, 16.0336),
        LocationInfo(19, "Cazin", 44.9669, 15.9431),
        LocationInfo(20, "Čajniče", 43.5569, 19.0717),
        LocationInfo(21, "Čapljina", 43.1119, 17.7006),
        LocationInfo(22, "Čelić", 44.7247, 18.8156),
        LocationInfo(23, "Čelinac", 44.7247, 17.3247),
        LocationInfo(24, "Čitluk", 43.2289, 17.7008),
        LocationInfo(25, "Derventa", 44.9783, 17.9078),
        LocationInfo(26, "Doboj", 44.7336, 18.0875),
        LocationInfo(27, "Donji Vakuf", 44.1436, 17.4008),
        LocationInfo(28, "Drvar", 44.3739, 16.3811),
        LocationInfo(29, "Foča", 43.5064, 18.7783),
        LocationInfo(30, "Fojnica", 43.9597, 17.9028),
        LocationInfo(31, "Gacko", 43.1672, 18.5358),
        LocationInfo(32, "Glamoč", 44.0458, 16.8489),
        LocationInfo(33, "Goražde", 43.6678, 18.9758),
        LocationInfo(34, "Gornji Vakuf", 43.9381, 17.5881),
        LocationInfo(35, "Gračanica", 44.7036, 18.3100),
        LocationInfo(36, "Gradačac", 44.8789, 18.4267),
        LocationInfo(37, "Grude", 43.3722, 17.4144),
        LocationInfo(38, "Hadžići", 43.8228, 18.2067),
        LocationInfo(39, "Han-Pijesak", 44.0817, 18.9517),
        LocationInfo(40, "Hlivno (Livno)", 43.8278, 17.0078),
        LocationInfo(41, "Ilijaš", 43.9514, 18.2711),
        LocationInfo(42, "Jablanica", 43.6603, 17.7617),
        LocationInfo(43, "Jajce", 44.3422, 17.2708),
        LocationInfo(44, "Kakanj", 44.1294, 18.1189),
        LocationInfo(45, "Kalesija", 44.4419, 18.8711),
        LocationInfo(46, "Kalinovik", 43.5047, 18.4464),
        LocationInfo(47, "Kiseljak", 43.9431, 18.0819),
        LocationInfo(48, "Kladanj", 44.2264, 18.6942),
        LocationInfo(49, "Ključ", 44.5328, 16.7767),
        LocationInfo(50, "Konjic", 43.6514, 17.9608),
        LocationInfo(51, "Kotor-Varoš", 44.6200, 17.3717),
        LocationInfo(52, "Kreševo", 43.8661, 18.0447),
        LocationInfo(53, "Kupres", 43.9903, 17.2789),
        LocationInfo(54, "Laktaši", 44.9083, 17.3014),
        LocationInfo(55, "Lopare", 44.6347, 18.8472),
        LocationInfo(56, "Lukavac", 44.5389, 18.5286),
        LocationInfo(57, "Ljubinje", 42.9500, 18.0872),
        LocationInfo(58, "Ljubuški", 43.1972, 17.5450),
        LocationInfo(59, "Maglaj", 44.5483, 18.1008),
        LocationInfo(60, "Modriča", 44.9575, 18.2989),
        LocationInfo(61, "Mostar", 43.3438, 17.8078),
        LocationInfo(62, "Mrkonjić-Grad", 44.4172, 17.0864),
        LocationInfo(63, "Neum", 42.9233, 17.6156),
        LocationInfo(64, "Nevesinje", 43.2589, 18.1139),
        LocationInfo(65, "Novi Travnik", 44.1706, 17.6578),
        LocationInfo(66, "Odžak", 45.0117, 18.3275),
        LocationInfo(67, "Olovo", 44.1286, 18.5806),
        LocationInfo(68, "Orašje", 45.0333, 18.6928),
        LocationInfo(69, "Pale", 43.8167, 18.5689),
        LocationInfo(70, "Posušje", 43.4719, 17.3289),
        LocationInfo(71, "Prijedor", 44.9797, 16.7139),
        LocationInfo(72, "Prnjavor", 44.8703, 17.6628),
        LocationInfo(73, "Prozor", 43.8236, 17.6089),
        LocationInfo(74, "Rogatica", 43.7989, 19.0044),
        LocationInfo(75, "Rudo", 43.6186, 19.3667),
        LocationInfo(76, "Sanski Most", 44.7667, 16.6667),
        LocationInfo(77, "Sarajevo", 43.8563, 18.4131),
        LocationInfo(78, "Skender-Vakuf", 44.4981, 17.3711),
        LocationInfo(79, "Sokolac", 43.9381, 18.8008),
        LocationInfo(80, "Srbac", 45.0972, 17.5244),
        LocationInfo(81, "Srebrenica", 44.1067, 19.2975),
        LocationInfo(82, "Srebrenik", 44.7081, 18.4883),
        LocationInfo(83, "Stolac", 43.0842, 17.9589),
        LocationInfo(84, "Šekovići", 44.2981, 18.8553),
        LocationInfo(85, "Šipovo", 44.2828, 17.0858),
        LocationInfo(86, "Široki Brijeg", 43.3828, 17.5936),
        LocationInfo(87, "Teslić", 44.6067, 17.8597),
        LocationInfo(88, "Tešanj", 44.6128, 17.9856),
        LocationInfo(89, "Tomislav-Grad", 43.7189, 17.2253),
        LocationInfo(90, "Travnik", 44.2264, 17.6658),
        LocationInfo(91, "Trebinje", 42.7119, 18.3436),
        LocationInfo(92, "Trnovo", 43.6586, 18.4483),
        LocationInfo(93, "Tuzla", 44.5384, 18.6671),
        LocationInfo(94, "Ugljevik", 44.6933, 18.9958),
        LocationInfo(95, "Vareš", 44.1644, 18.3283),
        LocationInfo(96, "Velika Kladuša", 45.1844, 15.8058),
        LocationInfo(97, "Visoko", 43.9889, 18.1781),
        LocationInfo(98, "Višegrad", 43.7828, 19.2922),
        LocationInfo(99, "Vitez", 44.1544, 17.7906),
        LocationInfo(100, "Vlasenica", 44.1817, 18.9961),
        LocationInfo(101, "Zavidovići", 44.4447, 18.1494),
        LocationInfo(102, "Zenica", 44.2034, 17.9077),
        LocationInfo(103, "Zvornik", 44.3858, 19.1039),
        LocationInfo(104, "Žepa", 43.9536, 19.1417),
        LocationInfo(105, "Žepče", 44.4267, 18.0378),
        LocationInfo(
            106,
            "Živinice",
            44.4497,
            18.6497,
            isDefault = true,
            parishNote = "Džemat Oskova"
        ),
        LocationInfo(107, "Bijelo Polje", 43.0381, 19.7475),
        LocationInfo(108, "Gusinje", 42.5619, 19.8336),
        LocationInfo(109, "Nova Varoš", 43.4606, 19.8131),
        LocationInfo(110, "Novi Pazar", 43.1367, 20.5122),
        LocationInfo(111, "Plav", 42.5969, 19.9456),
        LocationInfo(112, "Pljevlja", 43.3567, 19.3583),
        LocationInfo(113, "Priboj", 43.5878, 19.5256),
        LocationInfo(114, "Prijepolje", 43.3889, 19.6481),
        LocationInfo(115, "Rožaje", 42.8339, 20.1667)
    )

    fun getById(id: Int): LocationInfo {
        return LOCATIONS.find { it.id == id } ?: LOCATIONS.first { it.id == DEFAULT_LOCATION_ID }
    }

    /**
     * Finds nearest location using great-circle distance (Haversine formula).
     */
    fun findNearest(lat: Double, lon: Double): LocationInfo {
        var minDistance = Double.MAX_VALUE
        var nearest = getById(DEFAULT_LOCATION_ID)

        for (loc in LOCATIONS) {
            val dLat = Math.toRadians(loc.latitude - lat)
            val dLon = Math.toRadians(loc.longitude - lon)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat)) * cos(Math.toRadians(loc.latitude)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            val distance = 6371.0 * c // Earth radius in km

            if (distance < minDistance) {
                minDistance = distance
                nearest = loc
            }
        }
        return nearest
    }
}
