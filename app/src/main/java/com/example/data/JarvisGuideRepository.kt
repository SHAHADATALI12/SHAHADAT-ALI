package com.example.data

data class GuideArticle(
    val id: String,
    val category: String,
    val titleHindi: String,
    val titleEnglish: String,
    val subtitleHindi: String,
    val subtitleEnglish: String,
    val contentHindi: String,
    val contentEnglish: String,
    val keySteps: List<String>,
    val copyableScriptOrConfig: String? = null,
    val recommendedApps: List<String> = emptyList(),
    val iconName: String = "bolt"
)

data class AutomationBlueprint(
    val id: String,
    val name: String,
    val triggerType: String,
    val actionType: String,
    val descriptionHindi: String,
    val descriptionEnglish: String,
    val macrodroidRecipe: String,
    val taskerXmlStub: String,
    val difficulty: String = "Beginner"
)

object JarvisGuideRepository {

    val categories = listOf(
        "All" to "सभी",
        "Architecture" to "जार्विस ब्लूप्रिंट",
        "Voice Control" to "वॉयस कंट्रोल",
        "Automation" to "ऑटोमेशन ट्रिक्स",
        "Iron Man Voice" to "जार्विस आवाज़",
        "Secret Hacks" to "सीक्रेट फीचर्स",
        "Gemini AI" to "AI सेटअप"
    )

