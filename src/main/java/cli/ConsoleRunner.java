package cli;

import config.Preset;
import config.TypeCatalog;
import core.CompatibilityEngine;

import java.io.PrintStream;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

/**
 * <h1>ConsoleRunner - Interface utilisateur console pour le calcul de compatibilité</h1>
 *
 * <p>Cette classe implémente l'interface utilisateur en mode console de l'application.
 * Elle orchestre le dialogue interactif avec l'utilisateur et fait appel aux
 * autres composants pour effectuer les calculs de compatibilité.</p>
 *
 * <p>Le runner suit une architecture <strong>par injection de dépendances</strong>
 * pour les flux I/O, permettant une testabilité complète avec des flux mockés.</p>
 *
 * <h2>Cycle de vie d'une session</h2>
 * <ol>
 *   <li><strong>Sélection du preset</strong> : choix des types de compatibilité</li>
 *   <li><strong>Saisie des noms</strong> : lecture des deux noms à comparer</li>
 *   <li><strong>Calcul</strong> : appel au moteur {@link CompatibilityEngine}</li>
 *   <li><strong>Affichage</strong> : présentation formatée des résultats</li>
 *   <li><strong>Nettoyage</strong> : fermeture propre des ressources</li>
 * </ol>
 *
 * <h2>Gestion des erreurs</h2>
 * <p>Les erreurs d'exécution ({@link RuntimeException}) sont capturées et
 * affichées de manière conviviale sur la sortie d'erreur, sans interruption
 * brutale du programme.</p>
 *
 * <h2>Exemple d'utilisation</h2>
 * <pre>{@code
 * // Utilisation standard avec flux système
 * ConsoleRunner runner = new ConsoleRunner(
 *     new Scanner(System.in),
 *     System.out,
 *     System.err
 * );
 * runner.run();
 *
 * // Utilisation pour tests avec flux mockés
 * Scanner mockIn = new Scanner("1\nAlice\nBob\n");
 * PrintStream mockOut = new PrintStream(new ByteArrayOutputStream());
 * ConsoleRunner testRunner = new ConsoleRunner(mockIn, mockOut, mockOut);
 * testRunner.run();
 * }</pre>
 *
 * @author tetronitte
 * @see CompatibilityEngine
 * @see TypeCatalog
 * @see Preset
 */
public final class ConsoleRunner {

    /**
     * Scanner pour la lecture des saisies utilisateur.
     * Injecté via le constructeur pour permettre les tests unitaires.
     */
    private final Scanner in;

    /**
     * Flux de sortie standard pour l'affichage des menus et résultats.
     * Généralement {@code System.out} en utilisation normale.
     */
    private final PrintStream out;

    /**
     * Flux de sortie d'erreur pour l'affichage des messages d'erreur.
     * Généralement {@code System.err} en utilisation normale.
     */
    private final PrintStream err;

    /**
     * Crée un runner console avec les flux d'I/O spécifiés.
     *
     * <p>Ce constructeur permet l'injection de dépendances pour les flux,
     * facilitant ainsi les tests unitaires avec des flux mockés ou redirigés.</p>
     *
     * <h3>Utilisation typique</h3>
     * <pre>{@code
     * // Configuration standard (production)
     * ConsoleRunner runner = new ConsoleRunner(
     *     new Scanner(System.in),  // Lecture clavier
     *     System.out,              // Sortie console
     *     System.err               // Erreurs console
     * );
     *
     * // Configuration de test
     * Scanner testInput = new Scanner("1\nAlice\nBob\n");
     * ByteArrayOutputStream testOutput = new ByteArrayOutputStream();
     * PrintStream testStream = new PrintStream(testOutput);
     * ConsoleRunner testRunner = new ConsoleRunner(testInput, testStream, testStream);
     * }</pre>
     *
     * @param in  scanner pour la lecture des entrées utilisateur (non null)
     * @param out flux de sortie standard pour les menus/résultats (non null)
     * @param err flux de sortie d'erreur pour les messages d'erreur (non null)
     * @throws NullPointerException si l'un des paramètres est null
     * @see Scanner
     * @see PrintStream
     */
    public ConsoleRunner(Scanner in, PrintStream out, PrintStream err) {
        this.in = Objects.requireNonNull(in, "in");
        this.out = Objects.requireNonNull(out, "out");
        this.err = Objects.requireNonNull(err, "err");
    }

