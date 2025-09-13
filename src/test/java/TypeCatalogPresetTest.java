import config.Preset;
import config.TypeCatalog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <h1>Tests de configuration : {@code TypeCatalog} & {@code Preset}</h1>
 *
 * <p>
 * Valide que le dictionnaire maître contient bien les clés attendues,
 * et que la sélection par {@link Preset} respecte l’ordre défini par l’énum.
 * </p>
 *
 * @see TypeCatalog
 * @see Preset
 */
@DisplayName("TypeCatalog & Preset – sélection et ordre")
class TypeCatalogPresetTest {

    /**
     * Vérifie que le dictionnaire maître inclut quelques clés métiers essentielles.
     */
    @Test
    @DisplayName("MASTER_TYPES contient les clés métiers attendues")
    void master_contains_core_keys() {
        Map<String, String> m = TypeCatalog.MASTER_TYPES;
        assertTrue(m.containsKey("AMOUR"));
        assertTrue(m.containsKey("MUSIQUE"));
        assertTrue(m.containsKey("LUDIQUE"));
    }

    /**
     * Vérifie que {@link TypeCatalog#selectByPreset(Preset)} conserve l’ordre
     * des clés tel que défini dans {@link Preset#LISTE1}.
     */
    @Test
    @DisplayName("selectByPreset(LISTE1) : ordre conforme à l'énum")
    void selectByPreset_order_LISTE1() {
        Map<String, String> sel = TypeCatalog.selectByPreset(Preset.LISTE1);
        assertEquals(10, sel.size(), "LISTE1 doit exposer 10 types");
        List<String> expectedOrder = Preset.LISTE1.keys();
        assertEquals(expectedOrder.size(), sel.size());

        int i = 0;
        for (String key : sel.keySet()) {
            assertEquals(expectedOrder.get(i), key, "ordre différent à l'index " + i);
            i++;
        }
    }
}
