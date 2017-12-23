package org.nbp.b2g.ui.host;
import org.nbp.b2g.ui.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import org.nbp.common.CharacterUtilities;
import org.nbp.common.UnicodeUtilities;

public abstract class DiacriticAction extends Action {
  protected static class DiacriticMap extends HashMap<Character, Character> {
    public DiacriticMap () {
      super();
    }
  }

  protected static class CharacterMap extends HashMap<Character, DiacriticMap> {
    public CharacterMap () {
      super();
    }
  }

  protected abstract int getChooseMessage ();
  protected abstract CharacterMap getCharacterMap ();
  protected abstract void makeDiacriticMap (DiacriticMap map, Character base);

  private final DiacriticMap getDiacriticMap (Character character) {
    CharacterMap characterMap = getCharacterMap();

    synchronized (characterMap) {
      DiacriticMap diacriticMap = characterMap.get(character);
      if (diacriticMap != null) return diacriticMap;
      if (characterMap.containsKey(character)) return null;

      diacriticMap = new DiacriticMap();
      makeDiacriticMap(diacriticMap, character);
      if (diacriticMap.isEmpty()) diacriticMap = null;
      characterMap.put(character, diacriticMap);
      return diacriticMap;
    }
  }

  @Override
  public final boolean performAction () {
    final Endpoint endpoint = Endpoints.host.get();

    synchronized (endpoint) {
      if (endpoint.isInputArea()) {
        if (endpoint.hasCursor()) {
          final int end = endpoint.getSelectionEnd();

          if (end > 0) {
            final int start = end - 1;

            final DiacriticMap diacriticMap = getDiacriticMap(endpoint.getText().charAt(start));
            if (diacriticMap == null) return false;

            Set<Character> diacriticSet = diacriticMap.keySet();
            diacriticSet.retainAll(DiacriticUtilities.getSupportedDiacritics());
            if (diacriticSet.isEmpty()) return false;
            final Character[] diacriticArray = diacriticSet.toArray(new Character[diacriticSet.size()]);

            Arrays.sort(diacriticArray,
              new Comparator<Character>() {
                @Override
                public int compare (Character diacritic1, Character diacritic2) {
                  String name1 = DiacriticUtilities.getDiacriticName(diacritic1);
                  String name2 = DiacriticUtilities.getDiacriticName(diacritic2);
                  return name1.compareTo(name2);
                }
              }
            );

            StringBuilder message = new StringBuilder();
            message.append(getString(getChooseMessage()));

            for (Character diacritic : diacriticArray) {
              message.append('\n');
              message.append(DiacriticUtilities.getDiacriticName(diacritic));
            }

            Endpoints.setPopupEndpoint(message.toString(),
              new PopupClickHandler() {
                @Override
                public boolean handleClick (int index) {
                  if (index == 0) return false;
                  char character = diacriticMap.get(diacriticArray[--index]);
                  Endpoints.setHostEndpoint();
                  return endpoint.replaceText(start, end, Character.toString(character));
                }
              }
            );

            return true;
          }
        }
      }
    }

    return false;
  }

  protected final boolean mapDiacritic (DiacriticMap map, char diacritic, String decomposition) {
    String composition = UnicodeUtilities.compose(decomposition);
    if (composition == null) return false;
    if (composition.length() != 1) return false;

    map.put(diacritic, composition.charAt(0));
    return true;
  }

  protected DiacriticAction (Endpoint endpoint) {
    super(endpoint, false);
  }
}
