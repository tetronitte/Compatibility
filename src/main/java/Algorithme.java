import java.util.ArrayList;
import java.util.List;

public class Algorithme {

    private static final int SUPERIEUR_A = 10;
    private static final int ITERATION_MAX = 50;
    
    public static int calcul(List<Integer> list, int iteration) {

        int resultat = 0;
        List<Integer> tmp = new ArrayList<>();
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i) + list.get(i + 1) < SUPERIEUR_A) {
                tmp.add(list.get(i) + list.get(i + 1));
            } else {
                int x = list.get(i) + list.get(i + 1);
                tmp.add(x / 10);
                tmp.add(x - 10);
            }
        }
        if (tmp.size() != 2) {
            if (iteration < ITERATION_MAX) {
                resultat = calcul(tmp, iteration + 1);
            } else {
                resultat = tmp.get(0) * 10 + tmp.get(1);
            }
        } else {
            resultat = tmp.get(0) * 10 + tmp.get(1);
        }
        return resultat;
    }
}
