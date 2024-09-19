import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

public class Algorithme {

    private static final int BETTER_THAN = 10;
    private static final int MAX_ITERATION = 50;
    private static final String NAME1_OUT = "Entrez le premier nom : ";
    private static final String NAME2_OUT = "Entrez le deuxième nom : ";

    private Scanner scanner = new Scanner(System.in);
    private String name1;
    private String name2;
    private String concatenatedNames;
    private Map<Character, Integer> alphabet = new HashMap<>();
    private Map<String, String> compatibilities = new LinkedHashMap<>();

    public Algorithme() {
        initAlphabet();
        initCompatibilities();
    }

    /**
     * Méthode principale
     */
    public void execute() {

        readNames();
        calculOccurence();

        displayCompatibilities();

        scanner.close();
    }

    /**
     * Initialise une map des compatibilités
     */
    public void initCompatibilities() {
        compatibilities.put("AMOUR", "amoureuse");
        compatibilities.put("AMIS", "amicale");
        compatibilities.put("SEXE", "sexuelle");
        compatibilities.put("COOK", "culinaire");
        compatibilities.put("SPORT", "sportive");
        compatibilities.put("MUSIC", "musicale");
        compatibilities.put("ASTRO", "astrologique");
        compatibilities.put("POLITIQUE", "politique");
        compatibilities.put("ESPRIT", "spirituelle");
        compatibilities.put("LUDIQUE", "ludique");
    }

    /**
     * Lit les noms dans la console et les concatènes
     */
    private void readNames() {
        // Lecture du premier nom
        System.out.print(NAME1_OUT);
        name1 = removeAccent(scanner.nextLine()).toUpperCase();

        // Lecture du deuxième nom
        System.out.print(NAME2_OUT);
        name2 = removeAccent(scanner.nextLine()).toUpperCase();

        // Concaténation des deux noms
        concatenatedNames = name1 + name2;
    }

    /**
     * Effectue le calcul de compatibilité
     * @param list
     * @param iteration
     * @return le pourcentage de compatibilité
     */
    private int calcul(List<Integer> list, int iteration) {

        int resultat = 0;
        List<Integer> tmp = new ArrayList<>();
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i) + list.get(i + 1) < BETTER_THAN) {
                tmp.add(list.get(i) + list.get(i + 1));
            } else {
                int x = list.get(i) + list.get(i + 1);
                tmp.add(x / 10);
                tmp.add(x - 10);
            }
        }
        if (tmp.size() != 2) {
            if (iteration < MAX_ITERATION) {
                resultat = calcul(tmp, iteration + 1);
            } else {
                resultat = tmp.get(0) * 10 + tmp.get(1);
            }
        } else {
            resultat = tmp.get(0) * 10 + tmp.get(1);
        }
        return resultat;
    }

    /**
     * Affiche les différentes compatibilités
     */
    private void displayCompatibilities() {
        int percent;
        List<Integer> list;
        for (Map.Entry<String, String> compatibility : compatibilities.entrySet()) {
            list = genererListe(compatibility.getKey());
            percent = calcul(list, 0);
            System.out.println("Compatibilite "+compatibility.getValue()+" : "+percent+"%");
        }
    }

    /**
     * Calcul le nombre d'occurences des lettres dans les noms concaténés
     */
    private void calculOccurence() {
        for (char lettre : concatenatedNames.toCharArray()) {
            if (alphabet.containsKey(lettre)) {
                alphabet.put(lettre, alphabet.get(lettre) + 1);
            }
        }
    }

    /**
     * Initialise une map avec l'alphabet
     */
    private void initAlphabet() {
        for (int i = 65 ; i < 91 ; i++) {
            alphabet.put(Character.toString(i).charAt(0), 0);
        }
    }

    /**
     * Permet de supprimer tous les accents
     * @param nom
     * @return chaine sans accents
     */
    private String removeAccent(String nom) {
        // Suppression des accents
        return Normalizer.normalize(nom, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    /**
     * Parse les caractères de la compatibilité et insère dans une liste le nombre d'occurences
     * @param compatibility
     * @return liste d'occurences
     */
    private List<Integer> genererListe(String compatibility) {
        return compatibility.chars()
            .filter(letter -> alphabet.containsKey((char) letter))
            .mapToObj(occurence -> alphabet.get((char)occurence))
            .collect(Collectors.toList());
    }
}
