# Paceometer — Claude-instruktioner

Detta dokument innehåller allt du behöver för att förstå projektet och fortsätta arbetet i en ny session.

## Vad är Paceometer?

En Android-app som fungerar som en avancerad hastighetsmätare med GPS. Den visar en snygg, rund mörk urtavla och kan visa tre olika mätvärden:

1. **km/h** — aktuell GPS-hastighet som en vanlig hastighetsmätare
2. **min/10km** — pacevisning (hur många minuter för 10 km) vid aktuell hastighet
3. **ETA** — beräknad restid till en valfri sträcka vid aktuell hastighet

Alla tre kan slås av/på individuellt i inställningsskärmen.

## Projektlayout

```
Paceometer/
├── app/src/main/
│   ├── java/com/paceometer/
│   │   ├── MainActivity.kt       – GPS, layout, ETA-uppdatering, distansräknare
│   │   ├── SpeedometerView.kt    – Anpassad Canvas-vy, all grafik
│   │   ├── SettingsActivity.kt   – On/off-toggles + inställningar
│   │   └── Prefs.kt              – SharedPreferences-wrapper
│   ├── res/
│   │   ├── layout/activity_main.xml
│   │   ├── layout/activity_settings.xml
│   │   ├── drawable/             – ikoner och bakgrunder
│   │   ├── mipmap-anydpi-v26/    – launcher-ikoner
│   │   └── values/               – strings, colors, themes
│   └── AndroidManifest.xml
├── CLAUDE.md                     – denna fil
├── PROMPT.md                     – ursprunglig kravbeskrivning
├── CHANGELOG.md                  – versionshistorik
└── build.gradle.kts / settings.gradle.kts / gradle.properties
```

## SpeedometerView — design

- **Bakgrund:** mörkblå-svart (#0A0A14), mjuk ringkant
- **Arc-spår:** 270° sweep, startar 135° (nere till vänster), slutar 45° (nere till höger)
- **Aktiv båge:** standard blå→cyan gradient via `SweepGradient` + canvas-rotation; valfritt hastighetsfärgad (se nedan)
- **Skalmärken:** minor var 5 km/h, major var 20 km/h (11 st totalt: 0, 20, 40 … 200)
- **Nål:** röd linje med röd glow (`BlurMaskFilter` → kräver `LAYER_TYPE_SOFTWARE`)
- **Inre ring:** etikett vid varje 20 km/h-steg — två lägen (toggle via tap på urtavlan):
  - `KMH` — km/h-värden (0, 20, 40 … 200), vit fet text
  - `TIME_TO_DISTANCE` — tid kvar till vald sträcka vid respektive hastighet ("4h30", "45m" etc)
- **Yttre ring:** etikett vid varje 20 km/h-steg — byter automatiskt med inre ringen:
  - När inre = `KMH` → visar pace i min/10km (om pace-toggle är på)
  - När inre = `TIME_TO_DISTANCE` → visar km/h-värden
  - Yttre ring-text: ljus cyan (#4DD0E1), något större än inre ring-text
- **Centrumvisning:**
  - Båda på: km/h-tal (medelstor) + "km/h" + cyan "X.X min/10km"
  - Bara km/h: tal + enhet
  - Bara pace: cyan tal + "min/10km"
- **GPS-status:** "Söker GPS…" visas i centrum tills fix, döljs sedan
- **Lägesindikator:** liten text i nedre delen av urtavlan visar aktivt läge ("↕ km/h" / "↕ tid kvar")
- **Max hastighet:** 200 km/h

## Inställningar (SettingsActivity)

| Toggle | Beskrivning | Prefs-nyckel |
|--------|-------------|--------------|
| Hastighet (km/h) | Visar km/h på inre ringen och i centrum | `show_kmh` |
| Pace (min/10km) | Visar pace på yttre ringen och i centrum | `show_pace` |
| Tid till sträcka | Visar ETA-kort under urtavlan | `show_eta` |
| Hastighetsfärgad båge | Byter blå båge mot grön→röd-gradient | `speed_color_arc` |

ETA-sträckan (km) kan även ändras direkt från huvudskärmen via tap på ETA-kortet.

## Hastighetsfärgad båge (valbar)

När aktiverad ersätts den blå gradienten med en hastighetsskala:

| Hastighet | Färg |
|-----------|------|
| 0 km/h | Mörkgrön (#1B5E20) |
| ~80 km/h | Ljusgrönt (#00C853) |
| ~105 km/h | Gult (#FFD600) |
| ~130 km/h | Orange (#FF6D00) |
| ~160 km/h | Rött (#FF1744) |
| 200 km/h | Mörkrött (#7F0000) |

Implementerat som `SweepGradient` med 270°-sweep (=0.75 av 360°), positions skalade därefter.

## ETA-kort och distansräknare

- Visar återstående km + beräknad restid vid aktuell hastighet
- GPS-driven distansräknare: integrerar `location.speed × dt`
- Driven sträcka sparas i `Prefs.drivenKm` (persistent vid omstart)
- **Tap på ETA-kortet** → dialog för att ändra målsträckan; nollställer räknaren vid OK

## Byggmiljö

| Parameter | Värde |
|-----------|-------|
| Android SDK | `/home/bengt/Android/Sdk` |
| Gradle | 8.9 |
| Kotlin | 1.9.23 |
| AGP | 8.3.2 |
| Java | 17 |
| minSdk | 26 (Android 8.0) |
| targetSdk | 34 |

## Byggkommando

```bash
cd /home/bengt/Claude-workspace/Paceometer
ANDROID_HOME=/home/bengt/Android/Sdk \
  ~/.gradle/wrapper/dists/gradle-8.9-bin/90cnw93cvbtalezasaz0blq0a/gradle-8.9/bin/gradle \
  assembleDebug --no-daemon
```

**APK-utdata:** `app/build/outputs/apk/debug/app-debug.apk`

## Deployment

```bash
cp app/build/outputs/apk/debug/app-debug.apk \
   "/mnt/e/Google Drive/Projects/Paceometer/Paceometer-X.Y.Z.apk"
```

## Versionshantering

Format: `X.Y.Z`

| Del | Betydelse | Exempel |
|-----|-----------|---------|
| X | Ny funktionalitet | Ny typ av mätvärde, ny vy |
| Y | Mindre förändringar | UI-justering, nytt alternativ |
| Z | Buggfixar | Fix av krasch, felaktig beräkning |

### Filer att uppdatera vid versionsbump
1. `app/build.gradle.kts` — `versionName` och `versionCode`
2. `CHANGELOG.md` — ny sektion överst
3. `CLAUDE.md` — uppdatera "Aktuell version"

## Aktuell version: 2.2.1

Se CHANGELOG.md för fullständig versionshistorik.
