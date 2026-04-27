# Initial Prompt — Paceometer

**Datum:** 2026-03-29

## Beskrivning från användaren

> "En 'Paceometer'. Den ska se ut som en snygg vanlig rund hastighetsmätare, men jag vill kunna välja mellan att kunna visa min hastighet i km/h (som en vanlig hastighetsmätare) och/eller hur många minuter det skulle ta att köra 10 km för olika hastigheter. En tredje indikator skulle visa hur lång tid det skulle ta att köra en valfri sträcka i nuvarande hastighet. Default ska vara 500 km. Det ska gå att välja vilka av mätvärdena som alla visas."

## Sammanfattning av krav

- Android-app med en rund, snygg hastighetsmätare som huvudelement
- Tre valbara mätvärden som var och en kan slås av/på:
  1. **km/h** – hastighet som en vanlig bilhastighetsmätare
  2. **min/10km** – hur många minuter det tar att köra 10 km i nuvarande hastighet (pace)
  3. **Tid till X km** – ETA för en valfri sträcka vid nuvarande hastighet (default 500 km)
- Sträckan för ETA-beräkningen ska kunna ändras av användaren
- GPS-baserad hastighet (LocationManager)

## Deployment

- APK kopieras till: `/mnt/e/Google Drive/Projects/Paceometer/`
