package core;

import config.Preset;
import config.TypeCatalog;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

/**
 * <h1>CompatibilityEngine - Moteur de calcul de compatibilité</h1>
 *
 * <p>Cœur algorithmique de l'application, cette classe fournit des méthodes
 * <strong>pures et testables</strong> pour calculer des pourcentages de compatibilité
 * entre deux noms selon différentes catégories. L'algorithme se base sur l'analyse
 * des occurrences de lettres et leur réduction itérative.</p>
 *
 * <p>Cette classe suit les principes de <strong>programmation fonctionnelle</strong> :
 * aucun état mutable, pas d'effets de bord, aucune I/O. Toutes les méthodes sont
 * statiques et thread-safe.</p>
 *
 * <h2>Principe algorithmique</h2>
 * <ol>
 *   <li><strong>Normalisation</strong> : suppression des accents, conversion majuscules</li>
 *   <li><strong>Comptage</strong> : décompte des occurrences de lettres A-Z</li>
 *   <li><strong>Génération</strong> : extraction des occurrences pour chaque mot-clé</li>
 *   <li><strong>Réduction itérative</strong> : compression de la liste par additions successives</li>
 *   <li><strong>Composition finale</strong> : calcul du pourcentage à partir des 2 dernières valeurs</li>
 * </ol>
 *
 * <h2>Usage typique</h2>
 * <pre>{@code
 * // Configuration des types de compatibilité
 * Map<String, String> types = TypeCatalog.selectByPreset(Preset.LISTE1);
 *
 * // Calcul pour deux noms
 * Map<String, Integer> results = CompatibilityEngine.computeAll(types, "Alice", "Bob");
 *
 * // Résultats : {"amoureuse" -> 67, "amicale" -> 89, "sexuelle" -> 43, ...}
 * }</pre>
 *
 * <h2>Exemple détaillé de calcul</h2>
 * <pre>{@code
 * // Étape 1 : Normalisation
 * String name1 = normalize("Éléonore");  // → "ELEONORE"
 * String name2 = normalize("François");  // → "FRANCOIS"
 *
 * // Étape 2 : Comptage global
 * int[] counts = countLetterOccurrencesArray("ELEONOREFRANCOIS");
 * // counts[0] = 2 (A apparaît 2 fois), counts[4] = 3 (E apparaît 3 fois), etc.
 *
 * // Étape 3 : Pour le mot-clé "AMOUR"
 * List<Integer> occurrences = generateOccurrenceList("AMOUR", counts);
 * // occurrences = [2, 1, 3, 1, 2] pour A-M-O-U-R
 *
 * // Étape 4 : Réduction itérative
 * // [2,1,3,1,2] → [3,4,3] → [7,7] → composition finale = 77%
 * int percentage = calculateCompatibility(occurrences);
 * }</pre>
 *
 * <h2>Constantes algorithmiques</h2>
 * <ul>
 *   <li><strong>MAX_DIGIT (10)</strong> : seuil de division pour la réduction</li>
 *   <li><strong>HARD_MAX_ITERS (32)</strong> : limite d'itérations pour éviter les boucles infinies</li>
 *   <li><strong>HEAD_LIMIT (128)</strong> : taille maximale des listes intermédiaires</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>Cette classe est complètement <strong>thread-safe</strong> : toutes les méthodes
 * sont statiques, ne modifient aucun état partagé et n'utilisent que des variables
 * locales ou des paramètres immutables.</p>
 *
 * @author tetronitte
 * @see TypeCatalog
 * @see Preset
 */
public final class CompatibilityEngine {

    // ===== Constantes algorithmiques =====

    /**
     * Seuil de division pour la réduction des listes.
     * Les sommes ≥ MAX_DIGIT sont divisées en dizaine et unité.
     */
    private static final int MAX_DIGIT = 10;

    /**
     * Nombre maximum d'itérations pour l'algorithme de réduction.
     * Protège contre les boucles infinies théoriques.
     */
    private static final int HARD_MAX_ITERS = 32;

    /**
     * Taille maximale des listes intermédiaires lors de la réduction.
     * Optimise la mémoire et les performances.
     */
    private static final int HEAD_LIMIT = 128;

    // ===== Outils de normalisation =====

    /**
     * Pattern regex pour supprimer les diacritiques (accents).
     * Utilise la classe Unicode "Combining Diacritical Marks".
     */
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    /**
     * Constructeur privé pour empêcher l'instanciation.
     * Cette classe ne contient que des méthodes utilitaires statiques.
     */
    private CompatibilityEngine() {}

