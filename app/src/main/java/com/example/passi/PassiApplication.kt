package com.example.passi

import android.app.Application
import com.google.android.material.color.DynamicColors

/**
 * Punto di ingresso del processo, creato prima di qualunque Activity.
 *
 * Serve per attivare i COLORI DINAMICI (Material You): su Android 12+ il sistema
 * estrae una palette dallo sfondo scelto dall'utente e Material la mappa sui ruoli
 * M3 (colorPrimary, colorSurfaceContainer, ...). L'app eredita quindi i colori del
 * telefono senza che nessun layout debba saperlo.
 *
 * Funziona solo perche' temi e stili non nominano mai un colore fisso ma sempre un
 * ruolo (?attr/colorPrimary): i valori in colors.xml restano come fallback per
 * Android 11 e precedenti, dove i colori dinamici non esistono.
 *
 * Va registrata nel manifest con android:name=".PassiApplication", altrimenti Android
 * istanzia la Application di default e questo codice non viene mai eseguito.
 */
class PassiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // no-op sotto Android 12: il tema statico resta quello di themes.xml
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
