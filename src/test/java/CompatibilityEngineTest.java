import config.Preset;
import config.TypeCatalog;
import core.CompatibilityEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <h1>Tests unitaires du cœur de l’algorithme : {@code CompatibilityEngine}</h1>
 *
 * <p>
 * Ce jeu de tests couvre la normalisation des chaînes, le comptage des lettres,
 * la génération de listes d’occurrences en fonction d’un mot-clé, la réduction
 * itérative et la composition du pourcentage final, ainsi que la fonction
 * d’intégration {@link CompatibilityEngine#computeAll(Map, String, String)}.
 * </p>
 *
 * <h2>Stratégie de test</h2>
 * <ul>
 *   <li><strong>Nominal :</strong> chemin heureux sur chaque primitive (normalize, count, reduce, compose).</li>
 *   <li><strong>Garde-fous :</strong> arguments nuls, valeurs négatives, tailles invalides.</li>
 *   <li><strong>Intégration légère :</strong> calcul complet sur un set de types connu (LISTE1).</li>
 * </ul>
 *
 * @see CompatibilityEngine
 */
@DisplayName("CompatibilityEngine – tests du cœur de l'algo")
class CompatibilityEngineTest {

    /**
     * Construit le tableau de comptage A–Z pour deux noms bruts.
     *
     * @param name1 premier nom (brut)
     * @param name2 second nom (brut)
     * @return tableau de 26 entiers (index 0='A' … 25='Z')
     * @implNote Utilise {@link CompatibilityEngine#normalize(String)} avant le comptage.
     */
    private static int[] countsFor(String name1, String name2) {
        String n1 = CompatibilityEngine.normalize(name1);
        String n2 = CompatibilityEngine.normalize(name2);
        return CompatibilityEngine.countLetterOccurrencesArray(n1 + n2);
    }

    /**
     * Raccourci pour lire un pourcentage dans une map libellé → %, en renvoyant -1 si absent.
     *
     * @param results map des résultats
     * @param label   libellé lisible (ex. "amoureuse")
     * @return pourcentage ou -1 si absent
     */
    private static int pct(Map<String, Integer> results, String label) {
        return results.getOrDefault(label, -1);
    }

    /**
     * Vérifie que la normalisation supprime les diacritiques, met en majuscules
     * et retourne une chaîne vide pour les entrées vides ou nulles.
     */
    @Test
    @DisplayName("normalize : retire les diacritiques et uppercase")
    void normalize_basic() {
        assertEquals("DRENOR DARNUK", CompatibilityEngine.normalize("Drénor darnuk"));
        assertEquals("TELMA VELASCO", CompatibilityEngine.normalize("Telma Velasco"));
        assertEquals("", CompatibilityEngine.normalize("   "));
        assertEquals("", CompatibilityEngine.normalize(null));
    }

    /**
     * Vérifie que le comptage ne considère que les lettres A–Z et
     * renvoie un tableau de taille 26.
     */
    @Test
    @DisplayName("countLetterOccurrencesArray : compte uniquement A–Z")
    void countLetters_basic() {
        int[] counts = CompatibilityEngine.countLetterOccurrencesArray("ABZ-  A!");
        assertEquals(26, counts.length);
        assertEquals(2, counts['A' - 'A']);
        assertEquals(1, counts['B' - 'A']);
        assertEquals(1, counts['Z' - 'A']);
        for (int i = 0; i < 26; i++) {
            if (i != ('A' - 'A') && i != ('B' - 'A') && i != ('Z' - 'A')) {
                assertEquals(0, counts[i], "index " + i);
            }
        }
    }

    /**
     * Vérifie que la génération des occurrences est insensible à la casse
     * et ignore les caractères non alphabétiques (ex. chiffres).
     */
    @Test
    @DisplayName("generateOccurrenceList : insensible à la casse, ignore les chiffres")
    void generateOccurrenceList_caseAndDigits() {
        int[] counts = countsFor("John Doe", "Jane Doe");
        List<Integer> occ = CompatibilityEngine.generateOccurrenceList("tEsT1", counts);
        assertFalse(occ.isEmpty());
        assertTrue(occ.stream().allMatch(v -> v >= 0));
    }

    /**
     * Vérifie la réduction paire-à-paire (split des sommes ≥ 10) et
     * les garde-fous sur les arguments invalides.
     */
    @Test
    @DisplayName("reduceList : cas nominal + garde-fous")
    void reduceList_nominal_and_guards() {
        List<Integer> in = List.of(3, 8, 9); // (3+8)=11 -> 1,1 ; (8+9)=17 -> 1,7
        List<Integer> out = CompatibilityEngine.reduceList(in);
        assertEquals(1, out.get(0));
        assertEquals(1, out.get(1));

        assertThrows(NullPointerException.class, () -> CompatibilityEngine.reduceList(null));
        assertThrows(IllegalArgumentException.class, () -> CompatibilityEngine.reduceList(List.of(42)));
        assertThrows(IllegalArgumentException.class, () -> CompatibilityEngine.reduceList(Arrays.asList(1, null)));
        assertThrows(IllegalArgumentException.class, () -> CompatibilityEngine.reduceList(List.of(1, -2)));
    }

    /**
     * Vérifie la composition du pourcentage à partir des deux premières valeurs :
     * (a,b) chiffres ; a ≥ 10 ; a &lt; 10 et b ≥ 10. Teste également les garde-fous.
     */
    @Test
    @DisplayName("composePercentageFromHead : 2 chiffres | a>=10 | b>=10")
    void compose_from_two() {
        assertEquals(34, CompatibilityEngine.composePercentageFromHead(List.of(3, 4)));
        assertEquals(12, CompatibilityEngine.composePercentageFromHead(List.of(12, 99)));
        assertEquals(19, CompatibilityEngine.composePercentageFromHead(List.of(1, 95)));

        assertThrows(IllegalArgumentException.class, () -> CompatibilityEngine.composePercentageFromHead(List.of(1)));
        assertThrows(IllegalArgumentException.class, () -> CompatibilityEngine.composePercentageFromHead(Arrays.asList(1, null)));
        assertThrows(IllegalArgumentException.class, () -> CompatibilityEngine.composePercentageFromHead(List.of(-1, 2)));
    }

    /**
     * Vérifie les chemins courts (listes de taille 0, 1, 2) et un cas de réduction.
     */
    @Test
    @DisplayName("calculateCompatibility : chemins courts et réduction")
    void calculateCompatibility_paths() {
        assertEquals(0, CompatibilityEngine.calculateCompatibility(Collections.emptyList()));
        assertEquals(7, CompatibilityEngine.calculateCompatibility(List.of(7)));
        assertEquals(34, CompatibilityEngine.calculateCompatibility(List.of(3, 4)));
        assertTrue(CompatibilityEngine.calculateCompatibility(List.of(9, 9, 9)) >= 0);
    }

    /**
     * Vérifie l’intégration complète (LISTE1) pour un jeu de noms connu :
     * amoureuse doit valoir 19% pour "John Doe" + "Jane Doe".
     */
    @Test
    @DisplayName("computeAll(LISTE1) : 'John Doe' + 'Jane Doe' → amoureuse = 19%")
    void computeAll_johnJane_amour_19() {
        Map<String, String> types = TypeCatalog.selectByPreset(Preset.LISTE1);
        Map<String, Integer> res = CompatibilityEngine.computeAll(types, "John Doe", "Jane Doe");
        assertEquals(19, pct(res, "amoureuse"));
        assertEquals(types.size(), res.size(), "même nombre de catégories attendu");
    }

    /**
     * Vérifie l’intégration complète (LISTE1) pour tes valeurs attendues
     * "Drénor Darnuk" + "Telma Velasco".
     */
    @Test
    @DisplayName("computeAll(LISTE1) : 'Drénor Darnuk' + 'Telma Velasco' → valeurs attendues")
    void computeAll_drenor_telma_expected() {
        Map<String, String> types = TypeCatalog.selectByPreset(Preset.LISTE1);
        Map<String, Integer> res = CompatibilityEngine.computeAll(types, "Drénor Darnuk", "Telma Velasco");

        Map<String, Integer> expected = new LinkedHashMap<>();
        expected.put("amoureuse",    88);
        expected.put("amicale",      52);
        expected.put("sexuelle",     76);
        expected.put("culinaire",    77);
        expected.put("sportive",     28);
        expected.put("musicale",     75);
        expected.put("astrologique", 69);
        expected.put("politique",    93);
        expected.put("spirituelle",  57);
        expected.put("ludique",      18);

        assertEquals(expected.size(), res.size(), "même nombre de catégories");
        for (Map.Entry<String, Integer> e : expected.entrySet()) {
            assertTrue(res.containsKey(e.getKey()), "manque: " + e.getKey());
            assertEquals(e.getValue(), res.get(e.getKey()), "diff sur " + e.getKey());
        }
    }
}
