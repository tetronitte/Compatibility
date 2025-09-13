package config;

import core.CompatibilityEngine;

import java.util.*;

/**
 * <h1>TypeCatalog - Catalogue maître des types de compatibilité</h1>
 *
 * <p>Classe utilitaire qui maintient le dictionnaire de référence associant
 * les clés techniques (utilisées par l'algorithme) aux libellés lisibles
 * (affichés à l'utilisateur).</p>
 *
 * <p>Cette classe sert de <strong>source unique de vérité</strong> pour la
 * correspondance entre identifiants techniques et noms d'affichage, et fournit
 * des méthodes pour extraire des sous-ensembles selon les {@link Preset}.</p>
 *
 * <h2>Architecture</h2>
 * <ul>
 *   <li><strong>Dictionnaire maître</strong> : {@link #MASTER_TYPES} (immuable, complet)</li>
 *   <li><strong>Sélection dynamique</strong> : {@link #selectByPreset(Preset)} (sous-ensembles ordonnés)</li>
 *   <li><strong>Validation</strong> : vérification de cohérence avec les presets</li>
 * </ul>
 *
 * <h2>Format des données</h2>
 * <ul>
 *   <li><strong>Clés</strong> : identifiants techniques en MAJUSCULES (ex: "AMOUR", "POLITQ")</li>
 *   <li><strong>Libellés</strong> : noms d'affichage en minuscules (ex: "amoureuse", "politique")</li>
 * </ul>
 *
 * <h2>Exemple d'utilisation</h2>
 * <pre>{@code
 * // Sélection d'un sous-ensemble ordonné
 * Map<String, String> types = TypeCatalog.selectByPreset(Preset.LISTE1);
 *
 * // Utilisation avec le moteur de calcul
 * Map<String, Integer> results = CompatibilityEngine.computeAll(types, "Alice", "Bob");
 *
 * // Accès direct au catalogue complet (lecture seule)
 * String label = TypeCatalog.MASTER_TYPES.get("AMOUR"); // → "amoureuse"
 * }</pre>
 *
 * @author tetronitte
 * @see Preset
 * @see CompatibilityEngine
 */
public final class TypeCatalog {

    /**
     * Constructeur privé pour empêcher l'instanciation.
     * Cette classe ne contient que des méthodes et constantes statiques.
     */
    private TypeCatalog() {}

    /**
     * Dictionnaire maître immuable associant clés techniques et libellés d'affichage.
     *
     * <p>Cette map constitue la <strong>source unique de vérité</strong> pour tous
     * les types de compatibilité supportés par l'application. Elle est :</p>
     * <ul>
     *   <li><strong>Immuable</strong> : aucune modification possible après initialisation</li>
     *   <li><strong>Ordonnée</strong> : utilise une {@link LinkedHashMap} pour préserver l'ordre d'insertion</li>
     *   <li><strong>Complète</strong> : contient tous les types définis dans les presets</li>
     * </ul>
     *
     * <h3>Structure des données</h3>
     * <table border="1">
     *   <tr><th>Clé technique</th><th>Libellé d'affichage</th></tr>
     *   <tr><td>AMOUR</td><td>amoureuse</td></tr>
     *   <tr><td>AMIS</td><td>amicale</td></tr>
     *   <tr><td>SEXE</td><td>sexuelle</td></tr>
     *   <tr><td>COOK</td><td>culinaire</td></tr>
     *   <tr><td>SPORT</td><td>sportive</td></tr>
     *   <tr><td>MUSIQUE</td><td>musicale</td></tr>
     *   <tr><td>ASTRO</td><td>astrologique</td></tr>
     *   <tr><td>POLITQ</td><td>politique</td></tr>
     *   <tr><td>ESPRIT</td><td>spirituelle</td></tr>
     *   <tr><td>LUDIQUE</td><td>ludique</td></tr>
     * </table>
     *
     * @apiNote Cette constante est thread-safe et peut être accédée
     *          concurremment sans synchronisation
     * @see #selectByPreset(Preset)
     */
    public static final Map<String, String> MASTER_TYPES = buildMaster();

