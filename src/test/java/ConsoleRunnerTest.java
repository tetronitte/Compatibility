import cli.ConsoleRunner;
import core.CompatibilityEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <h1>Tests d’intégration console : {@code ConsoleRunner}</h1>
 *
 * <p>
 * Simule les flux d’entrée/sortie de la console pour tester le cycle complet :
 * choix du preset, saisie des noms, calcul via {@code CompatibilityEngine}
 * et rendu formaté du résultat.
 * </p>
 *
 * <h2>Pourquoi simuler l’I/O ?</h2>
 * <ul>
 *   <li>Rendre les tests <em>déterministes</em> (pas d’attente utilisateur).</li>
 *   <li>Éviter les dépendances à l’environnement d’exécution (pipeline CI, etc.).</li>
 * </ul>
 *
 * @see ConsoleRunner
 * @see CompatibilityEngine
 */
@DisplayName("ConsoleRunner – intégration console (I/O simulés)")
class ConsoleRunnerTest {

    /**
     * Vérifie le flux nominal :
     * <ol>
     *   <li>Choix du preset LISTE1 ;</li>
     *   <li>Saisie de "John Doe" et "Jane Doe" ;</li>
     *   <li>Affichage d’une ligne contenant "amoureuse" et "19%".</li>
     * </ol>
     *
     * @implNote Le test se contente de vérifier la présence de fragments
     *           (pas l’intégralité du formatage) afin de rester robuste.
     */
    @Test
    @DisplayName("Flux complet : preset 1, 'John Doe' + 'Jane Doe' → contient 'amoureuse : 19%'")
    void run_full_flow_contains_expected_line() {
        // Entrée simulée : "1" (LISTE1) puis les deux noms, avec fins de lignes.
        String input = String.join(System.lineSeparator(), "1", "John Doe", "Jane Doe") + System.lineSeparator();
        ByteArrayInputStream inBytes = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        ByteArrayOutputStream errBytes = new ByteArrayOutputStream();

        ConsoleRunner runner = new ConsoleRunner(new java.util.Scanner(inBytes),
                new PrintStream(outBytes, true, StandardCharsets.UTF_8),
                new PrintStream(errBytes, true, StandardCharsets.UTF_8));

        runner.run();

        String out = outBytes.toString(StandardCharsets.UTF_8);
        assertTrue(out.contains("Compatibilité amoureuse"), "entête manquante");
        assertTrue(out.contains("19%"), "amoureuse attendue à 19%");
        assertEquals(0, errBytes.size(), "stderr devrait être vide");
    }
}
