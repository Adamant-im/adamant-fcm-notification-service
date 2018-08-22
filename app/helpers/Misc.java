package helpers;

import java.util.List;

public class Misc {
    public static <T> T randomItem(List<T> items){
            int index =  (int) Math.round(Math.floor(Math.random() * items.size()));
            if (index >= items.size()){index = items.size() - 1;}

            return items.get(index);
    }
}
