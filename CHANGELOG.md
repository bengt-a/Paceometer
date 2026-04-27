# CHANGELOG

## [2.2.1] - 2026-04-27

### Fixed
- **Pace-visning nedflyttad:** "x.y min / 10 km" i centrum ligger nu klart under nålnavet
- **∞ min vid stillastående:** visar "∞ min" (pace i centrum) och "∞ min" (ETA-kort) istället för "—" vid 0 km/h
- **Copyright-text:** guldfärgad (#C9A227) och inkluderar årtalet — "Copyright © 2026 Bengt Alverborg"

## [2.2.0] - 2026-04-27

### Changed
- **Tredje vyläge (pace + tid):** tredje klick på urtavlan ger nu ett hybridläge — yttre ringen visar pace (min/10km) och inre ringen visar tid kvar. Toggle-cykel: km/h → tid kvar → pace+tid → km/h
- **Kortare tick-streck:** tick-markeringarna på inre ringen är kortare och etiketterna sitter närmre den färgade bågen
- **Större font för inre ring-etiketter:** fontstorleken ökad från 0.085 till 0.092 av radien (~+2 pt)
- **Pace i centrum på två rader:** "x.y min" på första raden och "/ 10 km" på andra — ersätter enbands-visningen
- **Mellanrum i ETA-kortet:** siffra och "min" separeras nu med mellanslag (t.ex. "5 min" istället för "5min")

## [2.1.0] - 2026-04-08

### Changed
- **Inre ring-etiketter:** vit text (från grå-lila) och något större font för bättre läsbarhet
- **Hastighetssiffra i mitten:** mindre storlek för luftigare centrumvisning
- **Yttre ring-swap:** när man togglar till tid-kvar-läget på hastighetsskalan visas km/h-etiketter i yttre ringen (pace flyttar bort); i normalt läge visas pace som tidigare
- **ETA-kortets klick:** tryck på ETA-kortet visar nu en dialog för att ändra målsträckan (räknaren nollställs vid ny sträcka) — slipper gå till Inställningar
- **Hastighetsfärgad båge (valbar):** ny inställning som byter blå båge mot grön→gul→orange→röd-gradient baserat på hastighet (0–80 grön, 80–130 övergång, 130+ röd)

## [2.0.0] - 2026-03-29

### Changed
- **Mittenvisning:** visar alltid digital hastighet i km/h (och pace om aktiverat) — oförändrat från v1
- **Inre ring — toggle:** tryck på urtavlan för att växla mellan km/h-skala (0, 20, 40…200) och dynamisk "tid till kvar sträcka" (t.ex. "4h30" vid 100 km/h om 450 km återstår)
- **Yttre ring (min/10km):** oförändrad, visas om aktiverad i inställningar
- **Nålen:** röd med rödglöd-effekt (ersätter cyan)
- **Arc:** smalare blå båge
- **Skärmen alltid på:** `FLAG_KEEP_SCREEN_ON` aktivt så länge appen är öppen
- **Distansräknare:** GPS-drivna kilometer räknas av mot vald sträcka; återstående km + ETA vid aktuell hastighet visas i kortet under urtavlan
- **ETA-kortet:** tryck för att nollställa distansräknaren (återgår till full startsträcka)
- **Persisterande räknare:** körda km sparas i SharedPreferences och finns kvar vid omstart

## [1.1.0] - 2026-03-29

### Changed
- **km/h-skalor:** visar nu alla intervall (0, 20, 40, 60, 80, 100, 120, 140, 160, 180, 200) istället för bara 0/100/200. Teckenstorlek något reducerad för att passa utan överlapp
- **min/10km på ytterskalan:** pace-värden (minuter per 10 km) visas nu på utsidan av bågspåret för varje 20 km/h-intervall. km/h är på insidan, min/10km på utsidan. Styrs av min/10km-togglen i inställningar


## [1.0.0] - 2026-03-29

### Added
- Rund mörk hastighetsmätare med blå-till-cyan gradient-båge (270° sweep, 0–200 km/h)
- Mjuk nålanimation via `ValueAnimator` med `DecelerateInterpolator`
- Cyan nålglow-effekt med `BlurMaskFilter`
- **Mätvärde 1 — km/h:** aktuell GPS-hastighet visas som stort digitalt tal + skalmarkeringar (0/100/200) på urtavlan
- **Mätvärde 2 — min/10km:** pace vid aktuell hastighet, visas som cyan text under km/h-siffran
- **Mätvärde 3 — ETA:** beräknad restid till valfri sträcka (default 500 km) i ett kort under urtavlan
- Inställningsskärm med toggle per mätvärde och redigerbart ETA-avstånd
- GPS-hastighet via `LocationManager` (GPS_PROVIDER), uppdateras var 500 ms
- "Söker GPS…"-status visas i centrum tills GPS-fix erhålls
- Behörighetsdialog för `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`
- Mörkt tema genomgående (`Theme.MaterialComponents.DayNight.NoActionBar`)
- APK deployad till `/mnt/e/Google Drive/Projects/Paceometer/Paceometer-1.0.0.apk`