    /**
     * Construit et initialise le dictionnaire maître des types.
     *
     * <p>Cette méthode factory privée est appelée une seule fois lors de
     * l'initialisation de la classe pour construire {@link #MASTER_TYPES}.</p>
     *
     * <h3>Implémentation</h3>
     * <ol>
     *   <li>Création d'une {@link LinkedHashMap} mutable</li>
     *   <li>Population avec tous les types supportés</li>
     *   <li>Retour d'une vue immuable via {@link Collections#unmodifiableMap}</li>
     * </ol>
     *
     * @return map immuable et ordonnée des types de compatibilité
     * @apiNote Cette méthode est appelée automatiquement au chargement de la classe
     */
    private static Map<String, String> buildMaster() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("AMOUR",   "amoureuse");
        m.put("AMIS",    "amicale");
        m.put("SEXE",    "sexuelle");
        m.put("COOK",    "culinaire");
        m.put("SPORT",   "sportive");
        m.put("MUSIQUE", "musicale");
        m.put("ASTRO",   "astrologique");
        m.put("POLITQ",  "politique");
        m.put("ESPRIT",  "spirituelle");
        m.put("LUDIQUE", "ludique");
        return Collections.unmodifiableMap(m);
    }

    /**
     * Sélectionne un sous-ensemble ordonné du catalogue maître selon un preset.
     *
     * <p>Cette méthode extrait du {@link #MASTER_TYPES} uniquement les entrées
     * correspondant aux clés définies dans le preset donné, en préservant
     * l'ordre spécifié par le preset.</p>
     *
     * <h3>Processus de sélection</h3>
     * <ol>
     *   <li>Itération sur les clés du preset dans l'ordre défini</li>
     *   <li>Normalisation de chaque clé (conversion en majuscules)</li>
     *   <li>Recherche de la clé dans le dictionnaire maître</li>
     *   <li>Ajout de l'entrée (clé, libellé) dans la map de résultat</li>
     *   <li>Retour d'une vue immuable du résultat</li>
     * </ol>
     *
     * <h3>Gestion des erreurs</h3>
     * <p>Si une clé du preset n'existe pas dans le dictionnaire maître,
     * une {@link IllegalArgumentException} est levée avec un message explicite.</p>
     *
     * <h3>Exemples</h3>
     * <pre>{@code
     * // Sélection complète de LISTE1 (10 types)
     * Map<String, String> types = TypeCatalog.selectByPreset(Preset.LISTE1);
     * // types.size() == 10
     * // types.get("AMOUR") == "amoureuse"
     *
     * // L'ordre est préservé selon le preset
     * String[] orderedLabels = types.values().toArray(new String[0]);
     * // orderedLabels[0] == "amoureuse" (premier dans LISTE1)
     * }</pre>
     *
     * @param preset le preset souhaité (non null)
     * @return une {@link LinkedHashMap} immuable associant clés techniques
     *         et libellés lisibles, dans l'ordre du preset
     * @throws NullPointerException si preset est null
     * @throws IllegalArgumentException si le preset contient une clé inconnue
     * @apiNote La map retournée est immuable et thread-safe
     * @see Preset#keys()
     * @see #MASTER_TYPES
     */
    public static Map<String, String> selectByPreset(Preset preset) {
        Objects.requireNonNull(preset, "preset");
        LinkedHashMap<String, String> out = new LinkedHashMap<>();
        for (String rawKey : preset.keys()) {
            String key = rawKey.toUpperCase(Locale.ROOT);
            String label = MASTER_TYPES.get(key);
            if (label == null) {
                throw new IllegalArgumentException("Type inconnu dans le preset: " + rawKey);
            }
            out.put(key, label);
        }
        return Collections.unmodifiableMap(out);
    }
}