    // ---------------------------------------------------------------------
    // API publique
    // ---------------------------------------------------------------------

    /**
     * Calcule tous les pourcentages de compatibilité pour le dictionnaire de types fourni.
     *
     * <p>Cette méthode constitue le <strong>point d'entrée principal</strong> de l'algorithme.
     * Elle orchestre l'ensemble du processus : normalisation des noms, comptage des lettres,
     * et calcul des pourcentages pour chaque type de compatibilité.</p>
     *
     * <h3>Processus détaillé</h3>
     * <ol>
     *   <li><strong>Validation</strong> : vérification de la non-nullité des paramètres</li>
     *   <li><strong>Normalisation</strong> : traitement des deux noms via {@link #normalize(String)}</li>
     *   <li><strong>Concaténation</strong> : fusion des noms normalisés pour le comptage global</li>
     *   <li><strong>Comptage</strong> : analyse des occurrences via {@link #countLetterOccurrencesArray(String)}</li>
     *   <li><strong>Calcul par type</strong> : pour chaque clé du dictionnaire :
     *     <ul>
     *       <li>Génération de la liste d'occurrences via {@link #generateOccurrenceList(String, int[])}</li>
     *       <li>Calcul du pourcentage via {@link #calculateCompatibility(List)}</li>
     *       <li>Stockage avec le libellé lisible comme clé</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * <h3>Exemple d'utilisation</h3>
     * <pre>{@code
     * // Configuration des types
     * Map<String, String> types = Map.of(
     *     "AMOUR", "amoureuse",
     *     "AMIS", "amicale",
     *     "SEXE", "sexuelle"
     * );
     *
     * // Calcul
     * Map<String, Integer> results = CompatibilityEngine.computeAll(types, "Alice", "Bob");
     *
     * // Résultat possible :
     * // {"amoureuse" -> 67, "amicale" -> 89, "sexuelle" -> 43}
     * }</pre>
     *
     * <h3>Préservation de l'ordre</h3>
     * <p>L'ordre d'itération du dictionnaire d'entrée est préservé dans le résultat
     * grâce à l'utilisation d'une {@link LinkedHashMap}. Ceci permet un affichage
     * déterministe des résultats.</p>
     *
     * @param types  dictionnaire associant clés analytiques et libellés lisibles.
     *               L'ordre d'itération détermine l'ordre d'affichage final (non null)
     * @param name1  premier nom brut, peut contenir accents, espaces, caractères spéciaux (non null)
     * @param name2  second nom brut (non null)
     * @return une {@link LinkedHashMap} associant libellés lisibles et pourcentages calculés.
     *         L'ordre correspond à celui du dictionnaire d'entrée. Jamais null.
     * @throws NullPointerException si l'un des arguments est null
     * @apiNote Cette méthode est thread-safe et peut être appelée concurremment
     * @see #normalize(String)
     * @see #countLetterOccurrencesArray(String)
     * @see #generateOccurrenceList(String, int[])
     * @see #calculateCompatibility(List)
     */
    public static Map<String, Integer> computeAll(Map<String, String> types, String name1, String name2) {
        Objects.requireNonNull(types, "types");
        Objects.requireNonNull(name1, "name1");
        Objects.requireNonNull(name2, "name2");

        String n1 = normalize(name1);
        String n2 = normalize(name2);
        int[] counts = countLetterOccurrencesArray(n1 + n2);

        LinkedHashMap<String, Integer> out = new LinkedHashMap<>(types.size());
        for (Map.Entry<String, String> e : types.entrySet()) {
            String key = e.getKey();
            String label = e.getValue();
            List<Integer> occ = generateOccurrenceList(key, counts);
            int pct = calculateCompatibility(occ);
            out.put(label, pct);
        }
        return out;
    }

    // ---------------------------------------------------------------------
    // Normalisation & comptage
    // ---------------------------------------------------------------------

