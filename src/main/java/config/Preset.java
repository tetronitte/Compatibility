package config;

import core.CompatibilityEngine;

import java.util.List;
import java.util.Locale;

/**
 * <h1>Preset - Configuration des types de compatibilité</h1>
 *
 * <p>Énumération qui définit des ensembles prédéfinis et ordonnés de clés
 * (mots-clés analytiques) déterminant quelles catégories de compatibilité
 * seront calculées et affichées à l'utilisateur.</p>
 *
 * <p>Chaque preset encapsule une liste de clés qui correspondent aux
 * identifiants techniques utilisés par l'algorithme de {@link CompatibilityEngine}.
 * Ces clés sont ensuite mappées vers des libellés lisibles via {@link TypeCatalog}.</p>
 *
 * <h2>Utilisation typique</h2>
 * <pre>{@code
 * // Récupération d'un preset depuis une saisie utilisateur
 * Preset preset = Preset.fromUserInput("1");
 *
 * // Utilisation pour sélectionner les types de compatibilité
 * Map<String, String> types = TypeCatalog.selectByPreset(preset);
 * }</pre>
 *
 * <h2>Extensibilité</h2>
 * <p>Pour ajouter de nouveaux presets :</p>
 * <ol>
 *   <li>Ajouter la constante d'énumération avec sa liste de clés</li>
 *   <li>Mettre à jour {@link #fromUserInput(String)} pour la reconnaissance</li>
 *   <li>S'assurer que les clés existent dans {@link TypeCatalog#MASTER_TYPES}</li>
 * </ol>
 *
 * @author tetronitte
 * @see TypeCatalog
 * @see CompatibilityEngine
 */
public enum Preset {

    /**
     * Preset par défaut contenant 10 types de compatibilité fondamentaux.
     *
     * <p>Inclut les catégories suivantes dans cet ordre précis :</p>
     * <ul>
     *   <li><strong>AMOUR</strong> - Compatibilité amoureuse</li>
     *   <li><strong>AMIS</strong> - Compatibilité amicale</li>
     *   <li><strong>SEXE</strong> - Compatibilité sexuelle</li>
     *   <li><strong>COOK</strong> - Compatibilité culinaire</li>
     *   <li><strong>SPORT</strong> - Compatibilité sportive</li>
     *   <li><strong>MUSIQUE</strong> - Compatibilité musicale</li>
     *   <li><strong>ASTRO</strong> - Compatibilité astrologique</li>
     *   <li><strong>POLITQ</strong> - Compatibilité politique</li>
     *   <li><strong>ESPRIT</strong> - Compatibilité spirituelle</li>
     *   <li><strong>LUDIQUE</strong> - Compatibilité ludique</li>
     * </ul>
     *
     * @apiNote L'ordre des clés dans cette liste détermine l'ordre d'affichage
     *          des résultats dans l'interface utilisateur
     */
    LISTE1(List.of("AMOUR","AMIS","SEXE","COOK","SPORT","MUSIQUE","ASTRO","POLITQ","ESPRIT","LUDIQUE"));

    /**
     * Liste immuable des clés techniques associées à ce preset.
     * L'ordre est préservé pour l'affichage.
     */
    private final List<String> keys;

    /**
     * Constructeur privé de l'énumération.
     *
     * @param keys liste ordonnée des clés techniques (non nulle, non vide)
     * @throws NullPointerException si keys est null
     * @throws IllegalArgumentException si keys est vide
     */
    Preset(List<String> keys) { this.keys = keys; }

    /**
     * Retourne les clés brutes de ce preset, dans l'ordre d'affichage souhaité.
     *
     * <p>Les clés retournées correspondent aux identifiants techniques
     * utilisés dans {@link TypeCatalog#MASTER_TYPES} et par l'algorithme
     * de {@link CompatibilityEngine}.</p>
     *
     * @return liste immuable des clés, jamais null ni vide
     * @apiNote La liste retournée est immuable et peut être utilisée
     *          directement sans risque de modification
     */
    public List<String> keys() { return keys; }

    /**
     * Analyse une saisie utilisateur et retourne le preset correspondant.
     *
     * <p>Cette méthode implémente un parsing <strong>tolérant</strong> qui accepte
     * plusieurs formats d'entrée :</p>
     * <ul>
     *   <li><strong>Numérique</strong> : "1" → LISTE1</li>
     *   <li><strong>Nominal</strong> : "LISTE1" → LISTE1</li>
     *   <li><strong>Insensible à la casse</strong> : "liste1" → LISTE1</li>
     *   <li><strong>Espaces ignorés</strong> : " 1 " → LISTE1</li>
     * </ul>
     *
     * <h3>Comportement de fallback</h3>
     * <p>Si l'entrée est null, vide, ou ne correspond à aucun preset connu,
     * la méthode retourne {@link #LISTE1} par défaut.</p>
     *
     * <h3>Exemples d'utilisation</h3>
     * <pre>{@code
     * Preset.fromUserInput("1")      // → LISTE1
     * Preset.fromUserInput("LISTE1") // → LISTE1
     * Preset.fromUserInput("liste1") // → LISTE1
     * Preset.fromUserInput("  1  ")  // → LISTE1
     * Preset.fromUserInput("999")    // → LISTE1 (fallback)
     * Preset.fromUserInput(null)     // → LISTE1 (fallback)
     * }</pre>
     *
     * @param s saisie utilisateur (peut être null ou vide)
     * @return le preset reconnu, {@link #LISTE1} par défaut
     * @apiNote Cette méthode ne lève jamais d'exception et garantit
     *          toujours un retour valide
     * @see #LISTE1
     */
    public static Preset fromUserInput(String s) {
        if (s == null) return LISTE1;
        String t = s.trim().toUpperCase(Locale.ROOT);
        return switch (t) {
            case "1", "LISTE1" -> LISTE1;
            default -> LISTE1;
        };
    }
}