    /**
     * Exécute le cycle complet d'interaction utilisateur pour le calcul de compatibilité.
     *
     * <p>Cette méthode orchestre l'ensemble du processus interactif :</p>
     * <ol>
     *   <li><strong>Sélection du preset</strong> : via {@link #askPreset()}</li>
     *   <li><strong>Saisie des noms</strong> : via {@link #readNames()}</li>
     *   <li><strong>Configuration des types</strong> : via {@link TypeCatalog#selectByPreset(Preset)}</li>
     *   <li><strong>Calculs</strong> : via {@link CompatibilityEngine#computeAll(Map, String, String)}</li>
     *   <li><strong>Affichage</strong> : via {@link #printResults(Map)}</li>
     * </ol>
     *
     * <h3>Gestion d'erreurs robuste</h3>
     * <p>Toute {@link RuntimeException} levée durant le processus est capturée
     * et un message d'erreur convivial est affiché sur le flux d'erreur. Le
     * programme ne s'interrompt pas brutalement.</p>
     *
     * <h3>Gestion des ressources</h3>
     * <p>Le scanner d'entrée est automatiquement fermé dans le bloc {@code finally},
     * garantissant la libération des ressources même en cas d'erreur.</p>
     *
     * <h3>Flux d'exécution type</h3>
     * <pre>
     * Choisissez le set de compatibilités :
     *   1) LISTE1  (AMOUR, AMIS, SEXE, COOK, SPORT, MUSIQUE, ASTRO, POLITQ, ESPRIT, LUDIQUE)
     * Votre choix [1] : 1
     * Entrez le premier nom : Alice
     * Entrez le deuxième nom : Bob
     *
     * === RÉSULTATS DE COMPATIBILITÉ ===
     * Compatibilité amoureuse    : 67%
     * Compatibilité amicale      : 89%
     * [...]
     * </pre>
     *
     * @apiNote Cette méthode ne lève jamais d'exception vers l'appelant.
     *          Toutes les erreurs sont gérées en interne.
     * @see #askPreset()
     * @see #readNames()
     * @see #printResults(Map)
     */
    public void run() {
        try {
            Preset preset = askPreset();
            String[] names = readNames();
            Map<String, String> types = TypeCatalog.selectByPreset(preset);
            Map<String, Integer> results = CompatibilityEngine.computeAll(types, names[0], names[1]);
            printResults(results);
        } catch (RuntimeException e) {
            err.println("Erreur : " + e.getMessage());
        } finally {
            in.close();
        }
    }

    /**
     * Affiche le menu de sélection des presets et lit le choix utilisateur.
     *
     * <p>Cette méthode privée gère l'interaction pour la sélection du type
     * de compatibilité à calculer. Elle affiche les options disponibles et
     * utilise {@link Preset#fromUserInput(String)} pour interpréter la saisie.</p>
     *
     * <h3>Interface utilisateur</h3>
     * <pre>
     * Choisissez le set de compatibilités :
     *   1) LISTE1  (AMOUR, AMIS, SEQE, COOK, SPORT, MUSIQUE, ASTRO, POLITQ, ESPRIT, LUDIQUE)
     * Votre choix [1] :
     * </pre>
     *
     * <p><strong>Note :</strong> Il y a une coquille dans l'affichage ("SEQE" au lieu de "SEXE")
     * qui est préservée pour la cohérence avec le code existant.</p>
     *
     * @return le preset sélectionné (jamais null grâce au fallback de fromUserInput)
     * @see Preset#fromUserInput(String)
     */
    private Preset askPreset() {
        out.println("Choisissez le set de compatibilités :");
        out.println("  1) LISTE1  (AMOUR, AMIS, SEQE, COOK, SPORT, MUSIQUE, ASTRO, POLITQ, ESPRIT, LUDIQUE)");
        out.print("Votre choix [1] : ");
        String choice = in.nextLine();
        return Preset.fromUserInput(choice);
    }