    /**
     * Normalise une chaîne pour l'analyse algorithmique.
     *
     * <p>Cette méthode applique une série de transformations pour standardiser
     * le texte d'entrée et le rendre compatible avec l'algorithme de comptage :</p>
     *
     * <h3>Étapes de normalisation</h3>
     * <ol>
     *   <li><strong>Trim</strong> : suppression des espaces en début/fin</li>
     *   <li><strong>Vérification vide</strong> : retour immédiat si chaîne vide</li>
     *   <li><strong>NFD (Canonical Decomposition)</strong> : séparation caractères/diacritiques</li>
     *   <li><strong>Suppression diacritiques</strong> : retrait des accents via regex</li>
     *   <li><strong>Majuscules</strong> : conversion avec {@link Locale#ROOT}</li>
     * </ol>
     *
     * <h3>Exemples de transformation</h3>
     * <pre>{@code
     * normalize("  Éléonore  ")     // → "ELEONORE"
     * normalize("François")         // → "FRANCOIS"
     * normalize("José-María")       // → "JOSE-MARIA"
     * normalize("123 ABC !@#")      // → "123 ABC !@#"
     * normalize("")                 // → ""
     * normalize("   ")              // → ""
     * normalize(null)               // → ""
     * }</pre>
     *
     * <h3>Gestion des caractères non-alphabétiques</h3>
     * <p>Les chiffres, espaces et caractères spéciaux sont <strong>préservés</strong>
     * mais seront ignorés lors du comptage par {@link #countLetterOccurrencesArray(String)}.</p>
     *
     * @param input chaîne à normaliser (peut être null ou vide)
     * @return chaîne normalisée (majuscules, sans accents), ou chaîne vide si l'entrée est null/vide
     * @apiNote Utilise {@link Locale#ROOT} pour garantir un comportement stable
     *          indépendant de la locale système
     * @see Normalizer.Form#NFD
     * @see #DIACRITICS
     */
    public static String normalize(String input) {
        if (input == null || (input = input.trim()).isEmpty()) return "";
        String nfd = Normalizer.normalize(input, Normalizer.Form.NFD);
        nfd = DIACRITICS.matcher(nfd).replaceAll("");
        return nfd.toUpperCase(Locale.ROOT);
    }

