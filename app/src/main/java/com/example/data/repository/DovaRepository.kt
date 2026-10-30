package com.example.data.repository

import com.example.model.Dova
import com.example.model.DovaCategory

object DovaRepository {

    val DOVE_LIST: List<Dova> = listOf(
        // 1. Dova poslije ezana
        Dova(
            id = "ezan_dova",
            title = "Dova poslije ezana",
            category = DovaCategory.EZAN_DOVE,
            arabicText = "اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ، وَالصَّلَاةِ الْقَائِمَةِ، آتِ مُحَمَّدًا الْوَسِيلَةَ وَالْفَضِيلَةَ، وَابْعَثْهُ مَقَامًا مَحْمُودًا الَّذِي وَعَدْتَهُ",
            transliteration = "Allahumme Rabbe hazihid-da'vetit-tammeh, ves-salatil-ka'imeh, ati Muhammedenil-vesilete vel-fadileh, veb'ashu mekamen mahmudenillezi ve'adteh.",
            translation = "Allahu moj, Gospodaru ovog potpunog poziva i namaza koji se upravo uspostavlja, podari Muhammedu 'Vesilu' (najuzvišenije mjesto u Džennetu) i svako dobro, i proživi ga na hvaljenom mjestu koje si mu obećao.",
            source = "Sahihul-Buhari (614)",
            virtue = "Ko prouči ovu dovu nakon što sasluša ezan, zagarantovan mu je šefa'at (zagovorništvo) Poslanika, s.a.v.s., na Sudnjem danu.",
            targetCount = 1
        ),

        // 2. Jutarnji zikr - Sejjidul istigfar
        Dova(
            id = "sejjidul_istigfar",
            title = "Sejjidul-istigfar (Prvak svih istigfara)",
            category = DovaCategory.JUTARNJI_ZIKR,
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            transliteration = "Allahumme Ente Rabbi, la ilahe illa Ente, halakteni ve ene 'abduke, ve ene 'ala 'ahdike ve va'dike mesteta'tu, e'uzu bike min šerri ma sana'tu, ebu'u leke bini'metike 'alejje, ve ebu'u bizenbi fagfir li fe innehu la jagfiruz-zunube illa Ente.",
            translation = "Allahu moj, Ti si moj Gospodar, nema boga osim Tebe. Stvorio si me i ja sam Tvoj rob, i ja se držim obećanja i ugovora Tebi datog koliko mogu. Utječem Ti se od zla koje sam počinio. Priznajem Tvoje blagodati prema meni, i priznajem svoje grijehe, pa mi oprosti, jer grijehe ne oprašta niko osim Tebe.",
            source = "Sahihul-Buhari (6306)",
            virtue = "Ko ovo prouči ujutro čvrsto vjerujući pa umre tog dana prije večeri, stanovnik je Dženneta; i ko ovo prouči uvečer pa umre prije jutra, stanovnik je Dženneta.",
            targetCount = 1
        ),

        // 3. Jutarnji zikr - Zaštita od svega (Bismillahi-llezi)
        Dova(
            id = "bismillahillez_la_yedurru",
            title = "Zaštita s imenom Allaha",
            category = DovaCategory.JUTARNJI_ZIKR,
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillahil-lezi la jedurru me'asmihi šej'un fil-erdi ve la fis-sema'i ve Huves-Semi'ul-'Alim.",
            translation = "U ime Allaha, s Čijim imenom ništa ne može nauditi ni na Zemlji ni na nebu, a On sve čuje i sve zna.",
            source = "Ebu Davud (5088), Tirmizi (3388)",
            virtue = "Ko ovu dovu prouči ujutro i navečer tri puta, ništa mu neće nauditi tog dana niti te noći.",
            targetCount = 3
        ),

        // 4. Večernji zikr - E'uzu bikelimatillahit-tammati
        Dova(
            id = "euzu_bikelimatillah",
            title = "Zaštita potpunim Allahovim riječima",
            category = DovaCategory.VECERNJI_ZIKR,
            arabicText = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            transliteration = "E'uzu bikelimatillahit-tammati min šerri ma halak.",
            translation = "Utječem se Allahovim savršenim riječima od zla onoga što je stvorio.",
            source = "Sahih Muslim (2709)",
            virtue = "Ko ovo prouči tri puta uvečer, neće mu nauditi nikakav otrov niti ujed te noći.",
            targetCount = 3
        ),

        // 5. Večernji zikr - Raditu billahi Rabba
        Dova(
            id = "raditu_billahi",
            title = "Zadovoljstvo Allahom, islamom i Poslanikom",
            category = DovaCategory.VECERNJI_ZIKR,
            arabicText = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا",
            transliteration = "Raditu billahi Rabba, ve bil-islami dina, ve bi Muhammedin sallallahu 'alejhi ve selleme nebijja.",
            translation = "Zadovoljan sam da mi je Allah Gospodar, islam vjera, i Muhammed, s.a.v.s., poslanik.",
            source = "Ebu Davud (1529), Tirmizi (3389)",
            virtue = "Ko ovo prouči 3 puta ujutro i navečer, Allah se obavezao da će ga učiniti zadovoljnim na Sudnjem danu.",
            targetCount = 3
        ),

        // 6. Namaske dove - Ajetul-Kursijja
        Dova(
            id = "ajetul_kursi",
            title = "Ajetul-Kursijja (Poslije svakog propisanog namaza)",
            category = DovaCategory.NAMASKE_DOVE,
            arabicText = "اللَّهُ لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ مَنْ ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلَّا بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلَا يُحِيطُونَ بِشَيْءٍ مِنْ عِلْمِهِ إِلَّا بِمَا شَاءَ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ وَلَا يَئُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ",
            transliteration = "Allahu la ilahe illa Huvel-Hajjul-Kajjum. La te'huzuhu sinetun ve la nevm. Lehu ma fis-semavati ve ma fil-erd. Men zellezi ješfe'u 'indehu illa bi iznih. Ja'lemu ma bejne ejdihim ve ma halfehum, ve la juhitune bi šej'in min 'ilmihi illa bima ša'. Vesi'a kursijjuhus-semavati vel-erd, ve la je'uduhu hifzuhuma, ve Huvel-'Alijjul-'Azim.",
            translation = "Allah je, nema boga osim Njega, Živi i Vječni Održavalac! Ne obuzima Ga ni drijemež ni san! Njegovo je ono što je na nebesima i ono što je na Zemlji! Ko se može pred Njim zauzimati bez Njegova dopuštenja?! On zna šta je bilo prije njih i šta će biti poslije njih, a od onoga što On zna – drugi znaju samo onoliko koliko On želi. Moć Njegova obuhvata i nebesa i Zemlju i Njemu ne dojadi održavanje njihovo; On je Svevišnji, Veličanstveni!",
            source = "Sura El-Bekare, 255; Nesai (Es-Sunen el-Kubra 9848)",
            virtue = "Ko prouči Ajetul-Kursijju poslije svakog propisanog namaza, ništa ga ne dijeli od ulaska u Džennet osim smrt.",
            targetCount = 1
        ),

        // 7. Namaske dove - Zikr poslije selama
        Dova(
            id = "poslije_selama_estagfirullah",
            title = "Dova neposredno poslije predaje selama",
            category = DovaCategory.NAMASKE_DOVE,
            arabicText = "أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ، اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
            transliteration = "Estagfirullah, Estagfirullah, Estagfirullah. Allahumme Entes-Selamu ve minkes-selam, tebarekte ja Zel-dželali vel-ikram.",
            translation = "Allaha za oprost molim (3x). Allahu moj, Ti si Mir i od Tebe je mir. Uzvišen si Ti, o Posjedniče veličine i plemenitosti.",
            source = "Sahih Muslim (591)",
            virtue = "Sunnet Poslanika, s.a.v.s., neposredno po predaji selama na kraju svakog namaza.",
            targetCount = 1
        ),

        // 8. Iftarska dova
        Dova(
            id = "iftarska_dova",
            title = "Iftarska dova (Pri prekidu posta)",
            category = DovaCategory.IFTAR_SEHUR,
            arabicText = "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ، وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللَّهُ",
            transliteration = "Zehebez-zame'u vebtelletil-'uruk, ve sebetel-edžru in ša'Allah.",
            translation = "Nestala je žeđ, natopile se vene, a nagrada je osigurana, ako Bog da.",
            source = "Ebu Davud (2357)",
            virtue = "Uči se prilikom samog iftara čim se pojede hurma ili popije gutljaj vode.",
            targetCount = 1
        ),

        // 9. Dova kod sehura i namjere za post
        Dova(
            id = "nijjet_posta",
            title = "Nijjet i dova za post (Sehur)",
            category = DovaCategory.IFTAR_SEHUR,
            arabicText = "اللَّهُمَّ إِنِّي لَكَ صُمْتُ وَبِكَ آمَنْتُ وَعَلَيْكَ تَوَكَّلْتُ وَعَلَى رِزْقِكَ أَفْطَرْتُ",
            transliteration = "Allahumme inni leke sumtu, ve bike amentu, ve 'alejke tevekkeltu, ve 'ala rizkike eftartu.",
            translation = "Allahu moj, u Tvoje ime postim, u Tebe vjerujem, na Tebe se oslanjam i Tvojom se opskrbom iftarim.",
            source = "Ebu Davud (2358)",
            virtue = "Iskrena predanost i potvrda posta u ime Allaha dž.š.",
            targetCount = 1
        ),

        // 10. Svakodnevne dove - Za roditelje
        Dova(
            id = "dova_za_roditelje",
            title = "Dova za roditelje",
            category = DovaCategory.SVAKODNEVNE,
            arabicText = "رَبِّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا",
            transliteration = "Rabbir-hamhuma kema rabbejani sagira.",
            translation = "Gospodaru moj, smiluj im se, onako kako su oni mene pazili i odgajali dok sam bio dijete!",
            source = "Kur'an, El-Isra (24)",
            virtue = "Kur'anska dova za dobročinstvo prema ocu i majci.",
            targetCount = 3
        ),

        // 11. Svakodnevne dove - Za otklanjanje brige i tuge
        Dova(
            id = "otklanjanje_brige_tuge",
            title = "Dova protiv brige, tuge i nemoći",
            category = DovaCategory.ZASTITA_ISTIGFAR,
            arabicText = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْبُخْلِ وَالْجُبْنِ، وَضَلَعِ الدَّيْنِ وَغَلَبَةِ الرِّجَالِ",
            transliteration = "Allahumme inni e'uzu bike minel-hemmi vel-hazeni, vel-'adžzi vel-keseli, vel-buhli vel-džubni, ve dala'id-dejni ve galebetir-ridžal.",
            translation = "Allahu moj, utječem Ti se od brige i tuge, od nemoći i lijenosti, od škrtosti i kukavičluka, od tereta duga i premoći ljudi.",
            source = "Sahihul-Buhari (2893)",
            virtue = "Poslanik, s.a.v.s., je često ponavljao ovu dovu za duševni mir i olakšanje tereta.",
            targetCount = 1
        ),

        // 12. Dova pri izlasku iz kuće
        Dova(
            id = "izlazak_iz_kuce",
            title = "Dova pri izlasku iz kuće",
            category = DovaCategory.SVAKODNEVNE,
            arabicText = "بِسْمِ اللَّهِ، تَوَكَّلْتُ عَلَى اللَّهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            transliteration = "Bismillahi, tevekkeltu 'alallahi, ve la havle ve la kuvvete illa billah.",
            translation = "U ime Allaha, u Allaha se uzdam, nema moći niti snage osim s Allahom.",
            source = "Ebu Davud (5095), Tirmizi (3426)",
            virtue = "Kada čovjek ovo izgovori, melek mu kaže: 'Uputio si se, zbrinut si i zaštićen si', pa se šejtani udalje od njega.",
            targetCount = 1
        ),

        // 13. Dova za putovanje
        Dova(
            id = "putna_dova",
            title = "Putna dova (Dova za putovanje)",
            category = DovaCategory.SVAKODNEVNE,
            arabicText = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ",
            transliteration = "Subhanel-lezi sehhare lena haza ve ma kunna lehu mukrinin, ve inna ila Rabbina lemunkalibun.",
            translation = "Slavljen neka je Onaj Koji nam je ovo potčinio, a mi to sami ne bismo mogli postići, i mi ćemo se, sigurno, Gospodaru svome vratiti!",
            source = "Kur'an, Ez-Zuhruf (13-14); Sahih Muslim (1342)",
            virtue = "Uči se pri ulasku u prijevozno sredstvo i na polasku na put.",
            targetCount = 1
        )
    )

    fun getAll(): List<Dova> = DOVE_LIST

    fun getByCategory(category: DovaCategory): List<Dova> =
        DOVE_LIST.filter { it.category == category }

    fun search(query: String): List<Dova> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return DOVE_LIST
        return DOVE_LIST.filter {
            it.title.lowercase().contains(q) ||
                    it.translation.lowercase().contains(q) ||
                    it.transliteration.lowercase().contains(q) ||
                    it.category.title.lowercase().contains(q)
        }
    }
}