    val articles = listOf(
        GuideArticle(
            id = "guide_1",
            category = "Architecture",
            titleHindi = "मोबाइल में जार्विस कैसे बनाएं (Complete Blueprint 2026)",
            titleEnglish = "How to Build JARVIS AI on Android (Full Blueprint)",
            subtitleHindi = "बिना कोडिंग या कोडिंग के साथ अपना पर्सनल आयरन मैन असिस्टेंट तैयार करें",
            subtitleEnglish = "Create your personal Iron Man AI assistant with or without coding",
            contentHindi = """
एक असली JARVIS असिस्टेंट बनाने के लिए मोबाइल में 4 मुख्य कंपोनेंट्स (Pillars) काम करते हैं:

1. Ears (Speech-to-Text): आपकी आवाज़ सुनकर टेक्स्ट में बदलना।
2. Brain (AI LLM - Google Gemini): आपकी बात को समझकर बुद्धिमान जवाब और कमांड्स तैयार करना।
3. Hands (Android Automation Engine): टॉर्च जलाना, ऐप्स खोलना, ब्लूटूथ ऑन करना, बैटरी चेक करना।
4. Mouth (Text-to-Speech Engine): आयरन मैन जार्विस की भारी रोबोटिक आवाज़ में जवाब देना।

इस ऐप में यह चारों तकनीकें सीधे इन-बिल्ट हैं! आप नीचे दिए गए स्टेप्स से इसे अपने पूरे मोबाइल का डिफ़ॉल्ट असिस्टेंट भी बना सकते हैं।
            """.trimIndent(),
            contentEnglish = """
To build a functional JARVIS on Android, 4 core pillars work together:

1. Ears (STT): Captures your voice command into digital text.
2. Brain (Gemini 3.5 Flash AI): Understands the intent, context, and crafts intelligent responses.
3. Hands (Automation Layer): Toggles Torch, launches apps, monitors sensors, and executes protocols.
4. Mouth (TTS): Speaks back with Tony Stark's British robotic accent.

This app bundles all 4 systems into one unified HUD interface!
            """.trimIndent(),
            keySteps = listOf(
                "Step 1: Android Speech Recognition परमिशन ऑन करें",
                "Step 2: Camera और Flashlight ऑटोमेशन परमिशन चालू करें",
                "Step 3: Google Gemini API Key जोड़ें (फ्री में प्राप्त करें)",
                "Step 4: TTS Settings में ब्रिटिश या हिंदी वॉयस ट्यून करें",
                "Step 5: MacroDroid या Google Assistant शॉर्टकट से लिंक करें"
            ),
            copyableScriptOrConfig = "// Android Voice Assistant Flow\nVoice Input -> SpeechRecognizer -> Intent Router (Local Hardware OR Gemini 3.5 Flash) -> TextToSpeech Synthesizer",
            recommendedApps = listOf("Jarvis AI App", "MacroDroid", "Google Speech Services"),
            iconName = "architecture"
        ),

        GuideArticle(
            id = "guide_2",
            category = "Voice Control",
            titleHindi = "'Hey Jarvis' कस्टम वेक वर्ड कैसे सेट करें (No-Coding)",
            titleEnglish = "How to Setup 'Hey Jarvis' Custom Wake Word (No-Coding)",
            subtitleHindi = "बिना स्क्रीन छुए सिर्फ आवाज़ से फोन को कमांड दें",
            subtitleEnglish = "Trigger your phone hands-free using custom voice trigger",
            contentHindi = """
एंड्रॉइड में 'Hey Jarvis' वेक-वर्ड सेट करने के दो सबसे आसान तरीके हैं:

طریقہ 1: Google Assistant Routine Rename Hack
- अपने फोन में Google Assistant सेटिंग्स खोलें।
- 'Routines' में जाएं और नया रूटीन बनाएं।
- 'When I say': इसमें लिखें 'Jarvis' या 'Hey Jarvis'।
- 'Action': 'Open Jarvis AI' या 'Open Camera' या कोई भी कस्टम कमांड।
- अब जैसे ही आप 'Hey Google, Jarvis' बोलेंगे, आपका जार्विस तुरंत सक्रिय हो जाएगा!

طریقہ 2: MacroDroid Hotword & Shake Shortcut
- MacroDroid ऐप में 'Volume Button Long Press' या 'Shake Device' को ट्रिगर बनाएं।
- एक्शन में 'Launch Jarvis AI' और वॉयस इनपुट ऑटो-स्टार्ट करें।
            """.trimIndent(),
            contentEnglish = """
Two proven methods to achieve custom 'Hey Jarvis' wake word activation on Android:

Method 1: Google Assistant Routine Redirection
- Open Google App -> Settings -> Google Assistant -> Routines.
- Create New Routine: Trigger phrase = 'Hey Jarvis' or 'Jarvis'.
- Set Action = 'Open Jarvis AI'.
- Whenever you say 'Hey Jarvis', your personal Stark assistant opens immediately.

Method 2: MacroDroid Gesture / Button Trigger
- Set Trigger: Double-tap power button or shake device.
- Action: Launch Jarvis AI and auto-listen.
            """.trimIndent(),
            keySteps = listOf(
                "Google Assistant खोलें -> Routines पर टैप करें",
                "नया ट्रिगर जोड़ें: 'Hey Jarvis'",
                "Action सेट करें: 'Open Jarvis AI'",
                "Optional: लॉक स्क्रीन पर भी एक्टिव रहने के लिए Voice Match चालू रखें"
            ),
            copyableScriptOrConfig = "Google Assistant Routine -> Starter: 'Jarvis' -> Action: 'Open Jarvis AI'",
            recommendedApps = listOf("Google Assistant", "MacroDroid"),
            iconName = "mic"
        ),

        GuideArticle(
            id = "guide_3",
            category = "Automation",
            titleHindi = "2026 के टॉप एंड्रॉइड ऑटोमेशन हैक्स (मोबाइल बनेगा रोबोट)",
            titleEnglish = "Top Android Automation Hacks 2026 (Make Phone a Robot)",
            subtitleHindi = "MacroDroid और Tasker से बनाएं पावरफुल ऑटोमैटिक फीचर्स",
            subtitleEnglish = "Build powerful automated routines with MacroDroid & Tasker",
            contentHindi = """
इन 5 ऑटोमेशन ट्रिक्स से आपका फोन किसी सुपरकंप्यूटर की तरह काम करेगा:

1. Smart Charging Alarm (बैटरी प्रोटेक्टर):
जब बैटरी 80% या 100% हो, तो जार्विस आवाज़ में बोले: 'Sir, Arc reactor is at 100%. Please disconnect power source.'

2. Shake for Torch (इमरजेंसी फ्लैश):
अंधेरे में फोन को दो बार हिलाने (Shake) पर तुरंत फ्लैशलाइट जल जाए और जार्विस बोले 'Illumination online.'

3. Car Bluetooth Auto-Protocol:
जैसे ही कार के ब्लूटूथ से कनेक्ट हो: गूगल मैप्स अपने आप खुले, जीपीएस ऑन हो और वॉल्यूम 80% पर सेट हो जाए।

4. Night Silence Mode:
रात 11:00 बजे अपने आप साइलेंट मोड, ब्लू लाइट फिल्टर और बैटरी सेवर ऑन हो जाए।
            """.trimIndent(),
            contentEnglish = """
5 high-impact automation recipes that transform your phone into an intelligent robotic companion:

1. Battery Health Guardian:
Speaks custom voice warning when battery hits 100% or falls below 15%.

2. Shake-to-Torch:
A quick double-wrist shake toggles the camera flashlight instantly without unlocking the screen.

3. Car Dock Protocol:
When connected to Car Bluetooth: Auto-launches Google Maps, maxes out media volume, and speaks: 'Drive safely, Sir.'

4. Automated Night Protocol:
At 11 PM: Activates DND, lowers brightness to 10%, enables Dark Theme, and speaks: 'Good night, Sir. All perimeter defenses armed.'
            """.trimIndent(),
            keySteps = listOf(
                "MacroDroid ऐप इंस्टॉल करें",
                "New Macro -> Add Trigger (जैसे Battery > 80%)",
                "Add Action -> Speak Text ('Sir, battery fully charged')",
                "Add Action -> Play Arc Reactor Sound",
                "सेव करें और टेस्ट करें"
            ),
            copyableScriptOrConfig = "Trigger: Battery Level = 100% -> Action: TextToSpeech ('Sir, battery core full. Unplug charger.')",
            recommendedApps = listOf("MacroDroid", "Tasker", "Automate"),
            iconName = "smart_toy"
        ),

        GuideArticle(
            id = "guide_4",
            category = "Iron Man Voice",
            titleHindi = "जार्विस जैसी असली आवाज़ (Iron Man Voice) कैसे लगाएं",
            titleEnglish = "How to Setup Authentic Iron Man / Jarvis Voice",
            subtitleHindi = "एंड्रॉइड TTS में ब्रिटिश और हिंदी रोबोटिक वॉयस ट्यूनिंग",
            subtitleEnglish = "Tune British English and Hindi robotic accent in Android TTS",
            contentHindi = """
आयरन मैन फिल्म में जार्विस (पॉल बेटनी) की आवाज़ एक शांत, भारी और ब्रिटिश एक्सेन्ट वाली है। इसे फोन में सेट करने की सीक्रेट ट्रिक:

1. अपने फोन की Settings -> System / Accessibility -> 'Text-to-speech output' (TTS) खोलें।
2. Preferred engine में 'Speech Services by Google' चुनें।
3. गियर आइकन पर टैप करें -> 'Install voice data' में जाएं।
4. 'English (United Kingdom)' डाउनलोड करें और 'Voice 1' या 'Voice 3' (Male accent) चुनें।
5. यदि आप हिंदी में जार्विस चाहते हैं: 'Hindi (India)' डाउनलोड करें और Male Voice चुनें।
6. Pitch को 0.95 (हल्का गहरा) और Speech rate को 1.05 रखें।

इस ऐप की 'Diagnostics & Voice Lab' टैब में जाकर आप इसे लाइव टेस्ट कर सकते हैं!
            """.trimIndent(),
            contentEnglish = """
In the Iron Man movies, JARVIS speaks with Paul Bettany's refined, calm British accent. Here is how to configure it on Android:

1. Open Android Settings -> Accessibility / System -> 'Text-to-Speech output'.
2. Select 'Speech Services by Google' as the preferred engine.
3. Tap the Settings Gear icon -> 'Install voice data'.
4. Download 'English (United Kingdom)' -> Select Male Voice option (Voice 1 or 3).
5. For Hindi JARVIS: Download 'Hindi (India)' Male voice package.
6. Calibrate Pitch to 0.95 (deeper resonance) and Speech Rate to 1.05.

You can preview and fine-tune these exact parameters inside the 'Voice Lab' tab of this app!
            """.trimIndent(),
            keySteps = listOf(
                "Phone Settings -> Accessibility -> Text-to-speech output",
                "Google TTS Engine चुनें",
                "English (United Kingdom) Male Voice पैक डाउनलोड करें",
                "Pitch: 0.95 | Speed: 1.05 पर सेट करें",
                "Jarvis AI ऐप में 'Voice Lab' से टेस्ट करें"
            ),
            copyableScriptOrConfig = "Settings -> TTS -> Engine: Google -> Language: en-GB -> Voice: Male #1 -> Pitch: 0.95 -> Rate: 1.05",
            recommendedApps = listOf("Google Speech Services", "Jarvis AI"),
            iconName = "record_voice_over"
        ),

        GuideArticle(
            id = "guide_5",
            category = "Secret Hacks",
            titleHindi = "एंड्रॉइड के सीक्रेट कोड्स और हिडन स्मार्ट फीचर्स 2026",
            titleEnglish = "Secret Android Codes & Hidden Smart Features 2026",
            subtitleHindi = "डेवलपर ऑप्शंस, जेस्चर ट्रिक्स और सिस्टम स्पीड बूस्टर",
            subtitleEnglish = "Developer options, gesture triggers, and system speedup hacks",
            contentHindi = """
अपने सामान्य मोबाइल को सुपरकंप्यूटर बनाने के लिए इन सीक्रेट सेटिंग्स को चालू करें:

1. 2x Super Fast Phone (Developer Options Hack):
- Settings -> About Phone -> 'Build Number' पर 7 बार टैप करें।
- Developer Options में जाएं।
- 'Window animation scale', 'Transition animation scale', 'Animator duration scale' को '0.5x' कर दें। आपका फोन तुरंत रॉकेट की तरह तेज़ महसूस होगा!

2. Quick Tap (Back of phone tap):
- Settings -> System -> Gestures -> 'Quick Tap' ऑन करें।
- फोन के पीछे दो बार उंगली थपथपाने (Double Tap) पर टॉर्च या जार्विस ऐप खुल जाएगा।

3. Notification Snooze & AI Summaries:
- नोटिफिकेशन बार से सीधे जरूरी मैसेज फिल्टर करने के लिए नोटिफिकेशन हिस्ट्री चालू रखें।
            """.trimIndent(),
            contentEnglish = """
Secret power user settings to make your phone feel like futuristic Stark technology:

1. 2X System Speed Optimization:
- Settings -> About Phone -> Tap 'Build Number' 7 times.
- Open Developer Options -> Find 'Window animation scale', 'Transition animation scale', and 'Animator duration scale'.
- Change all three from 1.0x to 0.5x. Animations will be instantaneous!

2. Back-Tap Gesture (Quick Tap):
- Settings -> System -> Gestures -> Quick Tap to start actions.
- Double-tap the back of your phone to launch Jarvis AI or toggle flashlight.

3. App Pinning & Focus Mode:
- Lock critical diagnostic tools in memory so Jarvis never lags.
            """.trimIndent(),
            keySteps = listOf(
                "Build Number पर 7 बार टैप कर डेवलपर ऑप्शन ऑन करें",
                "एनिमेशन स्केल 0.5x करें",
                "Gestures -> Quick Tap चालू करें",
                "Accessibility शॉर्टकट में Jarvis AI असाइन करें"
            ),
            copyableScriptOrConfig = "Settings -> About Phone -> Build Number (7 taps) -> Developer Options -> Animations 0.5x",
            recommendedApps = listOf("System Settings", "Jarvis AI"),
            iconName = "terminal"
        ),

        GuideArticle(
            id = "guide_6",
            category = "Gemini AI",
            titleHindi = "फ्री Google Gemini API Key कैसे प्राप्त करें और जोड़ें",
            titleEnglish = "How to Get & Connect Free Google Gemini API Key",
            subtitleHindi = "जार्विस के दिमाग को सुपर-इंटेलिजेंट बनाने का तरीका",
            subtitleEnglish = "Supercharge Jarvis neural reasoning with Gemini 3.5 Flash",
            contentHindi = """
इस जार्विस ऐप में डिवाइस कंट्रोल (टॉर्च, बैटरी, ऐप्स, अलार्म) बिना किसी इंटरनेट या चाबी के काम करता है। लेकिन जटिल सवालों, कोडिंग, शायरी, और गहरी बातचीत के लिए आप Google Gemini 3.5 Flash का दिमाग जोड़ सकते हैं:

1. aistudio.google.com पर जाएं और अपने Google अकाउंट से लॉगिन करें।
2. 'Get API key' बटन पर क्लिक करें।
3. 'Create API key' दबाएं और अपनी चाबी कॉपी कर लें।
4. यह की बिल्कुल फ्री होती है!
5. AI Studio के Secrets पैनल में GEMINI_API_KEY नाम से सेव करें।

अब आपका जार्विस दुनिया के किसी भी सवाल का जवाब सेकंडों में दे सकता है!
            """.trimIndent(),
            contentEnglish = """
While hardware device controls (Torch, Battery, Intents, Clock) work completely offline, you can unlock full conversational genius via Google Gemini 3.5 Flash:

1. Visit aistudio.google.com on your phone or PC.
2. Sign in with your Google account and click 'Get API key'.
3. Click 'Create API key in new project' and copy the token.
4. It is free with generous daily rate limits!
5. Set it in the Secrets panel as GEMINI_API_KEY.

Your JARVIS will now answer science, coding, history, calculations, and complex reasoning in real-time!
            """.trimIndent(),
            keySteps = listOf(
                "aistudio.google.com पर जाएं",
                "'Get API Key' पर क्लिक करें",
                "API Key कॉपी करें",
                "AI Studio Secrets पैनल में 'GEMINI_API_KEY' दर्ज करें"
            ),
            copyableScriptOrConfig = "https://aistudio.google.com/app/apikey -> Create Key -> GEMINI_API_KEY",
            recommendedApps = listOf("Google AI Studio", "Jarvis AI"),
            iconName = "key"
        )
    )

