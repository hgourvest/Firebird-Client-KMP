package com.progdigy.fbclient

import kotlin.jvm.JvmInline

@JvmInline
value class TimeZoneId(val id: Int) {
    fun getName(): String =
        if (id in fb_tzid_america_ciudad_juarez.id..fb_tzid_gmt.id) {
            fb_tzids.elementAt(fb_tzid_gmt.id - id)
        } else ""
}

val fb_tzids = setOf(
    "GMT",
    "ACT", // *
    "AET", // *
    "AGT", // *
    "ART", // *
    "AST", // *
    "Africa/Abidjan",
    "Africa/Accra",
    "Africa/Addis_Ababa",
    "Africa/Algiers",
    "Africa/Asmara",
    "Africa/Asmera",
    "Africa/Bamako",
    "Africa/Bangui",
    "Africa/Banjul",
    "Africa/Bissau",
    "Africa/Blantyre",
    "Africa/Brazzaville",
    "Africa/Bujumbura",
    "Africa/Cairo",
    "Africa/Casablanca",
    "Africa/Ceuta",
    "Africa/Conakry",
    "Africa/Dakar",
    "Africa/Dar_es_Salaam",
    "Africa/Djibouti",
    "Africa/Douala",
    "Africa/El_Aaiun",
    "Africa/Freetown",
    "Africa/Gaborone",
    "Africa/Harare",
    "Africa/Johannesburg",
    "Africa/Juba",
    "Africa/Kampala",
    "Africa/Khartoum",
    "Africa/Kigali",
    "Africa/Kinshasa",
    "Africa/Lagos",
    "Africa/Libreville",
    "Africa/Lome",
    "Africa/Luanda",
    "Africa/Lubumbashi",
    "Africa/Lusaka",
    "Africa/Malabo",
    "Africa/Maputo",
    "Africa/Maseru",
    "Africa/Mbabane",
    "Africa/Mogadishu",
    "Africa/Monrovia",
    "Africa/Nairobi",
    "Africa/Ndjamena",
    "Africa/Niamey",
    "Africa/Nouakchott",
    "Africa/Ouagadougou",
    "Africa/Porto-Novo",
    "Africa/Sao_Tome",
    "Africa/Timbuktu",
    "Africa/Tripoli",
    "Africa/Tunis",
    "Africa/Windhoek",
    "America/Adak",
    "America/Anchorage",
    "America/Anguilla",
    "America/Antigua",
    "America/Araguaina",
    "America/Argentina/Buenos_Aires",
    "America/Argentina/Catamarca",
    "America/Argentina/ComodRivadavia",
    "America/Argentina/Cordoba",
    "America/Argentina/Jujuy",
    "America/Argentina/La_Rioja",
    "America/Argentina/Mendoza",
    "America/Argentina/Rio_Gallegos",
    "America/Argentina/Salta",
    "America/Argentina/San_Juan",
    "America/Argentina/San_Luis",
    "America/Argentina/Tucuman",
    "America/Argentina/Ushuaia",
    "America/Aruba",
    "America/Asuncion",
    "America/Atikokan",
    "America/Atka",
    "America/Bahia",
    "America/Bahia_Banderas",
    "America/Barbados",
    "America/Belem",
    "America/Belize",
    "America/Blanc-Sablon",
    "America/Boa_Vista",
    "America/Bogota",
    "America/Boise",
    "America/Buenos_Aires",
    "America/Cambridge_Bay",
    "America/Campo_Grande",
    "America/Cancun",
    "America/Caracas",
    "America/Catamarca",
    "America/Cayenne",
    "America/Cayman",
    "America/Chicago",
    "America/Chihuahua",
    "America/Coral_Harbour",
    "America/Cordoba",
    "America/Costa_Rica",
    "America/Creston",
    "America/Cuiaba",
    "America/Curacao",
    "America/Danmarkshavn",
    "America/Dawson",
    "America/Dawson_Creek",
    "America/Denver",
    "America/Detroit",
    "America/Dominica",
    "America/Edmonton",
    "America/Eirunepe",
    "America/El_Salvador",
    "America/Ensenada",
    "America/Fort_Nelson",
    "America/Fort_Wayne",
    "America/Fortaleza",
    "America/Glace_Bay",
    "America/Godthab",
    "America/Goose_Bay",
    "America/Grand_Turk",
    "America/Grenada",
    "America/Guadeloupe",
    "America/Guatemala",
    "America/Guayaquil",
    "America/Guyana",
    "America/Halifax",
    "America/Havana",
    "America/Hermosillo",
    "America/Indiana/Indianapolis",
    "America/Indiana/Knox",
    "America/Indiana/Marengo",
    "America/Indiana/Petersburg",
    "America/Indiana/Tell_City",
    "America/Indiana/Vevay",
    "America/Indiana/Vincennes",
    "America/Indiana/Winamac",
    "America/Indianapolis",
    "America/Inuvik",
    "America/Iqaluit",
    "America/Jamaica",
    "America/Jujuy",
    "America/Juneau",
    "America/Kentucky/Louisville",
    "America/Kentucky/Monticello",
    "America/Knox_IN",
    "America/Kralendijk",
    "America/La_Paz",
    "America/Lima",
    "America/Los_Angeles",
    "America/Louisville",
    "America/Lower_Princes",
    "America/Maceio",
    "America/Managua",
    "America/Manaus",
    "America/Marigot",
    "America/Martinique",
    "America/Matamoros",
    "America/Mazatlan",
    "America/Mendoza",
    "America/Menominee",
    "America/Merida",
    "America/Metlakatla",
    "America/Mexico_City",
    "America/Miquelon",
    "America/Moncton",
    "America/Monterrey",
    "America/Montevideo",
    "America/Montreal",
    "America/Montserrat",
    "America/Nassau",
    "America/New_York",
    "America/Nipigon",
    "America/Nome",
    "America/Noronha",
    "America/North_Dakota/Beulah",
    "America/North_Dakota/Center",
    "America/North_Dakota/New_Salem",
    "America/Ojinaga",
    "America/Panama",
    "America/Pangnirtung",
    "America/Paramaribo",
    "America/Phoenix",
    "America/Port-au-Prince",
    "America/Port_of_Spain",
    "America/Porto_Acre",
    "America/Porto_Velho",
    "America/Puerto_Rico",
    "America/Punta_Arenas",
    "America/Rainy_River",
    "America/Rankin_Inlet",
    "America/Recife",
    "America/Regina",
    "America/Resolute",
    "America/Rio_Branco",
    "America/Rosario",
    "America/Santa_Isabel",
    "America/Santarem",
    "America/Santiago",
    "America/Santo_Domingo",
    "America/Sao_Paulo",
    "America/Scoresbysund",
    "America/Shiprock",
    "America/Sitka",
    "America/St_Barthelemy",
    "America/St_Johns",
    "America/St_Kitts",
    "America/St_Lucia",
    "America/St_Thomas",
    "America/St_Vincent",
    "America/Swift_Current",
    "America/Tegucigalpa",
    "America/Thule",
    "America/Thunder_Bay",
    "America/Tijuana",
    "America/Toronto",
    "America/Tortola",
    "America/Vancouver",
    "America/Virgin",
    "America/Whitehorse",
    "America/Winnipeg",
    "America/Yakutat",
    "America/Yellowknife",
    "Antarctica/Casey",
    "Antarctica/Davis",
    "Antarctica/DumontDUrville",
    "Antarctica/Macquarie",
    "Antarctica/Mawson",
    "Antarctica/McMurdo",
    "Antarctica/Palmer",
    "Antarctica/Rothera",
    "Antarctica/South_Pole",
    "Antarctica/Syowa",
    "Antarctica/Troll",
    "Antarctica/Vostok",
    "Arctic/Longyearbyen",
    "Asia/Aden",
    "Asia/Almaty",
    "Asia/Amman",
    "Asia/Anadyr",
    "Asia/Aqtau",
    "Asia/Aqtobe",
    "Asia/Ashgabat",
    "Asia/Ashkhabad",
    "Asia/Atyrau",
    "Asia/Baghdad",
    "Asia/Bahrain",
    "Asia/Baku",
    "Asia/Bangkok",
    "Asia/Barnaul",
    "Asia/Beirut",
    "Asia/Bishkek",
    "Asia/Brunei",
    "Asia/Calcutta",
    "Asia/Chita",
    "Asia/Choibalsan",
    "Asia/Chongqing",
    "Asia/Chungking",
    "Asia/Colombo",
    "Asia/Dacca",
    "Asia/Damascus",
    "Asia/Dhaka",
    "Asia/Dili",
    "Asia/Dubai",
    "Asia/Dushanbe",
    "Asia/Famagusta",
    "Asia/Gaza",
    "Asia/Harbin",
    "Asia/Hebron",
    "Asia/Ho_Chi_Minh",
    "Asia/Hong_Kong",
    "Asia/Hovd",
    "Asia/Irkutsk",
    "Asia/Istanbul",
    "Asia/Jakarta",
    "Asia/Jayapura",
    "Asia/Jerusalem",
    "Asia/Kabul",
    "Asia/Kamchatka",
    "Asia/Karachi",
    "Asia/Kashgar",
    "Asia/Kathmandu",
    "Asia/Katmandu",
    "Asia/Khandyga",
    "Asia/Kolkata",
    "Asia/Krasnoyarsk",
    "Asia/Kuala_Lumpur",
    "Asia/Kuching",
    "Asia/Kuwait",
    "Asia/Macao",
    "Asia/Macau",
    "Asia/Magadan",
    "Asia/Makassar",
    "Asia/Manila",
    "Asia/Muscat",
    "Asia/Nicosia",
    "Asia/Novokuznetsk",
    "Asia/Novosibirsk",
    "Asia/Omsk",
    "Asia/Oral",
    "Asia/Phnom_Penh",
    "Asia/Pontianak",
    "Asia/Pyongyang",
    "Asia/Qatar",
    "Asia/Qyzylorda",
    "Asia/Rangoon",
    "Asia/Riyadh",
    "Asia/Saigon",
    "Asia/Sakhalin",
    "Asia/Samarkand",
    "Asia/Seoul",
    "Asia/Shanghai",
    "Asia/Singapore",
    "Asia/Srednekolymsk",
    "Asia/Taipei",
    "Asia/Tashkent",
    "Asia/Tbilisi",
    "Asia/Tehran",
    "Asia/Tel_Aviv",
    "Asia/Thimbu",
    "Asia/Thimphu",
    "Asia/Tokyo",
    "Asia/Tomsk",
    "Asia/Ujung_Pandang",
    "Asia/Ulaanbaatar",
    "Asia/Ulan_Bator",
    "Asia/Urumqi",
    "Asia/Ust-Nera",
    "Asia/Vientiane",
    "Asia/Vladivostok",
    "Asia/Yakutsk",
    "Asia/Yangon",
    "Asia/Yekaterinburg",
    "Asia/Yerevan",
    "Atlantic/Azores",
    "Atlantic/Bermuda",
    "Atlantic/Canary",
    "Atlantic/Cape_Verde",
    "Atlantic/Faeroe",
    "Atlantic/Faroe",
    "Atlantic/Jan_Mayen",
    "Atlantic/Madeira",
    "Atlantic/Reykjavik",
    "Atlantic/South_Georgia",
    "Atlantic/St_Helena",
    "Atlantic/Stanley",
    "Australia/ACT",
    "Australia/Adelaide",
    "Australia/Brisbane",
    "Australia/Broken_Hill",
    "Australia/Canberra",
    "Australia/Currie",
    "Australia/Darwin",
    "Australia/Eucla",
    "Australia/Hobart",
    "Australia/LHI",
    "Australia/Lindeman",
    "Australia/Lord_Howe",
    "Australia/Melbourne",
    "Australia/NSW",
    "Australia/North",
    "Australia/Perth",
    "Australia/Queensland",
    "Australia/South",
    "Australia/Sydney",
    "Australia/Tasmania",
    "Australia/Victoria",
    "Australia/West",
    "Australia/Yancowinna",
    "BET", // *
    "BST", // *
    "Brazil/Acre",
    "Brazil/DeNoronha",
    "Brazil/East",
    "Brazil/West",
    "CAT", // *
    "CET",
    "CNT", // *
    "CST", // *
    "CST6CDT",
    "CTT", // *
    "Canada/Atlantic",
    "Canada/Central",
    "Canada/East-Saskatchewan", // *
    "Canada/Eastern",
    "Canada/Mountain",
    "Canada/Newfoundland",
    "Canada/Pacific",
    "Canada/Saskatchewan",
    "Canada/Yukon",
    "Chile/Continental",
    "Chile/EasterIsland",
    "Cuba",
    "EAT", // *
    "ECT", // *
    "EET",
    "EST", // *
    "EST5EDT",
    "Egypt",
    "Eire",
    "Etc/GMT",
    "Etc/GMT+0",
    "Etc/GMT+1",
    "Etc/GMT+10",
    "Etc/GMT+11",
    "Etc/GMT+12",
    "Etc/GMT+2",
    "Etc/GMT+3",
    "Etc/GMT+4",
    "Etc/GMT+5",
    "Etc/GMT+6",
    "Etc/GMT+7",
    "Etc/GMT+8",
    "Etc/GMT+9",
    "Etc/GMT-0",
    "Etc/GMT-1",
    "Etc/GMT-10",
    "Etc/GMT-11",
    "Etc/GMT-12",
    "Etc/GMT-13",
    "Etc/GMT-14",
    "Etc/GMT-2",
    "Etc/GMT-3",
    "Etc/GMT-4",
    "Etc/GMT-5",
    "Etc/GMT-6",
    "Etc/GMT-7",
    "Etc/GMT-8",
    "Etc/GMT-9",
    "Etc/GMT0",
    "Etc/Greenwich",
    "Etc/UCT",
    "Etc/UTC",
    "Etc/Universal",
    "Etc/Zulu",
    "Europe/Amsterdam",
    "Europe/Andorra",
    "Europe/Astrakhan",
    "Europe/Athens",
    "Europe/Belfast",
    "Europe/Belgrade",
    "Europe/Berlin",
    "Europe/Bratislava",
    "Europe/Brussels",
    "Europe/Bucharest",
    "Europe/Budapest",
    "Europe/Busingen",
    "Europe/Chisinau",
    "Europe/Copenhagen",
    "Europe/Dublin",
    "Europe/Gibraltar",
    "Europe/Guernsey",
    "Europe/Helsinki",
    "Europe/Isle_of_Man",
    "Europe/Istanbul",
    "Europe/Jersey",
    "Europe/Kaliningrad",
    "Europe/Kiev",
    "Europe/Kirov",
    "Europe/Lisbon",
    "Europe/Ljubljana",
    "Europe/London",
    "Europe/Luxembourg",
    "Europe/Madrid",
    "Europe/Malta",
    "Europe/Mariehamn",
    "Europe/Minsk",
    "Europe/Monaco",
    "Europe/Moscow",
    "Europe/Nicosia",
    "Europe/Oslo",
    "Europe/Paris",
    "Europe/Podgorica",
    "Europe/Prague",
    "Europe/Riga",
    "Europe/Rome",
    "Europe/Samara",
    "Europe/San_Marino",
    "Europe/Sarajevo",
    "Europe/Saratov",
    "Europe/Simferopol",
    "Europe/Skopje",
    "Europe/Sofia",
    "Europe/Stockholm",
    "Europe/Tallinn",
    "Europe/Tirane",
    "Europe/Tiraspol",
    "Europe/Ulyanovsk",
    "Europe/Uzhgorod",
    "Europe/Vaduz",
    "Europe/Vatican",
    "Europe/Vienna",
    "Europe/Vilnius",
    "Europe/Volgograd",
    "Europe/Warsaw",
    "Europe/Zagreb",
    "Europe/Zaporozhye",
    "Europe/Zurich",
    "Factory", // *
    "GB",
    "GB-Eire",
    "GMT+0", // *
    "GMT-0", // *
    "GMT0",
    "Greenwich",
    "HST", // *
    "Hongkong",
    "IET", // *
    "IST", // *
    "Iceland",
    "Indian/Antananarivo",
    "Indian/Chagos",
    "Indian/Christmas",
    "Indian/Cocos",
    "Indian/Comoro",
    "Indian/Kerguelen",
    "Indian/Mahe",
    "Indian/Maldives",
    "Indian/Mauritius",
    "Indian/Mayotte",
    "Indian/Reunion",
    "Iran",
    "Israel",
    "JST", // *
    "Jamaica",
    "Japan",
    "Kwajalein",
    "Libya",
    "MET",
    "MIT", // *
    "MST", // *
    "MST7MDT",
    "Mexico/BajaNorte",
    "Mexico/BajaSur",
    "Mexico/General",
    "NET", // *
    "NST", // *
    "NZ",
    "NZ-CHAT",
    "Navajo",
    "PLT", // *
    "PNT", // *
    "PRC",
    "PRT", // *
    "PST", // *
    "PST8PDT",
    "Pacific/Apia",
    "Pacific/Auckland",
    "Pacific/Bougainville",
    "Pacific/Chatham",
    "Pacific/Chuuk",
    "Pacific/Easter",
    "Pacific/Efate",
    "Pacific/Enderbury",
    "Pacific/Fakaofo",
    "Pacific/Fiji",
    "Pacific/Funafuti",
    "Pacific/Galapagos",
    "Pacific/Gambier",
    "Pacific/Guadalcanal",
    "Pacific/Guam",
    "Pacific/Honolulu",
    "Pacific/Johnston",
    "Pacific/Kiritimati",
    "Pacific/Kosrae",
    "Pacific/Kwajalein",
    "Pacific/Majuro",
    "Pacific/Marquesas",
    "Pacific/Midway",
    "Pacific/Nauru",
    "Pacific/Niue",
    "Pacific/Norfolk",
    "Pacific/Noumea",
    "Pacific/Pago_Pago",
    "Pacific/Palau",
    "Pacific/Pitcairn",
    "Pacific/Pohnpei",
    "Pacific/Ponape",
    "Pacific/Port_Moresby",
    "Pacific/Rarotonga",
    "Pacific/Saipan",
    "Pacific/Samoa",
    "Pacific/Tahiti",
    "Pacific/Tarawa",
    "Pacific/Tongatapu",
    "Pacific/Truk",
    "Pacific/Wake",
    "Pacific/Wallis",
    "Pacific/Yap",
    "Poland",
    "Portugal",
    "ROC", // *
    "ROK",
    "SST", // *
    "Singapore",
    "SystemV/AST4",
    "SystemV/AST4ADT",
    "SystemV/CST6",
    "SystemV/CST6CDT",
    "SystemV/EST5",
    "SystemV/EST5EDT",
    "SystemV/HST10",
    "SystemV/MST7",
    "SystemV/MST7MDT",
    "SystemV/PST8",
    "SystemV/PST8PDT",
    "SystemV/YST9",
    "SystemV/YST9YDT",
    "Turkey",
    "UCT",
    "US/Alaska",
    "US/Aleutian",
    "US/Arizona",
    "US/Central",
    "US/East-Indiana",
    "US/Eastern",
    "US/Hawaii",
    "US/Indiana-Starke",
    "US/Michigan",
    "US/Mountain",
    "US/Pacific",
    "US/Pacific-New", // *
    "US/Samoa",
    "UTC",
    "Universal",
    "VST", // *
    "W-SU",
    "WET",
    "Zulu",
    "America/Nuuk",
    "Asia/Qostanay",
    "Pacific/Kanton",
    "Europe/Kyiv",
    "America/Ciudad_Juarez"
)
// *  do not use