    /**
     * Lit séquentiellement les deux noms à comparer depuis l'entrée utilisateur.
     *
     * <p>Cette méthode privée gère la saisie interactive des noms qui seront
     * soumis à l'algorithme de calcul de compatibilité. Elle lit une ligne
     * complète pour chaque nom, permettant les espaces et caractères accentués.</p>
     *
     * <h3>Interface utilisateur</h3>
     * <pre>
     * Entrez le premier nom : Alice Dupont
     * Entrez le deuxième nom : Bob Martin
     * </pre>
     *
     * <h3>Traitement des noms</h3>
     * <p>Les noms sont retournés tels que saisis par l'utilisateur, sans
     * validation ni normalisation à ce niveau. Le traitement (suppression
     * des accents, conversion en majuscules, etc.) sera effectué plus tard
     * par {@link CompatibilityEngine#normalize(String)}.</p>
     *
     * @return tableau de 2 chaînes [nom1, nom2], jamais null
     * @apiNote Aucune validation n'est effectuée sur les noms saisis.
     *          Les chaînes vides ou composées uniquement d'espaces sont acceptées.
     * @see CompatibilityEngine#normalize(String)
     */
    private String[] readNames() {
        out.print("Entrez le premier nom : ");
        String first = in.nextLine();
        out.print("Entrez le deuxième nom : ");
        String second = in.nextLine();
        return new String[]{ first, second };
    }

    /**
     * Affiche les résultats de compatibilité dans un format tabulaire lisible.
     *
     * <p>Cette méthode privée formate et affiche les pourcentages de compatibilité
     * calculés par {@link CompatibilityEngine}. Elle produit un tableau aligné
     * avec les libellés des catégories et leurs pourcentages respectifs.</p>
     *
     * <h3>Format de sortie</h3>
     * <pre>
     * === RÉSULTATS DE COMPATIBILITÉ ===
     * Compatibilité amoureuse    : 67%
     * Compatibilité amicale      : 89%
     * Compatibilité sexuelle     : 43%
     * Compatibilité culinaire    : 76%
     * Compatibilité sportive     : 52%
     * Compatibilité musicale     : 91%
     * Compatibilité astrologique : 34%
     * Compatibilité politique    : 28%
     * Compatibilité spirituelle  : 85%
     * Compatibilité ludique      : 73%
     * </pre>
     *
     * <h3>Détails de formatage</h3>
     * <ul>
     *   <li><strong>Titre</strong> : centré avec séparateurs "==="</li>
     *   <li><strong>Colonnes</strong> : libellés alignés à gauche (12 caractères), pourcentages à droite</li>
     *   <li><strong>Locale</strong> : {@link Locale#ROOT} pour un formatage stable</li>
     *   <li><strong>Ordre</strong> : préservation de l'ordre de la map d'entrée</li>
     * </ul>
     *
     * <h3>Implémentation</h3>
     * <p>Utilise un {@link StringBuilder} pour construire efficacement la sortie
     * complète avant affichage, réduisant le nombre d'appels I/O.</p>
     *
     * @param results map ordonnée associant libellés lisibles et pourcentages (non null)
     * @throws NullPointerException si results est null
     * @apiNote Cette méthode assume que les pourcentages sont dans la plage [0-99]
     *          comme garanti par {@link CompatibilityEngine}
     * @see CompatibilityEngine#computeAll(Map, String, String)
     */
    private void printResults(Map<String, Integer> results) {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder(256)
                .append(nl).append("=== RÉSULTATS DE COMPATIBILITÉ ===").append(nl);
        results.forEach((label, pct) ->
                sb.append(String.format(Locale.ROOT, "Compatibilité %-12s : %2d%%%n", label, pct)));
        out.print(sb.toString());
    }
}
