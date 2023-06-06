import java.text.Normalizer;
import java.util.*;

public class main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Lecture du premier nom
        System.out.print("Entrez le premier nom : ");
        String nom1 = removeAccent(scanner.nextLine()).toUpperCase();

        // Lecture du deuxième nom
        System.out.print("Entrez le deuxième nom : ");
        String nom2 = removeAccent(scanner.nextLine()).toUpperCase();

        // Concaténation des deux noms
        String nomsConcatenes = nom1 + nom2;

        Map<Character, Integer> occurences = initHashMap();
        calculOccurence(occurences, nomsConcatenes);

        calculCompatibilite(occurences);

        scanner.close();

    }

    private static void calculCompatibilite(Map<Character, Integer> occurences) {
        int percent;
        StringBuilder compatibility = new StringBuilder();
        List<Integer> list;
        for (Choix choix : Choix.values()) {
            list = genererListe(occurences, choix);
            percent = Algorithme.calcul(list, 0);
            switch (choix.ordinal()) {
                case 0 :
                    compatibility.append("Compatibilite amoureuse : "+percent+"%");
                    break;
                case 1 :
                    compatibility.append("Compatibilite amicale : "+percent+"%");
                    break;
                case 2 :
                    compatibility.append("Compatibilite sexuelle : "+percent+"%");
                    break;
                case 3 :
                    compatibility.append("Compatibilite culinaire : "+percent+"%");
                    break;
            }
            System.out.println(compatibility);
            compatibility.setLength(0);
        }
    }

    private static void calculOccurence(Map<Character, Integer> occurences, String nomsConcatenes) {
        for (char lettre : nomsConcatenes.toCharArray()) {
            if (occurences.containsKey(lettre)) {
                occurences.put(lettre, occurences.get(lettre) + 1);
            }
        }
    }

    private static Map<Character, Integer> initHashMap() {
        Map<Character, Integer> map = new HashMap<>();
        map.put('A', 0);
        map.put('M', 0);
        map.put('O', 0);
        map.put('U', 0);
        map.put('R', 0);
        map.put('S', 0);
        map.put('E', 0);
        map.put('X', 0);
        map.put('I', 0);
        map.put('C', 0);
        map.put('K', 0);
        return map;
    }

    private static String removeAccent(String nom) {
        // Suppression des accents
        return Normalizer.normalize(nom, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private static List<Integer> genererListe(Map<Character, Integer> occurences, Choix choix) {
        List<Integer> list = new ArrayList<>();
        switch (choix) {
            case AMOUR:
                list.add(occurences.get('A'));
                list.add(occurences.get('M'));
                list.add(occurences.get('O'));
                list.add(occurences.get('U'));
                list.add(occurences.get('R'));
                break;
            case SEXE:
                list.add(occurences.get('S'));
                list.add(occurences.get('E'));
                list.add(occurences.get('X'));
                list.add(occurences.get('E'));
                break;
            case AMIS:
                list.add(occurences.get('A'));
                list.add(occurences.get('M'));
                list.add(occurences.get('I'));
                list.add(occurences.get('S'));
                break;
            case COOK:
                list.add(occurences.get('C'));
                list.add(occurences.get('O'));
                list.add(occurences.get('O'));
                list.add(occurences.get('K'));
                break;
        }
        return list;
    }
}