val fb_tzid_gmt = TimeZoneId(65535)
val fb_tzid_act = TimeZoneId(65534)
val fb_tzid_aet = TimeZoneId(65533)
val fb_tzid_agt = TimeZoneId(65532)
val fb_tzid_art = TimeZoneId(65531)
val fb_tzid_ast = TimeZoneId(65530)
val fb_tzid_africa_abidjan = TimeZoneId(65529)
val fb_tzid_africa_accra = TimeZoneId(65528)
val fb_tzid_africa_addis_ababa = TimeZoneId(65527)
val fb_tzid_africa_algiers = TimeZoneId(65526)
val fb_tzid_africa_asmara = TimeZoneId(65525)
val fb_tzid_africa_asmera = TimeZoneId(65524)
val fb_tzid_africa_bamako = TimeZoneId(65523)
val fb_tzid_africa_bangui = TimeZoneId(65522)
val fb_tzid_africa_banjul = TimeZoneId(65521)
val fb_tzid_africa_bissau = TimeZoneId(65520)
val fb_tzid_africa_blantyre = TimeZoneId(65519)
val fb_tzid_africa_brazzaville = TimeZoneId(65518)
val fb_tzid_africa_bujumbura = TimeZoneId(65517)
val fb_tzid_africa_cairo = TimeZoneId(65516)
val fb_tzid_africa_casablanca = TimeZoneId(65515)
val fb_tzid_africa_ceuta = TimeZoneId(65514)
val fb_tzid_africa_conakry = TimeZoneId(65513)
val fb_tzid_africa_dakar = TimeZoneId(65512)
val fb_tzid_africa_dar_es_salaam = TimeZoneId(65511)
val fb_tzid_africa_djibouti = TimeZoneId(65510)
val fb_tzid_africa_douala = TimeZoneId(65509)
val fb_tzid_africa_el_aaiun = TimeZoneId(65508)
val fb_tzid_africa_freetown = TimeZoneId(65507)
val fb_tzid_africa_gaborone = TimeZoneId(65506)
val fb_tzid_africa_harare = TimeZoneId(65505)
val fb_tzid_africa_johannesburg = TimeZoneId(65504)
val fb_tzid_africa_juba = TimeZoneId(65503)
val fb_tzid_africa_kampala = TimeZoneId(65502)
val fb_tzid_africa_khartoum = TimeZoneId(65501)
val fb_tzid_africa_kigali = TimeZoneId(65500)
val fb_tzid_africa_kinshasa = TimeZoneId(65499)
val fb_tzid_africa_lagos = TimeZoneId(65498)
val fb_tzid_africa_libreville = TimeZoneId(65497)
val fb_tzid_africa_lome = TimeZoneId(65496)
val fb_tzid_africa_luanda = TimeZoneId(65495)
val fb_tzid_africa_lubumbashi = TimeZoneId(65494)
val fb_tzid_africa_lusaka = TimeZoneId(65493)
val fb_tzid_africa_malabo = TimeZoneId(65492)
val fb_tzid_africa_maputo = TimeZoneId(65491)
val fb_tzid_africa_maseru = TimeZoneId(65490)
val fb_tzid_africa_mbabane = TimeZoneId(65489)
val fb_tzid_africa_mogadishu = TimeZoneId(65488)
val fb_tzid_africa_monrovia = TimeZoneId(65487)
val fb_tzid_africa_nairobi = TimeZoneId(65486)
val fb_tzid_africa_ndjamena = TimeZoneId(65485)
val fb_tzid_africa_niamey = TimeZoneId(65484)
val fb_tzid_africa_nouakchott = TimeZoneId(65483)
val fb_tzid_africa_ouagadougou = TimeZoneId(65482)
val fb_tzid_africa_porto_novo = TimeZoneId(65481)
val fb_tzid_africa_sao_tome = TimeZoneId(65480)
val fb_tzid_africa_timbuktu = TimeZoneId(65479)
val fb_tzid_africa_tripoli = TimeZoneId(65478)
val fb_tzid_africa_tunis = TimeZoneId(65477)
val fb_tzid_africa_windhoek = TimeZoneId(65476)
val fb_tzid_america_adak = TimeZoneId(65475)
val fb_tzid_america_anchorage = TimeZoneId(65474)
val fb_tzid_america_anguilla = TimeZoneId(65473)
val fb_tzid_america_antigua = TimeZoneId(65472)
val fb_tzid_america_araguaina = TimeZoneId(65471)
val fb_tzid_america_argentina_buenos_aires = TimeZoneId(65470)
val fb_tzid_america_argentina_catamarca = TimeZoneId(65469)
val fb_tzid_america_argentina_comodrivadavia = TimeZoneId(65468)
val fb_tzid_america_argentina_cordoba = TimeZoneId(65467)
val fb_tzid_america_argentina_jujuy = TimeZoneId(65466)
val fb_tzid_america_argentina_la_rioja = TimeZoneId(65465)
val fb_tzid_america_argentina_mendoza = TimeZoneId(65464)
val fb_tzid_america_argentina_rio_gallegos = TimeZoneId(65463)
val fb_tzid_america_argentina_salta = TimeZoneId(65462)
val fb_tzid_america_argentina_san_juan = TimeZoneId(65461)
val fb_tzid_america_argentina_san_luis = TimeZoneId(65460)
val fb_tzid_america_argentina_tucuman = TimeZoneId(65459)
val fb_tzid_america_argentina_ushuaia = TimeZoneId(65458)
val fb_tzid_america_aruba = TimeZoneId(65457)
val fb_tzid_america_asuncion = TimeZoneId(65456)
val fb_tzid_america_atikokan = TimeZoneId(65455)
val fb_tzid_america_atka = TimeZoneId(65454)
val fb_tzid_america_bahia = TimeZoneId(65453)
val fb_tzid_america_bahia_banderas = TimeZoneId(65452)
val fb_tzid_america_barbados = TimeZoneId(65451)
val fb_tzid_america_belem = TimeZoneId(65450)
val fb_tzid_america_belize = TimeZoneId(65449)
val fb_tzid_america_blanc_sablon = TimeZoneId(65448)
val fb_tzid_america_boa_vista = TimeZoneId(65447)
val fb_tzid_america_bogota = TimeZoneId(65446)
val fb_tzid_america_boise = TimeZoneId(65445)
val fb_tzid_america_buenos_aires = TimeZoneId(65444)
val fb_tzid_america_cambridge_bay = TimeZoneId(65443)
val fb_tzid_america_campo_grande = TimeZoneId(65442)
val fb_tzid_america_cancun = TimeZoneId(65441)
val fb_tzid_america_caracas = TimeZoneId(65440)
val fb_tzid_america_catamarca = TimeZoneId(65439)
val fb_tzid_america_cayenne = TimeZoneId(65438)
val fb_tzid_america_cayman = TimeZoneId(65437)
val fb_tzid_america_chicago = TimeZoneId(65436)
val fb_tzid_america_chihuahua = TimeZoneId(65435)
val fb_tzid_america_coral_harbour = TimeZoneId(65434)
val fb_tzid_america_cordoba = TimeZoneId(65433)
val fb_tzid_america_costa_rica = TimeZoneId(65432)
val fb_tzid_america_creston = TimeZoneId(65431)
val fb_tzid_america_cuiaba = TimeZoneId(65430)
val fb_tzid_america_curacao = TimeZoneId(65429)
val fb_tzid_america_danmarkshavn = TimeZoneId(65428)
val fb_tzid_america_dawson = TimeZoneId(65427)
val fb_tzid_america_dawson_creek = TimeZoneId(65426)
val fb_tzid_america_denver = TimeZoneId(65425)
val fb_tzid_america_detroit = TimeZoneId(65424)
val fb_tzid_america_dominica = TimeZoneId(65423)
val fb_tzid_america_edmonton = TimeZoneId(65422)
val fb_tzid_america_eirunepe = TimeZoneId(65421)
val fb_tzid_america_el_salvador = TimeZoneId(65420)
val fb_tzid_america_ensenada = TimeZoneId(65419)
val fb_tzid_america_fort_nelson = TimeZoneId(65418)
val fb_tzid_america_fort_wayne = TimeZoneId(65417)
val fb_tzid_america_fortaleza = TimeZoneId(65416)
val fb_tzid_america_glace_bay = TimeZoneId(65415)
val fb_tzid_america_godthab = TimeZoneId(65414)
val fb_tzid_america_goose_bay = TimeZoneId(65413)
val fb_tzid_america_grand_turk = TimeZoneId(65412)
val fb_tzid_america_grenada = TimeZoneId(65411)
val fb_tzid_america_guadeloupe = TimeZoneId(65410)
val fb_tzid_america_guatemala = TimeZoneId(65409)
val fb_tzid_america_guayaquil = TimeZoneId(65408)
val fb_tzid_america_guyana = TimeZoneId(65407)
val fb_tzid_america_halifax = TimeZoneId(65406)
val fb_tzid_america_havana = TimeZoneId(65405)
val fb_tzid_america_hermosillo = TimeZoneId(65404)
val fb_tzid_america_indiana_indianapolis = TimeZoneId(65403)
val fb_tzid_america_indiana_knox = TimeZoneId(65402)
val fb_tzid_america_indiana_marengo = TimeZoneId(65401)
val fb_tzid_america_indiana_petersburg = TimeZoneId(65400)
val fb_tzid_america_indiana_tell_city = TimeZoneId(65399)
val fb_tzid_america_indiana_vevay = TimeZoneId(65398)
val fb_tzid_america_indiana_vincennes = TimeZoneId(65397)
val fb_tzid_america_indiana_winamac = TimeZoneId(65396)
val fb_tzid_america_indianapolis = TimeZoneId(65395)
val fb_tzid_america_inuvik = TimeZoneId(65394)
val fb_tzid_america_iqaluit = TimeZoneId(65393)
val fb_tzid_america_jamaica = TimeZoneId(65392)
val fb_tzid_america_jujuy = TimeZoneId(65391)
val fb_tzid_america_juneau = TimeZoneId(65390)
val fb_tzid_america_kentucky_louisville = TimeZoneId(65389)
val fb_tzid_america_kentucky_monticello = TimeZoneId(65388)
val fb_tzid_america_knox_in = TimeZoneId(65387)
val fb_tzid_america_kralendijk = TimeZoneId(65386)
val fb_tzid_america_la_paz = TimeZoneId(65385)
val fb_tzid_america_lima = TimeZoneId(65384)
val fb_tzid_america_los_angeles = TimeZoneId(65383)
val fb_tzid_america_louisville = TimeZoneId(65382)
val fb_tzid_america_lower_princes = TimeZoneId(65381)
val fb_tzid_america_maceio = TimeZoneId(65380)
val fb_tzid_america_managua = TimeZoneId(65379)
val fb_tzid_america_manaus = TimeZoneId(65378)
val fb_tzid_america_marigot = TimeZoneId(65377)
val fb_tzid_america_martinique = TimeZoneId(65376)
val fb_tzid_america_matamoros = TimeZoneId(65375)
val fb_tzid_america_mazatlan = TimeZoneId(65374)
val fb_tzid_america_mendoza = TimeZoneId(65373)
val fb_tzid_america_menominee = TimeZoneId(65372)
val fb_tzid_america_merida = TimeZoneId(65371)
val fb_tzid_america_metlakatla = TimeZoneId(65370)
val fb_tzid_america_mexico_city = TimeZoneId(65369)
val fb_tzid_america_miquelon = TimeZoneId(65368)
val fb_tzid_america_moncton = TimeZoneId(65367)
val fb_tzid_america_monterrey = TimeZoneId(65366)
val fb_tzid_america_montevideo = TimeZoneId(65365)
val fb_tzid_america_montreal = TimeZoneId(65364)
val fb_tzid_america_montserrat = TimeZoneId(65363)
val fb_tzid_america_nassau = TimeZoneId(65362)
val fb_tzid_america_new_york = TimeZoneId(65361)
val fb_tzid_america_nipigon = TimeZoneId(65360)
val fb_tzid_america_nome = TimeZoneId(65359)
val fb_tzid_america_noronha = TimeZoneId(65358)
val fb_tzid_america_north_dakota_beulah = TimeZoneId(65357)
val fb_tzid_america_north_dakota_center = TimeZoneId(65356)
val fb_tzid_america_north_dakota_new_salem = TimeZoneId(65355)
val fb_tzid_america_ojinaga = TimeZoneId(65354)
val fb_tzid_america_panama = TimeZoneId(65353)
val fb_tzid_america_pangnirtung = TimeZoneId(65352)
val fb_tzid_america_paramaribo = TimeZoneId(65351)
val fb_tzid_america_phoenix = TimeZoneId(65350)
val fb_tzid_america_port_au_prince = TimeZoneId(65349)
val fb_tzid_america_port_of_spain = TimeZoneId(65348)
val fb_tzid_america_porto_acre = TimeZoneId(65347)
val fb_tzid_america_porto_velho = TimeZoneId(65346)
val fb_tzid_america_puerto_rico = TimeZoneId(65345)
val fb_tzid_america_punta_arenas = TimeZoneId(65344)
val fb_tzid_america_rainy_river = TimeZoneId(65343)
val fb_tzid_america_rankin_inlet = TimeZoneId(65342)
val fb_tzid_america_recife = TimeZoneId(65341)
val fb_tzid_america_regina = TimeZoneId(65340)
val fb_tzid_america_resolute = TimeZoneId(65339)
val fb_tzid_america_rio_branco = TimeZoneId(65338)
val fb_tzid_america_rosario = TimeZoneId(65337)
val fb_tzid_america_santa_isabel = TimeZoneId(65336)
val fb_tzid_america_santarem = TimeZoneId(65335)
val fb_tzid_america_santiago = TimeZoneId(65334)
val fb_tzid_america_santo_domingo = TimeZoneId(65333)
val fb_tzid_america_sao_paulo = TimeZoneId(65332)
val fb_tzid_america_scoresbysund = TimeZoneId(65331)
val fb_tzid_america_shiprock = TimeZoneId(65330)
val fb_tzid_america_sitka = TimeZoneId(65329)
val fb_tzid_america_st_barthelemy = TimeZoneId(65328)
val fb_tzid_america_st_johns = TimeZoneId(65327)
val fb_tzid_america_st_kitts = TimeZoneId(65326)
val fb_tzid_america_st_lucia = TimeZoneId(65325)
val fb_tzid_america_st_thomas = TimeZoneId(65324)
val fb_tzid_america_st_vincent = TimeZoneId(65323)
val fb_tzid_america_swift_current = TimeZoneId(65322)
val fb_tzid_america_tegucigalpa = TimeZoneId(65321)
val fb_tzid_america_thule = TimeZoneId(65320)
val fb_tzid_america_thunder_bay = TimeZoneId(65319)
val fb_tzid_america_tijuana = TimeZoneId(65318)
val fb_tzid_america_toronto = TimeZoneId(65317)
val fb_tzid_america_tortola = TimeZoneId(65316)
val fb_tzid_america_vancouver = TimeZoneId(65315)
val fb_tzid_america_virgin = TimeZoneId(65314)
val fb_tzid_america_whitehorse = TimeZoneId(65313)
val fb_tzid_america_winnipeg = TimeZoneId(65312)
val fb_tzid_america_yakutat = TimeZoneId(65311)
val fb_tzid_america_yellowknife = TimeZoneId(65310)
val fb_tzid_antarctica_casey = TimeZoneId(65309)
val fb_tzid_antarctica_davis = TimeZoneId(65308)
val fb_tzid_antarctica_dumontdurville = TimeZoneId(65307)
val fb_tzid_antarctica_macquarie = TimeZoneId(65306)
val fb_tzid_antarctica_mawson = TimeZoneId(65305)
val fb_tzid_antarctica_mcmurdo = TimeZoneId(65304)
val fb_tzid_antarctica_palmer = TimeZoneId(65303)
val fb_tzid_antarctica_rothera = TimeZoneId(65302)
val fb_tzid_antarctica_south_pole = TimeZoneId(65301)
val fb_tzid_antarctica_syowa = TimeZoneId(65300)
val fb_tzid_antarctica_troll = TimeZoneId(65299)
val fb_tzid_antarctica_vostok = TimeZoneId(65298)
val fb_tzid_arctic_longyearbyen = TimeZoneId(65297)
val fb_tzid_asia_aden = TimeZoneId(65296)
val fb_tzid_asia_almaty = TimeZoneId(65295)
val fb_tzid_asia_amman = TimeZoneId(65294)
val fb_tzid_asia_anadyr = TimeZoneId(65293)
val fb_tzid_asia_aqtau = TimeZoneId(65292)
val fb_tzid_asia_aqtobe = TimeZoneId(65291)
val fb_tzid_asia_ashgabat = TimeZoneId(65290)
val fb_tzid_asia_ashkhabad = TimeZoneId(65289)
val fb_tzid_asia_atyrau = TimeZoneId(65288)
val fb_tzid_asia_baghdad = TimeZoneId(65287)
val fb_tzid_asia_bahrain = TimeZoneId(65286)
val fb_tzid_asia_baku = TimeZoneId(65285)
val fb_tzid_asia_bangkok = TimeZoneId(65284)
val fb_tzid_asia_barnaul = TimeZoneId(65283)
val fb_tzid_asia_beirut = TimeZoneId(65282)
val fb_tzid_asia_bishkek = TimeZoneId(65281)
val fb_tzid_asia_brunei = TimeZoneId(65280)
val fb_tzid_asia_calcutta = TimeZoneId(65279)
val fb_tzid_asia_chita = TimeZoneId(65278)
val fb_tzid_asia_choibalsan = TimeZoneId(65277)
val fb_tzid_asia_chongqing = TimeZoneId(65276)
val fb_tzid_asia_chungking = TimeZoneId(65275)
val fb_tzid_asia_colombo = TimeZoneId(65274)
val fb_tzid_asia_dacca = TimeZoneId(65273)
val fb_tzid_asia_damascus = TimeZoneId(65272)
val fb_tzid_asia_dhaka = TimeZoneId(65271)
val fb_tzid_asia_dili = TimeZoneId(65270)
val fb_tzid_asia_dubai = TimeZoneId(65269)
val fb_tzid_asia_dushanbe = TimeZoneId(65268)
val fb_tzid_asia_famagusta = TimeZoneId(65267)
val fb_tzid_asia_gaza = TimeZoneId(65266)
val fb_tzid_asia_harbin = TimeZoneId(65265)
val fb_tzid_asia_hebron = TimeZoneId(65264)
val fb_tzid_asia_ho_chi_minh = TimeZoneId(65263)
val fb_tzid_asia_hong_kong = TimeZoneId(65262)
val fb_tzid_asia_hovd = TimeZoneId(65261)
val fb_tzid_asia_irkutsk = TimeZoneId(65260)
val fb_tzid_asia_istanbul = TimeZoneId(65259)
val fb_tzid_asia_jakarta = TimeZoneId(65258)
val fb_tzid_asia_jayapura = TimeZoneId(65257)
val fb_tzid_asia_jerusalem = TimeZoneId(65256)
val fb_tzid_asia_kabul = TimeZoneId(65255)
val fb_tzid_asia_kamchatka = TimeZoneId(65254)
val fb_tzid_asia_karachi = TimeZoneId(65253)
val fb_tzid_asia_kashgar = TimeZoneId(65252)
val fb_tzid_asia_kathmandu = TimeZoneId(65251)
val fb_tzid_asia_katmandu = TimeZoneId(65250)
val fb_tzid_asia_khandyga = TimeZoneId(65249)
val fb_tzid_asia_kolkata = TimeZoneId(65248)
val fb_tzid_asia_krasnoyarsk = TimeZoneId(65247)
val fb_tzid_asia_kuala_lumpur = TimeZoneId(65246)
val fb_tzid_asia_kuching = TimeZoneId(65245)
val fb_tzid_asia_kuwait = TimeZoneId(65244)
val fb_tzid_asia_macao = TimeZoneId(65243)
val fb_tzid_asia_macau = TimeZoneId(65242)
val fb_tzid_asia_magadan = TimeZoneId(65241)
val fb_tzid_asia_makassar = TimeZoneId(65240)
val fb_tzid_asia_manila = TimeZoneId(65239)
val fb_tzid_asia_muscat = TimeZoneId(65238)
val fb_tzid_asia_nicosia = TimeZoneId(65237)
val fb_tzid_asia_novokuznetsk = TimeZoneId(65236)
val fb_tzid_asia_novosibirsk = TimeZoneId(65235)
val fb_tzid_asia_omsk = TimeZoneId(65234)
val fb_tzid_asia_oral = TimeZoneId(65233)
val fb_tzid_asia_phnom_penh = TimeZoneId(65232)
val fb_tzid_asia_pontianak = TimeZoneId(65231)
val fb_tzid_asia_pyongyang = TimeZoneId(65230)
val fb_tzid_asia_qatar = TimeZoneId(65229)
val fb_tzid_asia_qyzylorda = TimeZoneId(65228)
val fb_tzid_asia_rangoon = TimeZoneId(65227)
val fb_tzid_asia_riyadh = TimeZoneId(65226)
val fb_tzid_asia_saigon = TimeZoneId(65225)
val fb_tzid_asia_sakhalin = TimeZoneId(65224)
val fb_tzid_asia_samarkand = TimeZoneId(65223)
val fb_tzid_asia_seoul = TimeZoneId(65222)
val fb_tzid_asia_shanghai = TimeZoneId(65221)
val fb_tzid_asia_singapore = TimeZoneId(65220)
val fb_tzid_asia_srednekolymsk = TimeZoneId(65219)
val fb_tzid_asia_taipei = TimeZoneId(65218)
val fb_tzid_asia_tashkent = TimeZoneId(65217)
val fb_tzid_asia_tbilisi = TimeZoneId(65216)
val fb_tzid_asia_tehran = TimeZoneId(65215)
val fb_tzid_asia_tel_aviv = TimeZoneId(65214)
val fb_tzid_asia_thimbu = TimeZoneId(65213)
val fb_tzid_asia_thimphu = TimeZoneId(65212)
val fb_tzid_asia_tokyo = TimeZoneId(65211)
val fb_tzid_asia_tomsk = TimeZoneId(65210)
val fb_tzid_asia_ujung_pandang = TimeZoneId(65209)
val fb_tzid_asia_ulaanbaatar = TimeZoneId(65208)
val fb_tzid_asia_ulan_bator = TimeZoneId(65207)
val fb_tzid_asia_urumqi = TimeZoneId(65206)
val fb_tzid_asia_ust_nera = TimeZoneId(65205)
val fb_tzid_asia_vientiane = TimeZoneId(65204)
val fb_tzid_asia_vladivostok = TimeZoneId(65203)
val fb_tzid_asia_yakutsk = TimeZoneId(65202)
val fb_tzid_asia_yangon = TimeZoneId(65201)
val fb_tzid_asia_yekaterinburg = TimeZoneId(65200)
val fb_tzid_asia_yerevan = TimeZoneId(65199)
val fb_tzid_atlantic_azores = TimeZoneId(65198)
val fb_tzid_atlantic_bermuda = TimeZoneId(65197)
val fb_tzid_atlantic_canary = TimeZoneId(65196)
val fb_tzid_atlantic_cape_verde = TimeZoneId(65195)
val fb_tzid_atlantic_faeroe = TimeZoneId(65194)
val fb_tzid_atlantic_faroe = TimeZoneId(65193)
val fb_tzid_atlantic_jan_mayen = TimeZoneId(65192)
val fb_tzid_atlantic_madeira = TimeZoneId(65191)
val fb_tzid_atlantic_reykjavik = TimeZoneId(65190)
val fb_tzid_atlantic_south_georgia = TimeZoneId(65189)
val fb_tzid_atlantic_st_helena = TimeZoneId(65188)
val fb_tzid_atlantic_stanley = TimeZoneId(65187)
val fb_tzid_australia_act = TimeZoneId(65186)
val fb_tzid_australia_adelaide = TimeZoneId(65185)
val fb_tzid_australia_brisbane = TimeZoneId(65184)
val fb_tzid_australia_broken_hill = TimeZoneId(65183)
val fb_tzid_australia_canberra = TimeZoneId(65182)
val fb_tzid_australia_currie = TimeZoneId(65181)
val fb_tzid_australia_darwin = TimeZoneId(65180)
val fb_tzid_australia_eucla = TimeZoneId(65179)
val fb_tzid_australia_hobart = TimeZoneId(65178)
val fb_tzid_australia_lhi = TimeZoneId(65177)
val fb_tzid_australia_lindeman = TimeZoneId(65176)
val fb_tzid_australia_lord_howe = TimeZoneId(65175)
val fb_tzid_australia_melbourne = TimeZoneId(65174)
val fb_tzid_australia_nsw = TimeZoneId(65173)
val fb_tzid_australia_north = TimeZoneId(65172)
val fb_tzid_australia_perth = TimeZoneId(65171)
val fb_tzid_australia_queensland = TimeZoneId(65170)
val fb_tzid_australia_south = TimeZoneId(65169)
val fb_tzid_australia_sydney = TimeZoneId(65168)
val fb_tzid_australia_tasmania = TimeZoneId(65167)
val fb_tzid_australia_victoria = TimeZoneId(65166)
val fb_tzid_australia_west = TimeZoneId(65165)
val fb_tzid_australia_yancowinna = TimeZoneId(65164)
val fb_tzid_bet = TimeZoneId(65163)
val fb_tzid_bst = TimeZoneId(65162)
val fb_tzid_brazil_acre = TimeZoneId(65161)
val fb_tzid_brazil_denoronha = TimeZoneId(65160)
val fb_tzid_brazil_east = TimeZoneId(65159)
val fb_tzid_brazil_west = TimeZoneId(65158)
val fb_tzid_cat = TimeZoneId(65157)
val fb_tzid_cet = TimeZoneId(65156)
val fb_tzid_cnt = TimeZoneId(65155)
val fb_tzid_cst = TimeZoneId(65154)
val fb_tzid_cst6cdt = TimeZoneId(65153)
val fb_tzid_ctt = TimeZoneId(65152)
val fb_tzid_canada_atlantic = TimeZoneId(65151)
val fb_tzid_canada_central = TimeZoneId(65150)
val fb_tzid_canada_east_saskatchewan = TimeZoneId(65149)
val fb_tzid_canada_eastern = TimeZoneId(65148)
val fb_tzid_canada_mountain = TimeZoneId(65147)
val fb_tzid_canada_newfoundland = TimeZoneId(65146)
val fb_tzid_canada_pacific = TimeZoneId(65145)
val fb_tzid_canada_saskatchewan = TimeZoneId(65144)
val fb_tzid_canada_yukon = TimeZoneId(65143)
val fb_tzid_chile_continental = TimeZoneId(65142)
val fb_tzid_chile_easterisland = TimeZoneId(65141)
val fb_tzid_cuba = TimeZoneId(65140)
val fb_tzid_eat = TimeZoneId(65139)
val fb_tzid_ect = TimeZoneId(65138)
val fb_tzid_eet = TimeZoneId(65137)
val fb_tzid_est = TimeZoneId(65136)
val fb_tzid_est5edt = TimeZoneId(65135)
val fb_tzid_egypt = TimeZoneId(65134)
val fb_tzid_eire = TimeZoneId(65133)
val fb_tzid_etc_gmt = TimeZoneId(65132)
val fb_tzid_etc_gmt_plus_0 = TimeZoneId(65131)
val fb_tzid_etc_gmt_plus_1 = TimeZoneId(65130)
val fb_tzid_etc_gmt_plus_10 = TimeZoneId(65129)
val fb_tzid_etc_gmt_plus_11 = TimeZoneId(65128)
val fb_tzid_etc_gmt_plus_12 = TimeZoneId(65127)
val fb_tzid_etc_gmt_plus_2 = TimeZoneId(65126)
val fb_tzid_etc_gmt_plus_3 = TimeZoneId(65125)
val fb_tzid_etc_gmt_plus_4 = TimeZoneId(65124)
val fb_tzid_etc_gmt_plus_5 = TimeZoneId(65123)
val fb_tzid_etc_gmt_plus_6 = TimeZoneId(65122)
val fb_tzid_etc_gmt_plus_7 = TimeZoneId(65121)
val fb_tzid_etc_gmt_plus_8 = TimeZoneId(65120)
val fb_tzid_etc_gmt_plus_9 = TimeZoneId(65119)
val fb_tzid_etc_gmt_minus_0 = TimeZoneId(65118)
val fb_tzid_etc_gmt_minus_1 = TimeZoneId(65117)
val fb_tzid_etc_gmt_minus_10 = TimeZoneId(65116)
val fb_tzid_etc_gmt_minus_11 = TimeZoneId(65115)
val fb_tzid_etc_gmt_minus_12 = TimeZoneId(65114)
val fb_tzid_etc_gmt_minus_13 = TimeZoneId(65113)
val fb_tzid_etc_gmt_minus_14 = TimeZoneId(65112)
val fb_tzid_etc_gmt_minus_2 = TimeZoneId(65111)
val fb_tzid_etc_gmt_minus_3 = TimeZoneId(65110)
val fb_tzid_etc_gmt_minus_4 = TimeZoneId(65109)
val fb_tzid_etc_gmt_minus_5 = TimeZoneId(65108)
val fb_tzid_etc_gmt_minus_6 = TimeZoneId(65107)
val fb_tzid_etc_gmt_minus_7 = TimeZoneId(65106)
val fb_tzid_etc_gmt_minus_8 = TimeZoneId(65105)
val fb_tzid_etc_gmt_minus_9 = TimeZoneId(65104)
val fb_tzid_etc_gmt0 = TimeZoneId(65103)
val fb_tzid_etc_greenwich = TimeZoneId(65102)
val fb_tzid_etc_uct = TimeZoneId(65101)
val fb_tzid_etc_utc = TimeZoneId(65100)
val fb_tzid_etc_universal = TimeZoneId(65099)
val fb_tzid_etc_zulu = TimeZoneId(65098)
val fb_tzid_europe_amsterdam = TimeZoneId(65097)
val fb_tzid_europe_andorra = TimeZoneId(65096)
val fb_tzid_europe_astrakhan = TimeZoneId(65095)
val fb_tzid_europe_athens = TimeZoneId(65094)
val fb_tzid_europe_belfast = TimeZoneId(65093)
val fb_tzid_europe_belgrade = TimeZoneId(65092)
val fb_tzid_europe_berlin = TimeZoneId(65091)
val fb_tzid_europe_bratislava = TimeZoneId(65090)
val fb_tzid_europe_brussels = TimeZoneId(65089)
val fb_tzid_europe_bucharest = TimeZoneId(65088)
val fb_tzid_europe_budapest = TimeZoneId(65087)
val fb_tzid_europe_busingen = TimeZoneId(65086)
val fb_tzid_europe_chisinau = TimeZoneId(65085)
val fb_tzid_europe_copenhagen = TimeZoneId(65084)
val fb_tzid_europe_dublin = TimeZoneId(65083)
val fb_tzid_europe_gibraltar = TimeZoneId(65082)
val fb_tzid_europe_guernsey = TimeZoneId(65081)
val fb_tzid_europe_helsinki = TimeZoneId(65080)
val fb_tzid_europe_isle_of_man = TimeZoneId(65079)
val fb_tzid_europe_istanbul = TimeZoneId(65078)
val fb_tzid_europe_jersey = TimeZoneId(65077)
val fb_tzid_europe_kaliningrad = TimeZoneId(65076)
val fb_tzid_europe_kiev = TimeZoneId(65075)
val fb_tzid_europe_kirov = TimeZoneId(65074)
val fb_tzid_europe_lisbon = TimeZoneId(65073)
val fb_tzid_europe_ljubljana = TimeZoneId(65072)
val fb_tzid_europe_london = TimeZoneId(65071)
val fb_tzid_europe_luxembourg = TimeZoneId(65070)
val fb_tzid_europe_madrid = TimeZoneId(65069)
val fb_tzid_europe_malta = TimeZoneId(65068)
val fb_tzid_europe_mariehamn = TimeZoneId(65067)
val fb_tzid_europe_minsk = TimeZoneId(65066)
val fb_tzid_europe_monaco = TimeZoneId(65065)
val fb_tzid_europe_moscow = TimeZoneId(65064)
val fb_tzid_europe_nicosia = TimeZoneId(65063)
val fb_tzid_europe_oslo = TimeZoneId(65062)
val fb_tzid_europe_paris = TimeZoneId(65061)
val fb_tzid_europe_podgorica = TimeZoneId(65060)
val fb_tzid_europe_prague = TimeZoneId(65059)
val fb_tzid_europe_riga = TimeZoneId(65058)
val fb_tzid_europe_rome = TimeZoneId(65057)
val fb_tzid_europe_samara = TimeZoneId(65056)
val fb_tzid_europe_san_marino = TimeZoneId(65055)
val fb_tzid_europe_sarajevo = TimeZoneId(65054)
val fb_tzid_europe_saratov = TimeZoneId(65053)
val fb_tzid_europe_simferopol = TimeZoneId(65052)
val fb_tzid_europe_skopje = TimeZoneId(65051)
val fb_tzid_europe_sofia = TimeZoneId(65050)
val fb_tzid_europe_stockholm = TimeZoneId(65049)
val fb_tzid_europe_tallinn = TimeZoneId(65048)
val fb_tzid_europe_tirane = TimeZoneId(65047)
val fb_tzid_europe_tiraspol = TimeZoneId(65046)
val fb_tzid_europe_ulyanovsk = TimeZoneId(65045)
val fb_tzid_europe_uzhgorod = TimeZoneId(65044)
val fb_tzid_europe_vaduz = TimeZoneId(65043)
val fb_tzid_europe_vatican = TimeZoneId(65042)
val fb_tzid_europe_vienna = TimeZoneId(65041)
val fb_tzid_europe_vilnius = TimeZoneId(65040)
val fb_tzid_europe_volgograd = TimeZoneId(65039)
val fb_tzid_europe_warsaw = TimeZoneId(65038)
val fb_tzid_europe_zagreb = TimeZoneId(65037)
val fb_tzid_europe_zaporozhye = TimeZoneId(65036)
val fb_tzid_europe_zurich = TimeZoneId(65035)
val fb_tzid_factory = TimeZoneId(65034)
val fb_tzid_gb = TimeZoneId(65033)
val fb_tzid_gb_eire = TimeZoneId(65032)
val fb_tzid_gmt_plus_0 = TimeZoneId(65031)
val fb_tzid_gmt_minus_0 = TimeZoneId(65030)
val fb_tzid_gmt0 = TimeZoneId(65029)
val fb_tzid_greenwich = TimeZoneId(65028)
val fb_tzid_hst = TimeZoneId(65027)
val fb_tzid_hongkong = TimeZoneId(65026)
val fb_tzid_iet = TimeZoneId(65025)
val fb_tzid_ist = TimeZoneId(65024)
val fb_tzid_iceland = TimeZoneId(65023)
val fb_tzid_indian_antananarivo = TimeZoneId(65022)
val fb_tzid_indian_chagos = TimeZoneId(65021)
val fb_tzid_indian_christmas = TimeZoneId(65020)
val fb_tzid_indian_cocos = TimeZoneId(65019)
val fb_tzid_indian_comoro = TimeZoneId(65018)
val fb_tzid_indian_kerguelen = TimeZoneId(65017)
val fb_tzid_indian_mahe = TimeZoneId(65016)
val fb_tzid_indian_maldives = TimeZoneId(65015)
val fb_tzid_indian_mauritius = TimeZoneId(65014)
val fb_tzid_indian_mayotte = TimeZoneId(65013)
val fb_tzid_indian_reunion = TimeZoneId(65012)
val fb_tzid_iran = TimeZoneId(65011)
val fb_tzid_israel = TimeZoneId(65010)
val fb_tzid_jst = TimeZoneId(65009)
val fb_tzid_jamaica = TimeZoneId(65008)
val fb_tzid_japan = TimeZoneId(65007)
val fb_tzid_kwajalein = TimeZoneId(65006)
val fb_tzid_libya = TimeZoneId(65005)
val fb_tzid_met = TimeZoneId(65004)
val fb_tzid_mit = TimeZoneId(65003)
val fb_tzid_mst = TimeZoneId(65002)
val fb_tzid_mst7mdt = TimeZoneId(65001)
val fb_tzid_mexico_bajanorte = TimeZoneId(65000)
val fb_tzid_mexico_bajasur = TimeZoneId(64999)
val fb_tzid_mexico_general = TimeZoneId(64998)
val fb_tzid_net = TimeZoneId(64997)
val fb_tzid_nst = TimeZoneId(64996)
val fb_tzid_nz = TimeZoneId(64995)
val fb_tzid_nz_chat = TimeZoneId(64994)
val fb_tzid_navajo = TimeZoneId(64993)
val fb_tzid_plt = TimeZoneId(64992)
val fb_tzid_pnt = TimeZoneId(64991)
val fb_tzid_prc = TimeZoneId(64990)
val fb_tzid_prt = TimeZoneId(64989)
val fb_tzid_pst = TimeZoneId(64988)
val fb_tzid_pst8pdt = TimeZoneId(64987)
val fb_tzid_pacific_apia = TimeZoneId(64986)
val fb_tzid_pacific_auckland = TimeZoneId(64985)
val fb_tzid_pacific_bougainville = TimeZoneId(64984)
val fb_tzid_pacific_chatham = TimeZoneId(64983)
val fb_tzid_pacific_chuuk = TimeZoneId(64982)
val fb_tzid_pacific_easter = TimeZoneId(64981)
val fb_tzid_pacific_efate = TimeZoneId(64980)
val fb_tzid_pacific_enderbury = TimeZoneId(64979)
val fb_tzid_pacific_fakaofo = TimeZoneId(64978)
val fb_tzid_pacific_fiji = TimeZoneId(64977)
val fb_tzid_pacific_funafuti = TimeZoneId(64976)
val fb_tzid_pacific_galapagos = TimeZoneId(64975)
val fb_tzid_pacific_gambier = TimeZoneId(64974)
val fb_tzid_pacific_guadalcanal = TimeZoneId(64973)
val fb_tzid_pacific_guam = TimeZoneId(64972)
val fb_tzid_pacific_honolulu = TimeZoneId(64971)
val fb_tzid_pacific_johnston = TimeZoneId(64970)
val fb_tzid_pacific_kiritimati = TimeZoneId(64969)
val fb_tzid_pacific_kosrae = TimeZoneId(64968)
val fb_tzid_pacific_kwajalein = TimeZoneId(64967)
val fb_tzid_pacific_majuro = TimeZoneId(64966)
val fb_tzid_pacific_marquesas = TimeZoneId(64965)
val fb_tzid_pacific_midway = TimeZoneId(64964)
val fb_tzid_pacific_nauru = TimeZoneId(64963)
val fb_tzid_pacific_niue = TimeZoneId(64962)
val fb_tzid_pacific_norfolk = TimeZoneId(64961)
val fb_tzid_pacific_noumea = TimeZoneId(64960)
val fb_tzid_pacific_pago_pago = TimeZoneId(64959)
val fb_tzid_pacific_palau = TimeZoneId(64958)
val fb_tzid_pacific_pitcairn = TimeZoneId(64957)
val fb_tzid_pacific_pohnpei = TimeZoneId(64956)
val fb_tzid_pacific_ponape = TimeZoneId(64955)
val fb_tzid_pacific_port_moresby = TimeZoneId(64954)
val fb_tzid_pacific_rarotonga = TimeZoneId(64953)
val fb_tzid_pacific_saipan = TimeZoneId(64952)
val fb_tzid_pacific_samoa = TimeZoneId(64951)
val fb_tzid_pacific_tahiti = TimeZoneId(64950)
val fb_tzid_pacific_tarawa = TimeZoneId(64949)
val fb_tzid_pacific_tongatapu = TimeZoneId(64948)
val fb_tzid_pacific_truk = TimeZoneId(64947)
val fb_tzid_pacific_wake = TimeZoneId(64946)
val fb_tzid_pacific_wallis = TimeZoneId(64945)
val fb_tzid_pacific_yap = TimeZoneId(64944)
val fb_tzid_poland = TimeZoneId(64943)
val fb_tzid_portugal = TimeZoneId(64942)
val fb_tzid_roc = TimeZoneId(64941)
val fb_tzid_rok = TimeZoneId(64940)
val fb_tzid_sst = TimeZoneId(64939)
val fb_tzid_singapore = TimeZoneId(64938)
val fb_tzid_systemv_ast4 = TimeZoneId(64937)
val fb_tzid_systemv_ast4adt = TimeZoneId(64936)
val fb_tzid_systemv_cst6 = TimeZoneId(64935)
val fb_tzid_systemv_cst6cdt = TimeZoneId(64934)
val fb_tzid_systemv_est5 = TimeZoneId(64933)
val fb_tzid_systemv_est5edt = TimeZoneId(64932)
val fb_tzid_systemv_hst10 = TimeZoneId(64931)
val fb_tzid_systemv_mst7 = TimeZoneId(64930)
val fb_tzid_systemv_mst7mdt = TimeZoneId(64929)
val fb_tzid_systemv_pst8 = TimeZoneId(64928)
val fb_tzid_systemv_pst8pdt = TimeZoneId(64927)
val fb_tzid_systemv_yst9 = TimeZoneId(64926)
val fb_tzid_systemv_yst9ydt = TimeZoneId(64925)
val fb_tzid_turkey = TimeZoneId(64924)
val fb_tzid_uct = TimeZoneId(64923)
val fb_tzid_us_alaska = TimeZoneId(64922)
val fb_tzid_us_aleutian = TimeZoneId(64921)
val fb_tzid_us_arizona = TimeZoneId(64920)
val fb_tzid_us_central = TimeZoneId(64919)
val fb_tzid_us_east_indiana = TimeZoneId(64918)
val fb_tzid_us_eastern = TimeZoneId(64917)
val fb_tzid_us_hawaii = TimeZoneId(64916)
val fb_tzid_us_indiana_starke = TimeZoneId(64915)
val fb_tzid_us_michigan = TimeZoneId(64914)
val fb_tzid_us_mountain = TimeZoneId(64913)
val fb_tzid_us_pacific = TimeZoneId(64912)
val fb_tzid_us_pacific_new = TimeZoneId(64911)
val fb_tzid_us_samoa = TimeZoneId(64910)
val fb_tzid_utc = TimeZoneId(64909)
val fb_tzid_universal = TimeZoneId(64908)
val fb_tzid_vst = TimeZoneId(64907)
val fb_tzid_w_su = TimeZoneId(64906)
val fb_tzid_wet = TimeZoneId(64905)
val fb_tzid_zulu = TimeZoneId(64904)
val fb_tzid_america_nuuk = TimeZoneId(64903)
val fb_tzid_asia_qostanay = TimeZoneId(64902)
val fb_tzid_pacific_kanton = TimeZoneId(64901)
val fb_tzid_europe_kyiv = TimeZoneId(64900)
val fb_tzid_america_ciudad_juarez = TimeZoneId(64899)
