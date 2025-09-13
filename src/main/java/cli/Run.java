package cli;

import config.Preset;
import config.TypeCatalog;
import core.CompatibilityEngine;

import java.util.Scanner;

/**
 * <h1>Point d'entrée de l'application de calcul de compatibilité</h1>
 *
 * <p>Cette classe suit le pattern <strong>Runner/Launcher</strong> : elle ne
 * contient aucune logique métier, seulement l'amorçage de l'IHM console.</p>
 *
 * <p>L'application permet de calculer des pourcentages de compatibilité entre
 * deux noms selon différentes catégories (amoureuse, amicale, sexuelle, etc.),
 * en utilisant l'algorithme implémenté dans {@link CompatibilityEngine}.</p>
 *
 * <h2>Architecture</h2>
 * <p>Cette classe fait partie d'une architecture en couches :</p>
 * <ul>
 *   <li><strong>Présentation</strong> : {@link Run} (point d'entrée) + {@link ConsoleRunner} (IHM)</li>
 *   <li><strong>Métier</strong> : {@link CompatibilityEngine} (algorithme de calcul)</li>
 *   <li><strong>Configuration</strong> : {@link Preset} + {@link TypeCatalog} (données de référence)</li>
 * </ul>
 *
 * <h2>Exemple d'utilisation</h2>
 * <pre>{@code
 * // Lancement direct via IDE ou ligne de commande
 * java Run
 *
 * // L'application demande interactivement :
 * // 1. Le choix du preset (types de compatibilité)
 * // 2. Le premier nom
 * // 3. Le second nom
 * // Puis affiche les résultats calculés
 * }</pre>
 *
 * @author tetronitte
 * @see ConsoleRunner
 * @see CompatibilityEngine
 */
public final class Run {

    /**
     * Constructeur privé pour empêcher l'instanciation.
     * Cette classe ne contient que des méthodes statiques.
     */
    private Run() {}

    /**
     * Lance l'application console interactive de calcul de compatibilité.
     *
     * <p>Cette méthode initialise un {@link ConsoleRunner} avec les flux
     * standards du système (entrée, sortie, erreur) et démarre le cycle
     * interactif complet :</p>
     * <ol>
     *   <li>Demande du choix de preset à l'utilisateur</li>
     *   <li>Saisie des deux noms à comparer</li>
     *   <li>Calcul des compatibilités via {@link CompatibilityEngine}</li>
     *   <li>Affichage formaté des résultats</li>
     * </ol>
     *
     * <p><strong>Gestion d'erreurs :</strong> Les erreurs d'exécution sont
     * capturées par le {@link ConsoleRunner} et affichées sur la sortie d'erreur.</p>
     *
     * <h3>Flux utilisés</h3>
     * <ul>
     *   <li><code>System.in</code> : lecture des saisies utilisateur</li>
     *   <li><code>System.out</code> : affichage des menus et résultats</li>
     *   <li><code>System.err</code> : affichage des messages d'erreur</li>
     * </ul>
     *
     * @param args arguments de ligne de commande (non utilisés dans cette version)
     * @apiNote Cette méthode ne retourne jamais d'exception, toute erreur
     *          est gérée en interne par le {@link ConsoleRunner}
     * @see ConsoleRunner#run()
     */
    public static void main(String[] args) {
        ConsoleRunner runner = new ConsoleRunner(new Scanner(System.in), System.out, System.err);
        runner.run();
    }
}
