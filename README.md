# RespMonitor

RespMonitor je Android aplikacija za mjerenje respiratorne frekvencije (broja udisaja u minuti) u realnom vremenu, korištenjem senzora pokreta pametnog telefona (žiroskop i akcelerometar), bez potrebe za dodatnom mjernom opremom.

## Kako radi

Aplikacija se postavlja na grudni koš korisnika i prati sitne oscilacije uzrokovane disanjem preko ugrađenih senzora telefona. Signal se zatim obrađuje kroz DSP pipeline kako bi se izdvojila respiratorna frekvencija.

## Tehnički stack

- **Obrada signala:** Butterworth filtriranje, FFT s parabolčnom interpolacijom pika za preciznije određivanje frekvencije, adaptivno EMA glačanje signala
- **Mašinsko učenje:** klasifikator validnosti pozicije telefona (žiroskop + akcelerometar), treniran na Edge Impulse platformi
- **Arhitektura:** Room baza podataka s reaktivnom Flow arhitekturom, Kotlin coroutines
- **UI:** Jetpack Compose

## Tačnost

Aplikacija je validirana u odnosu na ručno brojanje udisaja od strane posmatrača:
- 5 ispitanika × 6 mjerenja = 30 uparenih mjerenja
- MAE (srednja apsolutna greška): 0.93 udisaja/min
- r = 0.995

ML klasifikator validnosti pozicije postiže 86.67% tačnosti na podacima nepoznatih ispitanika (subject-wise holdout).

## Dokumentacija

Detaljan opis metodologije, implementacije i rezultata dostupan je u pratećem diplomskom radu:

📄 [`docs/diplomski_rad.pdf`](docs/diplomski_rad_denis_sofovic.pdf)

*(putanju prilagodi stvarnom nazivu/lokaciji fajla u repozitoriju)*

## Instalacija

```bash
git clone https://github.com/denissofovic/resp-monitor.git
```

Otvoriti projekat u Android Studiju i pokrenuti build.

## Autor

Denis Sofović — završni rad, Elektrotehnički fakultet Univerziteta u Tuzli, mentor: doc. dr. Alma Sećerbegović