    val blueprints = listOf(
        AutomationBlueprint(
            id = "bp_1",
            name = "Shake for Flashlight Protocol",
            triggerType = "Device Motion (Shake)",
            actionType = "Hardware (Torch Toggle)",
            descriptionHindi = "रात को फोन 2 बार हिलाने पर टॉर्च तुरंत ऑन हो जाए और जार्विस कंफर्म करे।",
            descriptionEnglish = "Shake phone twice to toggle flashlight with Jarvis voice confirmation.",
            macrodroidRecipe = "Trigger: Shake Device (Medium Sensitivity)\nAction: Flashlight Toggle\nAction: Speak Text ('Illumination protocol engaged, Sir.')",
            taskerXmlStub = "Profile: Shake -> Task: Torch On + Say 'Illumination online'"
        ),
        AutomationBlueprint(
            id = "bp_2",
            name = "Arc Reactor Full Charge Alert",
            triggerType = "Battery = 100% & Charging",
            actionType = "Voice Announcement + Sound FX",
            descriptionHindi = "जब बैटरी पूरी चार्ज हो जाए तो जार्विस आवाज़ में चार्जर हटाने की चेतावनी दे।",
            descriptionEnglish = "Speaks alert when battery reaches 100% to protect battery health.",
            macrodroidRecipe = "Trigger: Battery Level reaches 100%\nCondition: Power Connected\nAction: Play Sound (Arc Reactor chime)\nAction: Speak Text ('Sir, the Arc reactor is at 100% capacity. Please disconnect the charging cable.')",
            taskerXmlStub = "Profile: Battery Full (100) -> Task: Say 'Battery full, Sir.'"
        ),
        AutomationBlueprint(
            id = "bp_3",
            name = "Car Dock & Navigation Protocol",
            triggerType = "Bluetooth Connected (Car Audio)",
            actionType = "App Launch + Volume 80% + Voice",
            descriptionHindi = "कार के ब्लूटूथ से जुड़ते ही गूगल मैप्स खुले और वॉयस नेविगेशन एक्टिव हो।",
            descriptionEnglish = "Auto-launches Google Maps and sets media volume when connecting to Car Bluetooth.",
            macrodroidRecipe = "Trigger: Bluetooth Device Connected (Car System)\nAction: Media Volume -> 85%\nAction: Launch App -> Google Maps\nAction: Speak Text ('Telemetry synchronized, Sir. Route navigation ready.')",
            taskerXmlStub = "Profile: BT Near/Connected -> Task: Launch Maps + Set Volume 85"
        ),
        AutomationBlueprint(
            id = "bp_4",
            name = "Emergency Protocol Mark 85",
            triggerType = "Volume Down 3x or Voice 'Emergency'",
            actionType = "Flashlight Strobe + Max Volume Alarm",
            descriptionHindi = "इमरजेंसी में वॉल्यूम बटन 3 बार दबाने पर तेज सायरन और फ्लैश स्ट्रोब ऑन हो।",
            descriptionEnglish = "Emergency button sequence triggers flashlight strobe and loud beacon alarm.",
            macrodroidRecipe = "Trigger: Volume Button Pressed x3\nAction: Alarm Volume -> 100%\nAction: Flashlight -> Strobe\nAction: Speak Text ('Emergency protocol initiated!')",
            taskerXmlStub = "Profile: Hardware Key Combo -> Task: Strobe Flash + Max Alarm"
        ),
        AutomationBlueprint(
            id = "bp_5",
            name = "Morning Stark Briefing Protocol",
            triggerType = "Time = 07:00 AM (Weekdays)",
            actionType = "Time + Battery + Greeting Voice",
            descriptionHindi = "सुबह 7 बजे जार्विस का गुड मॉर्निंग ब्रीफिंग, बैटरी स्टेटस और समय की घोषणा।",
            descriptionEnglish = "Automated 7 AM wake-up greeting with current battery level and time.",
            macrodroidRecipe = "Trigger: Regular Interval / Time of Day (07:00 AM)\nAction: Speak Text ('Good morning, Sir. Time is 7:00 AM. Battery is ready. Systems are nominal. Have a productive day.')",
            taskerXmlStub = "Profile: Time 07:00 -> Task: Speak 'Good morning, Sir.'"
        )
    )
}