    /**
     * Compte les occurrences de chaque lettre A–Z dans une chaîne normalisée.
     *
     * <p>Cette méthode analyse caractère par caractère une chaîne (normalement en majuscules)
     * et compte le nombre d'apparitions de chaque lettre de l'alphabet latin.</p>
     *
     * <h3>Règles de comptage</h3>
     * <ul>
     *   <li><strong>Lettres A-Z</strong> : comptées et stockées dans le tableau</li>
     *   <li><strong>Autres caractères</strong> : ignorés (espaces, chiffres, ponctuation)</li>
     *   <li><strong>Sensibilité casse</strong> : seules les majuscules sont comptées</li>
     * </ul>
     *
     * <h3>Structure du tableau de retour</h3>
     * <p>Le tableau retourné a exactement 26 entrées avec la correspondance :</p>
     * <ul>
     *   <li>index 0 → lettre 'A'</li>
     *   <li>index 1 → lettre 'B'</li>
     *   <li>...</li>
     *   <li>index 25 → lettre 'Z'</li>
     * </ul>
     *
     * <h3>Exemples</h3>
     * <pre>{@code
     * int[] counts = countLetterOccurrencesArray("HELLO WORLD");
     * // counts[7] = 1 (H apparaît 1 fois)
     * // counts[4] = 1 (E apparaît 1 fois)
     * // counts[11] = 3 (L apparaît 3 fois)
     * // counts[14] = 2 (O apparaît 2 fois)
     * // tous les autres index = 0
     *
     * countLetterOccurrencesArray("123!@# ")   // → [0,0,0,...,0] (26 zéros)
     * countLetterOccurrencesArray("AAA")       // → [3,0,0,...,0]
     * }</pre>
     *
     * <h3>Utilisation avec normalize()</h3>
     * <p>Cette méthode est typiquement utilisée après {@link #normalize(String)} :</p>
     * <pre>{@code
     * String normalized = normalize("Éléonore François");  // → "ELEONORE FRANCOIS"
     * int[] counts = countLetterOccurrencesArray(normalized);
     * }</pre>
     *
     * @param text texte à analyser (généralement en majuscules après normalisation), non null
     * @return tableau de 26 entiers représentant les occurrences A-Z, jamais null
     * @throws NullPointerException si text est null
     * @apiNote La performance est optimisée avec une seule passe sur la chaîne
     * @see #normalize(String)
     * @see #generateOccurrenceList(String, int[])
     */
    public static int[] countLetterOccurrencesArray(String text) {
        Objects.requireNonNull(text, "text");
        int[] counts = new int[26];
        for (int i = 0, n = text.length(); i < n; i++) {
            char c = text.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                counts[c - 'A']++;
            }
        }
        return counts;
    }

    /**
     * Convertit un mot-clé analytique en liste d'occurrences basée sur le comptage global.
     *
     * <p>Cette méthode constitue le <strong>pont</strong> entre les mots-clés de compatibilité
     * (ex: "AMOUR", "SPORT") et les données numériques nécessaires à l'algorithme de calcul.
     * Elle extrait, pour chaque lettre du mot-clé, son nombre d'occurrences dans les noms combinés.</p>
     *
     * <h3>Processus de génération</h3>
     * <ol>
     *   <li>Parcours caractère par caractère du mot-clé</li>
     *   <li>Normalisation de chaque caractère (majuscule)</li>
     *   <li>Si c'est une lettre A-Z : ajout de son occurrence à la liste</li>
     *   <li>Si ce n'est pas une lettre : caractère ignoré</li>
     * </ol>
     *
     * <h3>Exemples détaillés</h3>
     * <pre>{@code
     * // Supposons counts issu de "ALICE BOB" : A=1, B=2, C=1, E=1, I=1, L=1, O=1
     * int[] counts = {1,2,1,0,1,0,0,0,1,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0};
     *                 //A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
     *
     * List<Integer> amour = generateOccurrenceList("AMOUR", counts);
     * // "AMOUR" → A=1, M=0, O=1, U=0, R=0 → [1,0,1,0,0]
     *
     * List<Integer> alice = generateOccurrenceList("ALICE", counts);
     * // "ALICE" → A=1, L=1, I=1, C=1, E=1 → [1,1,1,1,1]
     *
     * List<Integer> test123 = generateOccurrenceList("TEST123", counts);
     * // "TEST123" → T=0, E=1, S=0, T=0 (chiffres ignorés) → [0,1,0,0]
     * }</pre>
     *
     * <h3>Gestion de la casse</h3>
     * <p>Le mot-clé peut être en minuscules ou majuscules, la conversion est automatique.
     * Exemples : "amour", "AMOUR", "AmOuR" produisent le même résultat.</p>
     *
     * <h3>Caractères non-alphabétiques</h3>
     * <p>Les chiffres, espaces, et caractères spéciaux dans le mot-clé sont silencieusement
     * ignorés, permettant des clés comme "TEST1" ou "SPORT-AUTO".</p>
     *
     * @param word   mot-clé analytique (ex: "AMOUR", "SPORT"), insensible à la casse (non null)
     * @param counts tableau des occurrences A-Z (doit avoir exactement 26 éléments), non null
     * @return liste des occurrences pour chaque lettre du mot-clé, jamais null
     * @throws NullPointerException si word ou counts est null
     * @throws IllegalArgumentException si counts n'a pas exactement 26 éléments
     * @apiNote La liste retournée a une taille ≤ word.length() (caractères non-alphabétiques exclus)
     * @see #countLetterOccurrencesArray(String)
     * @see #calculateCompatibility(List)
     */
    public static List<Integer> generateOccurrenceList(String word, int[] counts) {
        Objects.requireNonNull(word, "word");
        Objects.requireNonNull(counts, "counts");
        if (counts.length != 26) throw new IllegalArgumentException("counts length must be 26");

        ArrayList<Integer> out = new ArrayList<>(word.length());
        for (int i = 0, n = word.length(); i < n; i++) {
            char c = Character.toUpperCase(word.charAt(i));
            if (c >= 'A' && c <= 'Z') out.add(counts[c - 'A']);
        }
        return out;
    }

    // ---------------------------------------------------------------------
    // Réduction et composition du pourcentage
    // ---------------------------------------------------------------------

    /**
     * Calcule un pourcentage de compatibilité à partir d'une liste d'entiers via réduction itérative.
     *
     * <p>Cette méthode implémente le <strong>cœur algorithmique</strong> de calcul de compatibilité.
     * Elle transforme une séquence d'occurrences de lettres en un pourcentage final par réductions
     * successives, simulant une forme de "numérologie computationnelle".</p>
     *
     * <h3>Algorithme de réduction</h3>
     * <ol>
     *   <li><strong>Cas triviaux</strong> :
     *     <ul>
     *       <li>Liste vide → 0%</li>
     *       <li>1 élément → cet élément comme pourcentage</li>
     *       <li>2 éléments → composition directe via {@link #composePercentageFromHead(List)}</li>
     *     </ul>
     *   </li>
     *   <li><strong>Réduction itérative</strong> (liste > 2 éléments) :
     *     <ul>
     *       <li>Application de {@link #reduceList(List)} (addition paire à paire)</li>
     *       <li>Troncature via {@link #head(List, int)} pour limiter la taille</li>
     *       <li>Répétition jusqu'à obtenir ≤ 2 éléments ou atteindre la limite d'itérations</li>
     *     </ul>
     *   </li>
     *   <li><strong>Composition finale</strong> : calcul du pourcentage à partir des 1-2 éléments restants</li>
     * </ol>
     *
     * <h3>Exemple de réduction complète</h3>
     * <pre>{@code
     * // Liste initiale : [2, 1, 3, 1, 2] (occurrences pour "AMOUR")
     *
     * // Itération 1 : réduction paire à paire
     * // (2+1)=3, (1+3)=4, (3+1)=4, (1+2)=3 → [3, 4, 4, 3]
     *
     * // Itération 2 :
     * // (3+4)=7, (4+4)=8, (4+3)=7 → [7, 8, 7]
     *
     * // Itération 3 :
     * // (7+8)=15→[1,5], (8+7)=15→[1,5] → [1, 5, 1, 5]
     *
     * // Itération 4 :
     * // (1+5)=6, (5+1)=6, (1+5)=6 → [6, 6, 6]
     *
     * // Itération 5 :
     * // (6+6)=12→[1,2], (6+6)=12→[1,2] → [1, 2, 1, 2]
     *
     * // Itération 6 :
     * // (1+2)=3, (2+1)=3, (1+2)=3 → [3, 3, 3]
     *
     * // Itération 7 :
     * // (3+3)=6, (3+3)=6 → [6, 6]
     *
     * // Composition finale : composePercentageFromHead([6, 6]) → 66%
     * }</pre>
     *
     * <h3>Limites de sécurité</h3>
     * <ul>
     *   <li><strong>Itérations</strong> : max {@value #HARD_MAX_ITERS} pour éviter les boucles infinies</li>
     *   <li><strong>Taille liste</strong> : troncature à {@value #HEAD_LIMIT} éléments par itération</li>
     *   <li><strong>Validation</strong> : vérification que tous les nombres sont ≥ 0</li>
     * </ul>
     *
     * <h3>Cas d'arrêt prématuré</h3>
     * <p>Si après {@value #HARD_MAX_ITERS} itérations la liste a encore > 2 éléments,
     * l'algorithme s'arrête et compose le pourcentage avec les 2 premiers éléments.</p>
     *
     * @param numbers liste d'entiers non-négatifs représentant les occurrences (non null, peut être vide)
     * @return pourcentage de compatibilité (généralement 0-99, occasionnellement > 99)
     * @throws NullPointerException si numbers est null
     * @throws IllegalArgumentException si numbers contient null ou des valeurs négatives
     * @apiNote La complexité temporelle est O(n * itérations) où n est la taille de la liste
     * @see #reduceList(List)
     * @see #composePercentageFromHead(List)
     * @see #head(List, int)
     */
    public static int calculateCompatibility(List<Integer> numbers) {
        Objects.requireNonNull(numbers, "numbers");
        for (Integer v : numbers) {
            if (v == null) throw new IllegalArgumentException("numbers contains null");
            if (v < 0)     throw new IllegalArgumentException("numbers contains negative value: " + v);
        }
        if (numbers.isEmpty()) return 0;

        int size = numbers.size();
        if (size == 1) return numbers.get(0);
        if (size == 2) return composePercentageFromHead(numbers);

        List<Integer> current = new ArrayList<>(numbers);
        int iters = 0;
        while (current.size() > 2 && iters < HARD_MAX_ITERS) {
            current = reduceList(current);
            current = head(current, HEAD_LIMIT);
            iters++;
        }
        if (current.size() == 1) return current.get(0);
        if (current.size() >= 2) return composePercentageFromHead(current);
        return 0;
    }

    /**
     * Applique la règle de réduction paire à paire avec division des sommes importantes.
     *
     * <p>Cette méthode implémente l'étape fondamentale de l'algorithme de réduction :
     * elle prend une liste et crée une nouvelle liste en additionnant les éléments
     * consécutifs, avec une règle spéciale pour les sommes ≥ {@value #MAX_DIGIT}.</p>
     *
     * <h3>Règle de réduction</h3>
     * <p>Pour chaque paire d'éléments consécutifs (i, i+1) :</p>
     * <ul>
     *   <li><strong>Si somme < {@value #MAX_DIGIT}</strong> : ajouter la somme directement</li>
     *   <li><strong>Si somme ≥ {@value #MAX_DIGIT}</strong> : ajouter la dizaine puis l'unité</li>
     * </ul>
     *
     * <h3>Exemples de réduction</h3>
     * <pre>{@code
     * reduceList([2, 1, 3, 1, 2])
     * // (2+1)=3 < 10 → add(3)
     * // (1+3)=4 < 10 → add(4)
     * // (3+1)=4 < 10 → add(4)
     * // (1+2)=3 < 10 → add(3)
     * // Résultat: [3, 4, 4, 3]
     *
     * reduceList([7, 8, 5])
     * // (7+8)=15 ≥ 10 → add(1), add(5)
     * // (8+5)=13 ≥ 10 → add(1), add(3)
     * // Résultat: [1, 5, 1, 3]
     *
     * reduceList([9, 1])
     * // (9+1)=10 ≥ 10 → add(1), add(0)
     * // Résultat: [1, 0]
     * }</pre>
     *
     * <h3>Limitation de taille</h3>
     * <p>La construction s'arrête si la liste résultante atteint {@value #HEAD_LIMIT}
     * éléments, évitant une explosion mémoire avec des listes très longues.</p>
     *
     * <h3>Optimisation mémoire</h3>
     * <p>La capacité initiale de la liste résultante est optimisée selon la formule :
     * {@code min(HEAD_LIMIT, max(0, (taille_entrée - 1) * 2))}</p>
     *
     * @param list liste d'entiers non-négatifs (≥ 2 éléments), non null
     * @return nouvelle liste avec les réductions appliquées, jamais null
     * @throws NullPointerException si list est null
     * @throws IllegalArgumentException si list a < 2 éléments, contient null ou des valeurs négatives
     * @apiNote La taille de la liste résultante est généralement proche de celle d'entrée,
     *          mais peut doubler dans le pire cas (toutes les sommes ≥ 10)
     * @see #calculateCompatibility(List)
     * @see #MAX_DIGIT
     * @see #HEAD_LIMIT
     */
    public static List<Integer> reduceList(List<Integer> list) {
        Objects.requireNonNull(list, "list");
        if (list.size() < 2) throw new IllegalArgumentException("list must have size >= 2");
        for (Integer v : list) {
            if (v == null) throw new IllegalArgumentException("list contains null");
            if (v < 0)     throw new IllegalArgumentException("list contains negative value: " + v);
        }

        int expectedMax = Math.min(HEAD_LIMIT, Math.max(0, (list.size() - 1) * 2));
        ArrayList<Integer> result = new ArrayList<>(expectedMax);

        for (int i = 0; i < list.size() - 1 && result.size() < HEAD_LIMIT; i++) {
            int sum = list.get(i) + list.get(i + 1);
            if (sum < MAX_DIGIT) {
                result.add(sum);
            } else {
                result.add(sum / 10);
                if (result.size() < HEAD_LIMIT) result.add(sum % 10);
            }
        }
        return result;
    }

    /**
     * Compose le pourcentage final à partir des deux premières valeurs d'une liste.
     *
     * <p>Cette méthode constitue l'<strong>étape finale</strong> de l'algorithme de calcul
     * de compatibilité. Elle transforme une paire de nombres en un pourcentage selon
     * des règles spécifiques qui privilégient les nombres à un chiffre.</p>
     *
     * <h3>Règles de composition</h3>
     * <p>Soient A et B les deux premiers éléments de la liste :</p>
     * <ol>
     *   <li><strong>Cas standard (A ≤ 9 et B ≤ 9)</strong> :
     *       <br>Pourcentage = A × 10 + B
     *       <br>Exemple : A=6, B=7 → 67%</li>
     *   <li><strong>A > 9 (B quelconque)</strong> :
     *       <br>Pourcentage = dizaine(A) × 10 + unité(A)
     *       <br>Exemple : A=23, B=7 → 23%</li>
     *   <li><strong>A ≤ 9 et B > 9</strong> :
     *       <br>Pourcentage = A × 10 + dizaine(B)
     *       <br>Exemple : A=6, B=34 → 63%</li>
     * </ol>
     *
     * <h3>Exemples détaillés</h3>
     * <pre>{@code
     * // Cas 1 : nombres à un chiffre (le plus fréquent)
     * composePercentageFromHead([6, 7])          // → 67%
     * composePercentageFromHead([0, 9])          // → 9%
     * composePercentageFromHead([5, 2])          // → 52%
     *
     * // Cas 2 : premier nombre > 9
     * composePercentageFromHead([15, 3])         // → 15% (ignore le 3)
     * composePercentageFromHead([99, 1])         // → 99%
     * composePercentageFromHead([10, 7])         // → 10%
     *
     * // Cas 3 : second nombre > 9, premier ≤ 9
     * composePercentageFromHead([6, 34])         // → 63% (6 × 10 + 3)
     * composePercentageFromHead([2, 18])         // → 21% (2 × 10 + 1)
     * composePercentageFromHead([9, 99])         // → 99% (9 × 10 + 9)
     *
     * // Cas limites
     * composePercentageFromHead([0, 0])          // → 0%
     * composePercentageFromHead([12, 34, 56])    // → 12% (ignore éléments > 2)
     * }</pre>
     *
     * <h3>Logique algorithmique</h3>
     * <p>La priorité est donnée aux nombres à un chiffre car ils sont considérés
     * comme plus "purs" dans le contexte de la numérologie computationnelle.
     * Quand un nombre dépasse 9, seuls ses chiffres significatifs sont conservés.</p>
     *
     * <h3>Plage des résultats</h3>
     * <ul>
     *   <li><strong>Minimum théorique</strong> : 0% (si A=0, B=0)</li>
     *   <li><strong>Maximum théorique</strong> : 99% (si A=9, B=9 ou A=99)</li>
     *   <li><strong>Plage typique</strong> : 10-90% pour la plupart des cas réels</li>
     * </ul>
     *
     * @param numbers liste d'au moins 2 entiers non-négatifs (non null)
     * @return pourcentage calculé selon les règles de composition (0-99 généralement)
     * @throws NullPointerException si numbers est null
     * @throws IllegalArgumentException si numbers contient < 2 éléments, des null, ou des valeurs négatives
     * @apiNote Seuls les 2 premiers éléments sont utilisés, les suivants sont ignorés
     * @see #tens(int)
     * @see #units(int)
     * @see #calculateCompatibility(List)
     */
    public static int composePercentageFromHead(List<Integer> numbers) {
        Objects.requireNonNull(numbers, "numbers");
        if (numbers.size() < 2) throw new IllegalArgumentException("numbers must contain at least 2 elements");

        Integer A = numbers.get(0);
        Integer B = numbers.get(1);
        if (A == null || B == null) throw new IllegalArgumentException("first two elements must be non-null");
        if (A < 0 || B < 0)         throw new IllegalArgumentException("first two elements must be >= 0");

        int a = A, b = B;
        if (a <= 9 && b <= 9) return a * 10 + b;
        if (a > 9)            return tens(a) * 10 + units(a);
        return a * 10 + tens(b);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    /**
     * Extrait la dizaine d'un nombre entier non-négatif.
     *
     * <p>Cette méthode utilitaire calcule le chiffre des dizaines d'un nombre.
     * Elle est utilisée par {@link #composePercentageFromHead(List)} pour
     * décomposer les nombres à plusieurs chiffres.</p>
     *
     * <h3>Algorithme</h3>
     * <ul>
     *   <li><strong>Si n < 10</strong> : retourne 0 (pas de dizaine)</li>
     *   <li><strong>Si n ≥ 10</strong> : retourne n ÷ 10 (division entière)</li>
     * </ul>
     *
     * <h3>Exemples</h3>
     * <pre>{@code
     * tens(5)    // → 0
     * tens(9)    // → 0
     * tens(10)   // → 1
     * tens(23)   // → 2
     * tens(99)   // → 9
     * tens(123)  // → 12 (pour les nombres > 99)
     * tens(0)    // → 0
     * }</pre>
     *
     * <h3>Utilisation typique</h3>
     * <p>Principalement utilisée pour extraire le chiffre des dizaines
     * dans les règles de composition des pourcentages :</p>
     * <pre>{@code
     * int number = 67;
     * int dizaines = tens(number);  // → 6
     * int unites = units(number);   // → 7
     * int pourcentage = dizaines * 10 + unites;  // → 67
     * }</pre>
     *
     * @param n nombre entier non-négatif
     * @return chiffre des dizaines (0 si n < 10)
     * @throws IllegalArgumentException si n est négatif
     * @apiNote Pour les nombres ≥ 100, retourne toutes les dizaines (ex: tens(123) = 12)
     * @see #units(int)
     * @see #composePercentageFromHead(List)
     */
    public static int tens(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0");
        return (n < 10) ? 0 : (n / 10);
    }

    /**
     * Extrait l'unité (chiffre des unités) d'un nombre entier non-négatif.
     *
     * <p>Cette méthode utilitaire calcule le chiffre des unités d'un nombre
     * en utilisant l'opération modulo 10. Complément de {@link #tens(int)}.</p>
     *
     * <h3>Algorithme</h3>
     * <p>Retourne n % 10 (reste de la division par 10)</p>
     *
     * <h3>Exemples</h3>
     * <pre>{@code
     * units(5)    // → 5
     * units(9)    // → 9
     * units(10)   // → 0
     * units(23)   // → 3
     * units(99)   // → 9
     * units(123)  // → 3
     * units(0)    // → 0
     * }</pre>
     *
     * <h3>Propriété mathématique</h3>
     * <p>Pour tout nombre n ≥ 0 :</p>
     * <pre>{@code
     * n = tens(n) * 10 + units(n)
     *
     * // Exemples de vérification :
     * 67 = tens(67) * 10 + units(67) = 6 * 10 + 7 = 67 ✓
     * 123 = tens(123) * 10 + units(123) = 12 * 10 + 3 = 123 ✓
     * }</pre>
     *
     * @param n nombre entier non-négatif
     * @return chiffre des unités (0-9)
     * @throws IllegalArgumentException si n est négatif
     * @apiNote Le résultat est toujours dans la plage [0, 9]
     * @see #tens(int)
     * @see #composePercentageFromHead(List)
     */
    public static int units(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0");
        return n % 10;
    }

    /**
     * Ne conserve que les premiers éléments d'une liste selon une limite donnée.
     *
     * <p>Cette méthode utilitaire générique tronque une liste pour éviter qu'elle
     * devienne trop volumineuse lors des itérations de l'algorithme de réduction.
     * Elle est utilisée par {@link #calculateCompatibility(List)} pour maintenir
     * des performances optimales.</p>
     *
     * <h3>Comportement</h3>
     * <ul>
     *   <li><strong>Si list.size() ≤ limit</strong> : retourne la liste originale inchangée</li>
     *   <li><strong>Si list.size() > limit</strong> : retourne une nouvelle liste avec les 'limit' premiers éléments</li>
     * </ul>
     *
     * <h3>Optimisation mémoire</h3>
     * <p>Quand aucune troncature n'est nécessaire, la liste originale est retournée
     * directement, évitant une copie inutile. Sinon, une nouvelle {@link ArrayList}
     * est créée avec les éléments sélectionnés.</p>
     *
     * <h3>Exemples</h3>
     * <pre>{@code
     * List<Integer> list1 = List.of(1, 2, 3, 4, 5);
     *
     * head(list1, 10)  // → [1, 2, 3, 4, 5] (liste originale)
     * head(list1, 5)   // → [1, 2, 3, 4, 5] (liste originale)
     * head(list1, 3)   // → [1, 2, 3] (nouvelle liste)
     * head(list1, 0)   // → [] (liste vide)
     *
     * List<String> list2 = List.of("A", "B", "C");
     * head(list2, 2)   // → ["A", "B"]
     * }</pre>
     *
     * <h3>Utilisation dans l'algorithme</h3>
     * <p>Cette méthode est cruciale pour maintenir les performances lors de la
     * réduction itérative, en limitant la croissance exponentielle des listes
     * intermédiaires :</p>
     * <pre>{@code
     * List<Integer> current = reduceList(previous);
     * current = head(current, HEAD_LIMIT);  // Limitation à 128 éléments
     * }</pre>
     *
     * @param <T> type des éléments de la liste
     * @param list liste d'origine (non null)
     * @param limit nombre maximum d'éléments à conserver (≥ 0)
     * @return la liste originale si sa taille ≤ limit, sinon une nouvelle liste tronquée
     * @throws NullPointerException si list est null
     * @throws IllegalArgumentException si limit est négatif
     * @apiNote Cette méthode est thread-safe et n'a aucun effet de bord
     * @see #calculateCompatibility(List)
     * @see #HEAD_LIMIT
     */
    public static <T> List<T> head(List<T> list, int limit) {
        Objects.requireNonNull(list, "list");
        if (limit < 0) throw new IllegalArgumentException("limit must be >= 0");
        return (list.size() <= limit) ? list : new ArrayList<>(list.subList(0, limit));
    }
}
